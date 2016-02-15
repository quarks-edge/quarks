/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
*/
package quarks.execution.services;

/**
 * At runtime a container provides services to
 * executing elements such as oplets and functions.
 *
 */
public interface RuntimeServices {
    
    /**
     * Get a service for this invocation.
     * <P>
     * These services must be provided by all implementations:
     * <UL>
     * <LI>
     * {@code java.util.concurrent.ThreadFactory} - Thread factory, runtime code should
     * create new threads using this factory.
     * </LI>
     * <LI>
     * {@code java.util.concurrent.ScheduledExecutorService} - Scheduler, runtime code should
     * execute asynchronous and repeating tasks using this scheduler. 
     * </LI>
     * </UL>
     * </P>
     * 
     * 
     * @param serviceClass Type of the service required.
     * @return Service of type implementing {@code serviceClass} if the 
     *      container this invocation runs in supports that service, 
     *      otherwise {@code null}.
     */
    <T> T getService(Class<T> serviceClass);
}
