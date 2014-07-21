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

import com.codenvy.client.CodenvyClient;
import com.codenvy.client.auth.Credentials;

/**
 * @author Stéphane Daviet
 */
public final class CredentialsHelper {

    private CodenvyClient                  codenvyClient;

    public CredentialsHelper(CodenvyClient codenvyClient) {
        this.codenvyClient = codenvyClient;
    }

    public EnvironmentCredentials convert(Credentials credentials) {
        EnvironmentCredentials environmentCredentials = new EnvironmentCredentials();
        environmentCredentials.setUsername(credentials.username());
        environmentCredentials.setToken(credentials.token().value());
        return environmentCredentials;
    }

    public Credentials convert(EnvironmentCredentials environmentCredentials) {
        return environmentCredentials != null ? codenvyClient.newCredentialsBuilder()
                                                        .withUsername(environmentCredentials.getUsername())
                                                        .withToken(codenvyClient.newTokenBuilder(environmentCredentials.getToken())
                                                                                .build())
                                                        .build() : null;
    }

}
