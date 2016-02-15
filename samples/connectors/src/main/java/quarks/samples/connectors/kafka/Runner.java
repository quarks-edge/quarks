/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors.kafka;

import static quarks.samples.connectors.kafka.KafkaClient.OPT_BOOTSTRAP_SERVERS;
import static quarks.samples.connectors.kafka.KafkaClient.OPT_PUB;
import static quarks.samples.connectors.kafka.KafkaClient.OPT_TOPIC;
import static quarks.samples.connectors.kafka.KafkaClient.OPT_ZOOKEEPER_CONNECT;

import quarks.console.server.HttpServer;
import quarks.providers.development.DevelopmentProvider;
import quarks.samples.connectors.Options;
import quarks.topology.Topology;

/**
 * Build and run the publisher or subscriber application.
 */
public class Runner {
    /**
     * Build and run the publisher or subscriber application.
     * @throws Exception
     */
    public static void run(Options options) throws Exception {
        boolean isPub = options.get(OPT_PUB); 

        // Get a topology runtime provider
        DevelopmentProvider tp = new DevelopmentProvider();

        Topology top;
        if (isPub) {
            PublisherApp publisher = new PublisherApp(tp, options);
            top = publisher.buildAppTopology();
        }
        else {
            SubscriberApp subscriber = new SubscriberApp(tp, options);
            top = subscriber.buildAppTopology();
        }
        
        // Submit the app/topology; send or receive the messages.
        System.out.println(
                "Using Kafka cluster at bootstrap.servers="
                + options.get(OPT_BOOTSTRAP_SERVERS)
                + " zookeeper.connect=" + options.get(OPT_ZOOKEEPER_CONNECT)
                + "\n" + (isPub ? "Publishing" : "Subscribing") 
                + " to topic " + options.get(OPT_TOPIC));
        System.out.println("Console URL for the job: "
                + tp.getServices().getService(HttpServer.class).getConsoleUrl());
        tp.submit(top);
    }

}
