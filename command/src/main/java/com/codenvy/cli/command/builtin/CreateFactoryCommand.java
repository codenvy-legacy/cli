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

import com.codenvy.cli.command.builtin.model.UserProjectReference;
import com.codenvy.client.Codenvy;
import com.codenvy.client.CodenvyErrorException;
import com.codenvy.client.model.Factory;
import com.codenvy.client.model.Link;
import com.codenvy.client.model.git.Log;
import com.codenvy.client.model.git.Revision;

import org.apache.karaf.shell.commands.Argument;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.codenvy.cli.command.builtin.Constants.TEMPLATE_PROJECT_FACTORY;
import static java.lang.String.format;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Create Factory command.
 * This command will create a factory.
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "create-factory", description = "Create a factory")
public class CreateFactoryCommand extends AbsCommand {

    @Argument(name = "projectId", description = "ProjectID")
    private String projectId;


    @Option(name = "--remote", description = "Name of the remote codenvy")
    private String remoteName;

    /**
     * Specify the path of a JSON file.
     */
    @Option(name = "--in", description = "Specify the input JSON file")
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

        // check remote
        if (!getSelectedRemote()) {
            return null;
        }

        String factoryLink = null;
        if (projectId != null) {
            factoryLink = createFactoryProject();
        } else if (path != null) {
            factoryLink = createPathFactory();
        }

        if (invoke && factoryLink != null) {
            openURL(factoryLink);
        } else if (factoryLink != null) {
            System.out.println("Factory URL: " + factoryLink);
        }

        return null;
    }

    protected String createFactoryProject() {
        // get project
        UserProjectReference project = getMultiRemoteCodenvy().getProjectReference(projectId);
        if (project == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No matching project for identifier '").a(projectId).a("'.");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        // get last commit ID
        Log log;
        try {
            log = project.getCodenvy().git().log(project.getInnerReference(), null).execute();
        } catch (CodenvyErrorException e) {
            // need to init the repo first
            project.getCodenvy().git().init(project.getInnerReference()).execute();

            // then get the log again
            log = project.getCodenvy().git().log(project.getInnerReference(), null).execute();
        }
        String commitId = "";
        List<Revision> commits = log.getCommits();
        if (!commits.isEmpty()) {
            commitId = commits.get(0).getId();
        }

        // get a git URL
        String gitURL = project.getCodenvy().git().readOnlyUrl(project.getInnerReference()).execute();


        //TODO: replace it with Factory class object
        String template = getTemplateFactoryJson();
        if (template.isEmpty()) {
            return null;
        }

        // replace template data
        template = template.replace("$GITURL$", gitURL)
                           .replace("$GITCOMMITID$", commitId)
                           .replace("$PROJECTNAME$", project.name())
                           .replace("$PROJECTTYPE$", project.getInnerReference().projectTypeId());

        if (nonEncoded && !encoded) {
            // write content of the json file
            File tmpFile = null;
            try {
                tmpFile = Files.createTempFile("projectjson", "json").toFile();
            } catch (IOException e) {
                Ansi buffer = Ansi.ansi();
                buffer.fg(RED);
                buffer.a("Unable to create temporary file");
                buffer.reset();
                System.out.println(buffer.toString());
                return null;
            }
            tmpFile.deleteOnExit();
            try (FileOutputStream fileOutputStream = new FileOutputStream(tmpFile)) {
                fileOutputStream.write(template.getBytes("UTF-8"));
            } catch (IOException e) {
                Ansi buffer = Ansi.ansi();
                buffer.fg(RED);
                buffer.a("Unable to write temporary file");
                buffer.reset();
                System.out.println(buffer.toString());
                return null;
            }

            return getNonEncodedFactory(tmpFile);
        } else {
            // default is encoded
            return getEncodedFactory(template);
        }


    }

    protected String getTemplateFactoryJson() {
        StringBuilder content = new StringBuilder();
        // now read factory template
        try (InputStream readInputStream = CreateFactoryCommand.class.getResourceAsStream(TEMPLATE_PROJECT_FACTORY);
             Reader inputStreamReader = new InputStreamReader(readInputStream, Charset.defaultCharset());
             BufferedReader reader = new BufferedReader(inputStreamReader)
        ) {
            String line = null;
            while( ( line = reader.readLine() ) != null ) {
                content.append( line );
                content.append( System.lineSeparator() );
            }
        } catch (IOException e) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a(format("Unable to load the content of the %s", TEMPLATE_PROJECT_FACTORY));
            buffer.reset();
            System.out.println(buffer.toString());
        }
        return content.toString();

    }

    protected boolean getSelectedRemote() {
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
                return false;
            }
        }
        return true;
    }


    /**
     * Create factory based on the path
     */
    protected String createPathFactory() {
        File jsonFile = new File(path);
        if (!jsonFile.exists()) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a(format("The path %s does not exists", path));
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        String factoryLink;
        if (nonEncoded && !encoded) {
            factoryLink = getNonEncodedFactory(jsonFile);
        } else {
            // default is encoded
            factoryLink = getEncodedFactory(jsonFile);
        }

        return factoryLink;
    }




    protected String getEncodedFactory(String content) {
        Codenvy codenvy = getMultiRemoteCodenvy().getCodenvy(remoteName);

        Factory factory = codenvy.factory().save(content).execute();

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
     * Build an encoded factory based on the given JSON file
     * @param jsonFile the file containing all the data
     * @return the factory link
     */
    protected String getEncodedFactory(File jsonFile) {
        StringBuilder content = new StringBuilder();
        try (InputStream readInputStream = new FileInputStream(jsonFile);
             Reader inputStreamReader = new InputStreamReader(readInputStream, Charset.defaultCharset());
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
        return getEncodedFactory(content.toString());
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
