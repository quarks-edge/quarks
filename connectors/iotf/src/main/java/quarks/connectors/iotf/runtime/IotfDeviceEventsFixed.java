/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.iotf.runtime;

import com.google.gson.JsonObject;

import quarks.function.Consumer;

/**
 * Consumer that publishes stream tuples as IoTf device events.
 *
 */
public class IotfDeviceEventsFixed implements Consumer<JsonObject> {
    private static final long serialVersionUID = 1L;
    private final IotfConnector connector;
    private final String eventId;
    private final int qos;

    public IotfDeviceEventsFixed(IotfConnector connector, String eventId, int qos) {
        this.connector = connector;
        this.eventId = eventId;
        this.qos = qos;
    }

    @Override
    public void accept(JsonObject event) {
        connector.publishEvent(eventId, event, qos);
    }
}
