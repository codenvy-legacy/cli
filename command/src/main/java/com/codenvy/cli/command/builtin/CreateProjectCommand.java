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

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

/**
 * Allows to create a project
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "create-project", description = "Create a project")
public class CreateProjectCommand extends AbsCommand {


    @Argument(name = "configFile", description = "Configuration file of the project", required = true, index = 0)
    private String configurationFile;

    @Argument(name = "name", description = "Name of the project", required = true, index = 1)
    private String name;

    @Option(name = "--workspace", description = "Specify the workspace to use")
    private String workspace;

    @Option(name = "--remote", description = "Specify the remote to use")
    private String remote;

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

        UserProjectReference userProjectReference = getMultiRemoteCodenvy().createProject(name, workspace, remote, configurationFile);

        if (userProjectReference != null) {
            System.out.println(String.format("Project %s has been created", name));
            if (openProject) {
                openURL(userProjectReference.getInnerReference().ideUrl());
            }
        }

        return null;
    }



}

