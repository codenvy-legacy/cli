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

import com.codenvy.cli.command.builtin.model.UserBuilderStatus;
import com.codenvy.cli.command.builtin.model.UserProject;
import com.codenvy.cli.command.builtin.model.UserRunnerStatus;
import com.codenvy.cli.command.builtin.util.ascii.AsciiArray;

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
        init();

        // not logged in
        if (!checkifEnabledEnvironments()) {
            return null;
        }

        Ansi buffer = Ansi.ansi();


        List<UserProject> projects = getMultiEnvCodenvy().getProjects();
        if (projects.isEmpty()) {
            buffer.a("No projects");
            System.out.println(buffer.toString());
            return null;
        }

        // Titles
        List<String> ids = new ArrayList<>();
        List<String> remotes = new ArrayList<>();
        List<String> workspaces = new ArrayList<>();
        List<String> projectNames = new ArrayList<>();
        List<String> runnerIDs = new ArrayList<>();
        List<String> builderIDs = new ArrayList<>();

        for (UserProject project : projects) {

            // get all runners and builders for this project
            List<UserRunnerStatus> runners = getMultiEnvCodenvy().getRunners(project);
            List<UserBuilderStatus> builders = getMultiEnvCodenvy().getBuilders(project);

            // ok, now we need to know how many lines we need
            int lines = 1;
            if (runners.size() > lines) {
                lines = runners.size();
            }
            if (builders.size() > lines) {
                lines = builders.size();
            }

            // loop on items
            for (int i = 1; i <= lines; i++) {
                // we add basic stuff only on the first line
                if (i == 1) {
                    ids.add(project.shortId());
                    remotes.add(project.getWorkspace().getRemote());
                    workspaces.add(project.getWorkspace().name());
                    projectNames.add(project.name());
                } else {
                    // blank data
                    ids.add("");
                    remotes.add("");
                    workspaces.add("");
                    projectNames.add("");
                }

                // runners ?
                if (i <= runners.size()) {
                    // print runner
                    runnerIDs.add(prettyPrint(runners.get(i - 1)));
                } else {
                    runnerIDs.add("");
                }

                // builders ?
                if (i <= builders.size()) {
                    // print builder
                    builderIDs.add(prettyPrint(builders.get(i - 1)));
                } else {
                    builderIDs.add("");
                }
            }

        }

        // Ascii array
        AsciiArray asciiArray = buildAsciiArray().withColumns(ids, remotes, workspaces, projectNames, builderIDs, runnerIDs).withTitle("ID", "Remote", "Workspace", "Project", "Builders", "Runners");
        System.out.println(asciiArray.toAscii());

        return null;
    }



    protected String prettyPrint(UserRunnerStatus runnerStatus) {
        StringBuilder sb = new StringBuilder(runnerStatus.shortId());
        sb.append("[");
        switch (runnerStatus.getInnerStatus().status()) {
            case RUNNING:
                sb.append("RUN");
                break;
            case CANCELLED:
                sb.append("CANCEL");
                break;
            case NEW:
                sb.append("WAIT");
                break;
            case FAILED:
                sb.append("FAIL");
                break;
            case STOPPED:
                sb.append("STOP");
                break;
        }
        sb.append("]");
        return sb.toString();
    }

    protected String prettyPrint(UserBuilderStatus builderStatus) {
        StringBuilder sb = new StringBuilder(builderStatus.shortId());
        sb.append("[");
        switch (builderStatus.getInnerStatus().status()) {
            case IN_QUEUE:
                sb.append("WAIT");
                break;
            case IN_PROGRESS:
                sb.append("RUN");
                break;
            case FAILED:
                sb.append("FAIL");
                break;
            case CANCELLED:
                sb.append("CANCEL");
                break;
            case SUCCESSFUL:
                sb.append("OK");
                break;
        }
        sb.append("]");
        return sb.toString();
    }

}
