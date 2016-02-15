/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016
*/
package quarks.samples.utils.sensor;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.util.Pair;

import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * A factory of simple periodic random sensor reading streams.
 * <p>
 * The generated {@link TStream} has a {@code org.apache.commons.math3.utils.Pair}
 * tuple type where {@link Pair#getFirst()} the reading's msecTimestamp
 * and {@link Pair#getSecond()} is the sensor value reading.
 * <p>
 * The sensor reading values are randomly generated via {@link Random}
 * and have the value distributions, as defined by {@code Random}.
 * <p>
 * Each stream has its own {@code Random} object instance.
 */
public class PeriodicRandomSensor {
    private Long seed;
    
    /**
     * Create a new random periodic sensor factory configured
     * to use {@link Random#Random()}. 
     */
    public PeriodicRandomSensor() {
    }
    
    /**
     * Create a new random periodic sensor factory configured
     * to use {@link Random#Random(long)}. 
     * 
     * @param seed seed to use when creating new sensor streams.
     */
    public PeriodicRandomSensor(long seed) {
        this.seed = seed;
    }
    
    /**
     * Set the seed to be used by subsequently created sensor streams.
     * @param seed the seed value
     */
    public void setSeed(long seed) {
        this.seed = seed;
    }
    
    private Random newRandom() {
        if (seed != null)
            return new Random(seed);
        return new Random();
    }
    
    /**
     * Create a periodic sensor stream with readings from {@link Random#nextGaussian()}.
     * @param t the topology to add the sensor stream to
     * @param periodMsec how frequently to generate a reading
     * @return the sensor value stream
     */
    public TStream<Pair<Long,Double>> newGaussian(Topology t, long periodMsec) {
        Random r = newRandom();
        return t.poll(() -> new Pair<Long,Double>(System.currentTimeMillis(), r.nextGaussian()), 
                periodMsec, TimeUnit.MILLISECONDS);
        
    }
    
    /**
     * Create a periodic sensor stream with readings from {@link Random#nextDouble()}.
     * @param t the topology to add the sensor stream to
     * @param periodMsec how frequently to generate a reading
     * @return the sensor value stream
     */
    public TStream<Pair<Long,Double>> newDouble(Topology t, long periodMsec) {
        Random r = newRandom();
        return t.poll(() -> new Pair<Long,Double>(System.currentTimeMillis(), r.nextDouble()), 
                periodMsec, TimeUnit.MILLISECONDS);
        
    }
    
    /**
     * Create a periodic sensor stream with readings from {@link Random#nextFloat()}.
     * @param t the topology to add the sensor stream to
     * @param periodMsec how frequently to generate a reading
     * @return the sensor value stream
     */
    public TStream<Pair<Long,Float>> newFloat(Topology t, long periodMsec) {
        Random r = newRandom();
        return t.poll(() -> new Pair<Long,Float>(System.currentTimeMillis(), r.nextFloat()), 
                periodMsec, TimeUnit.MILLISECONDS);
        
    }
    
    /**
     * Create a periodic sensor stream with readings from {@link Random#nextLong()}.
     * @param t the topology to add the sensor stream to
     * @param periodMsec how frequently to generate a reading
     * @return the sensor value stream
     */
    public TStream<Pair<Long,Long>> newLong(Topology t, long periodMsec) {
        Random r = newRandom();
        return t.poll(() -> new Pair<Long,Long>(System.currentTimeMillis(), r.nextLong()), 
                periodMsec, TimeUnit.MILLISECONDS);
        
    }
    
    /**
     * Create a periodic sensor stream with readings from {@link Random#nextInt()}.
     * @param t the topology to add the sensor stream to
     * @param periodMsec how frequently to generate a reading
     * @return the sensor value stream
     */
    public TStream<Pair<Long,Integer>> newInteger(Topology t, long periodMsec) {
        Random r = newRandom();
        return t.poll(() -> new Pair<Long,Integer>(System.currentTimeMillis(), r.nextInt()), 
                periodMsec, TimeUnit.MILLISECONDS);
        
    }
    
    /**
     * Create a periodic sensor stream with readings from {@link Random#nextInt(int)}.
     * @param t the topology to add the sensor stream to
     * @param periodMsec how frequently to generate a reading
     * @param bound the upper bound (exclusive). Must be positive.
     * @return the sensor value stream
     */
    public TStream<Pair<Long,Integer>> newInteger(Topology t, long periodMsec, int bound) {
        Random r = newRandom();
        return t.poll(() -> new Pair<Long,Integer>(System.currentTimeMillis(), r.nextInt(bound)), 
                periodMsec, TimeUnit.MILLISECONDS);
        
    }
    
    /**
     * Create a periodic sensor stream with readings from {@link Random#nextBoolean()}.
     * @param t the topology to add the sensor stream to
     * @param periodMsec how frequently to generate a reading
     * @return the sensor value stream
     */
    public TStream<Pair<Long,Boolean>> newBoolean(Topology t, long periodMsec) {
        Random r = newRandom();
        return t.poll(() -> new Pair<Long,Boolean>(System.currentTimeMillis(), r.nextBoolean()), 
                periodMsec, TimeUnit.MILLISECONDS);
        
    }
    
    /**
     * Create a periodic sensor stream with readings from {@link Random#nextBytes(byte[])}.
     * @param t the topology to add the sensor stream to
     * @param periodMsec how frequently to generate a reading
     * @return the sensor value stream
     */
    public TStream<Pair<Long,byte[]>> newBytes(Topology t, long periodMsec, int nBytes) {
        Random r = newRandom();
        return t.poll(() -> { byte[] bytes = new byte[nBytes];
                              r.nextBytes(bytes);
                              return new Pair<Long,byte[]>(System.currentTimeMillis(), bytes);
                            }, 
                periodMsec, TimeUnit.MILLISECONDS);
    }

}
