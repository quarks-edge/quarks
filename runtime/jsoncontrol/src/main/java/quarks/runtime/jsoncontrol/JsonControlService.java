/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.runtime.jsoncontrol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import quarks.execution.services.ControlService;

/**
 * Control service that accepts control instructions as JSON objects.
 * 
 * Currently just supports operations with no arguments as
 * a work in progress.
 */
public class JsonControlService implements ControlService {

    /**
     * Key for the type of the control MBean in a JSON request.
     * <BR>
     * Value is {@value}.
     */
    public static final String TYPE_KEY = "type";
    
    /**
     * Key for the alias of the control MBean in a JSON request.
     * <BR>
     * Value is {@value}.     */
    public static final String ALIAS_KEY = "alias";
    
    /**
     * Key for the operation name.
     * <BR>
     * Value is {@value}.
     */
    public static final String OP_KEY = "op";

    private final Map<String, ControlMBean<?>> mbeans = new HashMap<>();

    private static String getControlId(String type, String id, String alias) {
        return type + ":" + (alias == null ? id : alias);
    }

    /**
     * Handle a JSON control request.
     * 
     * The control action is executed directly
     * using the calling thread.
     * 
     * @return JSON response, JSON null if the request was not recognized.
     */
    public JsonElement controlRequest(JsonObject request) throws Exception {
        if (request.has(OP_KEY))
            return controlOperation(request);

        return JsonNull.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized <T> String registerControl(String type, String id, String alias, Class<T> controlInterface,
            T control) {
        final String controlId = getControlId(type, id, alias);
        if (mbeans.containsKey(controlId))
            throw new IllegalStateException();

        mbeans.put(controlId, new ControlMBean<T>(controlInterface, control));
        return controlId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void unregister(String controlId) {
        mbeans.remove(controlId);
    }

    /**
     * Handle a control operation.
     * An operation maps to a {@code void} method.
     * @param request Request to be executed.
     * @return JSON boolean true if the request was executed, false if it was not.
     * @throws Exception Exception executing the control instruction. 
     */
    private JsonElement controlOperation(JsonObject request) throws Exception {
        final String type = request.get(TYPE_KEY).getAsString();
        String alias = request.get(ALIAS_KEY).getAsString();
        final String controlId = getControlId(type, null, alias);

        ControlMBean<?> mbean;
        synchronized (this) {
            mbean = mbeans.get(controlId);
        }

        if (mbean == null)
            return new JsonPrimitive(Boolean.FALSE);

        String methodName = request.get(OP_KEY).getAsString();

        Method method = findMethod(mbean.getControlInterface(), methodName);

        if (method == null)
            return new JsonPrimitive(Boolean.FALSE);

        executeMethod(method, mbean.getControl(), getArguments(method, request));

        return new JsonPrimitive(Boolean.TRUE);
    }

    private Method findMethod(Class<?> controlInterface, String name) {
        Method[] methods = controlInterface.getDeclaredMethods();

        for (Method method : methods) {
            if (!Modifier.isPublic(method.getModifiers()))
                continue;
            if (name.equals(method.getName()))
                return method;
        }
        return null;
    }

    private Object[] getArguments(Method method, JsonObject request) {
        if (method.getParameters().length == 0)
            return null;
        throw new UnsupportedOperationException();
    }

    private void executeMethod(Method method, Object control, Object[] arguments)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        method.invoke(control, arguments);
    }

}
