package com.Eventhub.controller;

import com.Eventhub.dao.AddressDAO;
import com.Eventhub.model.Address;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/addresses/*")
public class AddressServlet extends JsonCrudServlet<Address> {

    private static final AddressDAO DAO = new AddressDAO();

    public AddressServlet() {
        super(Address.class,
                AddressServlet::list,
                (request, id) -> DAO.getAddressById(id),
                (request, address) -> {
                    if (!SessionAuth.isAdmin(request)) {
                        address.setUserId(SessionAuth.userId(request));
                    }
                    return DAO.createAddress(address);
                },
                (request, address, id) -> {
                    address.setId(id);
                    if (!SessionAuth.isAdmin(request)) {
                        address.setUserId(SessionAuth.userId(request));
                    }
                    return DAO.updateAddress(address);
                },
                (request, id) -> {
                    Address address = DAO.getAddressById(id);
                    long userId = SessionAuth.isAdmin(request)
                            ? address.getUserId() : SessionAuth.userId(request);
                    return DAO.deleteAddress(id, userId);
                },
                Address::setId,
                Policies.customerOwned(Address::getUserId, false, true));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String path = request.getPathInfo();
        if (path == null || !path.matches("/[1-9][0-9]*/default")) {
            super.doPost(request, response);
            return;
        }
        if (!requireWriteSession(request, response)) {
            return;
        }

        try {
            long addressId = Long.parseLong(path.substring(1, path.length() - "/default".length()));
            Address address = DAO.getAddressById(addressId);
            if (address == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Not found");
                return;
            }
            Long userId = SessionAuth.userId(request);
            if (!SessionAuth.isAdmin(request)
                    && (!SessionAuth.hasRole(request, "CUSTOMER")
                            || !userId.equals(address.getUserId()))) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                return;
            }
            long ownerId = SessionAuth.isAdmin(request) ? address.getUserId() : userId;
            if (DAO.setDefaultAddress(addressId, ownerId)) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Not found");
            }
        } catch (SQLException e) {
            getServletContext().log("Setting default address failed", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database operation failed");
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid address ID");
        }
    }

    private static java.util.List<Address> list(HttpServletRequest request)
            throws SQLException {
        long userId = SessionAuth.isAdmin(request)
                ? requiredParameter(request, "userId")
                : SessionAuth.userId(request);
        if ("true".equalsIgnoreCase(request.getParameter("default"))) {
            Address address = DAO.getDefaultAddress(userId);
            return address == null ? List.of() : List.of(address);
        }
        return DAO.getAddressesByUserId(userId);
    }
}
