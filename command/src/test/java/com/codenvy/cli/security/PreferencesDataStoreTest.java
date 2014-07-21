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
package com.codenvy.cli.security;


import com.codenvy.cli.command.builtin.AbsCommandTest;
import com.codenvy.cli.preferences.Preferences;
import com.codenvy.cli.preferences.file.FilePreferences;
import com.codenvy.client.auth.Credentials;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

/**
 * @author Florent Benoit
 */
public class PreferencesDataStoreTest extends AbsCommandTest {

    private Preferences preferences;

    private PreferencesDataStore dataStoreFlorent;
    private PreferencesDataStore dataStoreStephane;


    @Before
    public void init() throws Exception {

        FilePreferences filePreferences =
                new FilePreferences(new File(PreferencesDataStoreTest.class.getResource("preferences.json").toURI()));
        assertNotNull(filePreferences);

        // keep environments
        this.preferences = filePreferences.walk("environments");
        assertNotNull(preferences);

        this.dataStoreFlorent = new PreferencesDataStore(preferences, "florent_dev", getCodenvyClient());
        this.dataStoreStephane = new PreferencesDataStore(preferences, "stephane_dev", getCodenvyClient());

    }

    @Test
    public void testGet() throws Exception {

        // Return mock values extracted from store
        doReturn("florent").when(getCredentials()).username();
        doReturn("abc123").when(getToken()).value();

        Credentials credentialsFlorent = dataStoreFlorent.getCredentials("dummy");
        assertNotNull(credentialsFlorent);
        assertEquals("florent", credentialsFlorent.username());
        assertEquals("abc123", credentialsFlorent.token().value());

        doReturn("stephane").when(getCredentials()).username();
        doReturn("def456").when(getToken()).value();

        Credentials credentialsStephane = dataStoreStephane.getCredentials("dummy");
        assertNotNull(credentialsStephane);
        assertEquals("stephane", credentialsStephane.username());
        assertEquals("def456", credentialsStephane.token().value());

    }

    @Test
    public void testPut() throws Exception {
        // Return mock values extracted from store
        doReturn("florent").when(getCredentials()).username();
        doReturn("abc123").when(getToken()).value();

        Credentials credentialsFlorent = dataStoreFlorent.getCredentials("dummy");

        // change token
        Credentials update = Mockito.mock(Credentials.class);
        doReturn("toto").when(update).username();
        doReturn("toto").when(getCredentials()).username();
        doReturn(credentialsFlorent.token()).when(update).token();

        Credentials merged = dataStoreFlorent.put("florent_dev", update);

        assertEquals("toto", merged.username());

    }


        @Test
    public void testOnlyOneInstanceGet() throws Exception {
        Credentials one = dataStoreFlorent.get("one");
        assertNotNull(one);
        Credentials two = dataStoreFlorent.get("two");
        assertNotNull(two);

        assertEquals(one, two);

    }

}
