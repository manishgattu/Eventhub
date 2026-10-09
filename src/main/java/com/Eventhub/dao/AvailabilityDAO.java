
package com.Eventhub.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.Eventhub.model.Availability;
import com.Eventhub.util.DBConnection;

public class AvailabilityDAO {

    // 1. Retrieve all availability records
    public List<Availability> getAllAvailability() throws SQLException {
        List<Availability> records = new ArrayList<>();
        String sql = "SELECT * FROM availability ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                records.add(mapAvailability(resultSet));
            }
        }

        return records;
    }

    // 2. Retrieve an availability record by ID
    public Availability getAvailabilityById(Long id) throws SQLException {
        String sql = "SELECT * FROM availability WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAvailability(resultSet);
                }
            }
        }

        return null;
    }

    // 3. Retrieve availability for a service
    public List<Availability> getByServiceId(Long serviceId)
            throws SQLException {
        List<Availability> records = new ArrayList<>();
        String sql = "SELECT * FROM availability " +
                     "WHERE service_id = ? ORDER BY available_date, start_time";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, serviceId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapAvailability(resultSet));
                }
            }
        }

        return records;
    }

    // 4. Retrieve availability for a venue
    public List<Availability> getByVenueId(Long venueId)
            throws SQLException {
        List<Availability> records = new ArrayList<>();
        String sql = "SELECT * FROM availability " +
                     "WHERE venue_id = ? ORDER BY available_date, start_time";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, venueId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapAvailability(resultSet));
                }
            }
        }

        return records;
    }

    // 5. Create an availability record
    public long createAvailability(Availability availability)
            throws SQLException {

        validateResource(availability);

        String sql = "INSERT INTO availability " +
                "(service_id, venue_id, available_date, start_time, end_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            if (availability.getServiceId() != null) {
                statement.setLong(1, availability.getServiceId());
            } else {
                statement.setNull(1, java.sql.Types.BIGINT);
            }

            if (availability.getVenueId() != null) {
                statement.setLong(2, availability.getVenueId());
            } else {
                statement.setNull(2, java.sql.Types.BIGINT);
            }

            statement.setDate(3, availability.getAvailableDate());
            statement.setTime(4, availability.getStartTime());
            statement.setTime(5, availability.getEndTime());
            statement.setString(6,
                    availability.getStatus() != null
                            ? availability.getStatus()
                            : "AVAILABLE");

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Creating availability failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("No ID generated for availability.");
    }

    // 6. Update an availability record
    public boolean updateAvailability(Availability availability)
            throws SQLException {

        validateResource(availability);

        String sql = "UPDATE availability SET service_id = ?, venue_id = ?, " +
                "available_date = ?, start_time = ?, end_time = ?, status = ? " +
                "WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (availability.getServiceId() != null) {
                statement.setLong(1, availability.getServiceId());
            } else {
                statement.setNull(1, java.sql.Types.BIGINT);
            }

            if (availability.getVenueId() != null) {
                statement.setLong(2, availability.getVenueId());
            } else {
                statement.setNull(2, java.sql.Types.BIGINT);
            }

            statement.setDate(3, availability.getAvailableDate());
            statement.setTime(4, availability.getStartTime());
            statement.setTime(5, availability.getEndTime());
            statement.setString(6, availability.getStatus());
            statement.setLong(7, availability.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // 7. Delete an availability record
    public boolean deleteAvailability(Long id) throws SQLException {
        String sql = "DELETE FROM availability WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    // Ensure exactly one resource is selected
    private void validateResource(Availability availability) {
        boolean hasService = availability.getServiceId() != null;
        boolean hasVenue = availability.getVenueId() != null;

        if (hasService == hasVenue) {
            throw new IllegalArgumentException(
                    "Specify either serviceId or venueId, but not both.");
        }
    }

    // Convert a database row into an Availability object
    private Availability mapAvailability(ResultSet resultSet)
            throws SQLException {

        Availability availability = new Availability();

        availability.setId(resultSet.getLong("id"));

        long serviceId = resultSet.getLong("service_id");
        availability.setServiceId(
                resultSet.wasNull() ? null : serviceId);

        long venueId = resultSet.getLong("venue_id");
        availability.setVenueId(
                resultSet.wasNull() ? null : venueId);

        availability.setAvailableDate(resultSet.getDate("available_date"));
        availability.setStartTime(resultSet.getTime("start_time"));
        availability.setEndTime(resultSet.getTime("end_time"));
        availability.setStatus(resultSet.getString("status"));
        availability.setCreatedAt(resultSet.getTimestamp("created_at"));
        availability.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return availability;
    }
}
