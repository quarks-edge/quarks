/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.file.runtime;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;

import quarks.function.Supplier;

/**
 * Watch a directory for files being added to it and create a stream
 * of pathname strings for the files.
 * <p>
 * Hidden files (files starting with ".") are ignored.
 * <p>
 * The order of the files in the stream is dictated by a {@link Comparator}.
 * The default comparator orders files by {@link File#lastModified()} values.
 * There are no guarantees on the processing order of files that
 * have the same lastModified value.
 * Note, lastModified values are subject to filesystem timestamp
 * quantization - e.g., 1second.
 * <p>
 * Note: due to the asynchronous nature of things, if files in the
 * directory may be removed, the receiver of a tuple with a "new" file
 * pathname may need to be prepared for the pathname to no longer be
 * valid when it receives the tuple or during its processing of the tuple.
 * <p>
 * The behavior on MacOS may be unsavory, even as recent as Java8, as
 * MacOs Java lacks a native implementation of {@link WatchService}.
 * The result can be a delay in detecting newly created files (e.g., 10sec)
 * as well not detecting rapid deletion and recreation of a file.
 * See:
 * http://stackoverflow.com/questions/9588737/is-java-7-watchservice-slow-for-anyone-else
 */

public class DirectoryWatcher implements AutoCloseable, 
        FileFilter, Iterable<String> {

    private static final Logger trace = FileConnector.getTrace();
    private final Supplier<String> dirSupplier;
    private final Comparator<File> comparator;
    private final Set<String> seenFiles = Collections.synchronizedSet(new HashSet<>());
    private volatile File dirFile;
    private WatchService watcher;
    
    private Queue<String> pendingNames = new LinkedList<>();
    

    /**
     * Watch the specified directory and generate tuples corresponding
     * to files that are created in the directory.
     * <p>
     * If a null {@code comparator} is specified, the default comparator
     * described in {@link DirectoryWatcher} is used.
     * 
     * @param dirSupplier the directory to watch
     * @param comparator a comparator to order the processing of
     *        multiple newly seen files in the directory.  may be null.
     */
    public DirectoryWatcher(Supplier<String> dirSupplier, Comparator<File> comparator) {
        this.dirSupplier = dirSupplier;
        if (comparator == null) {
            comparator = // TODO 2nd order alfanum compare when same LMT?
                    (o1,o2) -> Long.compare(o1.lastModified(),
                                            o2.lastModified());
        }
        this.comparator = comparator;
    }
    
    private void initialize() throws IOException {
        dirFile = new File(dirSupplier.get());
        
        trace.info("watching directory {}", dirFile);
        
        Path dir = dirFile.toPath();

        watcher = FileSystems.getDefault().newWatchService();
        dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE);

        sortAndSubmit(Arrays.asList(dirFile.listFiles(this)));
    }

    @Override
    public void close() throws IOException {
        watcher.close();
    }

    protected void sortAndSubmit(List<File> files) {
        if (files.size() > 1) {
            Collections.sort(files, comparator);
        }

        for (File file : files) {
            if (accept(file) && file.exists()) {
                pendingNames.add(file.getAbsolutePath());
                seenFiles.add(file.getName());
            }
        }
    }

    /**
     * Waits for files to become available 
     * and adds them through {@link #sortAndSubmit(List)}
     * to the pendingNames list which the iterator pulls from.
     */
    @SuppressWarnings("unchecked")
    private void watchForFiles() throws Exception {

        WatchKey key = watcher.take();

        List<File> newFiles = new ArrayList<>();
        boolean needFullScan = false;
        for (WatchEvent<?> watchEvent : key.pollEvents()) {

            if (ENTRY_CREATE == watchEvent.kind()) {
                Path newPath = ((WatchEvent<Path>) watchEvent).context();
                File newFile = toAbsFile(newPath);
                if (accept(newFile))
                    newFiles.add(newFile);
            } else if (ENTRY_DELETE == watchEvent.kind()) {
                Path deletedPath = ((WatchEvent<Path>) watchEvent).context();
                File deletedFile = toAbsFile(deletedPath);
                seenFiles.remove(deletedFile.getName());
            } else if (OVERFLOW == watchEvent.kind()) {
                needFullScan = true;
            }
        }
        key.reset();

        if (needFullScan) {
            Collections.addAll(newFiles, dirFile.listFiles(this));
        }
        sortAndSubmit(newFiles);
    }

    private File toAbsFile(Path relPath) {
        return new File(dirFile, relPath.getFileName().toString());
    }

    @Override
    public boolean accept(File pathname) {
        // our "filter" function
        return !pathname.getName().startsWith(".")
                && !seenFiles.contains(pathname.getName());
    }

    @Override
    public Iterator<String> iterator() {
        try {
            initialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new WatcherIterator();
    }
    
    /*
     * Iterator that returns the file names.
     * It is endless for hasNext() always returns
     * true, and next() will block in WatcherService.take
     * if no files are available.
     */
    private class WatcherIterator implements Iterator<String> {

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public String next() {

            for (;;) {

                String name = pendingNames.poll();
                if (name != null)
                    return name;

                // blocks until a file appears
                // note that even when watchForFiles()
                // returns pendingNames might still be empty
                // due to filtering.
                try {
                    watchForFiles();
                } catch (InterruptedException e) {
                    // interpret as shutdown
                    trace.debug("Interrupted");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
