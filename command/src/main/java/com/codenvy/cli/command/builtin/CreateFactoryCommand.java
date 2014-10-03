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
import com.codenvy.client.model.Factory;
import com.codenvy.client.model.Link;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.Ansi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;

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
//        Log log;
//        try {
//            log = project.getCodenvy().git().log(project.getInnerReference(), null).execute();
//        } catch (CodenvyErrorException e) {
//            // need to init the repo first
//            project.getCodenvy().git().init(project.getInnerReference()).execute();
//
//            // then get the log again
//            log = project.getCodenvy().git().log(project.getInnerReference(), null).execute();
//        }
//        String commitId = "";
//        List<Revision> commits = log.getCommits();
//        if (!commits.isEmpty()) {
//            commitId = commits.get(0).getId();
//        }
//
//        // get a git URL
//        String gitURL = project.getCodenvy().git().readOnlyUrl(project.getInnerReference()).execute();


        // default is encoded
        return getEncodedFactory("");
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

        // default is encoded
        return getEncodedFactory(jsonFile);
    }




    protected String getEncodedFactory(String content) {
        Codenvy codenvy = getMultiRemoteCodenvy().getCodenvy(remoteName);
        if (codenvy == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a(format("Please login into the remote %s", remoteName));
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }
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






}
