/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.connectors.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Function that supplies a JDBC SQL {@link java.sql.PreparedStatement}.
 */
@FunctionalInterface
public interface StatementSupplier {
    /**
     * Create a JDBC SQL PreparedStatement containing 0 or more parameters.
     * <p>
     * Sample use:
     * <pre>{@code
     * StatementSupplier ss = 
     *     (cn) -> cn.prepareStatement("SELECT id, firstname, lastname"
     *                                  + " FROM persons WHERE id = ?");
     * }</pre>
     * @param cn JDBC connection
     */
    PreparedStatement get(Connection cn) throws SQLException;
}
