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

import jline.console.ConsoleReader;

import com.codenvy.cli.command.builtin.helper.UserPermissionsHelper;
import com.codenvy.cli.command.builtin.model.UserBuilderStatus;
import com.codenvy.cli.command.builtin.model.UserProjectReference;
import com.codenvy.cli.command.builtin.model.UserRunnerStatus;
import com.codenvy.cli.command.builtin.util.ascii.AsciiArray;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.Ansi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * List command.
 * This command will list all projects used in Codenvy.
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "list", description = "List workspaces, projects and processes")
public class ListCommand extends AbsCommand {

    @Option(name="-v", aliases = {"--verbose"}, description = "Verbose output")
    private boolean verbose;


    /**
     * Prints the current projects per workspace
     */
    protected Object doExecute() throws IOException {
        init();

        // not logged in
        if (!checkifEnabledRemotes()) {
            return null;
        }

        Ansi buffer = Ansi.ansi();

        if (isProgressEnabled()) {
            new ConsoleReader().resetPromptLine("Retrieving projects...", "", 0);
        }
        List<UserProjectReference> projects = getMultiRemoteCodenvy().getProjects();
        if (isProgressEnabled()) {
            new ConsoleReader().resetPromptLine("", "", 0);
        }
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
        List<String> types = new ArrayList<>();
        List<String> privacies = new ArrayList<>();
        List<String> runnerIDs = new ArrayList<>();
        List<String> builderIDs = new ArrayList<>();
        List<String> permissions = new ArrayList<>();

        int count = 0;
        for (UserProjectReference project : projects) {

            // get all runners and builders for this project
            if (isProgressEnabled()) {
                count++;
                String percent = (count * 100) / projects.size() + "/100";
                new ConsoleReader().resetPromptLine("Collecting projects data...", percent, percent.length());
            }
            List<UserRunnerStatus> runners;
            List<UserBuilderStatus> builders;
            List<String> userPermissions;
            if (verbose) {
                 runners = getMultiRemoteCodenvy().getRunners(project);
                 builders = getMultiRemoteCodenvy().getBuilders(project);
                userPermissions = getMultiRemoteCodenvy().getProjectPermissions(project);
            } else {
                runners = Collections.emptyList();
                builders = Collections.emptyList();
                userPermissions = Collections.emptyList();
            }

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
                    types.add(project.getInnerReference().projectTypeId());
                    privacies.add(project.getInnerReference().visibility());
                    permissions.add(UserPermissionsHelper.pretty(userPermissions));
                } else {
                    // blank data
                    ids.add("");
                    remotes.add("");
                    workspaces.add("");
                    projectNames.add("");
                    types.add("");
                    privacies.add("");
                    permissions.add("");
                }

                // runners ?
                if (runners.isEmpty() && i == 1) {
                    runnerIDs.add("none");
                } else if (i <= runners.size()) {
                    // print runner
                    runnerIDs.add(prettyPrint(runners.get(i - 1)));
                } else {
                    runnerIDs.add("");
                }

                // builders ?
                if (builders.isEmpty() && i == 1) {
                    builderIDs.add("none");
                } else if (i <= builders.size()) {
                    // print builder
                    builderIDs.add(prettyPrint(builders.get(i - 1)));
                } else {
                    builderIDs.add("");
                }
            }

        }
        if (isProgressEnabled()) {
            new ConsoleReader().resetPromptLine("", "", 0);
        }
        // Ascii array
        AsciiArray asciiArray;

        // verbose mode : add permissions, runners and builders
        if (verbose) {
            asciiArray = buildAsciiArray().withColumns(ids, remotes, workspaces, projectNames, types, privacies, permissions, builderIDs, runnerIDs).withTitle("ID", "Remote", "Workspace", "Project", "Type", "Privacy", "Perms", "Builders", "Runners");
        } else {
            asciiArray = buildAsciiArray().withColumns(ids, remotes, workspaces, projectNames, types, privacies).withTitle("ID", "Remote", "Workspace", "Project", "Type", "Privacy");
        }

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
