/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors.kafka;

import static quarks.samples.connectors.kafka.KafkaClient.OPT_BOOTSTRAP_SERVERS;
import static quarks.samples.connectors.kafka.KafkaClient.OPT_PUB_CNT;
import static quarks.samples.connectors.kafka.KafkaClient.OPT_TOPIC;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import quarks.connectors.kafka.KafkaProducer;
import quarks.samples.connectors.MsgSupplier;
import quarks.samples.connectors.Options;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.TopologyProvider;

/**
 * A Kafka producer/publisher topology application.
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
        Topology t = tp.newTopology("kafkaClientPublisher");
        
        // Create a sample stream of tuples to publish
        TStream<String> msgs = t.poll(new MsgSupplier(options.get(OPT_PUB_CNT)),
                                        1L, TimeUnit.SECONDS);

        // Create the KafkaProducer broker connector
        Map<String,Object> config = newConfig();
        KafkaProducer kafka = new KafkaProducer(t, () -> config);
        
        // Publish the stream to the topic.  The String tuple is the message value.
        kafka.publish(msgs, options.get(OPT_TOPIC));
        
        return t;
    }
    
    private Map<String,Object> newConfig() {
        Map<String,Object> config = new HashMap<>();
        // required kafka configuration items
        config.put("bootstrap.servers", options.get(OPT_BOOTSTRAP_SERVERS));
        return config;
    }

}
