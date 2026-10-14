package com.Eventhub.controller;

import com.Eventhub.dao.AvailabilityDAO;
import com.Eventhub.dao.ServiceDAO;
import com.Eventhub.dao.VenueDAO;
import com.Eventhub.model.Availability;
import com.Eventhub.model.Service;
import com.Eventhub.model.Venue;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/availability/*")
public class AvailabilityServlet extends JsonCrudServlet<Availability> {

    private static final AvailabilityDAO DAO = new AvailabilityDAO();
    private static final ServiceDAO SERVICE_DAO = new ServiceDAO();
    private static final VenueDAO VENUE_DAO = new VenueDAO();

    public AvailabilityServlet() {
        super(Availability.class,
                AvailabilityServlet::list,
                (request, id) -> DAO.getAvailabilityById(id),
                (request, availability) -> DAO.createAvailability(availability),
                (request, availability, id) -> {
                    availability.setId(id);
                    return DAO.updateAvailability(availability);
                },
                (request, id) -> DAO.deleteAvailability(id),
                Availability::setId,
                new Policy<>() {
                    @Override
                    public boolean canRead(HttpServletRequest request,
                            Availability availability) throws SQLException {
                        return SessionAuth.isAdmin(request)
                                || ownsResource(request, availability)
                                || (SessionAuth.hasRole(request, "CUSTOMER")
                                        && "AVAILABLE".equals(availability.getStatus()));
                    }

                    @Override
                    public boolean canCreate(HttpServletRequest request,
                            Availability availability) throws SQLException {
                        return SessionAuth.isAdmin(request)
                                || ownsResource(request, availability);
                    }

                    @Override
                    public boolean canUpdate(HttpServletRequest request,
                            Availability current, Availability proposed)
                            throws SQLException {
                        return SessionAuth.isAdmin(request)
                                || (ownsResource(request, current)
                                        && ownsResource(request, proposed));
                    }

                    @Override
                    public boolean canDelete(HttpServletRequest request,
                            Availability current) throws SQLException {
                        return SessionAuth.isAdmin(request)
                                || ownsResource(request, current);
                    }
                });
    }

    private static List<Availability> list(HttpServletRequest request)
            throws SQLException {
        String serviceId = request.getParameter("serviceId");
        String venueId = request.getParameter("venueId");
        if (serviceId != null && venueId != null) {
            throw new IllegalArgumentException("Use either serviceId or venueId");
        }
        if (serviceId != null) {
            return DAO.getByServiceId(requiredParameter(request, "serviceId"));
        }
        if (venueId != null) {
            return DAO.getByVenueId(requiredParameter(request, "venueId"));
        }
        return DAO.getAllAvailability();
    }

    private static boolean ownsResource(HttpServletRequest request,
            Availability availability) throws SQLException {
        if (!SessionAuth.hasRole(request, "PROVIDER")) {
            return false;
        }
        long providerId = Policies.providerId(request);
        if (availability.getServiceId() != null) {
            Service service = SERVICE_DAO.getServiceById(availability.getServiceId());
            return service != null && service.getProviderId() != null
                    && service.getProviderId() == providerId;
        }
        if (availability.getVenueId() != null) {
            Venue venue = VENUE_DAO.getVenueById(availability.getVenueId());
            return venue != null && venue.getProviderId() != null
                    && venue.getProviderId() == providerId;
        }
        return false;
    }
}
