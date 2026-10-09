
package com.Eventhub.dao;

import com.Eventhub.model.EventType;
import java.sql.SQLException;
import java.util.List;

public class EventTypeDAOTest {

    public static void main(String[] args) {
        EventTypeDAO eventTypeDAO = new EventTypeDAO();

        try {
            // Test 1: Retrieve all event types
            List<EventType> eventTypes = eventTypeDAO.getAllEventTypes();

            System.out.println("All Event Types:");
            if (eventTypes.isEmpty()) {
                System.out.println("No event types found in the database.");
            } else {
                for (EventType eventType : eventTypes) {
                    System.out.println(eventType);
                }
            }

            // Test 2: Retrieve an event type by ID
            EventType eventType = eventTypeDAO.getEventTypeById(1L);

            System.out.println("\nEvent Type with ID 1:");
            if (eventType != null) {
                System.out.println(eventType);
            } else {
                System.out.println("No event type found with ID 1.");
            }

        } catch (SQLException e) {
            System.out.println("Database test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
