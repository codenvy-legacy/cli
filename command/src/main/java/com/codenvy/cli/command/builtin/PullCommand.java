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

import jline.console.ConsoleReader;

import com.codenvy.cli.command.builtin.model.UserProject;
import com.codenvy.cli.command.builtin.util.metadata.CodenvyMetadata;
import com.codenvy.client.model.Project;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Allows to pull a given project
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "pull", description = "Pull a project")
public class PullCommand extends AbsCommand {

    @Option(name = "--override", description = "Override existing directory")
    private boolean override;

    @Argument(name = "project-id", description = "Specify the project ID to use", required = true, multiValued = false, index = 0)
    private String projectId;

    @Argument(name = "dest-directory", description = "Specify the directory for pulling the project", index = 1)
    private String directory;

    /**
     * Execute the command
     */
    @Override
    protected Object doExecute() throws Exception {
        init();

        // not logged in
        if (!checkifEnabledRemotes()) {
            return null;
        }

        // do we have the projectID ?
        if (projectId == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No projectID has been set");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        // get project for the given shortID
        UserProject project = getMultiRemoteCodenvy().getProject(projectId);

        if (project == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No matching project for identifier '").a(projectId).a("'.");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }


        final Project projectToPull = project.getInnerProject();

        File dest;
        if (directory == null) {
            // using current directory
            dest = new File(projectToPull.name());
        } else {
            File expectedDirectory = new File(directory);
            if (!expectedDirectory.exists()) {
                System.out.println(
                        String.format("Cannot pull project %s into %s as directory is not existing", projectToPull.name(), directory));
                return null;
            }

            dest = new File(expectedDirectory, projectToPull.name());
        }

        if (!override && dest.exists()) {
            System.out.println(
                    String.format("Cannot pull project %s into %s as directory already exists. Use --override option", projectToPull.name(),
                                  dest.getAbsolutePath()));
            return null;
        }


        // Ok now we have the project, checkout it
        System.out.println(String.format("Pulling project %s into %s", projectToPull.name(), dest.getAbsolutePath()));

        try (ZipInputStream zipInputStream = project.getCodenvy().project().exportResources(projectToPull, null).execute()) {
            byte[] buf = new byte[1024];
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            int total = 0;
            while (zipEntry != null) {


                File entryFile = new File(dest, zipEntry.getName());
                // Create directory
                if (zipEntry.isDirectory()) {
                    if (!entryFile.exists()) {
                        // create parent directories (with mkdirs)
                        if (!entryFile.mkdirs()) {
                            throw new IllegalStateException("Can not create directory " + entryFile + ", Check the write access.");
                        }
                    }
                    zipEntry = zipInputStream.getNextEntry();
                    continue;
                }
                // If it's a file, we must extract the file
                // Ensure that the directory exists.
                if (!entryFile.getParentFile().exists()) {
                    if (!entryFile.getParentFile().mkdirs()) {
                        throw new IllegalStateException("Unable to create directory" + entryFile.getParentFile());
                    }
                }


                int n;
                try (FileOutputStream fileoutputstream = new FileOutputStream(entryFile)) {

                    while ((n = zipInputStream.read(buf, 0, 1024)) > -1) {
                        fileoutputstream.write(buf, 0, n);
                    }
                }
                // getSize() is available once all has been read in the entry
                total += zipEntry.getSize();

                //
                String prettyTotal = "";
                if (total < 1024) {
                    prettyTotal = String.valueOf(total);
                } else if (total > 1024) {
                    prettyTotal = String.valueOf(total / 1024).concat(" KB");
                } else if (total > 1048576) {
                    prettyTotal = String.valueOf(total / 1048576).concat(" MB");
                }

                new ConsoleReader().resetPromptLine("Pulling...", prettyTotal, 0);
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();

            }
        }
        System.out.println("done !                                ");
        try {
            CodenvyMetadata codenvyMetadata = new CodenvyMetadata(project, dest);
            codenvyMetadata.write();
        } catch (Exception e) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("Unable to write metadata in pulled project " + project.name());
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        // Now compare resources (if overriding)
        if (override) {
            List<File> list = new ArrayList<>();
            getAllFiles(dest, list);
            int size = list.size();
            int count = 0;
            for (File f : list) {
                count++;
                String progress = String.valueOf((100 * count) / size).concat(" %");
                new ConsoleReader().resetPromptLine("Pruning local files...", progress , 0);
                if (f.isDirectory()) {
                    continue;
                }

                if (f.getPath().substring(project.name().length() + 1).startsWith(Constants.CODENVY_FOLDERNAME)) {
                    continue;
                }

                // check resources exists on the remote side
                if (!project.getCodenvy().project().isResource(projectToPull, f.getPath().substring(project.name().concat("/").length()))
                            .execute().booleanValue()) {
                    if (!f.delete()) {
                        System.out.println("Unable to remove local file " + f);
                    }
                }
            }
            if (!list.isEmpty()) {
                System.out.println("done !        ");
            }
        }


        return null;
    }


    public static void getAllFiles(File dir, List<File> fileList) throws IOException {
        File[] files = dir.listFiles();
        for (File file : files) {
            fileList.add(file);
            if (file.isDirectory()) {
                getAllFiles(file, fileList);
            }
        }
    }

}

