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

import com.codenvy.cli.command.builtin.util.ascii.DefaultAsciiForm;
import com.codenvy.client.model.RunnerState;
import com.codenvy.client.model.RunnerStatus;

import org.fusesource.jansi.Ansi;

import java.util.Date;

import static com.codenvy.cli.command.builtin.util.SHA1.sha1;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;

/**
 * Runner status implementation of {@link com.codenvy.cli.command.builtin.model.UserRunnerStatus}
 * @author Florent Benoit
 */
public class DefaultUserRunnerStatus implements UserRunnerStatus {

    /**
     * Runner status model
     */
    private RunnerStatus runnerStatus;

    /**
     * Project on which this runner process is linked
     */
    private UserProject userProject;

    /**
     * SHA-1.
     */
    private String sha1Id;

    /**
     * Default constructor
     * @param runnerStatus the status returned from Rest API
     * @param userProject the user project
     */
    public DefaultUserRunnerStatus(RunnerStatus runnerStatus, UserProject userProject) {
        this.runnerStatus = runnerStatus;
        this.userProject = userProject;

        // r is for runner
        this.sha1Id = sha1("r", String.valueOf(runnerStatus.processId()) + userProject.shortId());
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


    protected String bold(String name) {
        return Ansi.ansi().a(INTENSITY_BOLD).a(name).a(INTENSITY_BOLD_OFF).toString();
    }

    public String toString() {

        RunnerState state = getInnerStatus().status();

        String startTime;
        long start = getInnerStatus().startTime();
        if (start > 0) {
            startTime = new Date(start).toString();
        } else {
            startTime = "N/A";
        }

        String stopTime;
        long stop = getInnerStatus().stopTime();
        if (stop > 0) {
            stopTime = new Date(start).toString();
        } else {
            stopTime = "N/A";
        }

        return new DefaultAsciiForm().withEntry(bold("id"), shortId())
                                     .withEntry(bold("workspace"), getProject().getWorkspace().name())
                                     .withEntry(bold("project"), getProject().name())
                                     .withEntry(bold("ide url"), getProject().getInnerProject().ideUrl())
                                     .withEntry(bold("status"), state.toString())
                                     .withEntry(bold("start time"), startTime)
                                     .withEntry(bold("stop time"), stopTime)
                                     .withUppercasePropertyName()
                                     .toAscii();

    }
}
