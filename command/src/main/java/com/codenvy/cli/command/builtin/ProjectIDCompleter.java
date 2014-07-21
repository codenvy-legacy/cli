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
import com.codenvy.client.Codenvy;
import com.codenvy.client.Request;
import com.codenvy.client.WorkspaceClient;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.Workspace;
import com.codenvy.client.model.WorkspaceReference;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.console.CommandSessionHolder;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florent Benoit
 */
public class ProjectIDCompleter implements Completer {

    /**
     * @param buffer the beginning string typed by the user
     * @param cursor the position of the cursor
     * @param candidates the list of completions proposed to the user
     */
    public int complete(String buffer, int cursor, List candidates) {

        // gets the current session
        CommandSession commandSession = CommandSessionHolder.getSession();

        StringsCompleter delegate = new StringsCompleter();

        // get current client
        Codenvy codenvy = (Codenvy) commandSession.get(Codenvy.class.getName());

        if (codenvy != null) {
            // get current projects
            List<UserProject> projects = getProjects(codenvy);
            for (UserProject project : projects) {
                delegate.getStrings().add(project.shortId());
            }
        }
        return delegate.complete(buffer, cursor, candidates);
    }



    /**
     * Gets list of all projects for the current user
     * @param codenvy the codenvy object used to retrieve the data
     * @return the list of projects
     */
    protected List<UserProject> getProjects(Codenvy codenvy) {
        List<UserProject> projects = new ArrayList<>();

        // For each workspace, search the project and compute

        WorkspaceClient workspaceClient = codenvy.workspace();
        Request<List<? extends Workspace>> request = workspaceClient.all();
        List<? extends Workspace> readWorkspaces = request.execute();




        for (Workspace workspace : readWorkspaces) {
            WorkspaceReference ref = workspace.workspaceReference();

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
