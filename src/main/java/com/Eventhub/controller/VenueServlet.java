package com.Eventhub.controller;

import com.Eventhub.dao.VenueDAO;
import com.Eventhub.model.Venue;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/venues/*")
public class VenueServlet extends JsonCrudServlet<Venue> {

    private static final VenueDAO DAO = new VenueDAO();

    public VenueServlet() {
        super(Venue.class,
                VenueServlet::list,
                (request, id) -> DAO.getVenueById(id),
                (request, venue) -> {
                    if (!SessionAuth.isAdmin(request)) {
                        venue.setProviderId(Policies.providerId(request));
                    }
                    return DAO.createVenue(venue);
                },
                (request, venue, id) -> {
                    venue.setId(id);
                    return DAO.updateVenue(venue);
                },
                (request, id) -> DAO.deleteVenue(id),
                Venue::setId,
                Policies.providerOwned(Venue::getProviderId,
                        venue -> "AVAILABLE".equals(venue.getStatus())));
    }

    private static List<Venue> list(HttpServletRequest request) throws SQLException {
        if (SessionAuth.isAdmin(request)) {
            return DAO.getAllVenues();
        }
        String city = request.getParameter("city");
        if (SessionAuth.hasRole(request, "PROVIDER")) {
            long providerId = Policies.providerId(request);
            return DAO.getAllVenues().stream()
                    .filter(venue -> venue.getProviderId() != null
                            && venue.getProviderId() == providerId)
                    .toList();
        }
        List<Venue> venues = city == null
                ? DAO.getAllVenues() : DAO.getVenuesByCity(city);
        return venues.stream()
                .filter(venue -> "AVAILABLE".equals(venue.getStatus()))
                .toList();
    }
}
