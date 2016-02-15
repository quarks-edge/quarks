/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.test.graph;

import org.junit.Before;
import org.junit.Ignore;

import quarks.graph.Graph;

@Ignore("Abstract class proiding generic graph testing.")
public abstract class GraphAbstractTest {

    private Graph graph;

    @Before
    public void setup() {
        graph = createGraph();
    }

    protected Graph getGraph() {
        return graph;
    }

    protected abstract Graph createGraph();
}
