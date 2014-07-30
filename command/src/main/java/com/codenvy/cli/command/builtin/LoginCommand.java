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

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

/**
 * Allow to login in the default environment or a given environment
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "login", description = "Login into a remote Codenvy system")
public class LoginCommand extends AbsCommand {

    @Option(name = "--env", description = "Name of the remote codenvy", required = false)
    private String envName;

    @Argument(name = "username", description = "username of the remote environment", required = false, multiValued = false, index = 0)
    private String username;

    @Argument(name = "password", description = "password of the remote environment", required = false, multiValued = false, index = 1)
    private String password;


    @Override
    protected Object doExecute() throws Exception {

        init();

        // Is there any available environment ?
        if (!checkifAvailableEnvironments()) {
            return null;
        }

        if (getMultiEnvCodenvy().login(envName, username, password)) {
            if (envName == null) {
                System.out.println("Login success on default remote '" + getMultiEnvCodenvy().getDefaultEnvironmentName() + "'.");
            } else {
                System.out.println("Login success on remote '" + envName + "'.");
            }
        } else {
            System.out.println("Login failed: please check the credentials.");
        }

        return null;
    }
}
