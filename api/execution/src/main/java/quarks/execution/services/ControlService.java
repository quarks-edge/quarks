/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.execution.services;

/**
 * Service that allows an oplet to provide a control mechanism.
 * 
 *
 */
public interface ControlService {

    /**
     * Register a control bean for an oplet.
     * 
     * @param type Type of the control object.
     * @param id
     *            Unique identifier for the control object.
     * @param alias
     *            Alias for the control object.
     * @param controlInterface
     *            Public interface for the control object.
     * @param control
     *            The control bean
     * @return unique identifier that can be used to unregister an control mbean.
     */
    <T> String registerControl(String type, String id, String alias, Class<T> controlInterface, T control);
    
    /**
     * Unregister a control bean registered by {@link #registerControl(String, String, String, Class, Object)}
     */
    void unregister(String controlId);
}
