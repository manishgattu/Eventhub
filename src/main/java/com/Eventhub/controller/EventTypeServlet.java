package com.Eventhub.controller;

import com.Eventhub.dao.EventTypeDAO;
import com.Eventhub.model.EventType;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/api/event-types/*")
public class EventTypeServlet extends JsonCrudServlet<EventType> {

    private static final EventTypeDAO DAO = new EventTypeDAO();

    public EventTypeServlet() {
        super(EventType.class,
                (request) -> DAO.getAllEventTypes(),
                (request, id) -> DAO.getEventTypeById(id),
                (request, eventType) -> DAO.createEventType(eventType),
                null,
                null,
                EventType::setId,
                Policies.readForUsersWriteForAdmins());
    }
}
