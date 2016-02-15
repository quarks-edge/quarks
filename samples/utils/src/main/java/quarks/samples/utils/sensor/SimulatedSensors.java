/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.samples.utils.sensor;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;

import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * Streams of simulated sensors.
 *
 */
public class SimulatedSensors {

    /**
     * Create a stream of simulated bursty sensor readings.
     * 
     * Simulation of reading a sensor every 100ms with the readings
     * generally falling below 2.0 (absolute) but randomly have
     * prolonged bursts of higher values.
     * 
     * Each tuple is a JSON object containing:
     * <UL>
     * <LI>{@code name} - Name of the sensor from {@code name}.</LI>
     * <LI>{@code reading} - Value.</LI>
     * </UL>
     * 
     * @param topology Topology to be added to.
     * @param name Name of the sensor in the JSON output.
     * @return Stream containing bursty data.
     */
    public static TStream<JsonObject> burstySensor(Topology topology, String name) {

        Random r = new Random();

        TStream<Double> sensor = topology.poll(() -> r.nextGaussian(), 100, TimeUnit.MILLISECONDS);

        boolean[] abnormal = new boolean[1];
        int[] count = new int[1];
        double[] delta = new double[1];
        sensor = sensor.modify(t -> {
            if (abnormal[0] || r.nextInt(100) < 4) {
                if (!abnormal[0]) {
                    delta[0] = 0.5 + 2 * r.nextGaussian();
                    count[0] = 5 + r.nextInt(20);
                    abnormal[0] = true;
                }
                count[0]--;
                if (count[0] <= 0)
                    abnormal[0] = false;
                return t + delta[0];
            } else
                return t;
        });

        sensor = sensor.filter(t -> Math.abs(t) > 1.5);

        return sensor.map(t -> {
            JsonObject j = new JsonObject();
            j.addProperty("name", name);
            j.addProperty("reading", t);
            return j;
        });

    }

}
