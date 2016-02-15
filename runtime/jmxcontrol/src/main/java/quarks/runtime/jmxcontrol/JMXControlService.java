/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.runtime.jmxcontrol;

import java.lang.management.ManagementFactory;
import java.util.Hashtable;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import quarks.execution.services.ControlService;

/**
 * Control service that registers control objects
 * as MBeans in a JMX server.
 *
 */
public class JMXControlService implements ControlService {
	
	private final MBeanServer mbs;
	private final String domain;
	private final Hashtable<String,String> additionalKeys;
	
	/**
	 * JMX control service using the platform MBean server.
	 * @param domain Domain the MBeans are registered in.
	 */
	public JMXControlService(String domain, Hashtable<String,String> additionalKeys) {
		mbs = ManagementFactory.getPlatformMBeanServer();
		this.domain = domain;
		this.additionalKeys = additionalKeys;
	}
	
	
	/**
	 * Get the MBean server being used by this control service.
	 * @return MBean server being used by this control service.
	 */
	public MBeanServer getMbs() {
		return mbs;
	}
	
	/**
     * Get the JMX domain being used by this control service.
     * @return JMX domain being used by this control service.
     */
	public String getDomain() {
        return domain;
    }

	/**
	 * 
	 * Register a control object as an MBean.
	 * 
	 * {@inheritDoc}
	 * 
	 * The MBean is registered within the domain returned by {@link #getDomain()}
	 * and an `ObjectName` with these keys:
	 * <UL>
	 * <LI>type</LI> {@code type}
	 * <LI>interface</LI> {@code controlInterface.getName()}
	 * <LI>id</LI> {@code type}
	 * <LI>alias</LI> {@code alias}
	 * </UL>
	 * 
	 */
	@Override
	public <T> String registerControl(String type, String id, String alias, Class<T> controlInterface, T control) {
		Hashtable<String,String> table = new Hashtable<>();
		
		table.put("type", ObjectName.quote(type));
		table.put("interface", ObjectName.quote(controlInterface.getName()));
		table.put("id", ObjectName.quote(id));
		if (alias != null)
		   table.put("alias", ObjectName.quote(alias));
		
		additionalNameKeys(table);
			
        try {
            ObjectName on = ObjectName.getInstance(getDomain(), table);
            getMbs().registerMBean(control, on);

            return on.getCanonicalName();
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException
                | MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }

	}
	
	protected void additionalNameKeys(Hashtable<String,String> table) {
	    table.putAll(additionalKeys);
	}
	
	@Override
	public void unregister(String controlId) {
		try {
            mbs.unregisterMBean(ObjectName.getInstance(controlId));
        } catch (MBeanRegistrationException | InstanceNotFoundException | MalformedObjectNameException
                | NullPointerException e) {
            throw new RuntimeException(e);
        }
	}
}
