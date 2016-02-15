/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.samples.connectors;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import quarks.function.Function;

/**
 * Simple command option processor.
 */
public class Options {
    private static final Map<String,Function<String,?>> handlers = new HashMap<>();
    private static final Map<String,Object> options = new HashMap<>();
    private static final Map<String,Object> defaults = new HashMap<>();
    
    public <T> void addHandler(String opt, Function<String,T> cvtFn) {
        addHandler(opt, cvtFn, null);
    }
    
    public <T> void addHandler(String opt, Function<String,T> cvtFn, T dflt) {
        handlers.put(opt, cvtFn);
        if (dflt != null)
            defaults.put(opt, dflt);
    }
    
    public void processArgs(String[] args) {
        for (Map.Entry<String,Function<String,?>> e : handlers.entrySet()) {
            handleOpt(e.getKey(), e.getValue(), args);
        }

        for (String arg : args) {
            String[] item = arg.split("=");
            if (!handlers.containsKey(item[0]))
                throw new IllegalArgumentException("Unrecognized argument '"+arg+"'");
        }
    }
    
    private void handleOpt(String opt, Function<String,?> cvtFn, String[] args) {
        String v = getArg(cvtFn!=null ? opt : opt+"=true", args);
        if (v != null)
            options.put(opt, cvtFn==null ? true : cvtFn.apply(v));
        else if (defaults.get(opt) != null)
            options.put(opt, defaults.get(opt));
    }

    public <T> T get(String opt) {
        return get(opt, null);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(String opt, T dflt) {
        return options.get(opt) == null ? dflt : (T)options.get(opt); 
    }
    
    public Set<Map.Entry<String,Object>> getAll() {
        return Collections.unmodifiableSet(options.entrySet());
    }
    
    public void put(String opt, Object value) {
        options.put(opt, value);
    }
    
    private String getArg(String item, String[] args) {
        String[] itemParts = item.split("=");
        if (itemParts.length>1)
            item = itemParts[0];
        for (String arg : args) {
            String[] parts = arg.split("=");
            if (item.equals(parts[0])) {
                if (parts.length > 1)
                    return parts[1];
                else
                    return itemParts.length > 1 ? itemParts[1] : parts[1];
            }
        }
        return null;
    }
}
