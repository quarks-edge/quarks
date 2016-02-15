/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016
*/
package quarks.execution.mbeans;

/**
 * Control interface for a job.
 */
public interface JobMXBean {
    /**
     * TYPE is used to identify this bean as a job bean when building the bean's {@code ObjectName}.
     * The value is {@value} 
     */
    public static final String TYPE = "job";

    /**
     * Returns the identifier of the job.
     * 
     * @return the job identifier.
     */
    String getId();

    /**
     * Returns the name of the job.
     *  
     * @return the job name.
     */
    String getName();

    /**
     * Retrieves the current state of the job.
     *
     * @return the current state.
     */
    State getCurrentState();

    /**
     * Retrieves the next execution state when the job makes a state 
     * transition.
     *
     * @return the destination state while in a state transition.
     */
    State getNextState();
    
    /**
     * Enumeration for the current status of the job.
     */
    enum State {  
        /** Initial state, the graph nodes are not yet initialized. */
        CONSTRUCTED, 
        /** All the graph nodes have been initialized. */
        INITIALIZED,
        /** All the graph nodes are processing data. */
        RUNNING,
        /** All the graph nodes are paused. */
        PAUSED, 
        /** All the graph nodes are closed. */
        CLOSED;
        
        /**
         * Converts from a string representation of a job status to the corresponding enumeration value.
         * 
         * @param state specifies a job status string value.
         * 
         * @return the corresponding Status enumeration value.
         * 
         * @throws IllegalArgumentException if the input string does not map to an enumeration value.
         * @throws NullPointerException if the input value is null.
         */
        static public State fromString(String state) {
            if (state ==  null) {
                throw new NullPointerException("state");  
            }
            for (State value : State.values()) {
                if (value.name().equals(state)) {
                    return value;
                }
            }
            throw new IllegalArgumentException(state);
        }
    }
    
    /**
     * Takes a current snapshot of the running graph and returns it in JSON format.
     * <p>
     * <b>The graph snapshot JSON format</b>
     * <p>
     * The top-level object contains two properties: 
     * <ul>
     * <li>{@code vertices}: Array of JSON objects representing the graph vertices.</li>
     * <li>{@code edges}: Array of JSON objects representing the graph edges (an edge joins two vertices).</li>
     * </ul>
     * The vertex object contains the following properties:
     * <ul>
     * <li>{@code id}: Unique identifier within a graph's JSON representation.</li>
     * <li>{@code instance}: The oplet instance from the vertex.</li>
     * </ul>
     * The edge object contains the following properties:
     * <ul>
     * <li>{@code sourceId}: The identifier of the source vertex.</li>
     * <li>{@code sourceOutputPort}: The identifier of the source oplet output port connected to the edge.</li>
     * <li>{@code targetId}: The identifier of the target vertex.</li>
     * <li>{@code targetInputPort}: The identifier of the target oplet input port connected to the edge.</li>
     * </ul>
     *  
     * @return a JSON-formatted string representing the running graph. 
     */
    String graphSnapshot();
}
