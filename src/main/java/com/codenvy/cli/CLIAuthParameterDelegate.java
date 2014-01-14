/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.cli;

import com.beust.jcommander.*;

/**
 * A common set of parameters used across many commands.
 *
 */ 
public class CLIAuthParameterDelegate {

// TODO: Add the provider as part of this delegate?  If we do, then you will not be able to reuse a set of parameters against two different codenvy domains.

    @Parameter(names = { "-u", "--user" }, description = "User name in the form of email address")
	private String user;
    public String getUser() { return user; }

    @Parameter(names = { "-p", "--password" }, description = "Password for a Codenvy account.", password = true, echoInput = true)
	private String password;
    public String getPassword() { return password; }

    @Parameter(names = { "-t", "--token" }, description = "API token registered against an account")
	private String token;
    public String getToken() { return token; }

    @Parameter(names = { "--profile" }, description = "Property configuration profile to use")
	private String profile;
    public String getProfile() { return profile; }

    @Parameter(names = { "--provider" }, description = "Domain of Codenvy cloud where remote commands will be executed.  Default is https://codenvy.com.")
    private String provider="https://codenvy.com";
    public String getProvider() { return provider; }


}

