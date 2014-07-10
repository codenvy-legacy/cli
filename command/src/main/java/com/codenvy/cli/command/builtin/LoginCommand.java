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
import com.codenvy.client.CodenvyException;
import com.codenvy.client.auth.AuthenticationException;
import com.codenvy.client.auth.Credentials;
import static com.codenvy.cli.command.builtin.Constants.*;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.Ansi;

/**
 * Allows to be authenticated on Codenvy
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "login", description = "Login into Codenvy System")
public class LoginCommand extends AbsCommand {

    /**
     * Host on which to perform the authentication. (override host defined in the configuration file)
     */
    @Option(name = "-h", aliases = {"--host"}, description = "URL of codenvy server", required = false, multiValued = false)
    private String host;

    /**
     * TODO: check ?
     */
    @Option(name = "-c", aliases = {"--check"}, description = "Check", required = false, multiValued = false)
    private boolean check;

    /**
     * Specify the username (override user defined in the configuration file)
     */
    @Option(name = "-u", aliases = {"--username"}, description = "Username", required = false, multiValued = false)
    private String username;

    /**
     * Specify the password (override password defined in the configuration file)
     */
    @Option(name = "-p", aliases = {"--password"}, description = "Password", required = false, multiValued = false)
    private String password;

    /**
     * Launch the authentication
     * @return
     */
    protected Object doExecute() {
        // use configuration file if properties are not defined
        if (host == null) {
            host = getCodenvyProperty(HOST_PROPERTY);
        }
        if (username == null) {
            username = getCodenvyProperty(USERNAME_PROPERTY);
        }
        if (password == null) {
            password = getCodenvyProperty(PASSWORD_PROPERTY);
        }

        // Manage credentials
        Credentials credentials = new Credentials.Builder().withUsername(username)
                                                           .withPassword(password)
                                                           .build();
        Codenvy codenvy = new Codenvy.Builder(host, username).withCredentials(credentials).build();

        Ansi buffer = Ansi.ansi();

        buffer.a("Login ");

        // success or not ?
        try {
            // Login
            codenvy.user().current().execute();
            buffer.fg(Ansi.Color.GREEN);
            buffer.a("OK");
            buffer.fg(Ansi.Color.DEFAULT);
            buffer.a(" : Welcome ");
            buffer.fg(Ansi.Color.BLUE);
            buffer.a(username);
            buffer.fg(Ansi.Color.DEFAULT);
        } catch (CodenvyException | AuthenticationException e) {
            buffer.fg(Ansi.Color.RED);
            buffer.a("failed");
            buffer.fg(Ansi.Color.DEFAULT);
            buffer.a(" : Unable to perform login : ");
            buffer.a(e.getMessage());
        }

        // Keep the codenvy object
        session.put(Codenvy.class.getName(), codenvy);

        // print result
        session.getConsole().println(buffer.toString());


        return null;
    }
}
