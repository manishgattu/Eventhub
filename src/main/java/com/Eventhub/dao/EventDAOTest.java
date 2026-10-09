
package com.Eventhub.dao;

import java.sql.SQLException;
import java.util.List;

import com.Eventhub.model.Event;

public class EventDAOTest {

    public static void main(String[] args) {
        EventDAO eventDAO = new EventDAO();

        try {
            // Test 1: Retrieve all events
            List<Event> events = eventDAO.getAllEvents();

            System.out.println("All Events:");
            if (events.isEmpty()) {
                System.out.println("No events found in the database.");
            } else {
                for (Event event : events) {
                    System.out.println(event);
                }
            }

            // Test 2: Retrieve event with ID 1
            Event event = eventDAO.getEventById(1L);

            System.out.println("\nEvent with ID 1:");
            if (event != null) {
                System.out.println(event);
            } else {
                System.out.println("No event found with ID 1.");
            }

        } catch (SQLException e) {
            System.out.println("Database test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
