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

package com.codenvy.cli.command.builtin.helper;

import java.util.List;

/**
 * Allows to convert permissions to pretty print permissions
 * @author Florent Benoit
 */
public class UserPermissionsHelper {

    /**
     * Utility class, no public constructor.
     */
    private UserPermissionsHelper() {

    }


    public static String pretty(List<String> userPermissions) {
        if (userPermissions == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder("");

        if (userPermissions.contains("read")) {
            sb.append("R");
        }
        if (userPermissions.contains("write")) {
            sb.append("W");
        }
        if (userPermissions.contains("update_acl")) {
            sb.append("U");
        }
        if (userPermissions.contains("run")) {
            sb.append("X");
        }
        if (userPermissions.contains("build")) {
            sb.append("B");
        }
        return sb.toString();
    }
}
