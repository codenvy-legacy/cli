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
import com.codenvy.client.model.BuilderState;
import com.codenvy.client.model.BuilderStatus;
import com.codenvy.client.model.RunnerStatus;

import org.fusesource.jansi.Ansi;

import static com.codenvy.cli.command.builtin.util.SHA1.sha1;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;

/**
 * Runner status implementation of {@link com.codenvy.cli.command.builtin.model.UserBuilderStatus}
 * @author Florent Benoit
 */
public class DefaultUserBuilderStatus implements UserBuilderStatus {

    /**
     * Builder status model
     */
    private BuilderStatus builderStatus;

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
     * @param builderStatus the status returned from Rest API
     * @param userProject the user project
     */
    public DefaultUserBuilderStatus(BuilderStatus builderStatus, UserProject userProject) {
        this.builderStatus = builderStatus;
        this.userProject = userProject;

        // b is for builder
        this.sha1Id = sha1("b", String.valueOf(builderStatus.taskId()) + userProject.shortId());
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

    protected String bold(String name) {
        return Ansi.ansi().a(INTENSITY_BOLD).a(name).a(INTENSITY_BOLD_OFF).toString();
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
    public BuilderStatus getInnerStatus() {
        return builderStatus;
    }

    public String toString() {
        BuilderState state = getInnerStatus().status();

        return new DefaultAsciiForm().withEntry(bold("id"), shortId())
                                     .withEntry(bold("workspace"), getProject().getWorkspace().name())
                                     .withEntry(bold("project"), getProject().name())
                                     .withEntry(bold("ide url"), getProject().getInnerProject().ideUrl())
                                     .withEntry(bold("status"), state.toString())
                                     .withUppercasePropertyName()
                                     .toAscii();
    }
}
