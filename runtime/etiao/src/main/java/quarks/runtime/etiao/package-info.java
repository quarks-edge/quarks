/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/

/**
 * A runtime for executing a Quarks streaming topology, designed as an embeddable library 
 * so that it can be executed in a simple Java application.
 * 
 * <h2>"EveryThing Is An Oplet" (ETIAO)</h2>
 *
 * The runtime's focus is on executing oplets and their connected streams, where each 
 * oplet is just a black box. Specifically this means that functionality is added by the introduction 
 * of oplets into the graph that were not explicitly declared by the application developer. 
 * For example, metrics are implemented by oplets, not the runtime. A metric collector is an 
 * oplet that calculates metrics on tuples accepted on its input port, and them makes them 
 * available, for example through JMX.
 */
package quarks.runtime.etiao;
