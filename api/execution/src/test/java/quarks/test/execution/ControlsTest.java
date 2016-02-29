/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.execution;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static quarks.execution.services.Controls.isControlServiceMBean;

import java.io.DataInput;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

public class ControlsTest {
    
    @Test
    public void testNotValid() {
        
        // Not an interface
        assertFalse(isControlServiceMBean(Object.class));
        
        // List extends anther
        assertFalse(isControlServiceMBean(List.class));
        
        // Is parameterized
        assertFalse(isControlServiceMBean(Callable.class));
        
        // Methods with unsupported signatures.
        assertFalse(isControlServiceMBean(DataInput.class));  
    }
    @Test
    public void testValid() {
        assertTrue(isControlServiceMBean(Runnable.class));
    }
}
