 package com.Eventhub.controller;

import java.io.IOException;

import com.Eventhub.dao.UserDAO;
import com.Eventhub.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SessionAuth.isAuthenticated(request)) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required");
            return;
        }
        if (!SessionAuth.isAdmin(request)) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Forbidden");
            return;
        }
        if (!SessionAuth.hasValidCsrfToken(request)) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Invalid CSRF token");
            return;
        }

        String path = request.getPathInfo();
        if (path == null || !path.matches("/[1-9][0-9]*/role")) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Expected /{userId}/role");
            return;
        }

        try {
            long userId = Long.parseLong(path.substring(1, path.length() - "/role".length()));
            JsonNode body = objectMapper.readTree(request.getInputStream());
            if (body == null || !"PROVIDER".equals(body.path("role").asText())) {
                JsonCrudServlet.sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Only promotion to PROVIDER is supported");
                return;
            }
            if (userDAO.promoteToProvider(userId)) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                JsonCrudServlet.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                        "Active customer account not found");
            }
        } catch (JsonProcessingException e) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid JSON body");
        } catch (NumberFormatException e) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid user ID");
        } catch (SQLException e) {
            getServletContext().log("User role update failed", e);
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database operation failed");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        if (path == null || path.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("User ID is required.");
            return;
        }

        try {
            Long userId = Long.parseLong(path.substring(1));
            Long sessionUserId = SessionAuth.userId(request);
            if (sessionUserId == null) {
                JsonCrudServlet.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Authentication required");
                return;
            }
            if (!SessionAuth.isAdmin(request) && !sessionUserId.equals(userId)) {
                JsonCrudServlet.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Forbidden");
                return;
            }

            User user = userDAO.getUserById(userId);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            if (user != null) {

                // Don't expose the password hash through the API
                user.setPasswordHash(null);

                objectMapper.writeValue(
                        response.getWriter(),
                        user
                );

            } else {

                response.setStatus(HttpServletResponse.SC_NOT_FOUND);

                response.getWriter().write(
                        "{\"message\":\"User not found\"}"
                );
            }

        } catch (NumberFormatException e) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid user ID");
        } catch (SQLException e) {
            getServletContext().log("User lookup failed", e);
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database operation failed");
        }
    }
}