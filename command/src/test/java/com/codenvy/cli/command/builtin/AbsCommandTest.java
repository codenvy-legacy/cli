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

import com.codenvy.cli.command.builtin.model.UserProjectReference;
import com.codenvy.cli.preferences.Preferences;
import com.codenvy.cli.security.PreferencesDataStore;
import com.codenvy.cli.security.RemoteCredentials;
import com.codenvy.client.Codenvy;
import com.codenvy.client.CodenvyBuilder;
import com.codenvy.client.CodenvyClient;
import com.codenvy.client.ProjectClient;
import com.codenvy.client.Request;
import com.codenvy.client.UserClient;
import com.codenvy.client.WorkspaceClient;
import com.codenvy.client.auth.Credentials;
import com.codenvy.client.auth.CredentialsBuilder;
import com.codenvy.client.auth.CredentialsProvider;
import com.codenvy.client.auth.Token;
import com.codenvy.client.auth.TokenBuilder;
import com.codenvy.client.dummy.DummyCodenvy;
import com.codenvy.client.dummy.DummyCodenvyClient;
import com.codenvy.client.dummy.project.DummyProject;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.User;
import com.codenvy.client.model.Workspace;
import com.codenvy.client.model.WorkspaceReference;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Command;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.codenvy.cli.command.builtin.util.SHA1.sha1;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Super class of tests.
 *
 * @author Florent Benoit
 */
public abstract class AbsCommandTest {




    @BeforeClass
    public void initMocks() {
        MockitoAnnotations.initMocks(this);

    }


    /**
     * Prepare the given command by injecting default configuration
     *
     * @param command
     */
    protected CommandSession prepare(AbsCommand command, DummyCodenvyClient codenvyClient) {
        CommandSession commandSession = Mockito.mock(CommandSession.class);
        Preferences globalPreferences = Mockito.mock(Preferences.class);
        Preferences remotesPreferences = Mockito.mock(Preferences.class);
        Map<String, String> preferencesMap = new HashMap<>();
        preferencesMap.put("default", "default");
        doReturn(preferencesMap).when(globalPreferences).get("remotes", Map.class);
        doReturn(remotesPreferences).when(globalPreferences).path("remotes");
        Remote defaultRemote = Mockito.mock(Remote.class);
        doReturn(defaultRemote).when(remotesPreferences).get("default", Remote.class);
        RemoteCredentials defaultRemoteCredentials = Mockito.mock(RemoteCredentials.class);
        doReturn("defaultToken").when(defaultRemoteCredentials).getToken();
        doReturn(defaultRemoteCredentials).when(remotesPreferences).get(eq("default"), eq(RemoteCredentials.class));
        doReturn("http://default").when(defaultRemote).getUrl();
        doReturn("username").when(defaultRemoteCredentials).getUsername();

        MultiRemoteCodenvy multiRemoteCodenvy = new MultiRemoteCodenvy(codenvyClient, globalPreferences, commandSession);

        command.setCodenvyClient(codenvyClient);
        doReturn(multiRemoteCodenvy).when(commandSession).get(MultiRemoteCodenvy.class.getName());
        doReturn(globalPreferences).when(commandSession).get(Preferences.class.getName());
        doReturn(codenvyClient).when(commandSession).get(DummyCodenvyClient.class.getName());

        return commandSession;
    }


    protected String getProjectId(DummyProject dummyProject) {
        // compute short id
        String fullID = dummyProject.workspaceId() + dummyProject.id();

        // p is for project
        return sha1("p", fullID).substring(0, 7);
    }

}
