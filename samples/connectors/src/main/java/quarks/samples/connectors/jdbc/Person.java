/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors.jdbc;

/**
 * A Person object for the sample.
 */
public class Person {
    int id;
    String firstName;
    String lastName;
    Person(int id, String first, String last) {
        this.id = id;
        this.firstName = first;
        this.lastName = last;
    }
    public String toString() {
        return String.format("id=%d first=%s last=%s",
                id, firstName, lastName);
    }
}
