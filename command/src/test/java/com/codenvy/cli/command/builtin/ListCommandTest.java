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

import com.codenvy.client.CodenvyException;
import com.codenvy.client.ProjectClient;
import com.codenvy.client.Request;
import com.codenvy.client.auth.Token;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.Workspace;
import com.codenvy.client.model.WorkspaceRef;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static com.codenvy.cli.command.builtin.LoginCommand.DEFAULT_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test of the list command
 * @author Florent Benoit
 */
public class ListCommandTest extends AbsCommandTest {


    protected CommandInvoker getInvoker() {
        ListCommand listCommand = new ListCommand();

        // prepare
        prepare(listCommand);

        return new CommandInvoker(listCommand);
    }


    /**
     * Expect it works when there are no workspaces
     */
    @Test
    public void testNoWorkspace() throws Exception {

        CommandInvoker.Result result = getInvoker().invoke(getCommandSession());

        assertTrue(result.disableAnsi().getOutputStream().contains("No projects"));

    }

    /**
     * Expect it works when there is one workspace without projects
     */
    @Test
    public void testOneWorkspaceNoProjects() throws Exception {

        CommandInvoker commandInvoker = getInvoker();

        String workspaceName = "WORKSPACE1";
        addWorkspace(workspaceName);

        CommandInvoker.Result result = commandInvoker.invoke(getCommandSession());
        assertTrue(result.disableAnsi().getOutputStream().contains("No projects"));

    }


    /**
     * Expect it works when there is one workspace with one project
     */
    @Test
    public void testOneWorkspaceOneProject() throws Exception {

        CommandInvoker commandInvoker = getInvoker();

        String workspaceName = "WORKSPACE1";
        addWorkspace(workspaceName);
        addProject(workspaceName, "project1");

        CommandInvoker.Result result = commandInvoker.invoke(getCommandSession());
        assertEquals("+-------+----------+--------+\n" +
                     "|     ID| Workspace| Project|\n" +
                     "+-------+----------+--------+\n" +
                     "|d8ed618|WORKSPACE1|project1|\n" +
                     "+-------+----------+--------+\n", result.disableAnsi().getOutputStream());

    }


}
