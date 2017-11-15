package be.howest.sooa.o5.data;

import static be.howest.sooa.o5.data.AbstractRepository.getConnection;
import be.howest.sooa.o5.domain.RoomOption;
import be.howest.sooa.o5.ex.DBException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Hayk
 */
public class RoomOptionRepository extends AbstractRepository {

    private static final String SQL = "SELECT * FROM roomoption";
    private static final String SQL_FIND_ALL = SQL + " ORDER BY description";

    public List<RoomOption> findAll() {
        List<RoomOption> entities = new ArrayList<>();
        try (Connection connection = getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(SQL_FIND_ALL)) {
            while (resultSet.next()) {
                entities.add(build(resultSet));
            }
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
        return entities;
    }

    private RoomOption build(ResultSet resultSet) throws SQLException {
        return new RoomOption(
                resultSet.getLong("option_id"),
                resultSet.getString("description"),
                resultSet.getBigDecimal("price")
        );
    }
}
