/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.cli.command.builtin;

import com.codenvy.client.Codenvy;
import com.codenvy.client.model.Factory;
import com.codenvy.client.model.Link;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.Ansi;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Create Factory command.
 * This command will create a factory.
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "create-factory", description = "Create a factory")
public class CreateFactoryCommand extends AbsCommand {

    @Option(name = "--remote", description = "Name of the remote codenvy", required = false)
    private String remoteName;

    /**
     * Specify the path of a JSON file.
     */
    @Option(name = "--in", description = "Specify the input JSON file", required = true)
    private String path;

    /**
     * Use non-encoded factories
     */
    @Option(name = "--non-encoded", description = "Use non encoded factory")
    private boolean nonEncoded;

    /**
     * Use encoded factories
     */
    @Option(name = "--encoded", description = "Use encoded factory")
    private boolean encoded;

    /**
     * Invoke the factory link once it has been built
     */
    @Option(name = "--invoke")
    private boolean invoke;

    /**
     * Prints the current projects per workspace
     */
    protected Object doExecute() {
        init();


        File jsonFile = new File(path);
        if (!jsonFile.exists()) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a(format("The path %s does not exists", path));
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        // no remote, use default
        if (remoteName == null) {
            remoteName = getMultiRemoteCodenvy().getDefaultRemoteName();
        } else {
            if (getMultiRemoteCodenvy().getRemote(remoteName) == null) {
                Ansi buffer = Ansi.ansi();
                buffer.fg(RED);
                buffer.a(format("The specified remote %s does not exists", remoteName));
                buffer.reset();
                System.out.println(buffer.toString());
                return null;

            }
        }

        String factoryLink;
        if (nonEncoded && !encoded) {
            factoryLink = getNonEncodedFactory(jsonFile);
        } else {
            // default is encoded
            factoryLink = getEncodedFactory(jsonFile);
        }

        if (invoke && factoryLink != null) {
            openURL(factoryLink);
        } else if (factoryLink != null) {
            System.out.println("Factory URL: " + factoryLink);
        }
        return null;
    }


    /**
     * Build an encoded factory based on the given JSON file
     * @param jsonFile the path to the json file
     * @return the factory link
     */
    protected String getEncodedFactory(File jsonFile) {

        Codenvy codenvy = getMultiRemoteCodenvy().getCodenvy(remoteName);
        StringBuilder content = new StringBuilder();
        try (FileInputStream fileInputStream = new FileInputStream(jsonFile);
             Reader inputStreamReader = new InputStreamReader(fileInputStream, Charset.defaultCharset());
             BufferedReader reader = new BufferedReader(inputStreamReader)
        ) {
            String         line = null;
            while( ( line = reader.readLine() ) != null ) {
                content.append( line );
                content.append( System.lineSeparator() );
            }
        } catch (IOException e) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a(format("Unable to load the content of the JSON file %s", jsonFile.getAbsolutePath()));
            buffer.reset();
            System.out.println(buffer.toString());
        }

        Factory factory = codenvy.factory().save(content.toString()).execute();

        // Search links
        String createProjectUrl = null;
        List<Link> links = factory.getLinks();
        for (Link link : links) {
            if ("create-project".equals(link.rel())) {
                createProjectUrl = link.href();
                break;
            }
        }

        return createProjectUrl;

    }

    /**
     * Produces the non encoded factory link and return it
     * @param jsonFile the file containing all the data
     * @return the factory link URL
     */
     protected String getNonEncodedFactory(File jsonFile) {
        List<String> allParams = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        try (FileInputStream fileInputStream = new FileInputStream(jsonFile);
             Reader reader = new InputStreamReader(fileInputStream, Charset.defaultCharset())) {
            JSONObject object = (JSONObject)jsonParser.parse(reader);
            Set<Map.Entry<String, Object>> entrySet = object.entrySet();
            Iterator<Map.Entry<String, Object>> it = entrySet.iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                String key = entry.getKey();
                Object value = entry.getValue();

                // add name and value
                allParams.addAll(getParamValue(key, value));
            }
        } catch (ParseException | IOException e) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a(format("Unable to load the content of the JSON file %s", jsonFile.getAbsolutePath()));
            buffer.reset();
            System.out.println(buffer.toString());
        }


        // Build URL

        // First, add URL of the remote
        StringBuilder factoryUrlLink = new StringBuilder(getMultiRemoteCodenvy().getRemote(remoteName).getUrl());

        // add / if not already there
        if (!factoryUrlLink.toString().endsWith("/")) {
            factoryUrlLink.append("/");
        }

        // add factory path
        factoryUrlLink.append("factory?");
        Iterator<String> iteratorParam = allParams.iterator();
        while (iteratorParam.hasNext()) {
            factoryUrlLink.append(iteratorParam.next());

            if (iteratorParam.hasNext()) {
                factoryUrlLink.append("&");
            }
        }

        return factoryUrlLink.toString();
    }


    /**
     * Get parameter name=value based on the current value. If the value is a JSON object, then key is retrieved by adding parent key to the child.
     * @param key the key of the parameter
     * @param jsonValue the JSON value to get
     * @return a list of parameter (can be only one element if value is a simple string)
     * @throws UnsupportedEncodingException if unable to encode the values
     */
    protected List<String> getParamValue(String key, Object jsonValue) throws UnsupportedEncodingException {
        if (jsonValue instanceof JSONObject) {
            // get subcontent
            JSONObject jsonObject = ((JSONObject) jsonValue);
            List<String> list = new ArrayList<>();
            Set<Map.Entry<String, Object>> entrySet = jsonObject.entrySet();
            Iterator<Map.Entry<String, Object>> it = entrySet.iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                list.addAll(getParamValue(key.concat(".").concat(entry.getKey()), entry.getValue()));
            }
            return list;
        } else if (jsonValue instanceof JSONArray) {
            // get content for array
            String arrayValue = URLEncoder.encode( ((JSONAware) jsonValue).toJSONString(), "UTF-8");
            return Arrays.asList(key.concat("=").concat(arrayValue));
        }
        return Arrays.asList(key.concat("=").concat(URLEncoder.encode(jsonValue.toString(), "UTF-8")));
    }


}
