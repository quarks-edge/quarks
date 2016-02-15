/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.mqtt.runtime;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;

import quarks.function.Consumer;
import quarks.function.Function;

/**
 * Consumer that publishes stream tuples of type {@code T} to an MQTT server topic.
 *
 * @param <T> stream tuple type
 */
public class MqttPublisher<T> implements Consumer<T>, AutoCloseable {
    private static final long serialVersionUID = 1L;
    private final Logger logger;
    private transient String id;
    private final MqttConnector connector;
    private final Function<T, byte[]> payload;
    private final Function<T, String> topic;
    private final Function<T, Integer> qos;
    private final Function<T, Boolean> retain;

    public MqttPublisher(MqttConnector connector, Function<T, byte[]> payload, Function<T, String> topic,
            Function<T, Integer> qos, Function<T, Boolean> retain) {
        this.logger = connector.getLogger();
        this.connector = connector;
        this.payload = payload;
        this.topic = topic;
        this.qos = qos;
        this.retain = retain;
    }

    @Override
    public void accept(T t) {
        // right now, the caller of accept() doesn't do anything to
        // log or tolerate an unwind. address those issues here.
        String topicStr = topic.apply(t);
        try {
            MqttMessage message = new MqttMessage(payload.apply(t));
            message.setQos(qos.apply(t));
            message.setRetained(retain.apply(t));
            logger.trace("{} sending to topic:{}", id(), topicStr);
            connector.notIdle();
            connector.client().publish(topicStr, message);
        } catch (Exception e) {
            logger.error("{} sending to topic:{} failed.", id(), topicStr, e);
        }
    }
    
    protected String id() {
        if (id == null) {
            // use our short object Id
            id = connector.id() + " publisher " + toString().substring(toString().indexOf('@') + 1);
        }
        return id;
    }

    @Override
    public void close() throws Exception {
        connector.close();
    }
}
