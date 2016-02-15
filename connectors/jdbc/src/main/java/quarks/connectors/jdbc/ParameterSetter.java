/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.connectors.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Function that sets parameters in a JDBC SQL {@link java.sql.PreparedStatement}.
 *
 * @param <T> stream tuple type
 */
@FunctionalInterface
public interface ParameterSetter<T> {
    /**
     * Set 0 or more parameters in a JDBC PreparedStatement. 
     * <p>
     * Sample use for a PreparedStatement of:
     * <br>
     * {@code "SELECT id, firstname, lastname FROM persons WHERE id = ?"}
     * <pre>{@code
     * ParameterSetter<PersonId> ps = (personId,stmt) -> stmt.setInt(1, personId.getId());
     * }</pre>
     *
     * @param t stream tuple of type T
     * @param stmt PreparedStatement
     * @throws SQLException
     */
    void setParameters(T t, PreparedStatement stmt) throws SQLException;
}
