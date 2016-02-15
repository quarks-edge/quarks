/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.runtime.etiao.graph.model;

import quarks.oplet.Oplet;

/**
 * Generic type for an oplet invocation instance.
 * 
 * @param <I>
 *            Data container type for input tuples.
 * @param <O>
 *            Data container type for output tuples.
 */
public class InvocationType<I, O> {

    /**
     * Kind of the oplet to be invoked.
     */
    private final String kind;
    
    // TODO add port model
    
    public InvocationType(Oplet<I, O> value) {
        this.kind = value.getClass().getName();
    }
    
    public String getClassName() {
        return this.kind;
    }
}
