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

import com.codenvy.cli.command.builtin.model.UserProject;
import com.codenvy.cli.command.builtin.util.ascii.AsciiArray;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import java.util.ArrayList;
import java.util.List;

import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;

/**
 * Env command.
 * This command will allow to manage environments that can be connected to the CLI.
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "env", description = "Manage Codenvy environments")
public class EnvCommand extends AbsCommand {

    @Argument(name = "flag", description = "Manage environment : add/remove/rename", required = false, multiValued = false, index = 0)
    private String flag;

    @Argument(name = "name", description = "name of the environment", required = false, multiValued = false, index = 1)
    private String name;

    @Argument(name = "option1", description = "option of the command", required = false, multiValued = false, index = 2)
    private String option1;

    @Argument(name = "option2", description = "option of the command", required = false, multiValued = false, index = 3)
    private String option2;

    @Argument(name = "option3", description = "option of the command", required = false, multiValued = false, index = 4)
    private String option3;

    /**
     * Prints the current projects per workspace
     */
    protected Object doExecute() {
        init();

        // no method, so list all environments
        if (flag == null || flag.isEmpty()) {
            listEnvironments();
            return null;
        } else if ("add".equals(flag)) {
            addEnvironment();
            return null;
        } else if ("remove".equals(flag)) {
            removeEnvironment();
            return null;
/*        } else if ("rename".equals(flag)) {
            renameEnvironment();
            return null;
            */
        }

        // invalid flag
        Ansi buffer = Ansi.ansi();
            buffer.a("Invalid command '").a(flag).a(": should start with env <add|remove> [environment-name]");
            System.out.println(buffer.toString());
            return null;
    }

    protected void listEnvironments() {
        System.out.println(getMultiEnvCodenvy().listEnvironments());
        if (!getMultiEnvCodenvy().hasEnvironments()) {
            System.out.println("To add a new environment, use the env add <env-name> <URL> <username> <password> command");

        }
    }

    protected void addEnvironment() {
        String url = option1;
        String username = option2;
        String password = option3;


        Ansi buffer = Ansi.ansi();
        // OK, so we need to have name, URL, username and password
        if (!ok(name) || !ok(url) || !ok(username) || !ok(password)) {
            buffer.a("Invalid add command: should be env add <env-name> <URL> <username> <password>");
            System.out.println(buffer.toString());
            return;
        }

        // ok let's try to add the env
        if (getMultiEnvCodenvy().addEnvironment(name, url, username, password)) {
            buffer.a("The environment '").a(name).a("' has been successfully added");
            System.out.println(buffer.toString());
            return;
        }


    }

    protected void removeEnvironment() {
        Ansi buffer = Ansi.ansi();
        // OK, so we need to have name, URL, username and password
        if (!ok(name)) {
            buffer.a("Invalid remove command: should be env add <env-name>");
            System.out.println(buffer.toString());
            return;
        }

        if (getMultiEnvCodenvy().removeEnvironment(name)) {
            buffer.a("The environment '").a(name).a("'  has been successfully removed");
            System.out.println(buffer.toString());
            return;
        }

    }

    boolean ok(String param) {
        return param != null && !param.isEmpty();
    }


}
