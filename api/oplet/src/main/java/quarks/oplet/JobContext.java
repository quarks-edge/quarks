/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
*/
package quarks.oplet;

/**
 * Information about an oplet invocation's job. 
 */
public interface JobContext {
    /**
     * Get the runtime identifier for the job containing this {@link Oplet}.
     * @return The job identifier for the application being executed.
     */
    String getJobId();
    
    /**
     * Get the name of the job containing this {@link Oplet}.
     * @return The job name for the application being executed.
     */
    String getJobName();
}
