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
import org.fusesource.jansi.Ansi;

/**
 * Remote command.
 * This command will allow to manage remote environments that can be connected to the CLI.
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "remote", description = "Manage Codenvy remote environments")
public class RemoteCommand extends AbsCommand {

    @Argument(name = "flag", description = "Manage environment : add/remove/rename", required = false, multiValued = false, index = 0)
    private String flag;

    @Argument(name = "name", description = "name of the environment", required = false, multiValued = false, index = 1)
    private String name;

    @Argument(name = "option1", description = "option of the command", required = false, multiValued = false, index = 2)
    private String option1;

    /**
     * Prints the current projects per workspace
     */
    protected Object doExecute() {
        init();

        // no method, so list all environments
        if (flag == null || flag.isEmpty()) {
            listRemote();
            return null;
        } else if ("add".equals(flag)) {
            addRemote();
            return null;
        } else if ("remove".equals(flag)) {
            removeRemote();
            return null;
/*        } else if ("rename".equals(flag)) {
            renameEnvironment();
            return null;
            */
        }

        // invalid flag
        Ansi buffer = Ansi.ansi();
            buffer.a("Invalid command '").a(flag).a(": should start with env <add|remove> [remote-name]");
            System.out.println(buffer.toString());
            return null;
    }

    protected void listRemote() {
        System.out.println(getMultiEnvCodenvy().listEnvironments());
        if (!getMultiEnvCodenvy().hasReadyEnvironments()) {
            System.out.println("To add a new environment, use the remote add <remote-name> <URL> command");

        }
    }

    protected void addRemote() {
        String url = option1;

        Ansi buffer = Ansi.ansi();
        // OK, so we need to have name, URL
        if (!ok(name) || !ok(url)) {
            buffer.a("Invalid add command: should be env add <env-name> <URL>");
            System.out.println(buffer.toString());
            return;
        }

        // ok let's try to add the remote
        if (getMultiEnvCodenvy().addRemote(name, url)) {
            buffer.a("The environment '").a(name).a("' has been added. Login on this environment needs to be performed.");
            System.out.println(buffer.toString());
            return;
        }


    }

    protected void removeRemote() {
        Ansi buffer = Ansi.ansi();
        // OK, so we need to have name
        if (!ok(name)) {
            buffer.a("Invalid remove command: should be remote remove <env-name>");
            System.out.println(buffer.toString());
            return;
        }

        if (getMultiEnvCodenvy().removeEnvironment(name)) {
            buffer.a("The remote Codenvy '").a(name).a("'  has been successfully removed");
            System.out.println(buffer.toString());
            return;
        }

    }

    boolean ok(String param) {
        return param != null && !param.isEmpty();
    }


}
