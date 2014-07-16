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

import com.codenvy.cli.command.builtin.model.DefaultUserProject;
import com.codenvy.cli.command.builtin.model.DefaultUserWorkspace;
import com.codenvy.cli.command.builtin.model.UserProject;
import com.codenvy.cli.command.builtin.model.UserWorkspace;
import com.codenvy.cli.command.builtin.util.AsciiArray;
import com.codenvy.client.Codenvy;
import com.codenvy.client.Request;
import com.codenvy.client.WorkspaceClient;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.Workspace;
import com.codenvy.client.model.WorkspaceRef;

import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import java.util.ArrayList;
import java.util.List;

/**
 * List command.
 * This command will list all projects used in Codenvy.
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "list", description = "List all the projects from Codenvy System")
public class ListCommand extends AbsCommand {

    /**
     * Prints the current projects per workspace
     */
    protected Object doExecute() {
        Codenvy current = checkLoggedIn();

        Ansi buffer = Ansi.ansi();


       List<UserProject> projects = getProjects(current);
       if (projects.isEmpty()) {
            buffer.a("No projects");
           session.getConsole().println(buffer.toString());
           return null;
       }

        // Titles
        List<String> ids = new ArrayList<>();
        List<String> workspaces = new ArrayList<>();
        List<String> projectNames = new ArrayList<>();

        for (UserProject project : projects) {
            ids.add(project.shortId());
            workspaces.add(project.getWorkspace().name());
            projectNames.add(project.name());
        }

        // Ascii array
        AsciiArray asciiArray = new AsciiArray().withColumns(ids, workspaces, projectNames).withTitle("ID", "Workspace", "Project");
        session.getConsole().println(asciiArray.toAscii());

        return null;
    }

    /**
     * Gets list of all projects for the current user
     * @param codenvy the codenvy object used to retrieve the data
     * @return the list of projects
     */
    List<UserProject> getProjects(Codenvy codenvy) {
        List<UserProject> projects = new ArrayList<>();

        // For each workspace, search the project and compute

        WorkspaceClient workspaceClient = codenvy.workspace();
        Request<List<? extends Workspace>> request = workspaceClient.all();
        List<? extends Workspace> readWorkspaces = request.execute();

        for (Workspace workspace : readWorkspaces) {
            WorkspaceRef ref = codenvy.workspace().withName(workspace.workspaceRef().name()).execute();
            // Now skip all temporary workspaces
            if (ref.isTemporary()) {
                continue;
            }

            DefaultUserWorkspace defaultUserWorkspace = new DefaultUserWorkspace(codenvy, ref);

            List<? extends Project> readProjects = codenvy.project().getWorkspaceProjects(ref.id()).execute();
            for (Project readProject : readProjects) {
                DefaultUserProject project = new DefaultUserProject(codenvy, readProject, defaultUserWorkspace);
                projects.add(project);
            }
        }
        return projects;
    }


}
