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

    /**
     * Display the current commands.
     * @return nothing
     */
    protected Object doExecute() {
        init();

        Ansi buffer = Ansi.ansi();

        buffer.a(INTENSITY_BOLD).a("COMMANDS").a(INTENSITY_BOLD_OFF);

        // env
        buffer.a("\n");
        buffer.fg(CYAN);
        buffer.a("codenvy:env");
        buffer.reset();
        buffer.a(": Manage Codenvy environments");

        // List
        buffer.a("\n");
        buffer.fg(CYAN);
        buffer.a("codenvy:list");
        buffer.reset();
        buffer.a(": List all projects from Codenvy");

        // Build
        buffer.a("\n");
        buffer.fg(CYAN);
        buffer.a("codenvy:build");
        buffer.reset();
        buffer.a(": Build a given project");

        // Run
        buffer.a("\n");
        buffer.fg(CYAN);
        buffer.a("codenvy:run");
        buffer.reset();
        buffer.a(": Run a given project");

        // Logs
        buffer.a("\n");
        buffer.fg(CYAN);
        buffer.a("codenvy:logs");
        buffer.reset();
        buffer.a(": Display logs for a given runner/builder");


        // Environments
        buffer.a("\n");
        buffer.a("\n");
        buffer.a(getMultiEnvCodenvy().listEnvironments());

        buffer.a("To manage environments, use the codenvy:env command");


        System.out.println(buffer.toString());
        return null;
    }
}
