/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors.jdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

/**
 * Utilities for the sample's non-streaming JDBC database related actions.
 */
public class DbUtils {
    
    /**
     * Get the JDBC {@link DataSource} for the database.
     * <p>
     * The "db.name" property specifies the name of the database.
     * Defaults to "JdbcConnectorSampleDb".
     * 
     * @param props configuration properties
     * @return the DataSource
     * @throws Exception
     */
    public static DataSource getDataSource(Properties props) throws Exception {
        return createDerbyEmbeddedDataSource(props);
    }
    
    /**
     * Initialize the sample's database.
     * <p>
     * Tables are created as needed and purged.
     * @param ds the DataSource
     * @throws Exception
     */
    public static void initDb(DataSource ds) throws Exception {
        createTables(ds);
        purgeTables(ds);
    }
    
    /**
     * Purge the sample's tables
     * @param ds the DataSource
     * @throws Exception
     */
    public static void purgeTables(DataSource ds) throws Exception {
        try (Connection cn = ds.getConnection()) {
            Statement stmt = cn.createStatement();
            stmt.execute("DELETE FROM persons");
        }
    }

    private static void createTables(DataSource ds) throws Exception {
        try (Connection cn = ds.getConnection()) {
            Statement stmt = cn.createStatement();
            stmt.execute("CREATE TABLE persons "
                    + "("
                    + "id INTEGER NOT NULL,"
                    + "firstname VARCHAR(40) NOT NULL,"
                    + "lastname VARCHAR(40) NOT NULL,"
                    + "PRIMARY KEY (id)"
                    + ")"
                    );
        }
        catch (SQLException e) {
            if (e.getLocalizedMessage().contains("already exists"))
                return;
            else
                throw e;
        }
   }

   private static DataSource createDerbyEmbeddedDataSource(Properties props) throws Exception
   {
       String dbName = props.getProperty("db.name", "JdbcConnectorSampleDb");
       
       // For our sample, avoid a compile-time dependency to the jdbc driver.
       // At runtime, require that the classpath can find it.

       String DERBY_DATA_SOURCE = "org.apache.derby.jdbc.EmbeddedDataSource";
   
       Class<?> nsDataSource = null;
       try {
           nsDataSource = Class.forName(DERBY_DATA_SOURCE);
       }
       catch (ClassNotFoundException e) {
           String msg = "Fix the test classpath. ";
           if (System.getenv("DERBY_HOME") == null) {
               msg += "DERBY_HOME not set. ";
           }
           msg += "Class not found: "+e.getLocalizedMessage();
           System.err.println(msg);
           throw new IllegalStateException(msg);
       }
       DataSource ds = (DataSource) nsDataSource.newInstance();

       @SuppressWarnings("rawtypes")
       Class[] methodParams = new Class[] {String.class};
       Method dbname = nsDataSource.getMethod("setDatabaseName", methodParams);
       Object[] args = new Object[] {dbName};
       dbname.invoke(ds, args);

       // create the db if necessary
       Method create = nsDataSource.getMethod("setCreateDatabase", methodParams);
       args = new Object[] {"create"};
       create.invoke(ds, args);

       // set the user
       Method setuser = nsDataSource.getMethod("setUser", methodParams);
       args = new Object[] { props.getProperty("db.user", System.getProperty("user.name")) };
       setuser.invoke(ds, args);

       // optionally set the pw
       Method setpw = nsDataSource.getMethod("setPassword", methodParams);
       args = new Object[] { props.getProperty("db.password") };
       if (args[0] != null)
           setpw.invoke(ds, args);
   
       return ds;
   }
}
