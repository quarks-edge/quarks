/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.iotf;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.device.Command;

import quarks.connectors.iot.IotDevice;
import quarks.connectors.iot.QoS;
import quarks.connectors.iotf.runtime.IotfConnector;
import quarks.connectors.iotf.runtime.IotfDeviceCommands;
import quarks.connectors.iotf.runtime.IotfDeviceEventsFixed;
import quarks.connectors.iotf.runtime.IotfDeviceEventsFunction;
import quarks.function.Function;
import quarks.function.UnaryOperator;
import quarks.topology.TSink;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * Connector for IBM Watson IoT Platform.
 * <BR>
 * <em>Note IBM Watson IoT Platform was previously known as Internet of Things Foundation.</em>
 */
public class IotfDevice implements IotDevice {
    
    public static final String QUICKSTART_DEVICE_TYPE = "iotsamples-quarks";

    private final IotfConnector connector;
    private final Topology topology;
    private TStream<Command> commandStream;

    /**
     * Create a connector to the IBM Watson IoT Platform Bluemix service with the device
     * specified by {@code options}.
     * <p>
     * Connecting to the server occurs when the topology is submitted for
     * execution.
     * 
     * @param topology
     *            the connector's associated {@code Topology}.
     */
    public IotfDevice(Topology topology, Properties options) {
        this.topology = topology;
        this.connector = new IotfConnector(options);
    }

    public IotfDevice(Topology topology, File optionsFile) {
        this.topology = topology;
        this.connector = new IotfConnector(optionsFile);
    }
    
    public static IotfDevice quickstart(Topology topology, String deviceId) {
        Properties options = new Properties();
        options.setProperty("org", "quickstart");
        options.setProperty("type", QUICKSTART_DEVICE_TYPE);
        options.setProperty("id", deviceId);
        return new IotfDevice(topology, options);
    }

    /**
     * Publish a stream's tuples as device events.
     * <p>
     * Each tuple is published as a device event with the supplied functions
     * providing the event identifier, payload and QoS from the tuple.
     * The event identifier and {@link QoS Quality of Service}
     * can be generated based upon the tuple.
     * 
     * @param stream
     *            Stream to be published.
     * @param eventId
     *            function to supply the event identifier.
     * @param payload
     *            function to supply the event's payload.
     * @param qos
     *            function to supply the event's delivery {@link QoS Quality of Service}.
     * @return TSink sink element representing termination of this stream.
     * 
     * @see QoS
     */
    public TSink<JsonObject> events(TStream<JsonObject> stream, Function<JsonObject, String> eventId,
            UnaryOperator<JsonObject> payload,
            Function<JsonObject, Integer> qos) {
        return stream.sink(new IotfDeviceEventsFunction(connector, eventId, payload, qos));
    }
    
    /**
     * Publish a stream's tuples as device events.
     * <p>
     * Each tuple is published as a device event with fixed event identifier and
     * QoS.
     * 
     * @param stream
     *            Stream to be published.
     * @param eventId
     *            Event identifier.
     * @param qos
     *            Event's delivery {@link QoS Quality of Service}.
     * @return TSink sink element representing termination of this stream.
     * 
     * @see QoS
     */
    public TSink<JsonObject> events(TStream<JsonObject> stream, String eventId, int qos) {
        return stream.sink(new IotfDeviceEventsFixed(connector, eventId, qos));
    }

    /**
     * Create a stream of device commands as JSON objects.
     * Each command sent to the device matching {@code commands} will result in a tuple
     * on the stream. The JSON object has these keys:
     * <UL>
     * <LI>{@code command} - Command identifier as a String</LI>
     * <LI>{@code tsms} - IoTF Timestamp of the command in milliseconds since the 1970/1/1 epoch.</LI>
     * <LI>{@code format} - Format of the command as a String</LI>
     * <LI>{@code payload} - Payload of the command</LI>
     * <UL>
     * <LI>If {@code format} is {@code json} then {@code payload} is JSON</LI>
     * <LI>Otherwise {@code payload} is String
     * </UL>
     * </UL>
     * 
     * 
     * @param commands Commands to include. If no commands are provided then the
     * stream will contain all device commands.
     * @return Stream containing device commands.
     */
    public TStream<JsonObject> commands(String... commands) {
        TStream<Command> all = allCommands();
        
        if (commands.length != 0) {
            Set<String> uniqueCommands = new HashSet<>();
            uniqueCommands.addAll(Arrays.asList(commands));
            all = all.filter(cmd -> uniqueCommands.contains(cmd.getCommand()));
        }

        return all.map(cmd -> {
            JsonObject full = new JsonObject();
            full.addProperty("command", cmd.getCommand());
            full.addProperty("tsms", cmd.getTimestamp().getMillis());
            full.addProperty("format", cmd.getFormat());
            if ("json".equals(cmd.getFormat())) {
                JsonParser parser = new JsonParser();
                JsonObject jsonPayload = (JsonObject) parser.parse(cmd.getPayload());
                full.add("payload", jsonPayload);
            } else {
                full.addProperty("payload", cmd.getPayload());
            }
            return full;
            
        });
    }
    
    private TStream<Command> allCommands() {
        if (commandStream == null)
            commandStream = topology.events(new IotfDeviceCommands(connector));
        return commandStream;
    }

    @Override
    public Topology topology() {
        return topology;
    }
}
