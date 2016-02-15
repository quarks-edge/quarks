/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.execution;

import quarks.execution.services.ServiceContainer;

/**
 * An interface for submission of an executable
 * that is executed directly within the current
 * virtual machine.
 * 
 * @param <E> the executable type
 * @param <J> the submitted executable's future
 */
public interface DirectSubmitter<E, J extends Job> extends Submitter<E,J> {
    
    /**
     * Access to services.
     * 
     * Since any executables are executed directly within
     * the current virtual machine, callers may register
     * services that are visible to the executable
     * and its elements.
     * 
     * @return Service container for this submitter.
     */
    ServiceContainer getServices();
}
