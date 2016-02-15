/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.core;

import quarks.oplet.Oplet;
import quarks.oplet.OpletContext;

public abstract class AbstractOplet<I, O> implements Oplet<I, O> {

    private OpletContext<I, O> context;

    @Override
    public void initialize(OpletContext<I, O> context) {
        this.context = context;
    }

    public final OpletContext<I, O> getOpletContext() {
        return context;
    }
}
