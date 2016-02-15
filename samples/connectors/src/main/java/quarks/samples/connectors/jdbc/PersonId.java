/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors.jdbc;

/**
 * Another class containing a person id for the sample.
 */
public class PersonId {
    int id;
    PersonId(int id) {
        this.id = id;
    }
    public String toString() {
        return String.format("id=%d", id);
    }
}
