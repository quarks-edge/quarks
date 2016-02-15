/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.connectors.file;

import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import quarks.connectors.file.runtime.FileConnector;
import quarks.connectors.file.runtime.IFileWriterPolicy;

/**
 * A full featured {@link IFileWriterPolicy} implementation.
 * <p>
 * The policy implements strategies for:
 * <ul>
 * <li>Active and final file pathname control.</li>
 * <li>Active file flush control (via @{link FileWriterFlushControl})</li>
 * <li>Active file cycle control (when to close/finalize the current active file;
 *     via @{link FileWriterCycleControl})</li>
 * <li>file retention control (via @{link FileWriterRetentionControl})</li>
 * </ul>
 * The policy is very configurable.  If additional flexibility is required
 * the class can be extended and documented "hook" methods overridden,
 * or an alternative full implementation of {@code FileWriterPolicy} can be
 * created.
 * <p>
 * Sample use:
 * <pre>
 * FileWriterPolicy<String> policy = new FileWriterPolicy(
 *     FileWriterFlushConfig.newImplicitConfig(),
 *     FileWriterCycleConfig.newCountBasedConfig(1000),
 *     FileWriterRetentionConfig.newCountBasedConfig(10));
 * String basePathname = "/some/directory/and_base_name";
 * 
 * TStream<String> streamToWrite = ...
 * FileStreams.textFileWriter(streamToWrite, () -> basePathname, () -> policy)
 * </pre>
 * 
 * @param <T> stream tuple type
 * @see FileWriterFlushConfig
 * @see FileWriterCycleConfig
 * @see FileWriterRetentionConfig
 */
public class FileWriterPolicy<T> implements IFileWriterPolicy<T> {
    private static final Logger trace = FileConnector.getTrace();
    private final FileWriterFlushConfig<T> flushConfig; 
    private final FileWriterCycleConfig<T> cycleConfig; 
    private final FileWriterRetentionConfig retentionConfig; 
    private String basePathname;
    private Path parent;
    private String baseLeafname;
    private Flushable flushable;
    private Closeable closeable;
    private volatile int curTupleCnt;
    private volatile long curSize;
    private volatile boolean flushIt;
    private volatile boolean cycleIt;
    private volatile String lastYmdhms;
    private volatile int lastMinorSuffix;
    private final List<Path> retainedPaths = new ArrayList<>(); // oldest first
    private volatile ScheduledExecutorService executor;
    
    /**
     * Create a new file writer policy instance.
     * <p>
     * The configuration is:
     * <ul>
     * <li>10 second time based active file flushing</li>
     * <li>1MB file size based active file cycling</li>
     * <li>10 file retention count</li>
     * </ul>
     * The active and final file pathname behavior is specified in
     * {@link #FileWriterPolicy(FileWriterFlushConfig, FileWriterCycleConfig, FileWriterRetentionConfig)}
     */
    public FileWriterPolicy() {
        this(FileWriterFlushConfig.newTimeBasedConfig(TimeUnit.SECONDS.toMillis(10)),
            FileWriterCycleConfig.newFileSizeBasedConfig(1*1024*1024),
            FileWriterRetentionConfig.newFileCountBasedConfig(10)); 
    }
    
    /**
     * Create a new file writer policy instance.
     * <p>
     * {@code flushConfig}, {@code cycleConfig} and {@code retentionConfig}
     * specify the configuration of the various controls.
     * <p>
     * The active file and final file pathnames are based
     * on the {@code basePathname} received in 
     * {@link #initialize(String, Flushable, Closeable)}.
     * <p>
     * Where {@code parent} and {@code baseLeafname} are the 
     * parent path and file name respectively of {@code basePathname}:
     * <ul>
     * <li>the active file is {@code parent/.baseLeafname}"</li>
     * <li>final file names are {@code parent/baseLeafname_YYYYMMDD_HHMMSS[_<n>]}
     *     where the optional {@code _<n>} suffix is only present if needed
     *     to distinguish a file from the previously finalized file.
     *     {@code <n>} starts at 1 and is monotonically incremented.
     *     </li>
     * </ul>
     * @param flushConfig active file flush control configuration
     * @param cycleConfig active file cycle control configuration
     * @param retentionConfig final file retention control configuration
     */
    public FileWriterPolicy(FileWriterFlushConfig<T> flushConfig,
            FileWriterCycleConfig<T> cycleConfig,
            FileWriterRetentionConfig retentionConfig) {
        this.flushConfig = flushConfig;
        this.cycleConfig = cycleConfig;
        this.retentionConfig = retentionConfig;
    }
    
    @Override
    public void close() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    /**
     * Get the policy's active file flush configuration
     * @return the flush configuration
     */
    public FileWriterFlushConfig<T> getFlushConfig() {
        return flushConfig;
    }

    /**
     * Get the policy's active file cycle configuration
     * @return the cycle configuration
     */
    public FileWriterCycleConfig<T> getCycleConfig() {
        return cycleConfig;
    }

    /**
     * Get the policy's retention configuration
     * @return the retention configuration
     */
    public FileWriterRetentionConfig getRetentionConfig() {
        return retentionConfig;
    }

    @Override
    public void initialize(String basePathname, Flushable flushable,
            Closeable closeable) {
        this.basePathname = basePathname;
        this.flushable = flushable;
        this.closeable = closeable;
        Path basePath = new File(basePathname).toPath();
        this.parent = basePath.getParent();
        this.baseLeafname = basePath.getFileName().toString();
        
        if (flushConfig.getPeriodMsec() > 0) {
            long periodMsec = flushConfig.getPeriodMsec();
            getExecutor().scheduleAtFixedRate(
                    () -> { try { this.flushable.flush(); }
                    catch (IOException e) { /*ignore*/ }
                }, 
                periodMsec, periodMsec, TimeUnit.MILLISECONDS);
        }
        if (cycleConfig.getPeriodMsec() > 0) {
            long periodMsec = cycleConfig.getPeriodMsec();
            getExecutor().scheduleAtFixedRate(
                    () -> { try { this.closeable.close(); }
                    catch (IOException e) { /*ignore*/ }
                }, 
                periodMsec, periodMsec, TimeUnit.MILLISECONDS);
        }
        if (retentionConfig.getAgeSec() > 0) {
            long periodMsec = retentionConfig.getPeriodMsec();
            getExecutor().scheduleAtFixedRate(
                    () -> applyTimeBasedRetention(), 
                    periodMsec, periodMsec, TimeUnit.MILLISECONDS);
        }
    }    

    private ScheduledExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }
        return executor;
    }
    
    @Override
    public void wrote(T tuple, long nbytes) {
        curSize += nbytes; 
        curTupleCnt++;
        flushIt = flushConfig.evaluate(curTupleCnt, tuple);
        cycleIt = cycleConfig.evaluate(curSize, curTupleCnt, tuple);
    }
    
    @Override
    public boolean shouldFlush() {
        boolean b = flushIt;
        flushIt = false;
        return b;
    }
    
    @Override
    public boolean shouldCycle() {
        boolean b = cycleIt;
        cycleIt = false;
        return b;
    }
    
    @Override
    public Path getNextActiveFilePath() {
        Path path = hookGenerateNextActiveFilePath();
        trace.trace("next active file path={}", path);
        return path;
    }
    
    @Override
    public synchronized Path closeActiveFile(Path path) throws IOException {
        int tmpCurTupleCnt = curTupleCnt;
        resetActiveFileInfo();
        Path finalPath = hookGenerateFinalFilePath(path);
        trace.trace("closing active file nTuples={}, finalPath={}", tmpCurTupleCnt, finalPath);
        hookRenameFile(path, finalPath);
        retainedPaths.add(finalPath);
        applyRetention();
        return finalPath;
    }
    
    private void resetActiveFileInfo() {
        curSize = 0;
        curTupleCnt = 0;
        flushIt = false;
        cycleIt = false;
    }
    
    private synchronized void applyRetention() {
        long aggregateFileSize = 0; // compute when enabled
        if (retentionConfig.getAggregateFileSize() > 0) {
            for (Path path : retainedPaths) {
                File file = path.toFile();
                aggregateFileSize += file.length(); // 0 if doesn't exist
            }
        }
        
        if (retentionConfig.evaluate(retainedPaths.size(), aggregateFileSize)) {
            Path oldestPath = retainedPaths.remove(0);
            File file = oldestPath.toFile();
            trace.info("deleting file {}", file);
            file.delete();
        }
    }
    
    private synchronized void applyTimeBasedRetention() {
        long now = System.currentTimeMillis();
        long minTime = now - TimeUnit.SECONDS.toMillis(retentionConfig.getAgeSec());
        ArrayList<Path> toDelete = new ArrayList<>();
        for (Path path : retainedPaths) {  // oldest first
            File file = path.toFile();
            if (file.lastModified() < minTime)
                toDelete.add(path);
            else
                break;
        }
        for (Path path : toDelete) {
            trace.info("deleting file {}", path);
            path.toFile().delete();
        }
        retainedPaths.removeAll(toDelete);
    }
    
    private String ymdhms() {
        return new SimpleDateFormat("YYYYMMDD_HHmmss").format(new Date());
    }
    
    /**
     * Generate the final file path for the active file.
     * <p>
     * The default implementation yields:
     * <br>
     * final file names are {@code basePathname_YYYYMMDD_HHMMSS[_<n>]}
     * where the optional {@code _<n>} suffix is only present if needed
     * to distinguish a file from the previously finalized file.
     * {@code <n>} starts at 1 and is monitonically incremented.
     * <p>
     * This hook method can be overridden.
     * <p>
     * Note, the implementation must handle the unlikely, but happens
     * in tests, case where files are cycling very fast (multiple per sec)
     * and the retention config tosses some within that same second.
     * I.e., avoid generating final path sequences like:
     * <pre>
     * leaf_YYYYMMDD_103099
     * leaf_YYYYMMDD_103099_1
     * leaf_YYYYMMDD_103099_2
     *   delete leaf_YYYYMMDD_103099  -- retention cnt was 2
     * leaf_YYYYMMDD_103099   // should be _3
     * </pre>
     * 
     * @param path the active file path to finalize
     * @return final path for the file
     */
    protected Path hookGenerateFinalFilePath(Path path) {
        String ymdhms = ymdhms();
        if (ymdhms.equals(lastYmdhms)) {
            lastMinorSuffix++;
        }
        else {
            lastMinorSuffix = 0;
            lastYmdhms = ymdhms;
        }
        String pathStr = String.format("%s_%s", basePathname, ymdhms);
        String finalPathStr = pathStr;
        if (lastMinorSuffix > 0)
            finalPathStr += "_" + lastMinorSuffix;
        return new File(finalPathStr).toPath();
    }
    
    /**
     * Generate the path for the next active file.
     * <p>
     * The default implementation yields {@code parent/.baseLeafname}
     * from {@code basePathname}.
     * <p>
     * This hook method can be overridden.
     * <p>
     * See {@link IFileWriterPolicy#getNextActiveFilePath()} regarding
     * constraints.
     * 
     * @return path to use for the next active file.
     */
    protected Path hookGenerateNextActiveFilePath() {
        return parent.resolve("." + baseLeafname);
    }

    /**
     * "Rename" the active file to the final path.
     * <p>
     * The default implementation uses {@code java.io.File.renameTo()}
     * and works for the default {@link #hookGenerateNextActiveFilePath()}
     * and {@link #hookGenerateFinalFilePath(Path path)} implementations.
     * <p>
     * This hook method can be overridden.
     * 
     * @param activePath path of the active file
     * @param finalPath path to the final destination
     * @throws IOException
     */
    protected void hookRenameFile(Path activePath, Path finalPath) throws IOException {
        trace.info("finalizing to {}", finalPath);
        activePath.toFile().renameTo(finalPath.toFile());
    }
    
    @Override
    public String toString() {
        return String.format("basePathname:%s [retention: %s] [cycle: %s] [flush: %s]",
                basePathname,
                retentionConfig.toString(),
                cycleConfig.toString(), flushConfig.toString());
    }

}