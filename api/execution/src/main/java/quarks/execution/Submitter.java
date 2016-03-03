/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.execution;

import java.util.concurrent.Future;

import com.google.gson.JsonObject;


/**
 * An interface for submission of an executable.
 * <p>
 * The class implementing this interface is responsible
 * for the semantics of this operation.  e.g., an direct topology
 * provider would run the topology as threads in the current jvm.
 * 
 * @param <E> the executable type
 * @param <J> the submitted executable's future
 */
public interface Submitter<E, J extends Job> {
    
    /**
     * Submit an executable.
     * No configuration options are specified,
     * this is equivalent to {@code submit(executable, new JsonObject())}.
     * 
     * @param executable executable to submit
     * @return a future for the submitted executable
     */
    Future<J> submit(E executable);
    
    /**
     * Submit an executable.
     * 
     * @param executable executable to submit
     * @param config context information for the submission
     * @return a future for the submitted executable
     */
    Future<J> submit(E executable, JsonObject config);
}
