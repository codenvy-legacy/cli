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
import com.codenvy.client.model.Workspace;
import com.codenvy.client.model.WorkspaceRef;

import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import java.util.List;

/**
 * Allow to see workspaces
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "workspace", description = "List all the workspaces from Codenvy System")
public class WorkspaceCommand extends AbsCommand {

    /**
     * Prints the current workspaces
     */
    protected Object doExecute() {
        Codenvy current = checkLoggedIn();

        // Not set
        if (current == null) {
            return null;
        }

        Ansi buffer = Ansi.ansi();

        List<? extends Workspace> workspaces = current.workspace().all().execute();
        if (workspaces.size() == 0) {
            buffer.a(Ansi.Attribute.INTENSITY_BOLD);
            buffer.a("NO");
            buffer.fg(Ansi.Color.DEFAULT);
            buffer.a(" workspace");
        } else {
            buffer.a("ID").a("\t\t\t").a("NAME").a("\t").a("OrgID\n");
            for (Workspace workspace : workspaces) {
                WorkspaceRef ref = current.workspace().withName(workspace.workspaceRef().name()).execute();
                String id = ref.id();
                String name = ref.name();
                String organizationID = ref.organizationId();
                buffer.a(id).a("\t").a(name).a("\t").a(organizationID).a("\n");
            }
        }

        session.getConsole().println(buffer.toString());
        return null;
    }
}
