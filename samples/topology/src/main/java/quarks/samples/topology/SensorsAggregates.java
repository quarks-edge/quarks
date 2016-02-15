/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.samples.topology;

import static quarks.analytics.math3.stat.Statistic.MAX;
import static quarks.analytics.math3.stat.Statistic.MEAN;
import static quarks.analytics.math3.stat.Statistic.MIN;
import static quarks.analytics.math3.stat.Statistic.STDDEV;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import quarks.analytics.math3.json.JsonAnalytics;
import quarks.providers.development.DevelopmentProvider;
import quarks.providers.direct.DirectProvider;
import quarks.samples.utils.sensor.SimulatedSensors;
import quarks.topology.TStream;
import quarks.topology.TWindow;
import quarks.topology.Topology;

/**
 * Aggregation of sensor readings.
 * 
 * Demonstrates partitioned aggregation and filtering of simulated sensors
 * that are bursty in nature, so that only intermittently
 * is the data output to {@code System.out}.
 * <P>
 * The two sensors are read as independent streams but combined
 * into a single stream and then aggregated across the last 50 readings
 * using windows. The window is partitioned by the sensor name
 * so that each sensor will have its own independent window.
 * This partitioning is automatic so that the same code would
 * work if readings from one hundred different sensors were
 * on the same stream, is it just driven by a key function.
 * <BR>
 * The windows are then aggregated using Apache Common Math
 * provided statistics and the final stream filtered so
 * that it will only contain values when each sensor 
 * is (independently) out of range.
 * </P>
 *
 * @see SimulatedSensors#burstySensor(Topology, String)
 */
public class SensorsAggregates {
	
	/**
	 * Run a topology with two bursty sensors printing them to standard out.
	 */
    public static void main(String[] args) throws Exception {
    	
    	System.out.println("SensorsAggregates: Output will be randomly intermittent, be patient!");

        DirectProvider tp = new DevelopmentProvider();
        
        Topology topology = tp.newTopology("SensorsReadingAggregates");
        
        TStream<JsonObject> sensors = sensorsAB(topology);
        
        sensors.print();

        tp.submit(topology);
    }
    
    /**
     * Create a stream containing two aggregates from two bursty
     * sensors A and B that only produces output when the sensors
     * (independently) are having a burst period out of their normal range.
     * @param topology Topology to add the sub-graph to.
     * @return Stream containing two aggregates from two bursty
     * sensors A and B
     */
    public static TStream<JsonObject> sensorsAB(Topology topology) {
    	
    	// Simulate two sensors, A and B, both randomly bursty
        TStream<JsonObject> sensorA = SimulatedSensors.burstySensor(topology, "A");
        TStream<JsonObject> sensorB = SimulatedSensors.burstySensor(topology, "B");
        
        // Combine the sensor readings into a single stream
        TStream<JsonObject> sensors = sensorA.union(sensorB);
        
        // Create a window on the stream of the last 50 readings partitioned
        // by sensor name. In this case two independent windows are created (for a and b)
        TWindow<JsonObject,JsonElement> sensorWindow = sensors.last(50, j -> j.get("name"));
        
        // Aggregate the windows calculating the min, max, mean and standard deviation
        // across each window independently.
        sensors = JsonAnalytics.aggregate(sensorWindow, "name", "reading", MIN, MAX, MEAN, STDDEV);
        
        // Filter so that only when the sensor is beyond 2.0 (absolute) is a reading sent.
        sensors = sensors.filter(j -> Math.abs(j.get("reading").getAsJsonObject().get("MEAN").getAsDouble()) > 2.0);
        
        return sensors;

    }
}
