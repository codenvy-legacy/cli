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


import com.codenvy.client.Codenvy;

import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

/**
 * Logout the current user (remove the shared codenvy instance)
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "logout", description = "Logout of Codenvy System")
public class LogoutCommand extends AbsCommand {

    /**
     * Execute the command.
     */
    protected Object doExecute() {

        Codenvy current = getCurrentCodenvy();

        Ansi buffer = Ansi.ansi();
        buffer.a("Logout ");

        // unset
        if (current != null) {
            session.put(Codenvy.class.getName(), null);
            buffer.fg(Ansi.Color.GREEN);
            buffer.a("OK");
            buffer.fg(Ansi.Color.DEFAULT);
        } else {
            buffer.fg(Ansi.Color.RED);
            buffer.a("failed");
            buffer.fg(Ansi.Color.DEFAULT);
            buffer.a(" : Not logged in !");
        }

        // print
        session.getConsole().println(buffer.toString());


        return null;
    }
}
