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
import static org.fusesource.jansi.Ansi.Color.BLUE;
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

        Ansi buffer = Ansi.ansi();

        buffer.a(INTENSITY_BOLD).a("The current Codenvy commands are \n").a(INTENSITY_BOLD_OFF);

        // Login
        buffer.a("\n");
        buffer.fg(BLUE);
        buffer.a("codenvy:login");
        buffer.fg(DEFAULT);
        buffer.a(" : Log into codenvy");

        // List
        buffer.a("\n");
        buffer.fg(BLUE);
        buffer.a("codenvy:list");
        buffer.fg(DEFAULT);
        buffer.a(" : List all workspaces from Codenvy");

        // Logout
        buffer.a("\n");
        buffer.fg(BLUE);
        buffer.a("codenvy:logout");
        buffer.fg(DEFAULT);
        buffer.a(" : Logout from Codenvy");

        System.out.println(buffer.toString());
        return null;
    }
}
