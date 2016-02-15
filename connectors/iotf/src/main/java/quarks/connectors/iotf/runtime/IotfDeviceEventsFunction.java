/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.iotf.runtime;

import com.google.gson.JsonObject;

import quarks.function.Consumer;
import quarks.function.Function;
import quarks.function.UnaryOperator;

/**
 * Consumer that publishes stream tuples as IoTf device events with a fixed
 * event identifier and qos.
 *
 */
public class IotfDeviceEventsFunction implements Consumer<JsonObject> {

    private static final long serialVersionUID = 1L;
    private final IotfConnector connector;
    private final Function<JsonObject, String> eventId;
    private UnaryOperator<JsonObject> payload;
    private final Function<JsonObject, Integer> qos;

    public IotfDeviceEventsFunction(IotfConnector connector, Function<JsonObject, String> eventId,
            UnaryOperator<JsonObject> payload,
            Function<JsonObject, Integer> qos) {
        this.connector = connector;
        this.payload = payload;
        this.eventId = eventId;
        this.qos = qos;
    }

    @Override
    public void accept(JsonObject event) {
        connector.publishEvent(eventId.apply(event), payload.apply(event), qos.apply(event));
    }
}
