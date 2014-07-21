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
import com.codenvy.cli.command.builtin.util.ascii.DefaultAsciiArray;
import com.codenvy.client.Codenvy;

import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import java.util.ArrayList;
import java.util.List;

/**
 * List command.
 * This command will list all projects used in Codenvy.
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "list", description = "List all the projects from Codenvy System")
public class ListCommand extends AbsCommand {

    /**
     * Prints the current projects per workspace
     */
    protected Object doExecute() {
        init();

        // not logged in
        if (!checkifEnvironments()) {
            return null;
        }

        Ansi buffer = Ansi.ansi();


        List<UserProject> projects = getMultiEnvCodenvy().getProjects();
        if (projects.isEmpty()) {
            buffer.a("No projects");
            System.out.println(buffer.toString());
            return null;
        }

        // Titles
        List<String> ids = new ArrayList<>();
        List<String> workspaces = new ArrayList<>();
        List<String> projectNames = new ArrayList<>();

        for (UserProject project : projects) {
            ids.add(project.shortId());
            workspaces.add(project.getWorkspace().name());
            projectNames.add(project.name());
        }

        // Ascii array
        AsciiArray asciiArray = buildAsciiArray().withColumns(ids, workspaces, projectNames).withTitle("ID", "Workspace", "Project");
        System.out.println(asciiArray.toAscii());

        return null;
    }


}
