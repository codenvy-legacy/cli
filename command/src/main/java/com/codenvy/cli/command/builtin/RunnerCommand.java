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

import com.codenvy.cli.command.builtin.model.DefaultUserRunnerStatus;
import com.codenvy.cli.command.builtin.model.UserProject;
import com.codenvy.cli.command.builtin.model.UserRunnerStatus;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.RunnerStatus;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Allows to run a given project
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "run", description = "Run a project in Codenvy System")
public class RunnerCommand extends AbsCommand {

    @Argument(name = "project-id", description = "Specify the project ID to use", required = true, multiValued = false)
    private String projectId;

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

        // do we have the projectID ?
        if (projectId == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No projectID has been set");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        // get project for the given shortID
        UserProject project = getMultiRemoteCodenvy().getProject(projectId);

        if (project == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No matching project for identifier '").a(projectId).a("'.");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }


        final Project projectToRun = project.getInnerProject();

        // Ok now we have the project
        final RunnerStatus runnerStatus = project.getCodenvy().runner().run(projectToRun).execute();

        UserRunnerStatus userRunnerStatus = new DefaultUserRunnerStatus(runnerStatus, project);

        Ansi buffer = Ansi.ansi();
        buffer.a("Run task for project ").a(INTENSITY_BOLD).a(project.name()).a(INTENSITY_BOLD_OFF).a("' has been submitted with runner ID ").a(INTENSITY_BOLD).a(userRunnerStatus.shortId()).a(INTENSITY_BOLD_OFF).a(System.lineSeparator());
        System.out.println(buffer.toString());
        System.out.println(userRunnerStatus);

        return null;
    }
}

