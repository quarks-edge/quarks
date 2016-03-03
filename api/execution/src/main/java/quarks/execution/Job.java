/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
*/
package quarks.execution;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Actions and states for execution of a Quarks job.
 * <p>
 * The interface provides the main job lifecycle control, taking on the following 
 * execution state values:
 *
 * <ul>
 * <li><b>CONSTRUCTED</b>  This job has been constructed but the 
 *      nodes are not yet initialized.</li>
 * <li><b>INITIALIZED</b>  This job has been initialized and is 
 *      ready to process data.
 * <li><b>RUNNING</b>  This job is processing data.</li>
 * <li><b>PAUSED</b>  This job is paused.</li>
 * <li><b>CLOSED</b>  This job is closed.</li>
 * </ul>
 * 
 * The interface provides access to two state values:
 * <ul>
 * <li> {@link #getCurrentState() Current} - The current state of execution 
 *      when the job is not executing a state transition; the source state
 *      while the job is making a state transition after the client code 
 *      calls {@link #stateChange(Job.Action)}.</li>
 * <li> {@link #getNextState() Next} - The destination state while the job 
 *      is making a state transition; same as the {@link #getCurrentState() current} 
 *      state while the job state is stable (that is, not making a transition).</LI>
 * </ul>
 */
public interface Job {
    /**
     * States of a graph job.
     */
    public enum State {
        /** Initial state, the graph nodes are not yet initialized. */
        CONSTRUCTED, 
        /** All the graph nodes have been initialized. */
        INITIALIZED,
        /** All the graph nodes are processing data. */
        RUNNING, 
        /** All the graph nodes are paused. */
        PAUSED, 
        /** All the graph nodes are closed. */
        CLOSED
    }

    /**
     * Retrieves the current state of this job.
     *
     * @return the current state.
     */
    State getCurrentState();

    /**
     * Retrieves the next execution state when this job makes a state 
     * transition.
     *
     * @return the destination state while in a state transition; 
     *      otherwise the same as {@link #getCurrentState()}.
     */
    State getNextState();

    /**
     * Actions which trigger {@link Job.State} transitions.
     */
    public enum Action {
        /** Initialize the job */
        INITIALIZE,
        /** Start the execution. */
        START, 
        /** Pause the execution. */
        PAUSE,
        /** Resume the execution */
        RESUME,
        /** Close the job. */
        CLOSE
    }

    /**
     * Initiates a {@link Job.State State} change.
     * 
     * @param action which triggers the state change.
     * @throws IllegalArgumentException if the job is not in an appropriate 
     *      state for the requested action, or the action is not supported.
     */
    void stateChange(Action action) throws IllegalArgumentException;
    
    /**
     * Returns the name of this job. The name is set when the job is 
     * {@link quarks.execution.Submitter#submit(java.lang.Object,com.google.gson.JsonObject) submitted}.
     *
     * @return the job name.
     */
    String getName();

    /**
     * Returns the identifier of this job.
     * 
     * @return this job identifier.
     */
    String getId();

    /**
     * Waits for any outstanding job work to complete.
     * 
     * @throws ExecutionException if the job execution threw an exception.
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    void complete() throws ExecutionException, InterruptedException;

    /**
     * Waits for at most the specified time for the job to complete.
     * 
     * @param timeout the time to wait
     * @param unit the time unit of the timeout argument
     * 
     * @throws ExecutionException if the job execution threw an exception.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws TimeoutException if the wait timed out
     */
    void complete(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException;
}
