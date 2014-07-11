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
import com.codenvy.client.model.Project;
import com.codenvy.client.model.Workspace;
import com.codenvy.client.model.WorkspaceRef;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.Ansi;

import java.util.List;

/**
 * Allow to see projects of a workspace
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "project", description = "List all the projects from a Codenvy workspace")
public class ProjectCommand extends AbsCommand {


    @Option(name = "-w", description = "Specify the workspace name", required = true, multiValued = false)
    private String workspaceName;


    /**
     * Prints the current workspaces
     */
    protected Object doExecute() {
        final Codenvy current = checkLoggedIn();
        if (current == null) {
            return null;
        }

        Ansi buffer = Ansi.ansi();

        WorkspaceRef workspaceRef = null;

        Workspace workspace = getCurrentWorkspace();
        if (workspace != null) {
            workspaceRef = workspace.workspaceRef();
        }
        if (workspaceRef == null) {
            if (workspaceName == null) {
                session.getConsole().println("No current workspace is selected. Please select a workspace.");
                return null;
            } else {
                // select this workspace
                workspaceRef = current.workspace().withName(workspaceName).execute();
            }
        }

        // now call project api
        List<? extends Project> projects = current.project().getWorkspaceProjects(workspaceRef.id()).execute();
        buffer.a("NAME").a("\t\t").a("WorkspaceID\n");
        for (Project project : projects) {
            buffer.a(project.name()).a("\t\t").a(project.workspaceId()).a("\n");
        }

        session.getConsole().println(buffer.toString());
        return null;
    }
}
