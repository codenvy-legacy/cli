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
import com.codenvy.client.auth.Token;

import org.junit.Test;
import org.mockito.Mock;

import static com.codenvy.cli.command.builtin.LoginCommand.DEFAULT_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test of the login command
 * @author Florent Benoit
 */
public class LoginCommandTest extends AbsCommandTest {


    @Mock
    private Token token;

    /**
     * Test that we're submitting correct data
     * @throws Exception
     */
    @Test
    public void testLogin() throws Exception {
        LoginCommand loginCommand = new LoginCommand();

        // prepare
        prepare(loginCommand);


        // Dummy token
        doReturn(token).when(getCredentials()).token();

        String username = "florent";
        String password = "mySecretPassword";
        CommandInvoker commandInvoker = new CommandInvoker(loginCommand).argument("username", username).argument("password", password);

        CommandInvoker.Result result = commandInvoker.invoke(getCommandSession());

        // Check that we've called the credential builder with login/password
        verify(getCredentialsBuilder()).withUsername(username);
        verify(getCredentialsBuilder()).withPassword(password);


        // check also that credentials have been given
        verify(getCodenvyClient()).newCodenvyBuilder(DEFAULT_URL, username);
        verify(getCodenvyBuilder()).withCredentials(getCredentials());

        // check token is saved
        verify(getCommandSession()).put(Token.class.getName(), getCredentials().token());

        assertTrue(result.disableAnsi().getOutputStream().contains("Login OK : Welcome florent"));

    }


    /**
     * Test that login failed with invalid data
     * @throws Exception
     */
    @Test
    public void testLoginFails() throws Exception {
        LoginCommand loginCommand = new LoginCommand();

        // prepare
        prepare(loginCommand);

        // Dummy token
        doReturn(token).when(getCredentials()).token();

        String username = "florent";
        String password = "mySecretPassword";
        CommandInvoker commandInvoker = new CommandInvoker(loginCommand).argument("username", username).argument("password", password);

        // Throw an exception when searching user
        reset(getRequest());
        doThrow(CodenvyException.class).when(getRequest()).execute();

        CommandInvoker.Result result = commandInvoker.invoke(getCommandSession());

        // Check that we've called the credential builder with login/password
        verify(getCredentialsBuilder()).withUsername(username);
        verify(getCredentialsBuilder()).withPassword(password);


        // check token is not saved
        verify(getCommandSession(), times(0)).put(Token.class.getName(), getCredentials().token());

        assertEquals("Login failed : Unable to perform login : null\n", result.disableAnsi().getOutputStream());

    }


}
