/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.graph;

import java.util.Set;

/**
 * Represents an edge between two Vertices.
 */
public interface Edge {
    
    /**
     * Returns the source vertex.
     * @return the source vertex.
     */
	Vertex<?, ?, ?> getSource();

    /**
     * Returns the source output port index.
     * @return the source output port index.
     */
	int getSourceOutputPort();
	
    /**
     * Returns the target vertex.
     * @return the target vertex.
     */
	Vertex<?, ?, ?> getTarget();

    /**
     * Returns the target input port index.
     * @return the target input port index.
     */
	int getTargetInputPort();
	
    /**
     * Returns the set of tags associated with this edge.
     * 
     * @return set of tag values.
     */
    Set<String> getTags(); 
}
