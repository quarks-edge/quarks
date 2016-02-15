/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.file.runtime;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Generic class for writing of tuples to a file.
 * <p>
 * The class is not responsible for flush strategy, finalize strategy, etc
 */
abstract class AbstractWriterFile<T> {
    private final Path path;
    protected long size;
    private long tupleCnt;
    public AbstractWriterFile(Path path) {
        this.path = path;
    }
    public Path path() { return path; }
    public long size() { return size; }
    public long tupleCnt() { return tupleCnt; }
    public abstract void flush() throws IOException;
    public abstract void close() throws IOException;
    /** do what's needed to write the tuple */
    protected abstract int writeTuple(T tuple) throws IOException;
    /** returns the number of bytes written */
    public int write(T tuple) throws IOException {
        tupleCnt++;
        int nbytes = writeTuple(tuple);
        size += nbytes;
        return nbytes;
    }
}
