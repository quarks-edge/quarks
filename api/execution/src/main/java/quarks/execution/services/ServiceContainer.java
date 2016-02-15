/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.execution.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import quarks.execution.Job;
import quarks.function.BiConsumer;

/**
 * Provides a container for services.
 * <p>
 * The current implementation does not downcast the provided service class
 * when searching for an appropriate service implementation.  For example:
 * <pre>
 *   class Derived extends Base {}
 * 
 *   serviceContainer.add(Derived.class, instanceOfDerived);
 * 
 *   // Searching for the base class returns null
 *   assert(null == serviceContainer.get(Base.class, instanceOfDerived));
 * </pre>
 */
public class ServiceContainer {
    private final Map<Class<?>, Object> services;
    private final Set<BiConsumer<String,String>> cleaners;

    public ServiceContainer() {
        this.services = Collections.synchronizedMap(new HashMap<>());
        cleaners = Collections.synchronizedSet(new HashSet<>());
    }

    /**
     * Adds the specified service to this {@code ServiceContainer}.
     * <p>
     * Associates the specified service with the service class.  If the 
     * container has previously contained a service for the class, 
     * the old value is replaced by the specified value.
     *
     * @param serviceClass the class of service to add.
     * @param service service to add to this container.
     * @return the previous value associated with {@code serviceClass}, or
     *         {@code null} if there was no registered mapping.
     * @throws NullPointerException if the specified service class or 
     *         service is null.
     */
    public <T> T addService(Class<T> serviceClass, T service) {
        return serviceClass.cast(services.put(serviceClass, service));
    }

    /**
     * Removes the specified service from this {@code ServiceContainer}.
     *
     * @param serviceClass the class of service to remove.
     * @return the service previously associated with the service class, or
     *         {@code null} if there was no registered service.
     * @throws NullPointerException if the specified service class is null.
     */
    public <T> T removeService(Class<T> serviceClass) {
        return serviceClass.cast(services.remove(serviceClass));
    }

    /**
     * Returns the service to which the specified service class key is
     * mapped, or {@code null} if this {@code ServiceContainer} contains no 
     * service for that key.
     * 
     * @param serviceClass the class whose associated service is to be returned
     * @return the service instance mapped to the specified service class, or
     *         {@code null} if no service is registered for the class.
     */
    public <T> T getService(Class<T> serviceClass) {
        return serviceClass.cast(services.get(serviceClass));
    }
    
    /**
     * Registers a new cleaner.
     * <p>
     * A cleaner is a hook which is invoked when the runtime
     * closes an element of {@link Job}.
     * When an element of job is closed {@code cleaner.accept(jobId, elementId)}
     * is called where {@code jobId} is the {@link Job#getId() job identifier} and
     * {@code elementId} is the unique identifier for the element.
     * 
     * @param cleaner The cleaner.
     */
    public void addCleaner(BiConsumer<String,String> cleaner) {
        cleaners.add(cleaner);
    }
    
    /**
     * Invokes all the registered cleaners in the context of the specified 
     * job and element.
     * 
     * @param jobId The job identifier.
     * @param elementId The element identifier.
     */
    public void cleanOplet(String jobId, String elementId) {
        for (BiConsumer<String,String> cleaner : cleaners)
            cleaner.accept(jobId, elementId);
    }
}
