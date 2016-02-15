/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.console.servlets;

import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

final class JobUtil {
	
	static MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

	static String getJobsInfo(ObjectName jobObjName) {
        Set<ObjectInstance> jobInstances = mBeanServer.queryMBeans(jobObjName, null);
        
        Iterator<ObjectInstance> jobIterator = jobInstances.iterator();
        StringBuffer json = new StringBuffer("[");
        int counter = 0;
        while (jobIterator.hasNext()) {
        	if (counter > 0) {
        		json.append(",");
        	}
        	ObjectInstance jobInstance = jobIterator.next();
            ObjectName jobObjectName = jobInstance.getObjectName();
            MBeanInfo mBeanInfo = null;
			try {
				mBeanInfo = mBeanServer.getMBeanInfo(jobObjectName);
			} catch (IntrospectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstanceNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            /*
             * Get the names of all the attributes
             */
			            
			Set<String> names = new HashSet<String> ();
	    	for (MBeanAttributeInfo attributeInfo : mBeanInfo.getAttributes()) {
	    			names.add(attributeInfo.getName());
	    	}
	    	// now construct the job json and add it to the string buffer
	    	StringBuffer s = new StringBuffer();
	    	s.append("{\"");
	    	Iterator<String> it = names.iterator();
	    	while(it.hasNext()) {
	    		String attr = it.next();
	    		s.append(attr);
	    		s.append("\":\"");
	    		try {
					s.append((String)mBeanServer.getAttribute(jobObjectName, attr));
				} catch (AttributeNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstanceNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MBeanException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ReflectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		s.append("\",\"");
	    	}
	    	// remove the trailing ,\
	    	s.deleteCharAt(s.length()-1);
	    	s.deleteCharAt(s.length()-1);
	    	json.append(s.toString() + "}");
	    	counter++;
        }
        json.append("]");
        String jsonString = json.toString();
		
		return jsonString;
	}
	
	static String getJobGraph(ObjectName jobObjName) {
        Set<ObjectInstance> jobInstances = mBeanServer.queryMBeans(jobObjName, null);
        Iterator<ObjectInstance> jobIterator = jobInstances.iterator();
        ObjectInstance jobInstance = null;

        if(jobIterator.hasNext()) {
        	jobInstance = jobIterator.next();
        }
        String gSnapshot = "";
        if (jobInstance != null) {
            ObjectName jobObjectName = jobInstance.getObjectName();
            MBeanInfo mBeanInfo = null;
			try {
				mBeanInfo = mBeanServer.getMBeanInfo(jobObjectName);
			} catch (IntrospectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstanceNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	/*
	    	 * Now get the graph for the job
	    	 */
	    	Set<String> operations = new HashSet<String> ();
	    	for (MBeanOperationInfo operationInfo: mBeanInfo.getOperations()) {
	    		operations.add(operationInfo.getName());
	    		if (operationInfo.getName().equals("graphSnapshot")) {
	    			try {
						gSnapshot = (String) mBeanServer.invoke(jobObjectName, "graphSnapshot",null, null);
						//System.out.println(gSnapshot);
					} catch (InstanceNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ReflectionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MBeanException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
	    		
	    	}
	}
		return gSnapshot;
	}
	
}
