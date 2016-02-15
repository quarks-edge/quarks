/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.test.runtime.jmxcontrol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.Hashtable;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.timer.Timer;
import javax.management.timer.TimerMBean;

import org.junit.Test;

import quarks.execution.services.ControlService;
import quarks.runtime.jmxcontrol.JMXControlService;

public class JMXControlServiceTest {
    
    private static final String DOMAIN = JMXControlServiceTest.class.getName();

    @Test
    public void testControlObject() throws Exception {
        
        ControlService cs = new JMXControlService(DOMAIN, new Hashtable<>());
        
        String type = "timer";
        String id = "a";
        String alias = "ControlA";
        String controlId = cs.registerControl(type, id, alias, TimerMBean.class, new Timer());
        
        assertNotNull(controlId);
        
        ObjectName on = ObjectName.getInstance(controlId);
        
        assertEquals(DOMAIN, on.getDomain());
        
        assertEquals(type, ObjectName.unquote(on.getKeyProperty("type")));
        assertEquals(id, ObjectName.unquote(on.getKeyProperty("id")));
        assertEquals(alias, ObjectName.unquote(on.getKeyProperty("alias")));
        assertEquals(TimerMBean.class.getName(), ObjectName.unquote(on.getKeyProperty("interface")));
        
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        
        assertTrue(mbs.isRegistered(on));
        
        cs.unregister(controlId);
        assertFalse(mbs.isRegistered(on));  
    }
    
    @Test
    public void testAdditionalKeys() throws Exception {
        
        Hashtable<String,String> addKeys = new Hashtable<>();
        addKeys.put("job", "jobid");
        addKeys.put("device", ObjectName.quote("pi"));
        
        ControlService cs = new JMXControlService(DOMAIN, addKeys);
        
        String type = "timer";
        String id = "a";
        String alias = "ControlA";
        String controlId = cs.registerControl(type, id, alias, TimerMBean.class, new Timer());
        
        assertNotNull(controlId);
        
        ObjectName on = ObjectName.getInstance(controlId);
        
        assertEquals(DOMAIN, on.getDomain());
        
        assertEquals(type, ObjectName.unquote(on.getKeyProperty("type")));
        assertEquals(id, ObjectName.unquote(on.getKeyProperty("id")));
        assertEquals(alias, ObjectName.unquote(on.getKeyProperty("alias")));
        assertEquals(TimerMBean.class.getName(), ObjectName.unquote(on.getKeyProperty("interface")));
        
        assertEquals("jobid", on.getKeyProperty("job"));
        assertEquals("pi", ObjectName.unquote(on.getKeyProperty("device")));
 
        
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        
        assertTrue(mbs.isRegistered(on));
        
        cs.unregister(controlId);
        assertFalse(mbs.isRegistered(on));  
    }
    
    @Test(expected=RuntimeException.class)
    public void testDoubleRegister() throws Exception {
        
        ControlService cs = new JMXControlService(DOMAIN, new Hashtable<>());
        
        String type = "timer";
        String id = "a";
        String alias = "ControlA";
        String controlId = cs.registerControl(type, id, alias, TimerMBean.class, new Timer());
        try {
            cs.registerControl(type, id, alias, TimerMBean.class, new Timer());
        } finally {
            cs.unregister(controlId);
        }
    }
    @Test(expected=RuntimeException.class)
    public void testDoubleunregister() throws Exception {
        
        ControlService cs = new JMXControlService(DOMAIN, new Hashtable<>());
        
        String type = "timer";
        String id = "a";
        String alias = "ControlA";
        String controlId = cs.registerControl(type, id, alias, TimerMBean.class, new Timer());
        cs.unregister(controlId);
        cs.unregister(controlId);
    }
}
