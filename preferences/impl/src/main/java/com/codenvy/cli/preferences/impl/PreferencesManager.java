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
package com.codenvy.cli.preferences.impl;

import java.io.File;
import java.io.IOException;

import com.codenvy.cli.preferences.Preferences;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Entry point to globally manage all cli preferences. Load a preferences file in JSON format in memory and dump it when preferences are
 * modified. This class store a inner {@link Preferences} root node and act as an adapter to it, decorating it with its specific file
 * persisting action when needed.
 *
 * @author Stéphane Daviet
 */
public class PreferencesManager implements Preferences {
    private JsonPreferences    rootNode;

    private final ObjectMapper mapper;

    private final File         preferencesFile;

    /**
     * Create a {@link PreferencesManager} linked to the specified {@link File}.
     *
     * @param preferencesFile the {@link File} to link to this new {@link PreferencesManager} instance.
     */
    public PreferencesManager(File preferencesFile) {
        this.mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.preferencesFile = preferencesFile;
        loadFile();
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        return rootNode.get(key, clazz);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Write the file back on the file system./
     * <p>
     */
    @Override
    public void put(String key, Object value) {
        rootNode.put(key, value);
        dumpFile();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Write the file back on the file system./
     * <p>
     */
    @Override
    public void merge(String key, Object value) {
        rootNode.merge(key, value);
        dumpFile();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Write the file back on the file system./
     * <p>
     */
    @Override
    public void delete(String key) {
        rootNode.delete(key);
        dumpFile();
    }

    /**
     * {@inheritDoc}
     * <p>
     * As a void preferences node is not significant, choice has been made to not write back on the file system.
     * </p>
     */
    @Override
    public Preferences path(String key) {
        return rootNode.path(key);
    }

    @Override
    public Preferences walk(String key) {
        return rootNode.walk(key);
    }

    @Override
    public boolean pathExists(String key) {
        return rootNode.pathExists(key);
    }

    /**
     * Load the file where {@link Credentials} are stored.
     *
     * @return the current {@link PreferencesManager} to chain commands.
     */
    protected PreferencesManager loadFile() {
        try {
            if (!preferencesFile.exists()) {
                if (!preferencesFile.createNewFile()) {
                    throw new RuntimeException("Can not create file.");
                }
                this.rootNode = new JsonPreferences();
            } else {
                this.rootNode = mapper.readValue(preferencesFile, JsonPreferences.class);
            }
        } catch (JsonMappingException e) {
            this.rootNode = new JsonPreferences();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Dump the in-memory cache into the file where {@link Credentials}.
     *
     * @return the current {@link PreferencesManager} to chain commands.
     */
    protected PreferencesManager dumpFile() {
        synchronized (preferencesFile) {
            try {
                mapper.writeValue(preferencesFile, preferencesFile);
                return this;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
