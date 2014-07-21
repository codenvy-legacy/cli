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

import com.codenvy.client.model.RunnerStatus;

import static com.codenvy.cli.command.builtin.util.SHA1.sha1;

/**
 * @author Florent Benoit
 */
public class DefaultUserRunnerStatus implements UserRunnerStatus {

    private RunnerStatus runnerStatus;

    private UserProject userProject;

    private String sha1Id;

    public DefaultUserRunnerStatus(RunnerStatus runnerStatus, UserProject userProject) {
        this.runnerStatus = runnerStatus;
        this.userProject = userProject;

        this.sha1Id = sha1("ps", String.valueOf(runnerStatus.processId()) + userProject.shortId());
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
     * @return the linked project
     */
    @Override
    public UserProject getProject() {
        return userProject;
    }

    /**
     * @return inner status
     */
    public RunnerStatus getInnerStatus() {
        return runnerStatus;
    }
}
