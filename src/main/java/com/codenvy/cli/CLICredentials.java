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

/**
 * A Java Object that represents all of the configuration parameters for the CLI.
 *
 */ 
public class CLICredentials {
	private String provider;
	private String user;
	private String pass;
	private String token;

	public String getProvider() { return provider; }
	public String getUser() { return user; }
	public String getPass() { return pass; }
	public String getToken() { return token; }

	public void setProvider(String p) { provider = p; }
	public void setUser(String u) { user = u; }
	public void setPass(String p) { pass = p; }
	public void setToken(String t) { token = t; }

}
