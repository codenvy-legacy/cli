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
package com.codenvy.cli.command.builtin.model;

import com.codenvy.client.Codenvy;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.WorkspaceRef;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link com.codenvy.cli.command.builtin.model.UserWorkspace} that allow to have relationship between project and
 * workspaces.
 *
 * @author Florent Benoit
 */
public class DefaultUserWorkspace implements UserWorkspace {

    /**
     * Codenvy object used to interact with remote API.
     */
    private Codenvy codenvy;

    /**
     * Workspace ref object used to extract the data
     */
    private WorkspaceRef linkWorkspace;

    /**
     * Build a new workspace with a link to the remote api and workspace ref
     *
     * @param codenvy
     *         codenvy object
     * @param workspaceRef
     *         the given workspace reference
     */
    public DefaultUserWorkspace(Codenvy codenvy, WorkspaceRef workspaceRef) {
        this.codenvy = codenvy;
        this.linkWorkspace = workspaceRef;
    }

    /**
     * @return identifier of the workspace
     */
    @Override
    public String id() {
        return linkWorkspace.id();
    }

    /**
     * @return the name of the workspace
     */
    @Override
    public String name() {
        return linkWorkspace.name();
    }

    /**
     * @return projects that are in this workspace
     */
    @Override
    public List<UserProject> getProjects() {
        List<? extends Project> readProjects = codenvy.project().getWorkspaceProjects(id()).execute();
        List<UserProject> projects = new ArrayList<>();
        for (Project readProject : readProjects) {
            UserProject userProject = new DefaultUserProject(codenvy, readProject, this);
            projects.add(userProject);
        }
        return projects;
    }

}
