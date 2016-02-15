/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
/**
 * Oplets API.
 * <P>
 * An oplet is a stream processor that can have 0-N input ports and 0-M output ports.
 * Tuples on streams connected to an oplet's input port are delivered to the oplet for processing.
 * The oplet submits tuples to its output ports which results in the tuples
 * being present on the connected streams.
 * </P>
 */
package quarks.oplet;