/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.window;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import quarks.function.Supplier;
import quarks.window.PartitionedState;


public class StateTest {
    
    /**
     * Test PartitionedState with immutable state.
     */
    @Test
    public void partitionedImmutableStateTest() {
        
        TestState<Integer> state = new TestState<>(() -> 73);
 
        assertEquals(73, state.getState("A").intValue());
        assertEquals(73, state.getState("B").intValue());
        
        assertEquals(73, state.removeState("A").intValue());
        // and it reverts back to the initial value.
        assertEquals(73, state.getState("A").intValue());
        
        assertEquals(73, state.setState("B", 102).intValue());
        assertEquals(102, state.getState("B").intValue());
        
        assertEquals(73, state.getState("A").intValue());
    }
    
    /**
     * Test PartitionedState with mutable state, basically
     * checking that the state does not get confused.
     */
    @Test
    public void partitionedMutableStateTest() {
        
        TestState<int[]> state = new TestState<>(() -> new int[1]);
 
        assertEquals(0, state.getState("A")[0]);
        assertEquals(0, state.getState("B")[0]);
        
        // change A, must not change B
        state.getState("A")[0] = 73;
        assertEquals(73, state.getState("A")[0]);
        assertEquals(0, state.getState("B")[0]);
        
        // change B, must not change A
        state.getState("B")[0] = 102;
        assertEquals(73, state.getState("A")[0]);
        assertEquals(102, state.getState("B")[0]);
        
        assertEquals(73, state.removeState("A")[0]);
        assertEquals(0, state.getState("A")[0]);
        
        int[] newB = new int[1];
        newB[0] = 9214;
        assertEquals(102, state.setState("B", newB)[0]);
        assertEquals(9214, state.getState("B")[0]);
    }
    
    
    private static class TestState<S> extends PartitionedState<String, S> {

        protected TestState(Supplier<S> initialState) {
            super(initialState);
        }
        @Override
        public S getState(String key) {
            return super.getState(key);
        }
        @Override
        public S removeState(String key) {
            return super.removeState(key);
        }
        @Override
        public S setState(String key, S state) {
            return super.setState(key, state);
        } 
    }
}
