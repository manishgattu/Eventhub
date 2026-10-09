
package com.Eventhub.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.Eventhub.model.Venue;
import com.Eventhub.util.DBConnection;

public class VenueDAO {

    // 1. Retrieve all venues
    public List<Venue> getAllVenues() throws SQLException {
        List<Venue> venues = new ArrayList<>();
        String sql = "SELECT * FROM venues ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                venues.add(mapVenue(resultSet));
            }
        }

        return venues;
    }

    // 2. Retrieve a venue by ID
    public Venue getVenueById(Long id) throws SQLException {
        String sql = "SELECT * FROM venues WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapVenue(resultSet);
                }
            }
        }

        return null;
    }

    // 3. Retrieve venues by city
    public List<Venue> getVenuesByCity(String city) throws SQLException {
        List<Venue> venues = new ArrayList<>();
        String sql = "SELECT * FROM venues WHERE city = ? ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, city);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    venues.add(mapVenue(resultSet));
                }
            }
        }

        return venues;
    }

    // 4. Create a new venue
    public long createVenue(Venue venue) throws SQLException {
        String sql = "INSERT INTO venues " +
                "(provider_id, name, description, address_line1, address_line2, " +
                "city, state, postal_code, country, capacity, price, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            if (venue.getProviderId() != null) {
                statement.setLong(1, venue.getProviderId());
            } else {
                statement.setNull(1, java.sql.Types.BIGINT);
            }

            statement.setString(2, venue.getName());
            statement.setString(3, venue.getDescription());
            statement.setString(4, venue.getAddressLine1());
            statement.setString(5, venue.getAddressLine2());
            statement.setString(6, venue.getCity());
            statement.setString(7, venue.getState());
            statement.setString(8, venue.getPostalCode());
            statement.setString(9,
                    venue.getCountry() != null ? venue.getCountry() : "India");

            if (venue.getCapacity() != null) {
                statement.setInt(10, venue.getCapacity());
            } else {
                statement.setNull(10, java.sql.Types.INTEGER);
            }

            statement.setBigDecimal(11, venue.getPrice());
            statement.setString(12,
                    venue.getStatus() != null ? venue.getStatus() : "AVAILABLE");

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Creating venue failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("No ID generated for the venue.");
    }

    // 5. Update a venue
    public boolean updateVenue(Venue venue) throws SQLException {
        String sql = "UPDATE venues SET provider_id = ?, name = ?, " +
                "description = ?, address_line1 = ?, address_line2 = ?, " +
                "city = ?, state = ?, postal_code = ?, country = ?, " +
                "capacity = ?, price = ?, status = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (venue.getProviderId() != null) {
                statement.setLong(1, venue.getProviderId());
            } else {
                statement.setNull(1, java.sql.Types.BIGINT);
            }

            statement.setString(2, venue.getName());
            statement.setString(3, venue.getDescription());
            statement.setString(4, venue.getAddressLine1());
            statement.setString(5, venue.getAddressLine2());
            statement.setString(6, venue.getCity());
            statement.setString(7, venue.getState());
            statement.setString(8, venue.getPostalCode());
            statement.setString(9, venue.getCountry());

            if (venue.getCapacity() != null) {
                statement.setInt(10, venue.getCapacity());
            } else {
                statement.setNull(10, java.sql.Types.INTEGER);
            }

            statement.setBigDecimal(11, venue.getPrice());
            statement.setString(12, venue.getStatus());
            statement.setLong(13, venue.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // 6. Delete a venue
    public boolean deleteVenue(Long id) throws SQLException {
        String sql = "DELETE FROM venues WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    // Convert a database row into a Venue object
    private Venue mapVenue(ResultSet resultSet) throws SQLException {
        Venue venue = new Venue();

        venue.setId(resultSet.getLong("id"));

        long providerId = resultSet.getLong("provider_id");
        venue.setProviderId(resultSet.wasNull() ? null : providerId);

        venue.setName(resultSet.getString("name"));
        venue.setDescription(resultSet.getString("description"));
        venue.setAddressLine1(resultSet.getString("address_line1"));
        venue.setAddressLine2(resultSet.getString("address_line2"));
        venue.setCity(resultSet.getString("city"));
        venue.setState(resultSet.getString("state"));
        venue.setPostalCode(resultSet.getString("postal_code"));
        venue.setCountry(resultSet.getString("country"));
        venue.setCapacity(resultSet.getInt("capacity"));
        venue.setPrice(resultSet.getBigDecimal("price"));
        venue.setStatus(resultSet.getString("status"));
        venue.setCreatedAt(resultSet.getTimestamp("created_at"));
        venue.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return venue;
    }
}
