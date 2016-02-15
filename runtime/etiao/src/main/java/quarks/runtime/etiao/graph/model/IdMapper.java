/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.runtime.etiao.graph.model;

/**
 * A set of elements associated with unique identifiers.
 * 
 * Null element references cannot be part of the set. Attempting to add or 
 * to retrieve the id of a null element throws a NullPointerException.
 * 
 * @param <ID> the identifier type.
 */
interface IdMapper<ID> {
    /**
     * Adds the specified object to the collection and generates a unique 
     * identifier for that object.  If the object has already been added 
     * to the set, then returns its identifier.
     *    
     * @param o the object to add
     * @return a unique identifier associated with the object.
     */
    ID add(Object o);    

    /**
     * Adds the specified object to the collection and associates it with the
     * specified identifier.
     * <p>
     * If the object has already been added to the set with the same 
     * identifier, then returns its identifier.  If the object has already 
     * been added to the set with another identifier, then throw an 
     * {@code IllegalStateException}. 
     *    
     * @param o the object to add
     * @return the identifier associated with the object.
     * @throws IllegalStateException if the object has already been added to 
     *          the set with another identifier
     */
    ID add(Object o, ID id);

    /**
     * Returns the identifier associated to the specified object, or {@code null}
     * if the object is not associated with an identifier.
     *    
     * @param o the object to add
     * @return the identifier associated with the object.
     */
    ID getId(Object o);
}
