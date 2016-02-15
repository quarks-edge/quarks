/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.topology.spi;

import java.util.Arrays;
import java.util.Collection;

import quarks.function.Supplier;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.spi.functions.EndlessSupplier;
import quarks.topology.tester.Tester;

/**
 * Topology implementation that uses the basic functions to implement most
 * sources streams.
 *
 */
public abstract class AbstractTopology<X extends Tester> implements Topology {

    private final String name;

    protected AbstractTopology(String name) {
        this.name = name;
    }

    @Override
    public Topology topology() {
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TStream<String> strings(String... tuples) {
        return source(() -> Arrays.asList(tuples));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> TStream<T> of(T... values) {
        return source(() -> Arrays.asList(values));
    }

    @Override
    public <T> TStream<T> generate(Supplier<T> data) {
        return source(new EndlessSupplier<T>(data));
    }

    X tester;

    @Override
    public X getTester() {
        if (tester == null)
            tester = newTester();
        return tester;
    }

    protected abstract X newTester();
    
    @Override
    public <T> TStream<T> collection(Collection<T> tuples) {
        return source(() -> tuples);
    }
}
