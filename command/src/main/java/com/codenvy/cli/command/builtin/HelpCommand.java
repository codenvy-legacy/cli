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


import com.codenvy.cli.command.builtin.util.ascii.AsciiForm;

import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import java.util.Collection;

import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;
import static org.fusesource.jansi.Ansi.Color.CYAN;
import static org.fusesource.jansi.Ansi.Color.DEFAULT;

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

        String value = buildAsciiForm().withEntry(color("remote"), "Manage remote Codenvy instances")
                                       .withEntry(color("login"), "Login in remote Codenvy instance")
                                       .withEntry(color("list"), "List all projects from Codenvy")
                                       .withEntry(color("build"), "Build a given project")
                                       .withEntry(color("run"), "Run a given project")
                                       .withEntry(color("logs"), "Display logs for a given runner/builder")
                                       .withEntry(color("info"), "Display information on a given project/runner/builder ID")
                                       .withEntry(color("stop"), "Stop the given runner ID or all runners of a given project ID")
                                       .alphabeticalSort().toAscii();

        buffer.a(value);

        // Remotes
        buffer.a("\n");
        buffer.a("\n");
        buffer.a(getMultiEnvCodenvy().listEnvironments());

        buffer.a("To manage remote instances, use the remote command");


        System.out.println(buffer.toString());
        return null;
    }
}
