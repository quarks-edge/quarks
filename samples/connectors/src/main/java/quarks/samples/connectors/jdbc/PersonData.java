/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors.jdbc;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Utilities for loading the sample's person data.
 */
public class PersonData {
    
    /**
     * Load the person data from the path specified by the "persondata.path"
     * property.
     * @param props configuration properties
     * @return the loaded person data
     * @throws Exception
     */
    public static List<Person> loadPersonData(Properties props) throws Exception {
        String pathname = props.getProperty("persondata.path");
        List<Person> persons = new ArrayList<>();
        Path path = new File(pathname).toPath();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            int lineno = 0;
            String line;
            while ((line = br.readLine()) != null) {
                lineno++;
                Object[] fields = parseLine(line, lineno, pathname);
                if (fields == null)
                    continue;
                persons.add(new Person((Integer)fields[0], (String)fields[1], (String)fields[2]));
            }
        }
        return persons;
    }
    
    private static Object[] parseLine(String line, int lineno, String pathname) {
        line = line.trim();
        if (line.startsWith("#"))
            return null;

        // id,firstName,lastName
        String[] items = line.split(",");
        if (items.length < 3)
            throw new IllegalArgumentException("Invalid data on line "+lineno+" in "+pathname);
        int id;
        try {
           id = new Integer(items[0]);
           if (id < 1)
               throw new IllegalArgumentException("Invalid data on line "+lineno+" in "+pathname);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid data on line "+lineno+" in "+pathname);
        }
        
        Object[] fields = new Object[3];
        fields[0] = id;
        fields[1] = items[1].trim();
        fields[2] = items[2].trim();
        return fields;
    }

    /**
     * Convert a {@code List<Person>} to a {@code List<PersonId>}
     * @param persons the person list
     * @return the person id list
     */
    public static List<PersonId> toPersonIds(List<Person> persons) {
        return persons.stream()
            .map(person -> new PersonId(person.id))
            .collect(Collectors.toList());
    }
}
