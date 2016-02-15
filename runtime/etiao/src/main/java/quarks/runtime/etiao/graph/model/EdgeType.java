/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.runtime.etiao.graph.model;

import java.util.HashSet;
import java.util.Set;

import quarks.graph.Edge;

/**
 * Represents an edge between two {@link VertexType} nodes.
 */
public class EdgeType {
    /** Source vertex identifier */
	private final String sourceId;
    /** Source output port index */
	private final int sourceOutputPort;
    /** Target vertex identifier */
	private final String targetId;
    /** Target input port index */
	private final int targetInputPort;
    /** Set of tags associated with this edge */
    private final Set<String> tags;
    
    public EdgeType() {
        this.sourceId = null;
        this.sourceOutputPort = 0;
        this.targetId = null;
        this.targetInputPort = 0;
        this.tags = new HashSet<>();
    }

    public EdgeType(Edge value, IdMapper<String> ids) {
        this.sourceId = ids.getId(value.getSource()).toString();
        this.sourceOutputPort = value.getSourceOutputPort();
        this.targetId = ids.getId(value.getTarget()).toString();
        this.targetInputPort = value.getTargetInputPort();
        this.tags = value.getTags();
    }

    public String getSourceId() {
        return sourceId;
    }
    
    public int getSourceOutputPort() {
        return sourceOutputPort;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public int getTargetInputPort() {
        return targetInputPort;
    }

    public Set<String> getTags() {
        return tags;
    }
}
