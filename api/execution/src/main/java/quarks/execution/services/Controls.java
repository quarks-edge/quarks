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
     * <LI>Method parameters and return types restricted to these types:
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
            if (cim.getReturnType() != Void.TYPE
                    && !validType(cim.getReturnType()))
                return false;
            
            for (Parameter pt : cim.getParameters()) {
                Class<?> ptt = pt.getType();
                if (!validType(ptt))
                    return false;
            }
        }
        
        return true;
    }

    static boolean validType(Class<?> type) {
        if (String.class == type)
            return true;
        if (Boolean.TYPE == type)
            return true;
        if (Integer.TYPE == type)
            return true;
        if (Long.TYPE == type)
            return true;
        if (Double.TYPE == type)
            return true;
        if (type.isEnum())
            return true;
        
        return false;
    }
}
