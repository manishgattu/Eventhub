package com.Eventhub.controller;

import com.Eventhub.dao.EventDAO;
import com.Eventhub.model.Event;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/api/events/*")
public class EventServlet extends JsonCrudServlet<Event> {

    private static final EventDAO DAO = new EventDAO();

    public EventServlet() {
        super(Event.class,
                (request) -> DAO.getAllEvents(),
                (request, id) -> DAO.getEventById(id),
                (request, event) -> DAO.createEvent(event),
                (request, event, id) -> {
                    event.setId(id);
                    return DAO.updateEvent(event);
                },
                (request, id) -> DAO.deleteEvent(id),
                Event::setId,
                Policies.adminOnly());
    }
}
