/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.oplet.functional;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import quarks.oplet.core.Pipe;
import quarks.oplet.functional.Filter;

public class FilterTest {

    @Test
    public void testHierachy() {
        assertTrue(Pipe.class.isAssignableFrom(Filter.class));
    }
}
