/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.file.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;

import quarks.function.BiFunction;
import quarks.function.Consumer;
import quarks.function.Function;
import quarks.oplet.OpletContext;
import quarks.oplet.core.Pipe;

public class TextFileReader extends Pipe<String,String> {

    private static final long serialVersionUID = 1L;
    private static final Logger trace = FileConnector.getTrace();
    private volatile String encoding = "UTF-8";
    private volatile Charset charset;
    private volatile boolean shutdown;
    private volatile Function<String,String> preFn = path -> null;
    private volatile BiFunction<String,Exception,String> postFn = (path,exc) -> null;

    private void setShutdown(boolean b) {
        shutdown = b;
    }

    private boolean isShutdown() {
        return shutdown;
    }
    
    private String getEncoding() {
        return encoding;
    }
    
    public void setPre(Function<String,String> preFn) {
        if (preFn == null)
            this.preFn = path -> null;
        else
            this.preFn = preFn;
    }
    
    public void setPost(BiFunction<String,Exception,String> postFn) {
        if (postFn == null)
            this.postFn = (path,exc) -> null;
        else
            this.postFn = postFn;
    }

    @Override
    public synchronized void initialize(OpletContext<String,String> context) {
        super.initialize(context);

        charset = Charset.forName(getEncoding());
    }
    
    private void pre(String pathname, Consumer<String> dst) {
        String preStr = preFn.apply(pathname);
        if (preStr != null)
            dst.accept(preStr);
    }
    
    private void post(String pathname, Exception e, Consumer<String> dst) {
        String postStr = postFn.apply(pathname, e);
        if (postStr != null)
            dst.accept(postStr);
    }

    @Override
    public void accept(String pathname) {
        trace.trace("reading path={}", pathname);
        Consumer<String> dst = getDestination();
        pre(pathname, dst);
        Path path = new File(pathname).toPath();
        Exception exc = null;
        int nlines = 0;
        try (BufferedReader br = Files.newBufferedReader(path, charset)) {
            for (int i = 0;;i++) {
                if (i % 10 == 0 && isShutdown())
                    break;
                String line = br.readLine();
                if (line == null)
                    break;
                nlines++;
                dst.accept(line);
            }
        }
        catch (IOException e) {
            trace.error("Error processing file '{}'", pathname, e);
            exc = e;
        }
        finally {
            trace.trace("done reading nlines={} path={} ", nlines, pathname);
            post(pathname, exc, dst);
        }
    }

    @Override
    public void close() throws Exception {
        setShutdown(true);
    }
}
