/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors.mqtt;

import static quarks.samples.connectors.mqtt.MqttClient.OPT_PUB_CNT;
import static quarks.samples.connectors.mqtt.MqttClient.OPT_QOS;
import static quarks.samples.connectors.mqtt.MqttClient.OPT_RETAIN;
import static quarks.samples.connectors.mqtt.MqttClient.OPT_TOPIC;

import java.util.concurrent.TimeUnit;

import quarks.connectors.mqtt.MqttConfig;
import quarks.connectors.mqtt.MqttStreams;
import quarks.samples.connectors.MsgSupplier;
import quarks.samples.connectors.Options;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.TopologyProvider;

/**
 * A MQTT publisher topology application.
 */
public class PublisherApp {
    private final TopologyProvider tp;
    private final Options options;

    /**
     * @param tp the TopologyProvider to use.
     * @param options
     */
    PublisherApp(TopologyProvider tp, Options options) {
        this.tp = tp;
        this.options = options;
    }
    
    /**
     * Create a topology for the publisher application.
     */
    public Topology buildAppTopology() {
        Topology t = tp.newTopology("mqttClientPublisher");
        
        // Create a sample stream of tuples to publish
        TStream<String> msgs = t.poll(new MsgSupplier(options.get(OPT_PUB_CNT)),
                                        1L, TimeUnit.SECONDS);

        // Create the MQTT broker connector
        MqttConfig config= Runner.newConfig(options);
        MqttStreams mqtt = new MqttStreams(t, () -> config);
        
        // Publish the stream to the topic.  The String tuple is the message value.
        mqtt.publish(msgs, options.get(OPT_TOPIC), 
                    options.get(OPT_QOS), options.get(OPT_RETAIN));
        
        return t;
    }

}
