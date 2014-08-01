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


import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;
import static org.fusesource.jansi.Ansi.Color.CYAN;

/**
 * Defines a global help for Codenvy commands available.
 * This will be used when using for example ./codenvy without arguments as the name of this command is empty and is in the codenvy prefix.
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "", description = "Help")
public class HelpCommand extends AbsCommand {


    protected String color(String name) {
        return Ansi.ansi().fg(CYAN).a(name).reset().toString();
    }

    /**
     * Display the current commands.
     * @return nothing
     */
    protected Object doExecute() {
        init();

        Ansi buffer = Ansi.ansi();

        buffer.a(INTENSITY_BOLD).a("COMMANDS").a(INTENSITY_BOLD_OFF).a("\n");

        String value = buildAsciiForm().withEntry(color("remote"), "Add or remove remote Codenvy cloud references")
                                       .withEntry(color("login"), "Login to a remote Codenvy cloud")
                                       .withEntry(color("list"), "List workspaces, projects and processes")
                                       .withEntry(color("build"), "Build a project")
                                       .withEntry(color("run"), "Run a project")
                                       .withEntry(color("logs"), "Display output logs for a runner or builder")
                                       .withEntry(color("info"), "Display information for a project, runner, or builder")
                                       .withEntry(color("open"), "Starts a browser session to access a project, builder or runner")
                                       .withEntry(color("stop"), "Stop one or more runner processes")
                                       .alphabeticalSort().toAscii();

        buffer.a(value);

        // Remotes
        buffer.a("\n");
        buffer.a("\n");
        buffer.a(getMultiRemoteCodenvy().listRemotes());
        buffer.a("\n");
        buffer.a("To manage remote instances, use the remote command");


        System.out.println(buffer.toString());
        return null;
    }
}
