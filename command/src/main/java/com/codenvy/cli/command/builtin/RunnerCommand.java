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
import com.codenvy.client.model.RunnerStatus;
import com.codenvy.client.model.Workspace;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import java.util.List;

import static com.codenvy.client.model.RunnerStatus.Status.STOPPED;

/**
 * Allows to run a given project
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "run", description = "Run a project in Codenvy System")
public class RunnerCommand extends AbsCommand {

    @Option(name = "-w", aliases = {"--workspace"}, description = "Specify the workspace name", required = false, multiValued = false)
    private String workspaceName;

    @Option(name = "-p", aliases = {"--project"}, description = "Specify the project name", required = false, multiValued = false)
    private String projectName;

    @Override
    protected Object doExecute() throws Exception {
        final Codenvy current = checkLoggedIn();
        if (current == null) {
            return null;
        }

        Workspace.WorkspaceRef workspaceRef = null;

        Workspace workspace = getCurrentWorkspace();
        if (workspace != null) {
            workspaceRef = workspace.workspaceRef;
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

        // get workspace ID
        String workspaceID = workspaceRef.id;
        session.getConsole().println("Using workspace ID" + workspaceID);

        Project project = getCurrentProject();
        if (project == null) {
            if (projectName == null) {
                session.getConsole().println("No current project is selected. Please select a project.");
                return null;
            } else {
                // select a project
                List<Project> projects = current.project().getWorkspaceProjects(workspaceID).execute();
                for (Project foundProject : projects) {
                    if (foundProject.name.equals(projectName)) {
                        project = foundProject;
                        break;
                    }
                }
            }
        }

        final Project projectToRun = project;

        // Ok now we have the project
        final RunnerStatus runnerStatus = current.runner().run(project).execute();

        new Runnable() {

            @Override
            public void run() {
                RunnerStatus updatedStatus = runnerStatus;
                while (updatedStatus.status != STOPPED) {
                    updatedStatus = current.runner().status(projectToRun, runnerStatus.processId).execute();

                    String log = null;
                    try {
                        log = current.runner().logs(projectToRun, runnerStatus.processId).execute();
                    } catch (RuntimeException e) {
                        session.getConsole().println(e);
                    }
                    if (log != null && log.length() > 0) {
                        session.getConsole().println("Found log = " + log);
                    }
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        session.getConsole().println(e);
                    }
                }
            }
        }.run();

        return null;
    }
}

