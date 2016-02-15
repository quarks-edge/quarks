/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.samples.topology;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import quarks.providers.direct.DirectProvider;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * Periodic polling of source data.
 *
 */
public class PeriodicSource {
	/**
	 * Shows polling a data source to periodically obtain a value.
	 * Polls a random number generator for a new value every second
	 * and then prints out the raw value and a filtered and transformed stream.
	 */
    public static void main(String[] args) throws Exception {

        DirectProvider tp = new DirectProvider();

        Topology t = tp.newTopology("PeriodicSource");

        // Since this is the Direct provider the graph can access
        // objects created while the topology is being defined
        // (in this case the Random object r).
        Random r = new Random();
        TStream<Double> gaussian = t.poll(() -> r.nextGaussian(), 1, TimeUnit.SECONDS);

        // Peek at the value on the Stream printing it to System.out
        gaussian = gaussian.peek(g -> System.out.println("R:" + g));

        // A filter
        gaussian = gaussian.filter(g -> g > 0.5);

        // A transformation
        TStream<String> gs = gaussian.map(g -> "G:" + g + ":");
        gs.print();

        tp.submit(t);
    }
}
