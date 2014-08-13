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

import com.codenvy.cli.command.builtin.model.UserProjectReference;
import com.codenvy.client.model.ProjectReference;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Allows to push a given project
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "push", description = "Push a project")
public class PushCommand extends AbsPushPullCommand {

    @Argument(name = "codenvy-directory", description = "Specify the directory of a codenvy project")
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

        // try to see if we have a Codenvy project in the directory
        File directoryToSend;
        if (directory != null) {
            directoryToSend = new File(directory);
        } else {
            directoryToSend = new File(new File("").getAbsolutePath());
        }


        String projectId = getProjectFromDirectory(directoryToSend);
        if (projectId == null) {
            return null;
        }

        // get project for the given shortID
        UserProjectReference project = getMultiRemoteCodenvy().getProjectReference(projectId);

        if (project == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a(
                    "The project of the specified directory is no longer existing in the connected remote directories. Unable to continue.");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }


        final ProjectReference projectToPush = project.getInnerReference();

        // ok now we perform push
        System.out.print("Pushing...");

        // create zip of the current archive
        final InputStream exportedZipInputStream = getZipProjectStream(directoryToSend);

        // send it
        project.getCodenvy().project().importArchive(projectToPush.workspaceId(), projectToPush, exportedZipInputStream).execute();

        System.out.println(" done !");



        // now download the remote zip in order to compare if there are files to delete
        int deleteCount = 0;
        try (ZipInputStream zipInputStream = project.getCodenvy().project().exportResources(projectToPush, null).execute()) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {

                // path has been removed locally ?
                File path = new File(directoryToSend, zipEntry.getName());
                new ConsoleReader().resetPromptLine("Pruning remote files...", String.valueOf(deleteCount).concat("removed. ".concat(" comparing :") + zipEntry.getName()) , 0);
                if (!path.exists() && path.getParentFile().exists()) {
                    // remote resources on the remote side
                    project.getCodenvy().project().deleteResources(projectToPush, zipEntry.getName()).execute();
                    deleteCount++;

                }

                // read next entry
                zipEntry = zipInputStream.getNextEntry();
            }
            new ConsoleReader().resetPromptLine("Pruning remote files...", "done", 0);
            System.out.println();
        }


        return null;
    }


    public static InputStream getZipProjectStream(final File directoryToSend) {


        final AtomicBoolean writeDoneSignal = new AtomicBoolean(false);
        final CountDownLatch writeStartLock = new CountDownLatch(1);

        final PipedInputStream pipedInputStream = new PipedInputStream() {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                try {
                    // Wait until writer get created and connected
                    writeStartLock.await();

                    // Hack to avoid premature closing of this PipedInputStream by a reader whose basing closing action on assumption that
                    // if read() send -1 the stream is to be closed (for instance InputStreamProvider of Jersey). The stream can be only
                    // closed when the thread where PipedOutputStream write is ended. Otherwise, if PipedOutputStream write ’slower’ than
                    // PipedInputStream is read, there could be underflow and read could send -1 whereas there's still some data to be
                    // written.
                    int result = super.read(b, off, len);
                    if (result == -1 && !writeDoneSignal.get()) {
                        return 0;
                    }
                    return result;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        };

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new ZipRunnable(pipedInputStream, writeStartLock, directoryToSend, writeDoneSignal));

        return pipedInputStream;
    }

    private static class ZipRunnable implements Runnable {
        private final PipedInputStream pipedInputStream;
        private final CountDownLatch writeStartLock;
        private final File           directoryToSend;
        private final AtomicBoolean  writeDoneSignal;

        public ZipRunnable(PipedInputStream pipedInputStream, CountDownLatch writeStartLock, File directoryToSend,
                           AtomicBoolean writeDoneSignal) {
            this.pipedInputStream = pipedInputStream;
            this.writeStartLock = writeStartLock;
            this.directoryToSend = directoryToSend;
            this.writeDoneSignal = writeDoneSignal;
        }

        @Override
    public void run() {
        try {
            final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
            // Writer is on, unlock the reader
            writeStartLock.countDown();
            final ZipOutputStream outputStream = new ZipOutputStream(pipedOutputStream);


            List<File> listFiles = new ArrayList<>();
            getAllFiles(directoryToSend, listFiles);
            for (File file : listFiles) {
                if (!file.isDirectory()) { // we only zip files, not directories

                    String entryPath = file.getPath().substring(directoryToSend.getPath().length() + 1);

                    ZipEntry zipEntry = new ZipEntry(entryPath);
                    outputStream.putNextEntry(zipEntry);

                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = fis.read(bytes)) >= 0) {
                            outputStream.write(bytes, 0, length);
                        }

                        outputStream.closeEntry();
                    }
                }
            }

            // Flag for writing end, see hack above for PipedInputStream#read(…).
            writeDoneSignal.set(true);
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    }
}

