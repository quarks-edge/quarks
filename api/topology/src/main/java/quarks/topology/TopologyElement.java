/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.topology;

/**
 * An element of a {@code Topology}.
 *
 */
public interface TopologyElement {

	/**
	 * Topology this element is contained in.
	 * @return Topology this element is contained in.
	 */
    Topology topology();
}
