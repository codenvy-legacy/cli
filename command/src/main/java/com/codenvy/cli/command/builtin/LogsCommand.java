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
import com.codenvy.client.Codenvy;
import com.codenvy.client.model.Link;
import com.codenvy.client.model.RunnerState;
import com.codenvy.client.model.RunnerStatus;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.Ansi;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows to print the logs of a given runner
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "logs", description = "Show the logs of the given runner/builder ID")
public class LogsCommand extends AbsCommand {

    @Argument(name = "id", description = "Specify the runner/builder ID", required = true, multiValued = false)
    private String processID;

    protected Object doExecute() throws Exception {
        final Codenvy current = checkLoggedIn();

        // not logged in
        if (current == null) {
            return null;
        }

        // first collect all processes
        List<UserProject> projects = getProjects(current);

        List<UserRunnerStatus> matchingStatuses = new ArrayList<>();

        // then for each project, gets the process IDs
        for (UserProject userProject : projects) {
            final List<? extends RunnerStatus> runnerStatuses = current.runner().processes(userProject.getInnerProject()).execute();
            for (RunnerStatus runnerStatus : runnerStatuses) {

                UserRunnerStatus tmpStatus = new DefaultUserRunnerStatus(runnerStatus, userProject);
                if (tmpStatus.shortId().startsWith(processID)) {
                    matchingStatuses.add(tmpStatus);
                }
            }
        }

        if (matchingStatuses.size() == 0) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(Ansi.Color.RED);
            buffer.a("No Runner ID found with identifier '").a(processID).a("'.");
            buffer.fg(Ansi.Color.DEFAULT);
            session.getConsole().println(buffer.toString());
            return null;
        } else if (matchingStatuses.size() > 1) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(Ansi.Color.RED);
            buffer.a("Too many runners have been found with identifier '").a(processID).a("'. Please add extra data to the identifier");
            buffer.fg(Ansi.Color.DEFAULT);
            session.getConsole().println(buffer.toString());
            return null;
        }

        // only one matching status
        UserRunnerStatus foundStatus = matchingStatuses.get(0);

        RunnerState state = foundStatus.getInnerStatus().status();

        // not in a valid state
        if (state != RunnerState.RUNNING && state != RunnerState.STOPPED) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(Ansi.Color.RED);
            buffer.a("Logs are only available in RUNNING or STOPPED state. Current state is ").a(state);
            buffer.fg(Ansi.Color.DEFAULT);
            session.getConsole().println(buffer.toString());
            return null;
        }


        // want to print log
        String log = current.runner().logs(foundStatus.getProject().getInnerProject(), foundStatus.getInnerStatus().processId()).execute();
        session.getConsole().println(log);

        return null;

    }

}

