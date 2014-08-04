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

import com.codenvy.cli.command.builtin.model.UserProject;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Allows to create a project
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "create-project", description = "Create a project")
public class CreateProjectCommand extends AbsCommand {


    @Argument(name = "name", description = "Name of the project", required = true, index = 0)
    private String name;

    @Option(name = "--workspace", description = "Specify the workspace to use")
    private String workspace;

    @Option(name = "--remote", description = "Specify the remote to use")
    private String remote;

    @Option(name = "--type", description = "Specify the type of the project")
    private String projectType;

    @Option(name = "--open", description = "Open the project once created")
    private boolean openProject;

    /**
     * Execute the command
     */
    @Override
    protected Object doExecute() throws Exception {
        init();

        // not logged in
        if (!checkifEnabledRemotes()) {
            return null;
        }

        // do we have the project name ?
        if (name == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No name of the project has been set");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }


        UserProject userProject = getMultiRemoteCodenvy().createProject(name, workspace, remote, projectType);

        if (userProject != null) {
            System.out.println(String.format("Project %s has been created", name));
            if (openProject) {
                openURL(userProject.getInnerProject().ideUrl());
            }
        }

        return null;
    }



}

