package com.Eventhub.controller;

import com.Eventhub.dao.ServiceProviderDAO;
import com.Eventhub.dao.UserDAO;
import com.Eventhub.model.ServiceProvider;
import com.Eventhub.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final ServiceProviderDAO providerDAO = new ServiceProviderDAO();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!"me".equals(request.getPathInfo() == null
                ? "" : request.getPathInfo().substring(1))) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Not found");
            return;
        }

        Long userId = SessionAuth.userId(request);
        if (userId == null) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required");
            return;
        }
        try {
            User user = userDAO.getUserById(userId);
            if (user == null || !"ACTIVE".equals(user.getStatus())) {
                request.getSession(false).invalidate();
                JsonCrudServlet.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Authentication required");
                return;
            }
            user.setPasswordHash(null);
            JsonCrudServlet.writeJson(response, HttpServletResponse.SC_OK, user);
        } catch (SQLException e) {
            reportDatabaseError(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String action = request.getPathInfo() == null
                ? "" : request.getPathInfo().substring(1);
        if ("logout".equals(action)) {
            logout(request, response);
            return;
        }
        try {
            JsonNode body = mapper.readTree(request.getInputStream());
            if (body == null || !body.isObject()) {
                JsonCrudServlet.sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "A JSON object is required");
                return;
            }

            switch (action) {
                case "register" -> register(body, request, response);
                case "login" -> login(body, request, response);
                default -> JsonCrudServlet.sendError(
                        response, HttpServletResponse.SC_NOT_FOUND, "Not found");
            }
        } catch (JsonProcessingException e) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid JSON body");
        } catch (SQLException e) {
            reportDatabaseError(response, e);
        } catch (GeneralSecurityException e) {
            getServletContext().log("Password hashing failed", e);
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Authentication operation failed");
        }
    }

    private void register(JsonNode body, HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, SQLException, GeneralSecurityException {
        String firstName = text(body, "firstName");
        String lastName = text(body, "lastName");
        String email = text(body, "email").toLowerCase(java.util.Locale.ROOT);
        String password = secret(body, "password");
        String phone = optionalText(body, "phone");

        if (firstName.isBlank() || lastName.isBlank()
                || firstName.length() > 100 || lastName.length() > 100
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || email.length() > 255
                || (phone != null && phone.length() > 20)
                || password.length() < 12 || password.length() > 1024) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Provide firstName, lastName, a valid email, and a password between 12 and 1024 characters");
            return;
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPasswordHash(PasswordUtil.hash(password));
        user.setPhone(phone);
        user.setId(userDAO.createUser(user));
        user.setRole("CUSTOMER");
        user.setStatus("ACTIVE");
        user.setPasswordHash(null);

        String csrfToken = establishSession(request, response, user, null);
        JsonCrudServlet.writeJson(response, HttpServletResponse.SC_CREATED,
                Map.of("user", user, "csrfToken", csrfToken));
    }

    private void login(JsonNode body, HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, SQLException, GeneralSecurityException {
        String email = text(body, "email").toLowerCase(java.util.Locale.ROOT);
        String password = secret(body, "password");
        if (password.length() > 1024) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid email or password");
            return;
        }
        User user = email.isBlank() ? null : userDAO.getUserByEmail(email);

        if (user == null || !"ACTIVE".equals(user.getStatus())
                || !PasswordUtil.verify(password, user.getPasswordHash())) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid email or password");
            return;
        }
        if (PasswordUtil.needsUpgrade(user.getPasswordHash())) {
            userDAO.updatePasswordHash(user.getId(), PasswordUtil.hash(password));
        }

        Long providerId = null;
        if ("PROVIDER".equals(user.getRole())) {
            ServiceProvider provider = providerDAO.getProviderByUserId(user.getId());
            if (provider != null && "ACTIVE".equals(provider.getStatus())) {
                providerId = provider.getId();
            }
        }

        user.setPasswordHash(null);
        String csrfToken = establishSession(request, response, user, providerId);
        JsonCrudServlet.writeJson(response, HttpServletResponse.SC_OK,
                Map.of("user", user, "csrfToken", csrfToken));
    }

    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!SessionAuth.isAuthenticated(request)) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required");
            return;
        }
        if (!SessionAuth.hasValidCsrfToken(request)) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Invalid CSRF token");
            return;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        Cookie cookie = newSessionCookie(request, "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private String establishSession(HttpServletRequest request,
            HttpServletResponse response, User user, Long providerId) {
        return SessionAuth.establish(request, user.getId(), user.getRole(), providerId);
    }

    private Cookie newSessionCookie(HttpServletRequest request, String value) {
        Cookie cookie = new Cookie("JSESSIONID", value);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    private static String text(JsonNode body, String field) {
        JsonNode value = body.get(field);
        return value == null || !value.isTextual() ? "" : value.asText().trim();
    }

    private static String optionalText(JsonNode body, String field) {
        JsonNode value = body.get(field);
        return value == null || value.isNull() || !value.isTextual()
                ? null : value.asText().trim();
    }

    private static String secret(JsonNode body, String field) {
        JsonNode value = body.get(field);
        return value == null || !value.isTextual() ? "" : value.asText();
    }

    private void reportDatabaseError(HttpServletResponse response, SQLException error)
            throws IOException {
        if (error.getErrorCode() == 1062) {
            JsonCrudServlet.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "An account with this email already exists");
            return;
        }
        getServletContext().log("Authentication database operation failed", error);
        JsonCrudServlet.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Authentication operation failed");
    }
}
