/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.samples.utils.metrics;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import quarks.console.server.HttpServer;
import quarks.metrics.Metrics;
import quarks.providers.development.DevelopmentProvider;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * Instruments a topology with a tuple counter on a specified stream.
 */
public class SplitWithMetrics {

    public static void main(String[] args) throws Exception {
        DevelopmentProvider dtp = new DevelopmentProvider();
        
        Topology t = dtp.newTopology(SplitWithMetrics.class.getSimpleName());
        
        Random r = new Random();
        
        TStream<Integer> d  = t.poll(() -> (int)(r.nextGaussian() * 3.0), 
                100, TimeUnit.MILLISECONDS);

        List<TStream<Integer>> splits = d.split(3, tuple -> {
            switch (tuple.intValue()) {
            case 0:
                return 0;
            case 1:
                return 1;
            default:
                return 2;
            }
        });

        /* 
         * Insert a metric counter for the zeroes stream.  Note that the 
         * DevelopmentProvider submitter will insert metric counters at 
         * submit time on the output of each oplet, including the counter
         * explicitly inserted below.
         */
        Metrics.counter(splits.get(0)).sink(tuple -> System.out.print("."));

        splits.get(1).sink(tuple -> System.out.print("#"));
        splits.get(2).sink(tuple -> System.out.print("@"));
        
        dtp.submit(t);
        System.out.println(dtp.getServices().getService(HttpServer.class).getConsoleUrl());
    }
}
