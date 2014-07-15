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
import com.codenvy.client.CodenvyBuilder;
import com.codenvy.client.CodenvyClient;
import com.codenvy.client.Request;
import com.codenvy.client.UserClient;
import com.codenvy.client.auth.Credentials;
import com.codenvy.client.auth.CredentialsBuilder;
import com.codenvy.client.model.User;

import org.apache.felix.service.command.CommandSession;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Super class of tests.
 * @author Florent Benoit
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbsCommandTest {

    @Mock
    private CommandSession commandSession;

    @Mock
    private CodenvyClient codenvyClient;

    @Mock
    private CredentialsBuilder credentialsBuilder;

    @Mock
    private Credentials credentials;

    @Mock
    private CodenvyBuilder codenvyBuilder;

    @Mock
    private Codenvy codenvy;

    @Mock
    private UserClient userClient;

    @Mock
    private User user;

    @Mock
    private Request<User> request;

    @Before
    public void setUp() {
        // return current system.out when invocation is performed
        when(commandSession.getConsole()).thenAnswer(
                new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) {
                        return System.out;
                    }
                });

    }

    protected CommandSession getCommandSession() {
        return commandSession;
    }

    /**
     * Prepare the given command by injecting default configuration
     * @param command
     */
    protected void prepare(AbsCommand command) {

        doReturn(credentials).when(credentialsBuilder).build();
        doReturn(credentialsBuilder).when(credentialsBuilder).withPassword(anyString());
        doReturn(credentialsBuilder).when(credentialsBuilder).withUsername(anyString());
        doReturn(credentialsBuilder).when(getCodenvyClient()).newCredentialsBuilder();

        doReturn(codenvyBuilder).when(getCodenvyClient()).newCodenvyBuilder(anyString(), anyString());
        doReturn(codenvyBuilder).when(codenvyBuilder).withCredentials(credentials);
        doReturn(codenvy).when(codenvyBuilder).build();

        // UserClient
        doReturn(request).when(userClient).current();
        doReturn(user).when(request).execute();

        doReturn(userClient).when(codenvy).user();



        command.setCodenvyClient(codenvyClient);
    }

    protected CodenvyClient getCodenvyClient() {
        return codenvyClient;
    }

    protected CredentialsBuilder getCredentialsBuilder() {
        return credentialsBuilder;
    }

    protected Credentials getCredentials() {
        return credentials;
    }

    protected CodenvyBuilder getCodenvyBuilder() {
        return codenvyBuilder;
    }

    protected User getUser() {
        return user;
    }

    protected UserClient getUserClient() {
        return userClient;
    }

    public Codenvy getCodenvy() {
        return codenvy;
    }

    public Request<User> getRequest() {
        return request;
    }
}
