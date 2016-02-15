/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.oplet.operators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import quarks.function.Consumer;
import quarks.oplet.Oplet;
import quarks.oplet.core.AbstractOplet;
import quarks.oplet.core.Pipe;

public class PipeTest {

    @Test
    public void testHierachy() {
        assertTrue(Oplet.class.isAssignableFrom(Pipe.class));
        assertTrue(AbstractOplet.class.isAssignableFrom(Pipe.class));
    }

    @Test
    public void testDestination() {
        Pipe<String, Integer> pipe = new Pipe<String, Integer>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void accept(String tuple) {
            }

            @Override
            public void close() throws Exception {
            };
        };

        List<? extends Consumer<String>> inputs = pipe.getInputs();
        assertEquals(1, inputs.size());
        assertSame(pipe, inputs.get(0));
    }
}
