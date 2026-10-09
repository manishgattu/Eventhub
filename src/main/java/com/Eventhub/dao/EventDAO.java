
package com.Eventhub.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.Eventhub.model.Event;
import com.Eventhub.util.DBConnection;

public class EventDAO {

    // 1. Create a new event
    public long createEvent(Event event) throws SQLException {
        String sql = "INSERT INTO events " +
                "(user_id, event_type_id, name, description, event_date, " +
                "start_time, end_time, guest_count, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, event.getUserId());
            statement.setLong(2, event.getEventTypeId());
            statement.setString(3, event.getName());
            statement.setString(4, event.getDescription());
            statement.setDate(5, event.getEventDate());
            statement.setTime(6, event.getStartTime());
            statement.setTime(7, event.getEndTime());

            if (event.getGuestCount() != null) {
                statement.setInt(8, event.getGuestCount());
            } else {
                statement.setNull(8, java.sql.Types.INTEGER);
            }

            statement.setString(9,
                    event.getStatus() != null ? event.getStatus() : "DRAFT");

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Creating event failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("No ID generated for the event.");
    }

    // 2. Retrieve an event by ID
    public Event getEventById(Long id) throws SQLException {
        String sql = "SELECT * FROM events WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapEvent(resultSet);
                }
            }
        }

        return null;
    }

    // 3. Retrieve all events
    public List<Event> getAllEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                events.add(mapEvent(resultSet));
            }
        }

        return events;
    }

    // 4. Update an existing event
    public boolean updateEvent(Event event) throws SQLException {
        String sql = "UPDATE events SET user_id = ?, event_type_id = ?, " +
                "name = ?, description = ?, event_date = ?, start_time = ?, " +
                "end_time = ?, guest_count = ?, status = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, event.getUserId());
            statement.setLong(2, event.getEventTypeId());
            statement.setString(3, event.getName());
            statement.setString(4, event.getDescription());
            statement.setDate(5, event.getEventDate());
            statement.setTime(6, event.getStartTime());
            statement.setTime(7, event.getEndTime());

            if (event.getGuestCount() != null) {
                statement.setInt(8, event.getGuestCount());
            } else {
                statement.setNull(8, java.sql.Types.INTEGER);
            }

            statement.setString(9, event.getStatus());
            statement.setLong(10, event.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // 5. Delete an event by ID
    public boolean deleteEvent(Long id) throws SQLException {
        String sql = "DELETE FROM events WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    // Convert a database row into an Event object
    private Event mapEvent(ResultSet resultSet) throws SQLException {
        Event event = new Event();

        event.setId(resultSet.getLong("id"));
        event.setUserId(resultSet.getLong("user_id"));
        event.setEventTypeId(resultSet.getLong("event_type_id"));
        event.setName(resultSet.getString("name"));
        event.setDescription(resultSet.getString("description"));
        event.setEventDate(resultSet.getDate("event_date"));
        event.setStartTime(resultSet.getTime("start_time"));
        event.setEndTime(resultSet.getTime("end_time"));
        event.setGuestCount(resultSet.getInt("guest_count"));

        if (resultSet.wasNull()) {
            event.setGuestCount(null);
        }

        event.setStatus(resultSet.getString("status"));
        event.setCreatedAt(resultSet.getTimestamp("created_at"));
        event.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return event;
    }
}
