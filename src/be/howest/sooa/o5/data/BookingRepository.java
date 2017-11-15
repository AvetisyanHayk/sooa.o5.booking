package be.howest.sooa.o5.data;

import be.howest.sooa.o5.domain.Booking;
import be.howest.sooa.o5.domain.RoomOption;
import be.howest.sooa.o5.domain.RoomType;
import be.howest.sooa.o5.ex.DBException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hayk
 */
public class BookingRepository extends AbstractRepository {

    private static final String SQL_READ_FULL
            = "SELECT booking.id, name, roomtype_id,"
            + " roomtype.description as roomtype_description, roomtype.price as roomtype_price,"
            + " roomoption.option_id, roomoption.description as option_description, roomoption.price as option_price"
            + " FROM booking"
            + " LEFT JOIN roomtype ON booking.roomtype_id = roomtype.id"
            + " LEFT JOIN booking_option ON booking.id = booking_option.booking_id"
            + " LEFT JOIN roomoption ON booking_option.option_id = roomoption.option_id"
            + " WHERE booking.id = ?";
    private static final String SQL_INSERT = "INSERT INTO booking(name, roomtype_id) values(?, ?)";
    private static final String SQL_INSERT_ROOM_OPTIONS
            = "INSERT INTO booking_option(booking_id, option_id)"
            + " VALUES(?, ?)";

    public Booking read(long id) {
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_READ_FULL)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                Booking booking = null;
                if (resultSet.next()) {
                    booking = build(resultSet);
                    RoomOption option = buildOption(resultSet);
                    booking.addRoomOption(option);
                    while (resultSet.next()) {
                        option = buildOption(resultSet);
                        booking.addRoomOption(option);
                    }
                }
                return booking;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            throw new DBException(ex);
        }
    }

//    public long saveBookingWithOptions(Booking booking) {
//
//    }

    public long save(Booking booking) {
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                        Statement.RETURN_GENERATED_KEYS)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            statement.setString(1, booking.getName());
            statement.setLong(2, booking.getRoomType().getId());
            statement.executeUpdate();

            long lastInsertId = 0L;
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    lastInsertId = resultSet.getLong(1);

                    booking.setId(lastInsertId);
                    if (booking.hasOptions()) {
                        try (PreparedStatement batch = connection.prepareStatement(SQL_INSERT_ROOM_OPTIONS)) {
                            for (RoomOption roomOption : booking.getRoomOptions()) {
                                batch.setLong(1, booking.getId());
                                batch.setLong(2, roomOption.getId());
                                batch.addBatch();
                            }
                            batch.executeBatch();
                            connection.commit();
                        } catch (SQLException ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                    return lastInsertId;
                }
            }
            return lastInsertId;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            throw new DBException(ex);
        }
    }

    public void saveOptions(Booking booking) {
        try (Connection connection = getConnection();
                PreparedStatement batch = connection.prepareStatement(SQL_INSERT_ROOM_OPTIONS)) {
            for (RoomOption roomOption : booking.getRoomOptions()) {
                batch.setLong(1, booking.getId());
                batch.setLong(2, roomOption.getId());
                batch.addBatch();
            }
            batch.executeBatch();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private Booking build(ResultSet resultSet) throws SQLException {
        Booking booking = new Booking(resultSet.getLong("id"),
                resultSet.getString("name"));
        RoomType roomType = new RoomType(resultSet.getLong("roomtype_id"),
                resultSet.getString("roomtype_description"),
                resultSet.getBigDecimal("roomtype_price"));
        booking.setRoomType(roomType);
        return booking;
    }

    private RoomOption buildOption(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("option_id");
        String description = resultSet.getString("option_description");
        BigDecimal price = resultSet.getBigDecimal("option_price");
        if (description == null || price == null) {
            return null;
        } else {
            return new RoomOption(id, description, price);
        }
    }
}
