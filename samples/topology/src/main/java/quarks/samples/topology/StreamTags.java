/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.samples.topology;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import quarks.console.server.HttpServer;
import quarks.providers.development.DevelopmentProvider;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * Illustrates tagging TStreams with string labels.
 */
public class StreamTags {
    public static void main(String[] args) throws Exception {
        DevelopmentProvider dtp = new DevelopmentProvider();
        
        Topology t = dtp.newTopology("StreamTags");
        
        // Tag the source stream with 
        Random r = new Random();
        TStream<Double> d  = t.poll(() -> (r.nextDouble() * 3), 
                100, TimeUnit.MILLISECONDS).tag("dots", "hashes", "ats");

        List<TStream<Double>> splits = d.split(3, tuple -> {
            switch (tuple.intValue()) {
            case 0:
                return 0;
            case 1:
                return 1;
            default:
                return 2;
            }
        });

        splits.get(0).tag("dots").sink(tuple -> System.out.print("."));
        splits.get(1).tag("hashes").sink(tuple -> System.out.print("#"));
        splits.get(2).tag("ats").sink(tuple -> System.out.print("@"));
        
        dtp.submit(t);
        
        System.out.println(dtp.getServices().getService(HttpServer.class).getConsoleUrl());
    }
}
