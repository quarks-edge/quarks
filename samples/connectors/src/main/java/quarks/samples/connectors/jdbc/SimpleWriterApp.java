/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors.jdbc;

import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

import quarks.connectors.jdbc.JdbcStreams;
import quarks.providers.direct.DirectProvider;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * A simple JDBC connector sample demonstrating streaming write access
 * of a dbms to add stream tuples to a table.
 */
public class SimpleWriterApp {
    private final Properties props;

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("missing pathname to jdbc.properties file");
        SimpleWriterApp writer = new SimpleWriterApp(args[0]);
        DbUtils.initDb(DbUtils.getDataSource(writer.props));
        writer.run();
    }

    /**
     * @param jdbcPropsPath pathname to properties file
     */
    SimpleWriterApp(String jdbcPropsPath) throws Exception {
        props = new Properties();
        props.load(Files.newBufferedReader(new File(jdbcPropsPath).toPath()));
    }
    
    /**
     * Create a topology for the writer application and run it.
     */
    private void run() throws Exception {
        DirectProvider tp = new DirectProvider();
        
        // build the application/topology
        
        Topology t = tp.newTopology("jdbcSampleWriter");

        // Create the JDBC connector
        JdbcStreams myDb = new JdbcStreams(t,
                () -> DbUtils.getDataSource(props),
                dataSource -> dataSource.getConnection());
        
        // Create a sample stream of Person tuples
        TStream<Person> persons = t.collection(PersonData.loadPersonData(props));
        
        // Write stream tuples to a table.
        myDb.executeStatement(persons,
                () -> "INSERT INTO persons VALUES(?,?,?)",
                (person,stmt) -> {
                    System.out.println("Inserting into persons table: person "+person);
                    stmt.setInt(1, person.id);
                    stmt.setString(2, person.firstName);
                    stmt.setString(3, person.lastName);
                    }
                );
        
        // run the application / topology
        tp.submit(t);
    }
}
