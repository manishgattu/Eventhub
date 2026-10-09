
package com.Eventhub.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.Eventhub.model.BookingItem;
import com.Eventhub.util.DBConnection;

public class BookingItemDAO {

    // 1. Retrieve all items for a booking
    public List<BookingItem> getItemsByBookingId(Long bookingId)
            throws SQLException {

        List<BookingItem> items = new ArrayList<>();
        String sql = "SELECT * FROM booking_items WHERE booking_id = ? ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, bookingId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapBookingItem(resultSet));
                }
            }
        }

        return items;
    }

    // 2. Retrieve a booking item by ID
    public BookingItem getBookingItemById(Long id) throws SQLException {
        String sql = "SELECT * FROM booking_items WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapBookingItem(resultSet);
                }
            }
        }

        return null;
    }

    // 3. Add an item to a booking
    public long createBookingItem(BookingItem item) throws SQLException {
        String sql = "INSERT INTO booking_items " +
                "(booking_id, service_id, quantity, unit_price, total_price) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, item.getBookingId());
            statement.setLong(2, item.getServiceId());
            statement.setInt(3, item.getQuantity());
            statement.setBigDecimal(4, item.getUnitPrice());
            statement.setBigDecimal(5, item.getTotalPrice());

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Creating booking item failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("No ID generated for the booking item.");
    }

    // 4. Update a booking item
    public boolean updateBookingItem(BookingItem item) throws SQLException {
        String sql = "UPDATE booking_items SET booking_id = ?, service_id = ?, " +
                "quantity = ?, unit_price = ?, total_price = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, item.getBookingId());
            statement.setLong(2, item.getServiceId());
            statement.setInt(3, item.getQuantity());
            statement.setBigDecimal(4, item.getUnitPrice());
            statement.setBigDecimal(5, item.getTotalPrice());
            statement.setLong(6, item.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // 5. Delete a booking item
    public boolean deleteBookingItem(Long id) throws SQLException {
        String sql = "DELETE FROM booking_items WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    // Convert a database row into a BookingItem object
    private BookingItem mapBookingItem(ResultSet resultSet)
            throws SQLException {

        BookingItem item = new BookingItem();

        item.setId(resultSet.getLong("id"));
        item.setBookingId(resultSet.getLong("booking_id"));
        item.setServiceId(resultSet.getLong("service_id"));
        item.setQuantity(resultSet.getInt("quantity"));
        item.setUnitPrice(resultSet.getBigDecimal("unit_price"));
        item.setTotalPrice(resultSet.getBigDecimal("total_price"));
        item.setCreatedAt(resultSet.getTimestamp("created_at"));

        return item;
    }
}
