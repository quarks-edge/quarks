/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.samples.connectors.iotf;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;

import quarks.connectors.iot.IotDevice;
import quarks.connectors.iot.QoS;
import quarks.connectors.iotf.IotfDevice;
import quarks.providers.direct.DirectProvider;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * IBM Watson IoT Platform Quickstart sample.
 * Submits a JSON device event every second using the
 * same format as the IoTF device simulator,
 * with keys {@code temp}, {@code humidity}  and {@code objectTemp}
 * and random values.
 * 
 * The device type is {@code iotsamples-quarks} and a random
 * device identifier is generated. Both are printed out when
 * the application starts.
 */
public class IotfQuickstart {

    public static void main(String[] args) {

        DirectProvider tp = new DirectProvider();
        Topology topology = tp.newTopology("IotfQuickstart");
        
        // Declare a connection to IoTF Quickstart service
        String deviceId = "qs" + Long.toHexString(new Random().nextLong());
        IotDevice device = IotfDevice.quickstart(topology, deviceId);
        
        System.out.println("Quickstart device type:" + IotfDevice.QUICKSTART_DEVICE_TYPE);
        System.out.println("Quickstart device id  :" + deviceId);
             
        Random r = new Random();
        TStream<double[]> raw = topology.poll(() -> {
            double[]  v = new double[3];
            
            v[0] = r.nextGaussian() * 10.0 + 40.0;
            v[1] = r.nextGaussian() * 10.0 + 50.0;
            v[2] = r.nextGaussian() * 10.0 + 60.0;
            
            return v;
        }, 1, TimeUnit.SECONDS);
        
        TStream<JsonObject> json = raw.map(v -> {
            JsonObject j = new JsonObject();
            j.addProperty("temp", v[0]);
            j.addProperty("humidity", v[1]);
            j.addProperty("objectTemp", v[2]);
            return j;
        });
        
        device.events(json, "sensors", QoS.FIRE_AND_FORGET);
        

        tp.submit(topology);
    }
}
