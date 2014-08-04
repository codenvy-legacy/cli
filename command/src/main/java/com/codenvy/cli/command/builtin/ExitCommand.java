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

import com.codenvy.cli.command.builtin.model.DefaultUserBuilderStatus;
import com.codenvy.cli.command.builtin.model.UserBuilderStatus;
import com.codenvy.cli.command.builtin.model.UserProject;
import com.codenvy.client.model.BuilderStatus;
import com.codenvy.client.model.Project;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;
import org.osgi.framework.Bundle;

import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Exit of the current CLI
 *
 * @author Florent Benoit
 */
@Command(scope = "*", name = "exit", description = "Exit of the shell")
public class ExitCommand extends AbsCommand {

    /**
     * Execute the command
     */
    @Override
    protected Object doExecute() throws Exception {
        init();

        // stop system bundle
        try {
            Bundle bundle = getBundleContext().getBundle(0);
            bundle.stop();
        } catch (Exception e) {
            log.error("Error when shutting down Apache Karaf", e);
        }
        return null;
    }
}

