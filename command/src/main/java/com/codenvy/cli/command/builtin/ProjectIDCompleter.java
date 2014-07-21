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

import com.codenvy.cli.command.builtin.model.DefaultUserProject;
import com.codenvy.cli.command.builtin.model.DefaultUserWorkspace;
import com.codenvy.cli.command.builtin.model.UserProject;
import com.codenvy.client.Codenvy;
import com.codenvy.client.Request;
import com.codenvy.client.WorkspaceClient;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.Workspace;
import com.codenvy.client.model.WorkspaceReference;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.console.CommandSessionHolder;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florent Benoit
 */
public class ProjectIDCompleter implements Completer {

    /**
     * @param buffer the beginning string typed by the user
     * @param cursor the position of the cursor
     * @param candidates the list of completions proposed to the user
     */
    public int complete(String buffer, int cursor, List candidates) {

        // gets the current session
        CommandSession commandSession = CommandSessionHolder.getSession();

        StringsCompleter delegate = new StringsCompleter();

        // get current client
        MultiEnvCodenvy multiEnvCodenvy = (MultiEnvCodenvy)commandSession.get(MultiEnvCodenvy.class.getName());

        if (multiEnvCodenvy != null) {
            // get current projects
            List<UserProject> projects = multiEnvCodenvy.getProjects();
            for (UserProject project : projects) {
                delegate.getStrings().add(project.shortId());
            }
        }
        return delegate.complete(buffer, cursor, candidates);
    }

}
