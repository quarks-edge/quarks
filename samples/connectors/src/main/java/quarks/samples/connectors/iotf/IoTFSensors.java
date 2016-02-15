/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.samples.connectors.iotf;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;

import quarks.connectors.iot.IotDevice;
import quarks.connectors.iot.QoS;
import quarks.connectors.iotf.IotfDevice;
import quarks.providers.direct.DirectProvider;
import quarks.providers.direct.DirectTopology;
import quarks.samples.topology.SensorsAggregates;
import quarks.topology.TStream;

/**
 * Sample sending sensor device events to IoTF. <BR>
 * Simulates a couple of bursty sensors and sends the readings from the sensors
 * to IoTF as device with id {@code sensors}. <BR>
 * Subscribes to device commands with identifier {@code display}.
 * <P>
 * This sample requires an IBM Watson IoT Platform service and a device configuration.
 * The device configuration is read from the file {@code device.cfg} in the
 * current directory. <BR>
 * In order to see commands send from IoTF there must be an analytic application
 * that sends commands with the identifier {@code display}.
 * </P>
 */
public class IoTFSensors {

    public static void main(String[] args) {
        
        String deviceCfg = args[0];

        DirectProvider tp = new DirectProvider();
        DirectTopology topology = tp.newTopology("IoTFSensors");

        // Declare a connection to IoTF
        IotDevice device = new IotfDevice(topology, new File(deviceCfg));

        // Simulated sensors for this device.
        simulatedSensors(device, true);
        
        // In addition create a heart beat event to
        // ensure there is some immediate output and
        // the connection to IoTF happens as soon as possible.
        TStream<Date> hb = topology.poll(() -> new Date(), 1, TimeUnit.MINUTES);
        // Convert to JSON
        TStream<JsonObject> hbj = hb.map(d -> {
            JsonObject j = new  JsonObject();
            j.addProperty("when", d.toString());
            j.addProperty("hearbeat", d.getTime());
            return j;
        });
        hbj.print();
        device.events(hbj, "heartbeat", QoS.FIRE_AND_FORGET);

        // Subscribe to commands of id "display" for this
        // device and print them to standard out
        TStream<String> statusMsgs = displayMessages(device);
        statusMsgs.print();

        tp.submit(topology);
    }

    /**
     * Simulate two bursty sensors and send the readings as IoTF device events
     * with an identifier of {@code sensors}.
     * 
     * @param device
     *            IoTF device
     * @param print
     *            True if the data submitted as events should also be printed to
     *            standard out.
     */
    public static void simulatedSensors(IotDevice device, boolean print) {

        TStream<JsonObject> sensors = SensorsAggregates.sensorsAB(device.topology());
        if (print)
            sensors.print();

        // Send the device streams as IoTF device events
        // with event identifier "sensors".
        device.events(sensors, "sensors", QoS.FIRE_AND_FORGET);
    }

    /**
     * Subscribe to IoTF device commands with identifier {@code display}.
     * Subscribing to device commands returns a stream of JSON objects that
     * include a timestamp ({@code tsms}), command identifier ({@code command})
     * and payload ({@code payload}). Payload is the application specific
     * portion of the command. <BR>
     * In this case the payload is expected to be a JSON object containing a
     * {@code msg} key with a string display message. <BR>
     * The returned stream consists of the display message string extracted from
     * the JSON payload.
     * <P>
     * Note to receive commands a analytic application must exist that generates
     * them through IBM Watson IoT Platform.
     * </P>
     * 
     * @see IotDevice#commands(String...)
     */
    public static TStream<String> displayMessages(IotDevice device) {
        // Subscribe to commands of id "status" for this device
        TStream<JsonObject> statusMsgs = device.commands("display");

        // The returned JSON object includes several fields
        // tsms - Timestamp in milliseconds (this is generic to a command)
        // payload.msg - Status message (this is specific to this application)

        // Map to a String object containing the message
        return statusMsgs.map(j -> j.getAsJsonObject("payload").getAsJsonPrimitive("msg").getAsString());
    }
}
