/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.topology.spi;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import quarks.topology.TStream;
import quarks.topology.spi.AbstractTStream;

public class AbstractTStreamTest {

    @Test
    public void testHierachy() {
        assertTrue(TStream.class.isAssignableFrom(AbstractTStream.class));
    }
}
