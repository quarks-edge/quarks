/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.runtime.etiao.graph;

class Target<T> {
	
	public Target(ExecutableVertex<?, T, ?> vertex, int port) {
		super();
		this.vertex = vertex;
		this.port = port;
	}
	final ExecutableVertex<?, T, ?> vertex;
	final int port;
}