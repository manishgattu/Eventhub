package com.Eventhub.controller;

import com.Eventhub.dao.ServiceDAO;
import com.Eventhub.model.Service;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/services/*")
public class ServiceServlet extends JsonCrudServlet<Service> {

    private static final ServiceDAO DAO = new ServiceDAO();

    public ServiceServlet() {
        super(Service.class,
                ServiceServlet::list,
                (request, id) -> DAO.getServiceById(id),
                (request, service) -> {
                    if (!SessionAuth.isAdmin(request)) {
                        service.setProviderId(Policies.providerId(request));
                    }
                    return DAO.createService(service);
                },
                (request, service, id) -> {
                    service.setId(id);
                    return DAO.updateService(service);
                },
                (request, id) -> DAO.deleteService(id),
                Service::setId,
                Policies.providerOwned(Service::getProviderId,
                        service -> "ACTIVE".equals(service.getStatus())));
    }

    private static List<Service> list(HttpServletRequest request) throws SQLException {
        if (SessionAuth.isAdmin(request)) {
            String providerId = request.getParameter("providerId");
            if (providerId != null) {
                return DAO.getServicesByProviderId(
                        requiredParameter(request, "providerId"));
            }
            return DAO.getAllServices();
        }
        if (SessionAuth.hasRole(request, "PROVIDER")) {
            return DAO.getServicesByProviderId(Policies.providerId(request));
        }
        String categoryId = request.getParameter("categoryId");
        if (categoryId != null) {
            return DAO.getServicesByCategoryId(requiredParameter(request, "categoryId"))
                    .stream().filter(service -> "ACTIVE".equals(service.getStatus()))
                    .toList();
        }
        return DAO.getAllServices().stream()
                .filter(service -> "ACTIVE".equals(service.getStatus()))
                .toList();
    }
}
