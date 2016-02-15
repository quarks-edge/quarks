/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.runtime.etiao.graph.model;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import quarks.graph.Edge;
import quarks.graph.Graph;
import quarks.graph.Vertex;
import quarks.oplet.Oplet;

/**
 * A generic directed graph of vertices, connectors and edges.
 * <p>
 * The graph consists of {@link VertexType} objects, each having
 * 0 or more input and/or output {@link EdgeType} objects.
 * {@link EdgeType} objects connect an output connector to
 * an input connector.
 * <p>
 * A vertex has an associated {@link Oplet}.
 */
public class GraphType {
    /**
     * List of all vertices in this graph.
     */
    private final List<VertexType<?,?>> vertices;

    /**
     * List of all edges in this graph.
     */
    private final List<EdgeType> edges;

    /**
     * Create an instance of {@link GraphType}.
     */
    public GraphType(Graph graph) {
        this(graph, null);
    }

    /**
     * Create an instance of {@link GraphType} using the specified 
     * {@link IdMapper} to generate unique object identifiers.
     */
    public GraphType(Graph g, IdMapper<String> ids) {
        if (ids == null) {
            ids = new GraphType.Mapper();
        }
        ArrayList<VertexType<?,?>> vertices = 
                new ArrayList<VertexType<?,?>>();
        ArrayList<EdgeType> edges = new ArrayList<EdgeType>();
        
        for (Vertex<? extends Oplet<?,?>, ?, ?> v : g.getVertices()) {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            VertexType<?,?> vertex = new VertexType(v, ids);
            vertices.add(vertex);
        }

        for (Edge e : g.getEdges()) {
            edges.add(new EdgeType(e, ids));
        }

        this.vertices = vertices;
        this.edges = edges;
    }

    /**
     * Default constructor of {@link GraphType}.
     */
    public GraphType() {
        this.vertices = null;
        this.edges = null;
    }

    public List<VertexType<?,?>> getVertices() {
        return vertices;
    }

    public List<EdgeType> getEdges() {
        return edges;
    }

    static class Mapper implements IdMapper<String> {
        private int lastId = 0;
        // Map using reference-equality in place of object-equality.
        private IdentityHashMap<Object,String> ids = new IdentityHashMap<Object,String>();
        
        @Override
        public String add(Object o) {
            if (o == null)
                throw new NullPointerException();
    
            synchronized (ids) {
                String id = ids.get(o);
                if (id == null) {
                    id = String.valueOf(lastId++);
                    ids.put(o, id);
                }
                return id;
            }
        }
    
        @Override
        public String getId(Object o) {
            if (o == null)
                throw new NullPointerException();
    
            synchronized (ids) {
                return ids.get(o);
            }
        }

        @Override
        public String add(Object o, String id) {
            if (o == null || id == null)
                throw new NullPointerException();
            
            synchronized (ids) {
                if (ids.containsKey(o)) {
                    throw new IllegalStateException();
                }
                else {
                    ids.put(o, id);
                }
                return id;
            }
        }
    }
}
