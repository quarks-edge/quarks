/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016
*/
package quarks.samples.apps;

import java.util.Properties;

import quarks.providers.direct.DirectProvider;

/**
 * A configuration driven factory for a Quarks topology provider.
 */
public class TopologyProviderFactory {
    private final Properties props;
    
    /**
     * Construct a factory
     * @param props configuration information.
     */
    public TopologyProviderFactory(Properties props) {
        this.props = props;
    }
    
    /**
     * Get a new topology provider.
     * <p>
     * The default provider is {@code quarks.providers.direct.DirectProvider}.
     * <p>
     * The {@code topology.provider} configuration property can specify
     * an alternative.
     * 
     * @return the provider
     * @throws Exception if the provider couldn't be created
     */
    public DirectProvider newProvider() throws Exception {
        String name = props.getProperty("topology.provider", "quarks.providers.direct.DirectProvider");
        Class<?> clazz = null;
        try {
            clazz = Class.forName(name);
        }
        catch (ClassNotFoundException e) {
            String msg = "Class not found: "+e.getLocalizedMessage();
            System.err.println(msg);
            throw new IllegalStateException(msg);
        }
        return (DirectProvider) clazz.newInstance();
    }
}
