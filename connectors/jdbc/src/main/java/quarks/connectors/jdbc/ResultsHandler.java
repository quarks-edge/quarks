/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.connectors.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import quarks.function.Consumer;

/**
 * Handle the results of executing an SQL statement.
 * <p>
 * Sample use:
 * <br>
 * For a ResultSet created by executing the SQL statement:
 * <br>
 * {@code "SELECT id, firstname, lastname FROM persons WHERE id = ?"}
 * <pre>{@code 
 * // create a Person tuple from db person info and add it to a stream
 * ResultsHandler<PersonId,Person> rh = 
 *     (tuple,rs,exc,consumer) -> {
 *         if (exc != null)
 *             return;
 *         rs.next();
 *         int id = rs.getInt("id");
 *         String firstName = rs.getString("firstname");
 *         String lastName = rs.getString("lastname");
 *         consumer.accept(new Person(id, firstName, lastName));
 *         }
 *      }
 *    };
 * }</pre>
 *
 * @param <T> type of the tuple inducing the SQL statement execution / results
 * @param <R> type of tuple of a result stream consumer
 */
@FunctionalInterface
public interface ResultsHandler<T,R> {
    /**
     * Process the {@code ResultSet} and add 0 or more tuples to {@code consumer}.
     * @param tuple the tuple that induced the resultSet
     * @param resultSet the SQL statement's result set. null if {@code exc}
     *        is non-null or if the statement doesn't generate a {@code ResultSet}.
     * @param exc non-null if there was an exception executing the statement.
     *        Typically a SQLException.
     * @param consumer a Consumer to a result stream.
     * @throws SQLException if there are problems handling the result
     */
    public void handleResults(T tuple, ResultSet resultSet, Exception exc, Consumer<R> consumer) throws SQLException;
}
