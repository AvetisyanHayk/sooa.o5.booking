package be.howest.sooa.o5.data;

import be.howest.sooa.o5.domain.RoomType;
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
public class RoomTypeRepository extends AbstractRepository {
    
    private static final String SQL = "SELECT * FROM roomtype";
    private static final String SQL_FIND_ALL = SQL + " ORDER BY description";
    
    public List<RoomType> findAll() {
        List<RoomType> entities = new ArrayList<>();
        try(Connection connection = getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(SQL_FIND_ALL)) {
            while(resultSet.next()) {
                entities.add(build(resultSet));
            }
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
        return entities;
    }
    
    private RoomType build(ResultSet resultSet) throws SQLException {
        return new RoomType(
                resultSet.getLong("id"),
                resultSet.getString("description"),
                resultSet.getBigDecimal("price")
        );
    }
    
}
