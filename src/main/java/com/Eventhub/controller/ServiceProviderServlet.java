package com.Eventhub.controller;

import com.Eventhub.dao.ServiceProviderDAO;
import com.Eventhub.model.ServiceProvider;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/api/service-providers/*")
public class ServiceProviderServlet extends JsonCrudServlet<ServiceProvider> {

    private static final ServiceProviderDAO DAO = new ServiceProviderDAO();

    public ServiceProviderServlet() {
        super(ServiceProvider.class,
                (request) -> DAO.getAllProviders(),
                (request, id) -> DAO.getProviderById(id),
                (request, provider) -> {
                    if (!SessionAuth.isAdmin(request)) {
                        provider.setUserId(SessionAuth.userId(request));
                        provider.setStatus("PENDING");
                    }
                    return DAO.createProvider(provider);
                },
                (request, provider, id) -> {
                    provider.setId(id);
                    if (!SessionAuth.isAdmin(request)) {
                        var current = DAO.getProviderById(id);
                        if (current == null) {
                            return false;
                        }
                        provider.setStatus(current.getStatus());
                    }
                    return DAO.updateProvider(provider);
                },
                (request, id) -> DAO.deleteProvider(id),
                ServiceProvider::setId,
                Policies.providerProfile(ServiceProvider::getUserId,
                        provider -> "ACTIVE".equals(provider.getStatus())));
    }
}
