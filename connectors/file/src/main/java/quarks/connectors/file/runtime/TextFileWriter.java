/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.file.runtime;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.slf4j.Logger;

import quarks.function.Consumer;
import quarks.function.Supplier;

public class TextFileWriter implements Consumer<String>, AutoCloseable {
    private static final long serialVersionUID = 1L;
    static final Logger trace = FileConnector.getTrace();
    private volatile String encoding = "UTF-8";
    private volatile Charset charset;
    private final Supplier<String> basePathname;
    private final Supplier<IFileWriterPolicy<String>> policyFn;
    private volatile boolean initialized;
    private volatile IFileWriterPolicy<String> policy;
    private StringWriterFile activeFile;
    
    private String getEncoding() {
        return encoding;
    }

    public TextFileWriter(Supplier<String> basePathname, Supplier<IFileWriterPolicy<String>> policy) {
        this.basePathname = basePathname;
        this.policyFn = policy;
        charset = Charset.forName(getEncoding());
    }
    
    private IFileWriterPolicy<String> getPolicy() {
        if (policy == null) {
            policy = policyFn.get();
        }
        return policy;
    }
    
    private void initialize() {
        getPolicy().initialize(basePathname.get(),
                                () -> flushActiveFile(),
                                () -> closeActiveFile());
        initialized = true;
        trace.info("writer policy: {}", getPolicy());
    }
    
    private synchronized void flushActiveFile() {
        if (activeFile != null) {
            try {
                activeFile.flush();
            } catch (IOException e) {
                trace.trace("flush of {} failed", activeFile.path(), e);
            }
        }
    }

    @Override
    public void accept(String line) {
        if (!initialized)
            initialize();
        writeLine(line);
    }
    
    private void writeLine(String line) {
        // prevent async time based cycle or flush while writing the tuple
        synchronized(this) {
            try {
                if (activeFile == null) {
                    newActiveFile();
                }
                int nbytes = activeFile.write(line);
                getPolicy().wrote(line, nbytes);
            }
            catch (IOException e) {
                trace.error("Error writing tuple {} of length {} to {}",
                        activeFile.tupleCnt(), line.length(), activeFile.path(), e);
            }
        }
        if (getPolicy().shouldCycle()) {
            closeActiveFile();
        }
        else if (getPolicy().shouldFlush()) {
            flushActiveFile();
        }
    }
    
    private synchronized void newActiveFile() throws IOException {
        Path path = getPolicy().getNextActiveFilePath();
        activeFile = new StringWriterFile(path, charset);
    }

    /**
     * close, finalize, and apply retention policy
     */
    private synchronized void closeActiveFile() {
        StringWriterFile activeFile = this.activeFile;
        try {
            this.activeFile = null;
            if (activeFile != null) {
                activeFile.close();
                getPolicy().closeActiveFile(activeFile.path());
                activeFile = null;
            }
        }
        catch (IOException e) {
            trace.error("error closing active file '{}'", activeFile.path(), e);
        }
    }

    @Override
    public void close() throws Exception {
        closeActiveFile();
        getPolicy().close();
    }
}
