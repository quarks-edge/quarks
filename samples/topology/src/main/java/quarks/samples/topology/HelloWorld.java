/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016
*/
package quarks.samples.topology;

import quarks.providers.direct.DirectProvider;
import quarks.topology.Topology;

/**
 * Hello World Topology sample.
 *
 */
public class HelloWorld {
	
	/**
	 * Print Hello World as two tuples.
	 */
    public static void main(String[] args) throws Exception {

        DirectProvider tp = new DirectProvider();

        Topology t = tp.newTopology("HelloWorld");

        t.strings("Hello", "World!").print();

        tp.submit(t);
    }
}
