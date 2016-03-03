/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
*/
package quarks.graph.spi.execution;

import quarks.execution.Job;

/**
 * Placeholder for a skeletal implementation of the {@link Job} interface,
 * to minimize the effort required to implement the interface.
 */
public abstract class AbstractGraphJob implements Job {
    private State currentState;
    private State nextState;
    
    protected AbstractGraphJob() {
        this.currentState = State.CONSTRUCTED;
        this.nextState = currentState;
    }

    @Override
    public synchronized State getCurrentState() {
        return currentState;
    }

    @Override
    public synchronized State getNextState() {
        return nextState;
    }

    @Override
    public abstract void stateChange(Action action);
    
    protected synchronized boolean inTransition() {
        return getNextState() != getCurrentState();
    }

    protected synchronized void setNextState(State value) {
        this.nextState = value;
    }
    
    protected synchronized void completeTransition() {
        if (inTransition()) {
            currentState = nextState;
        }
    }
}
