/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.file.runtime;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;

class StringWriterFile extends AbstractWriterFile<String> {
    private static Logger trace = FileConnector.getTrace();
    private BufferedWriter bw;
    private final Charset cs;

    public StringWriterFile(Path path, Charset cs) {
        super(path);
        this.cs = cs;
    }

    @Override
    protected int writeTuple(String tuple) throws IOException {
        if (bw == null) {
            trace.info("creating file {}", path());
            bw = Files.newBufferedWriter(path(), cs);
        }
        bw.write(tuple);
        bw.write("\n");
        // ugh. inefficient
        int nbytes = tuple.getBytes(cs).length;
        nbytes++;
        return nbytes;
    }

    @Override
    public void flush() throws IOException {
        if (bw != null) {
            trace.trace("flushing {}", path());
            bw.flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (bw != null) {
            trace.info("closing {}", path());
            BufferedWriter bw = this.bw;
            this.bw = null;
            bw.close();
        }
    }
    
}
