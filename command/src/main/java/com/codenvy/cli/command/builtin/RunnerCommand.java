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

import com.codenvy.cli.command.builtin.model.DefaultUserRunnerStatus;
import com.codenvy.cli.command.builtin.model.UserProjectReference;
import com.codenvy.cli.command.builtin.model.UserRunnerStatus;
import com.codenvy.client.CodenvyErrorException;
import com.codenvy.client.model.Link;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.ProjectReference;
import com.codenvy.client.model.RunnerState;
import com.codenvy.client.model.RunnerStatus;
import com.codenvy.client.model.runner.RunOptions;
import com.codenvy.client.model.runner.RunOptionsBuilder;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.SessionProperties;
import org.fusesource.jansi.Ansi;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.codenvy.client.model.RunnerState.CANCELLED;
import static com.codenvy.client.model.RunnerState.FAILED;
import static com.codenvy.client.model.RunnerState.RUNNING;
import static com.codenvy.client.model.RunnerState.STOPPED;
import static java.lang.String.format;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Allows to run a given project
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "run", description = "Run a project")
public class RunnerCommand extends AbsCommand {

    @Argument(name = "project-id", description = "Specify the project ID to use", required = true, multiValued = false)
    private String projectId;

    @Option(name = "--fg", description = "Run foreground")
    private boolean foreground;

    @Option(name = "--bg", description = "Run background")
    private boolean background;

    @Option(name = "--ram", description = "Set RAM for runner process")
    private int memorySize;

    @Option(name = "--env", description = "Set Environment for runner process")
    private String environment;

    private ExecutorService executorService;

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
        UserProjectReference project = getMultiRemoteCodenvy().getProjectReference(projectId);

        if (project == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No matching project for identifier '").a(projectId).a("'.");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        final ProjectReference projectToRun = project.getInnerReference();

        // first check if the project has a runner
        Project projectDescription = project.getCodenvy().project().getProject(projectToRun.workspaceId(), projectToRun).execute();
        if (projectDescription != null) {
            if (projectDescription.runners() == null || projectDescription.runners().defaultRunner() == null) {
                Ansi buffer = Ansi.ansi();
                buffer.fg(RED);
                buffer.a("The selected project '").a(projectDescription.name()).a("' with ID '").a(projectId)
                      .a("' has no runner defined so this project can't be run.");
                buffer.reset();
                System.out.println(buffer.toString());
                return null;
            }
        }

        RunOptionsBuilder runOptionsBuilder = getMultiRemoteCodenvy().getRunOptionsBuilder();
        if (memorySize > 0) {
            runOptionsBuilder.withMemorySize(memorySize);
        }
        if (environment != null) {
            runOptionsBuilder.withEnvironmentId(environment);
        }

        RunOptions runOptions = runOptionsBuilder.build();


        // Ok now we have the project, run it
        final RunnerStatus runnerStatus;
        try {
            runnerStatus = project.getCodenvy().runner().run(projectToRun, runOptions).execute();
        } catch (CodenvyErrorException e) {
            Boolean val = (Boolean)session.get(SessionProperties.PRINT_STACK_TRACES);
            if (val != null && val.booleanValue()) {
                // let print trace
                throw e;
            }
            // display only the message
            System.out.println(e.getMessage());
            return null;
        }

        UserRunnerStatus userRunnerStatus = new DefaultUserRunnerStatus(runnerStatus, project);


        // ok so now we've started the run
        if (background) {
            useBackGround(userRunnerStatus);
        } else {
            this.executorService = Executors.newFixedThreadPool(3);
            try {
                useForeGround(userRunnerStatus);
            } finally {
                this.executorService.shutdown();
            }
        }

        return null;
    }

    /**
     * Run has been launched, as we're in foregroudn mode, we need to wait that the process start
     * @param userRunnerStatus the runner status
     */
    protected void useForeGround(UserRunnerStatus userRunnerStatus) {
        if (executorService == null) {
            throw new IllegalStateException("No executor thead");
        }
        //ok process has been launched

        // now we have to wait that the process is updated
        ChangeStatus changeStatus = new ChangeStatus(userRunnerStatus);

        Future<UserRunnerStatus> newStatusTask = executorService.submit(changeStatus);
        UserRunnerStatus newStatus;
        // wait for 5 minutes
        try {
            newStatus = newStatusTask.get(5, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException  | TimeoutException e) {
            if (isStackTraceEnabled()) {
                throw new IllegalStateException(e);
            }
            System.out.println("Unable to get updated status for runner " + userRunnerStatus);
            return;
        }


        if (newStatus == null) {
            System.out.println("Unable to find updated status");
            return;
        }

        // it is now running
        if (RUNNING == newStatus.getInnerStatus().status()) {
            String link = "";
            Link webLink = newStatus.getInnerStatus().getWebLink();
            if (webLink != null) {
                link = webLink.href();
            }
            System.out.println(format("Project %s is now running. URL is '%s'", newStatus.getProject().name(), link));
        } else if (STOPPED == newStatus.getInnerStatus().status()) {
            System.out.println(format("Project %s has been executed", newStatus.getProject().name()));
        } else if (FAILED == newStatus.getInnerStatus().status()) {
            System.out.println(format("Project %s has failed", newStatus.getProject().name()));
        } else if (CANCELLED == newStatus.getInnerStatus().status()) {
            System.out.println(format("Project %s has been cancelled", newStatus.getProject().name()));
        }


        // print logs if not cancelled
        if (CANCELLED != newStatus.getInnerStatus().status()) {
            String logs = newStatus.getProject().getCodenvy().runner()
                                   .logs(newStatus.getProject().getInnerReference(), newStatus.getInnerStatus().processId()).execute();
            System.out.println("Logs:");
            System.out.println(logs);
        }

        Ansi buffer = Ansi.ansi();
        buffer.a("Runner ID is ").a(INTENSITY_BOLD).a(userRunnerStatus.shortId()).a(INTENSITY_BOLD_OFF);
        System.out.println(buffer.toString());

    }


    /**
     * Run has been launched, just inform the user that process has been launched.
     * @param userRunnerStatus the runner status
     */
    protected void useBackGround(UserRunnerStatus userRunnerStatus) {
        Ansi buffer = Ansi.ansi();
        buffer.a("Run task for project ").a(INTENSITY_BOLD).a(userRunnerStatus.getProject().name()).a(INTENSITY_BOLD_OFF).a("' has been submitted with runner ID ").a(INTENSITY_BOLD).a(userRunnerStatus.shortId()).a(INTENSITY_BOLD_OFF).a(System.lineSeparator());
        System.out.println(buffer.toString());
        System.out.println(userRunnerStatus);

    }


    static class ChangeStatus implements Callable<UserRunnerStatus> {

        private UserRunnerStatus status;

        private RunnerState current;

        public ChangeStatus(UserRunnerStatus status) {
            this.status = status;
            this.current = status.getInnerStatus().status();
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception
         *         if unable to compute a result
         */
        @Override
        public UserRunnerStatus call() throws Exception {

            RunnerState newState = current;
            RunnerStatus updatedRunnerStatus = null;
            new ConsoleReader().resetPromptLine("Starting ...", "", 1);
            List<String> progress = Arrays.asList("|", "/", "-", "\\");

            int index = 0;

            while (newState == current) {
                new ConsoleReader().resetPromptLine("Starting ...", progress.get(index), 1);

                try {
                    updatedRunnerStatus = status.getProject().getCodenvy().runner()
                                                .status(status.getProject().getInnerReference(), status.getInnerStatus().processId())
                                                .execute();
                } catch (CodenvyErrorException e) {
                    return null;
                }

                newState = updatedRunnerStatus.status();

                // Wait 3 seconds
                Thread.sleep(3000L);

                index++;

                if (index >= progress.size()) {
                    index = 0;
                }

            }
            new ConsoleReader().resetPromptLine("Starting done", "", 0);
            System.out.println();

            // status has been changed
            return new DefaultUserRunnerStatus(updatedRunnerStatus, status.getProject());

        }
    }
}

