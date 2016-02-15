/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.metrics;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import quarks.metrics.oplets.CounterOp;
import quarks.metrics.oplets.RateMeter;
import quarks.oplet.Oplet;
import quarks.oplet.core.AbstractOplet;
import quarks.oplet.core.Peek;

public class MetricsCommonTest {
    @Test
    public void counterOpHierachy() {
        assertTrue(Oplet.class.isAssignableFrom(CounterOp.class));
        assertTrue(AbstractOplet.class.isAssignableFrom(CounterOp.class));
        assertTrue(Peek.class.isAssignableFrom(CounterOp.class));
    }
    
    @Test
    public void rateMeterHierachy() {
        assertTrue(Oplet.class.isAssignableFrom(RateMeter.class));
        assertTrue(AbstractOplet.class.isAssignableFrom(RateMeter.class));
        assertTrue(Peek.class.isAssignableFrom(RateMeter.class));
    }
}
