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
package com.codenvy.cli.command.builtin.util.ascii;

/**
 * @author Florent Benoit
 */
public class ModernFormatter implements TableFormatter {


    /**
     * No border for modern formatter
     * @param asciiArrayInfo
     * @return empty
     */
    @Override
    public String getBorderLine(AsciiArrayInfo asciiArrayInfo) {
        return null;
    }

    @Override
    public String getFormatter(AsciiArrayInfo asciiArrayInfo) {
        StringBuilder buffer = new StringBuilder();

        for (Integer columnSize : asciiArrayInfo.getColumnsSize()) {
            buffer.append("%-" + columnSize + "s");
            buffer.append("  ");
        }
        buffer.append("%n");

        return buffer.toString();

    }
}