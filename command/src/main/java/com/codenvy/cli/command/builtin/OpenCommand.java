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

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Open command.
 * This command will open browser on the IDE URL of a given project.
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "open", description = "List all the projects from Codenvy System")
public class OpenCommand extends AbsCommand {

    @Argument(name = "projectID", description = "Specify the project ID to use", required = true, multiValued = false)
    private String projectID;


    /**
     * Prints the current projects per workspace
     */
    protected Object doExecute() {
        init();

        // not logged in
        if (!checkifEnvironments()) {
            return null;
        }

        UserProject project = getMultiEnvCodenvy().getProject(projectID);
        if (project == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No matching project for identifier '").a(projectID).a("'.");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        String ideURL = project.getInnerProject().ideUrl();

        if (!Desktop.getDesktop().isDesktopSupported()) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("Unable to open the URL of the project '").a(ideURL).a("' as this system is not supported.");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        // open the default web browser for the HTML page
        URI uri = null;
        try {
            uri = new URI(ideURL);
        } catch (URISyntaxException e) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("Invalid URL of the project found: '").a(ideURL).a("'.");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }


        try {
            Desktop.getDesktop().browse(uri);
            Ansi buffer = Ansi.ansi();
            buffer.a("URL '").a(ideURL).a("' has been opened in the web browser");
            System.out.println(buffer.toString());
        } catch (IOException e) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("Unable to open URL '").a(ideURL).a("'.");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        return null;
    }

}
