
package com.Eventhub.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.Eventhub.model.EventType;
import com.Eventhub.util.DBConnection;

public class EventTypeDAO {

    // Retrieve all event types
    public List<EventType> getAllEventTypes() throws SQLException {

        List<EventType> eventTypes = new ArrayList<>();

        String sql = "SELECT id, name, description " +
                     "FROM event_types ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                EventType eventType = new EventType(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description")
                );

                eventTypes.add(eventType);
            }
        }

        return eventTypes;
    }

    // Retrieve an event type by ID
    public EventType getEventTypeById(Long id) throws SQLException {

        String sql = "SELECT id, name, description " +
                     "FROM event_types WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return new EventType(
                            resultSet.getLong("id"),
                            resultSet.getString("name"),
                            resultSet.getString("description")
                    );
                }
            }
        }

        return null;
    }

    // Insert a new event type
    public long createEventType(EventType eventType)
            throws SQLException {

        String sql = "INSERT INTO event_types (name, description) " +
                     "VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, eventType.getName());
            statement.setString(2, eventType.getDescription());

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted == 0) {
                throw new SQLException("Creating event type failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("No ID generated for the event type.");
    }
}
