
package com.Eventhub.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.Eventhub.model.Booking;
import com.Eventhub.util.DBConnection;

public class BookingDAO {

    // 1. Retrieve all bookings
    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                bookings.add(mapBooking(resultSet));
            }
        }

        return bookings;
    }

    // 2. Retrieve a booking by ID
    public Booking getBookingById(Long id) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapBooking(resultSet);
                }
            }
        }

        return null;
    }

    // 3. Retrieve bookings for a user
    public List<Booking> getBookingsByUserId(Long userId)
            throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE user_id = ? ORDER BY id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bookings.add(mapBooking(resultSet));
                }
            }
        }

        return bookings;
    }

    // 4. Retrieve bookings for an event
    public List<Booking> getBookingsByEventId(Long eventId)
            throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE event_id = ? ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, eventId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bookings.add(mapBooking(resultSet));
                }
            }
        }

        return bookings;
    }

    // 5. Create a booking
    public long createBooking(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings " +
                "(event_id, user_id, venue_id, booking_date, total_amount, status, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, booking.getEventId());
            statement.setLong(2, booking.getUserId());

            if (booking.getVenueId() != null) {
                statement.setLong(3, booking.getVenueId());
            } else {
                statement.setNull(3, java.sql.Types.BIGINT);
            }

            statement.setDate(4, booking.getBookingDate());
            statement.setBigDecimal(5, booking.getTotalAmount());
            statement.setString(6,
                    booking.getStatus() != null
                            ? booking.getStatus()
                            : "PENDING");
            statement.setString(7, booking.getNotes());

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Creating booking failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("No ID generated for the booking.");
    }

    // 6. Update booking status
    public boolean updateBookingStatus(Long id, String status)
            throws SQLException {

        String sql = "UPDATE bookings SET status = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setLong(2, id);

            return statement.executeUpdate() > 0;
        }
    }

    // 7. Update booking details
    public boolean updateBooking(Booking booking) throws SQLException {
        String sql = "UPDATE bookings SET event_id = ?, user_id = ?, " +
                "venue_id = ?, booking_date = ?, total_amount = ?, " +
                "status = ?, notes = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, booking.getEventId());
            statement.setLong(2, booking.getUserId());

            if (booking.getVenueId() != null) {
                statement.setLong(3, booking.getVenueId());
            } else {
                statement.setNull(3, java.sql.Types.BIGINT);
            }

            statement.setDate(4, booking.getBookingDate());
            statement.setBigDecimal(5, booking.getTotalAmount());
            statement.setString(6, booking.getStatus());
            statement.setString(7, booking.getNotes());
            statement.setLong(8, booking.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // 8. Delete a booking
    public boolean deleteBooking(Long id) throws SQLException {
        String sql = "DELETE FROM bookings WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    // Convert a database row into a Booking object
    private Booking mapBooking(ResultSet resultSet) throws SQLException {
        Booking booking = new Booking();

        booking.setId(resultSet.getLong("id"));
        booking.setEventId(resultSet.getLong("event_id"));
        booking.setUserId(resultSet.getLong("user_id"));

        long venueId = resultSet.getLong("venue_id");
        booking.setVenueId(resultSet.wasNull() ? null : venueId);

        booking.setBookingDate(resultSet.getDate("booking_date"));
        booking.setTotalAmount(resultSet.getBigDecimal("total_amount"));
        booking.setStatus(resultSet.getString("status"));
        booking.setNotes(resultSet.getString("notes"));
        booking.setCreatedAt(resultSet.getTimestamp("created_at"));
        booking.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return booking;
    }
}
