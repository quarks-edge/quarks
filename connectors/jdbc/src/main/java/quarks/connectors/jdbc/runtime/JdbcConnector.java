/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.connectors.jdbc.runtime;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quarks.connectors.jdbc.CheckedFunction;
import quarks.connectors.jdbc.CheckedSupplier;

public class JdbcConnector {
    
    private static final Logger logger = LoggerFactory.getLogger(JdbcConnector.class);
    private final CheckedSupplier<DataSource> dataSourceFn;
    private final CheckedFunction<DataSource,Connection> connFn;
    private DataSource ds;
    private final Map<JdbcStatement<?,?>,Connection> cnMap = new HashMap<>();
    
    public JdbcConnector(CheckedSupplier<DataSource> dataSourceFn, CheckedFunction<DataSource,Connection> connFn) {
        this.dataSourceFn = dataSourceFn;
        this.connFn = connFn;
    }
    
    Logger getLogger() {
        return logger;
    }
    
    void unregister(JdbcStatement<?,?> oplet) {
        logger.trace("unregistering statement");
        closeCn(oplet);
    }
    
    private DataSource getDataSource() throws Exception {
        if (ds == null) {
            logger.trace("getting DataSource");
            ds = dataSourceFn.get();
        }
        return ds;
    }
    
    synchronized Connection getConnection(JdbcStatement<?,?> oplet) throws Exception {
        // Apparently a bad idea for multiple threads (operators
        // in our case) to use a single Connection instance.
        Connection cn = cnMap.get(oplet);
        if (cn == null) {
            try {
                logger.trace("getting jdbc connection");
                cn = connFn.apply(getDataSource());
                cnMap.put(oplet, cn);
            }
            catch (Exception e) {
                logger.error("unable to connect", e);
                throw e;
            }
        }
        return cn;
    }

    void statementFailed(JdbcStatement<?,?> oplet, Exception e) {
        logger.error("statement failed", e);
        if (!(e instanceof SQLTransientException)) {
            closeCn(oplet);
        }
    }
    
    private synchronized void closeCn(JdbcStatement<?,?> oplet) {
        try {
            Connection cn = cnMap.remove(oplet);
            if (cn != null) {
                logger.trace("closing jdbc connection");
                cn.close();
            }
        }
        catch (SQLException e) {
            logger.error("jdbc close cn failed", e);
        }
    }
}
