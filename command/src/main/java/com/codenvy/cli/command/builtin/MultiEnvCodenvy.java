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
import com.codenvy.cli.preferences.Preferences;
import com.codenvy.cli.security.EnvironmentCredentials;
import com.codenvy.cli.security.PreferencesDataStore;
import com.codenvy.cli.security.TokenRetrieverDatastore;
import com.codenvy.client.Codenvy;
import com.codenvy.client.CodenvyClient;
import com.codenvy.client.CodenvyException;
import com.codenvy.client.Request;
import com.codenvy.client.WorkspaceClient;
import com.codenvy.client.auth.CodenvyAuthenticationException;
import com.codenvy.client.auth.Credentials;
import com.codenvy.client.auth.Token;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.User;
import com.codenvy.client.model.Workspace;
import com.codenvy.client.model.WorkspaceReference;

import org.fusesource.jansi.Ansi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;

/**
 * @author Florent Benoit
 */
public class MultiEnvCodenvy {

    private CodenvyClient codenvyClient;

    private ConcurrentMap<String, Codenvy> environments;

    private Preferences globalPreferences;

    private boolean hasEnvironments = false;

    public MultiEnvCodenvy(CodenvyClient codenvyClient, Preferences globalPreferences) {
        this.codenvyClient = codenvyClient;
        this.globalPreferences = globalPreferences;
        this.environments = new ConcurrentHashMap<String, Codenvy>();
        init();
    }

    protected void init() {
        environments.clear();
        // now read envionments and add a new datastore for each env
        Map preferencesEnvironments = globalPreferences.get("environments", Map.class);
        if (preferencesEnvironments != null) {
            Iterator<String> environmentIterator = preferencesEnvironments.keySet().iterator();
            Preferences environmentsPreferences = globalPreferences.path("environments");
            while (environmentIterator.hasNext()) {
                hasEnvironments = true;
                String environment = environmentIterator.next();
                // create store
                PreferencesDataStore preferencesDataStore = new PreferencesDataStore(environmentsPreferences, environment, codenvyClient);

                // read environment
                EnvironmentCredentials environmentCredentials = environmentsPreferences.get(environment, EnvironmentCredentials.class);


                // add remote env
                // Manage credentials
                Codenvy codenvy = codenvyClient.newCodenvyBuilder(environmentCredentials.getUrl(), environmentCredentials.getUsername())
                                               .withCredentialsProvider(preferencesDataStore)
                                               .withCredentialsStoreFactory(preferencesDataStore)
                                               .build();


                environments.put(environment, codenvy);

            }
        }
    }


    protected List<UserProject> getProjects() {
        List<UserProject> projects = new ArrayList<>();

        Set<Map.Entry<String, Codenvy>> entries = environments.entrySet();
        Iterator<Map.Entry<String, Codenvy>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Codenvy> entry = iterator.next();
            try {
                List<UserProject> foundProjects = getProjects(entry.getValue());
                if (foundProjects.size() > 0) {
                    projects.addAll(foundProjects);
                }
            } catch (CodenvyAuthenticationException e) {
                System.err.println("Authentication problem on environment '" + entry.getKey() + "'");
            }
        }
        return projects;
    }


    /**
     * Gets list of all projects for the current user
     *
     * @param codenvy
     *         the codenvy object used to retrieve the data
     * @return the list of projects
     */
    protected List<UserProject> getProjects(Codenvy codenvy) {
        List<UserProject> projects = new ArrayList<>();

        // For each workspace, search the project and compute

        WorkspaceClient workspaceClient = codenvy.workspace();
        Request<List<? extends Workspace>> request = workspaceClient.all();
        List<? extends Workspace> readWorkspaces = request.execute();

        for (Workspace workspace : readWorkspaces) {
            WorkspaceReference ref = workspace.workspaceReference();
            // Now skip all temporary workspaces
            if (ref.isTemporary()) {
                continue;
            }

            DefaultUserWorkspace defaultUserWorkspace = new DefaultUserWorkspace(codenvy, ref);

            List<? extends Project> readProjects = codenvy.project().getWorkspaceProjects(ref.id()).execute();
            for (Project readProject : readProjects) {
                DefaultUserProject project = new DefaultUserProject(codenvy, readProject, defaultUserWorkspace);
                projects.add(project);
            }
        }
        return projects;
    }


    /**
     * Allows to search a project
     */
    protected UserProject getProject(String shortId) {
        if (shortId == null || shortId.length() < 2) {
            throw new IllegalArgumentException("The identifier should at least contain two digits");
        }


        // get all projects
        List<UserProject> projects = getProjects();

        // no projects
        if (projects.size() == 0) {
            return null;
        }

        // now search in the given projects
        List<UserProject> matchingProjects = new ArrayList<>();
        for (UserProject project : projects) {
            // match
            if (project.shortId().startsWith(shortId)) {
                matchingProjects.add(project);
            }
        }

        // No matching project
        if (matchingProjects.size() == 0) {
            return null;
        } else if (matchingProjects.size() == 1) {
            // one matching project
            return matchingProjects.get(0);
        } else {
            throw new IllegalArgumentException("Too many matching projects. Try with a longer identifier");
        }


    }


    public boolean hasEnvironments() {
        return hasEnvironments;
    }

    public Collection<String> getEnvironmentNames() {
        return environments.keySet();
    }

    public String listEnvironments() {
        Ansi buffer = Ansi.ansi();
        buffer.a(INTENSITY_BOLD).a("ENVIRONMENTS\n").a(INTENSITY_BOLD_OFF);
        buffer.reset();

        Collection<String> envs = getEnvironmentNames();
        if (envs.size() == 1) {
            buffer.a("There is ").a(envs.size()).a(" Codenvy environment");
        } else if (envs.size() > 1) {
            buffer.a("There are ").a(envs.size()).a(" Codenvy environments");
        } else {
            buffer.a("There is no Codenvy environment");
        }
        buffer.a(System.lineSeparator());
        for (String env : getEnvironmentNames()) {
            buffer.a("\t").a(env).a(System.lineSeparator());
        }
        return buffer.toString();
    }


    protected boolean addEnvironment(String name, String url, String username, String password) {
        // check env doesn't exists
        if (getEnvironmentNames().contains(name)) {
            System.out.println("The environment with name '" + name + "' already exists");
            return false;
        }

        TokenRetrieverDatastore tokenRetrieverDatastore = new TokenRetrieverDatastore();

        // check that this is valid
        Credentials codenvyCredentials = codenvyClient.newCredentialsBuilder()
                                                    .withUsername(username)
                                                    .withPassword(password)
                                                    .build();
        Codenvy codenvy = codenvyClient.newCodenvyBuilder(url, username)
                                            .withCredentials(codenvyCredentials)
                                            .withCredentialsStoreFactory(tokenRetrieverDatastore)
                                            .build();

        // try to connect to the remote side
        try {
            codenvy.user().current().execute();
        } catch (CodenvyException e) {
            System.out.println("Unable to authenticate for the given credentials on URL '" + url + "'. Check the username and password.");
            // invalid login so we don't add env
            return false;
        }

        Token token = tokenRetrieverDatastore.getToken();
        if (token == null) {
            System.out.println("Unable to get token for the given credentials on URL '" + url + "'");
            // invalid login so we don't add env
            return false;
        }

        Preferences preferencesEnvironments = globalPreferences.path("environments");

        // add the new environment
        EnvironmentCredentials credentials = new EnvironmentCredentials();
        credentials.setUrl(url);
        credentials.setUsername(username);
        credentials.setToken(token.value());

        preferencesEnvironments.put(name, credentials);

        // refresh current links
        refresh();

        return true;

    }

    protected void refresh() {
        init();
    }


    protected boolean removeEnvironment(String name) {
        // check env does exists
        if (!getEnvironmentNames().contains(name)) {
            System.out.println("The environment with name '" + name + "' does not exists");
            return false;
        }

        // it exists, remove it
        Preferences preferencesEnvironments = globalPreferences.path("environments");

        // delete
        preferencesEnvironments.delete(name);

        // refresh current links
        refresh();

        // OK
        return true;
    }

}
