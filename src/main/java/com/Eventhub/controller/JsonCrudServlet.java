package com.Eventhub.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

abstract class JsonCrudServlet<T> extends HttpServlet {

    @FunctionalInterface
    interface ListOperation<T> {
        List<T> apply(HttpServletRequest request) throws SQLException;
    }

    @FunctionalInterface
    interface FindOperation<T> {
        T apply(HttpServletRequest request, long id) throws SQLException;
    }

    @FunctionalInterface
    interface CreateOperation<T> {
        Object apply(HttpServletRequest request, T entity) throws SQLException;
    }

    @FunctionalInterface
    interface UpdateOperation<T> {
        boolean apply(HttpServletRequest request, T entity, long id) throws SQLException;
    }

    @FunctionalInterface
    interface DeleteOperation {
        boolean apply(HttpServletRequest request, long id) throws SQLException;
    }

    interface Policy<T> {
        boolean canRead(HttpServletRequest request, T entity) throws SQLException;

        boolean canCreate(HttpServletRequest request, T entity) throws SQLException;

        boolean canUpdate(HttpServletRequest request, T current, T proposed)
                throws SQLException;

        boolean canDelete(HttpServletRequest request, T current) throws SQLException;
    }

    private final Class<T> entityType;
    private final ListOperation<T> listOperation;
    private final FindOperation<T> findOperation;
    private final CreateOperation<T> createOperation;
    private final UpdateOperation<T> updateOperation;
    private final DeleteOperation deleteOperation;
    private final BiConsumer<T, Long> setId;
    private final Policy<T> policy;
    private final ObjectMapper mapper = new ObjectMapper();

    JsonCrudServlet(
            Class<T> entityType,
            ListOperation<T> listOperation,
            FindOperation<T> findOperation,
            CreateOperation<T> createOperation,
            UpdateOperation<T> updateOperation,
            DeleteOperation deleteOperation,
            BiConsumer<T, Long> setId,
            Policy<T> policy) {
        this.entityType = entityType;
        this.listOperation = listOperation;
        this.findOperation = findOperation;
        this.createOperation = createOperation;
        this.updateOperation = updateOperation;
        this.deleteOperation = deleteOperation;
        this.setId = setId;
        this.policy = policy;
    }

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!SessionAuth.isAuthenticated(request)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }
        try {
            String path = request.getPathInfo();
            if (path == null || "/".equals(path)) {
                if (listOperation == null) {
                    methodNotAllowed(response);
                    return;
                }
                List<T> entities = listOperation.apply(request);
                List<T> visibleEntities = new ArrayList<>();
                for (T entity : entities) {
                    if (policy.canRead(request, entity)) {
                        visibleEntities.add(entity);
                    }
                }
                writeJson(response, HttpServletResponse.SC_OK, visibleEntities);
                return;
            }

            if (findOperation == null) {
                methodNotAllowed(response);
                return;
            }
            long id = parseId(path);
            T entity = findOperation.apply(request, id);
            if (entity == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Not found");
            } else if (!policy.canRead(request, entity)) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            } else {
                writeJson(response, HttpServletResponse.SC_OK, entity);
            }
        } catch (IllegalArgumentException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            reportDatabaseError(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!requireWriteSession(request, response)) {
            return;
        }
        if (request.getPathInfo() != null && !"/".equals(request.getPathInfo())) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "POST must target the collection URL");
            return;
        }
        if (createOperation == null) {
            methodNotAllowed(response);
            return;
        }
        try {
            T entity = readEntity(request);
            if (!policy.canCreate(request, entity)) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                return;
            }
            Object result = createOperation.apply(request, entity);
            writeJson(response, HttpServletResponse.SC_CREATED,
                    result instanceof Number ? Map.of("id", result) : Map.of("created", result));
        } catch (JsonProcessingException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
        } catch (IllegalArgumentException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            reportDatabaseError(response, e);
        }
    }

    @Override
    protected final void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!requireWriteSession(request, response)) {
            return;
        }
        if (updateOperation == null) {
            methodNotAllowed(response);
            return;
        }
        try {
            long id = parseId(request.getPathInfo());
            T current = findOperation == null ? null : findOperation.apply(request, id);
            if (current == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Not found");
                return;
            }
            T proposed = readEntity(request);
            setId.accept(proposed, id);
            if (!policy.canUpdate(request, current, proposed)) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                return;
            }
            if (updateOperation.apply(request, proposed, id)) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Not found");
            }
        } catch (JsonProcessingException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
        } catch (IllegalArgumentException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            reportDatabaseError(response, e);
        }
    }

    @Override
    protected final void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!requireWriteSession(request, response)) {
            return;
        }
        if (deleteOperation == null) {
            methodNotAllowed(response);
            return;
        }
        try {
            long id = parseId(request.getPathInfo());
            T current = findOperation == null ? null : findOperation.apply(request, id);
            if (current == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Not found");
                return;
            }
            if (!policy.canDelete(request, current)) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                return;
            }
            if (deleteOperation.apply(request, id)) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Not found");
            }
        } catch (IllegalArgumentException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            reportDatabaseError(response, e);
        }
    }

    private T readEntity(HttpServletRequest request) throws IOException {
        T entity = mapper.readValue(request.getInputStream(), entityType);
        if (entity == null) {
            throw new IllegalArgumentException("A JSON request body is required");
        }
        return entity;
    }

    protected final boolean requireWriteSession(
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!SessionAuth.isAuthenticated(request)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return false;
        }
        if (!SessionAuth.hasValidCsrfToken(request)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
            return false;
        }
        return true;
    }

    static long parseId(String path) {
        if (path == null || !path.matches("/[1-9][0-9]*")) {
            throw new IllegalArgumentException("A positive numeric ID is required");
        }
        try {
            return Long.parseLong(path.substring(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ID");
        }
    }

    static long requiredParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || !value.matches("[1-9][0-9]*")) {
            throw new IllegalArgumentException(name + " must be a positive integer");
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " is invalid");
        }
    }

    static void writeJson(HttpServletResponse response, int status, Object value)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        new ObjectMapper().writeValue(response.getWriter(), value);
    }

    static void sendError(HttpServletResponse response, int status, String message)
            throws IOException {
        writeJson(response, status, Map.of("error", message == null ? "Request failed" : message));
    }

    private static void methodNotAllowed(HttpServletResponse response) throws IOException {
        sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "This operation is not supported");
    }

    private void reportDatabaseError(HttpServletResponse response, SQLException error)
            throws IOException {
        getServletContext().log("Database operation failed", error);
        sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Database operation failed");
    }
}
