/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.topology.spi;

import java.util.Collections;

import quarks.function.UnaryOperator;
import quarks.topology.TSink;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * Abstract stream that uses the functional primitives to implement most
 * methods.
 * <P>
 * The functional primitives are:
 * <UL>
 * <LI>{@link TStream#filter(quarks.function.Predicate)}</LI>
 * <LI>{@link TStream#map(quarks.function.Function)}</LI>
 * <LI>{@link TStream#sink(quarks.function.Consumer)}
 * </UL>
 * These methods are unimplemented, thus left to the specific implementation
 * used to build the topology.
 * </P>
 * 
 * @param <G>
 *            Type of the {@link Topology} implementation.
 * @param <T>
 *            Type of data on the stream.
 */
public abstract class AbstractTStream<G extends Topology, T> implements TStream<T> {

    private final G topology;

    protected AbstractTStream(G topology) {
        this.topology = topology;
    }

    @Override
    public G topology() {
        return topology;
    }
    
    protected void verify(TStream<T> other) {
        if (topology() != other.topology())
            throw new IllegalArgumentException();
    }
    
    /**
     * Declare a new stream that modifies each tuple from this stream into one
     * (or zero) tuple of the same type {@code T}. For each tuple {@code t}
     * on this stream, the returned stream will contain a tuple that is the
     * result of {@code modifier.apply(t)} when the return is not {@code null}.
     * The function may return the same reference as its input {@code t} or
     * a different object of the same type.
     * If {@code modifier.apply(t)} returns {@code null} then no tuple
     * is submitted to the returned stream for {@code t}.
     * 
     * <P>
     * Example of modifying a stream  {@code String} values by adding the suffix '{@code extra}'.
     * 
     * <pre>
     * <code>
     * TStream&lt;String> strings = ...
     * TStream&lt;String> modifiedStrings = strings.modify(t -> t.concat("extra"));
     * </code>
     * </pre>
     * 
     * </P>
     * <P>
     * This method is equivalent to
     * {@code map(Function<T,T> modifier}).
     * </P
     * 
     * @param modifier
     *            Modifier logic to be executed against each tuple.
     * @return Stream that will contain tuples of type {@code T} modified from this
     *         stream's tuples.
     */
     @Override
     public TStream<T> modify(UnaryOperator<T> modifier) {
        return map(modifier);
    }

    /**
     * Convert this stream to a stream of {@code String} tuples by calling
     * {@code toString()} on each tuple. This is equivalent to
     * {@code map(Object::toString)}.
     * 
     * @return Declared stream that will contain each the string representation
     *         of each tuple on this stream.
     */
    @Override
    public TStream<String> asString() {
        return map(Object::toString);
    }

    /**
     * Utility method to print the contents of this stream to {@code System.out}
     * at runtime. Each tuple is printed using {@code System.out.println(tuple)}
     * .
     * 
     * @return {@code TSink} for the sink processing.
     */
    @Override
    public TSink<T> print() {
        return sink(tuple -> System.out.println(tuple));
    }

    /**
     * Declare a stream that will contain all tuples from this stream and
     * {@code other}. A stream cannot be unioned with itself, in this case
     * {@code this} will be returned.
     * 
     * @param other
     * @return A stream that is the union of {@code this} and {@code other}.
     */
    @Override
    public TStream<T> union(TStream<T> other) {
        return union(Collections.singleton(other));
    }
}
