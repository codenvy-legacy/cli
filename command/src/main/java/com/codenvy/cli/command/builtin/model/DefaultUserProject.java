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

import com.codenvy.cli.command.builtin.util.SHA1;
import com.codenvy.client.Codenvy;
import com.codenvy.client.model.Project;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.codenvy.cli.command.builtin.util.SHA1.sha1;

/**
 * Implements the {@link com.codenvy.cli.command.builtin.model.UserProject} interface providing links between project and workspaces.
 *
 * @author Florent Benoit
 */
public class DefaultUserProject implements UserProject {

    /**
     * Current project
     */
    private Project project;

    /**
     * Current workspace
     */
    private UserWorkspace workspace;

    /**
     * Short identifier
     */
    private String sha1Id;

    private Codenvy codenvy;

    /**
     * Build a project that will be linked to its workspace
     *
     * @param codenvy
     *         the codenvy object used to interact with the remote API
     * @param project
     *         the given Remote project
     * @param workspace
     *         the current workspace
     */
    public DefaultUserProject(Codenvy codenvy, Project project, UserWorkspace workspace) {
        this.codenvy = codenvy;
        this.project = project;
        this.workspace = workspace;

        // compute short id
        String fullID = workspace.id() + project.id();

        // p is for project
        this.sha1Id = sha1("p", fullID);
    }

    /**
     * @return the name of the project
     */
    public String name() {
        return project.name();
    }

    /**
     * @return the workspace used by this project
     */
    @Override
    public UserWorkspace getWorkspace() {
        return workspace;
    }

    /**
     * @return full sha1 ID
     */
    public String sha1ID() {
        return sha1Id;
    }

    /**
     * @return only the first 7 digits
     */
    public String shortId() {
        return sha1Id.substring(0, 7);
    }


    /**
     * @return the inner project object
     */
    public Project getInnerProject() {
        return project;
    }


    public Codenvy getCodenvy() {
        return codenvy;
    }


}
