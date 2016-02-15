/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.file.runtime;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.file.Path;

import quarks.connectors.file.FileStreams;

/**
 * An interface for file writer policies.
 * <p>
 * {@code IFileWriterPolicy} is for use by file writer implementations
 * for interacting with a policy implementation.
 * <p>
 * A policy generally implements strategies related to:
 * <ul>
 * <li>Active and final file pathname control.</li>
 * <li>Active file flush control</li>
 * <li>Active file cycle control (when to close/finalize the current active file)</li>
 * <li>file retention control</li>
 * </ul>
 * <p>
 * A file writer uses a {@code IFileWriterPolicy} in the following manner:
 * <pre>
 * IFileWriterPolicy<T> policy = some policy implementation
 * policy.initialize(basePathname, () -> myFlushFn(), () -> myCycleFn());
 * Path activeFilePath = null;
 * for each tuple {
 *   if (activePathFile == null) {
 *     activeFilePath = policy.getNextActivePath();
 *     open an output stream to the path
 *   }
 *   write the appropriate contents to the active file output stream
 *   policy.wrote(tuple, number of bytes written);
 *   if (policy.shouldCycle()) {
 *     close the active file output stream
 *     policy.closeActiveFile(activeFilePath);
 *     activeFilePath = null;
 *   } 
 *   if (policy.shouldFlush()) {
 *     flush the active file output stream
 *   }
 * }
 * policy.close();
 * 
 * void myFlushFn() {
 *   flush the active file output stream
 * }
 * 
 * void myCycleFn() {
 *   close the active file output stream
 *   policy.closeActiveFile(activeFilePath);
 *   activeFilePath = null;
 * }
 * </pre>
 * 
 * @param <T> stream tuple type
 */
public interface IFileWriterPolicy<T> {
    
    /**
     * Initialize the policy with the base pathname of files to generate
     * and objects that can be
     * called to perform timer based flush or close (cycle) of the active file.
     * <p>
     * Cycling involves finalizing the active file (getting it to its
     * final destination / pathname) and applying any retention policy.
     * <p>
     * The supplied {@code closeable} must close the active file's output stream
     * and then call {@link #closeActiveFile(Path)}.
     * <p>
     * For non-timer based strategies, the file writer generally triggers
     * flush and cycle processing
     * following a tuple write as informed by {@link #shouldCycle()} and
     * {@link #shouldFlush()}. 
     * 
     * @param basePathname the directory and base leafname for final files
     * @param flushable
     * @param closeable
     */
    void initialize(String basePathname, Flushable flushable, Closeable closeable);
    
    /**
     * Inform the policy of every tuple written to the active file.
     * <p>
     * The policy can use this to update its state so that it
     * can answer the questions {@link #shouldFlush()}
     * and {@link #shouldCycle()} for count, size, or
     * tuple attribute based policies.
     * <p>
     * The policy can also use this to update its state
     * for implementing time based flush and cycle policies. 
     * @param tuple the tuple written
     * @param nbytes the number of bytes written
     */
    void wrote(T tuple, long nbytes);
    
    /**
     * Answers the question "should the active file be flushed?".
     * <p>
     * The state is reset to false after this returns.
     * @return true if the active file should be flushed
     */
    boolean shouldFlush();
    
    /**
     * Answers the question "should the active file be cycled?".
     * <p>
     * The state is reset to false after this returns.
     * @return true if the active file should be cycled
     */
    boolean shouldCycle();
    
    /**
     * Return the path for the next active file to write to.
     * <p>
     * If there was a current active file, {@link #closeActiveFile(Path)}
     * must be called prior to this.
     * <p>
     * The leafname must be a hidden file ({@code java.io.File.isHidden()==true}
     * to be compatible with a directory watcher
     * {@link FileStreams#directoryWatcher(quarks.topology.TopologyElement, quarks.function.Supplier)}
     * 
     * @return path for the active file
     */
    Path getNextActiveFilePath();
    
    /**
     * Close the active file {@code path}.
     * <p>
     * Generate the final path for the active file and  
     * rename/move/copy it as necessary to be at that final path.
     * <p>
     * Apply the retention policy.
     * <p>
     * The active file's writer iostream must be closed prior to calling this.
     * 
     * @param path the active file (from {@link #getNextActiveFilePath()}).
     * @return the final path
     * @throws IOException
     */
    Path closeActiveFile(Path path) throws IOException;
    
    /**
     * Release any resources utilized by this policy.
     */
    void close();
}
