/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.cli.preferences.file;


import org.testng.annotations.Test;

/**
 * @author Stéphane Daviet
 */
public class JsonPreferencesTest {
    @Test
    public void testPutThenGet() {
        JsonPreferences jsonPreferences = new JsonPreferences();

        jsonPreferences.put("uselessPojo", FakePojo.getDumbInstance());

        jsonPreferences.get("uselessPojo", FakePojo.class);
    }
}
