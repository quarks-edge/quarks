/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.connectors.iot;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import quarks.connectors.iot.QoS;

public class QoSTest {
    
    @Test
    public void testConstants() {
        assertEquals(Integer.valueOf(0), QoS.AT_MOST_ONCE);
        assertEquals(Integer.valueOf(0), QoS.FIRE_AND_FORGET);
        assertEquals(Integer.valueOf(1), QoS.AT_LEAST_ONCE);
        assertEquals(Integer.valueOf(2), QoS.EXACTLY_ONCE);
    }
}
