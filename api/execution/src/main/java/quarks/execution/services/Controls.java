package quarks.execution.services;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Utilities for the control service.
 *
 * @see ControlService
 */
public class Controls {
    
    /**
     * Test to see if an interface represents a valid
     * control service MBean.
     * All implementations of {@code ControlService}
     * must support control MBeans for which this
     * method returns true.
     * <BR>
     * An interface is a valid control service MBean if
     * all of the following are true:
     * <UL>
     * <LI>An interface that does not extend any other interface.</LI>
     * <LI>Not be parameterized</LI>
     * <LI>All methods must be void</LI>
     * <LI>Method parameters restricted to thise types:
     * <UL>
     * <LI>{@code String, boolean, int, long, double}.</LI>
     * <LI>Any enumeration</LI>
     * </UL> 
     * </UL>
     * @param controlInterface
     * @return True
     */
    public static boolean isControlServiceMBean(Class<?> controlInterface) {

        if (!controlInterface.isInterface())
            return false;
        
        if (controlInterface.getInterfaces().length != 0)
            return false;
        
        if (controlInterface.getTypeParameters().length != 0)
            return false;
        
        for (Method cim : controlInterface.getDeclaredMethods()) {
            if (!Void.TYPE.equals(cim.getReturnType()))
                return false;
            
            for (Parameter pt : cim.getParameters()) {
                Class<?> ptt = pt.getType();
                if (String.class == ptt)
                    continue;
                if (Boolean.TYPE == ptt)
                    continue;
                if (Integer.TYPE == ptt)
                    continue;
                if (Long.TYPE == ptt)
                    continue;
                if (Double.TYPE == ptt)
                    continue;
                if (ptt.isEnum())
                    continue;
            }
        }
        
        return true;
    }
}
