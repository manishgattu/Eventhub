package com.Eventhub.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Function;
import java.util.function.Predicate;

final class Policies {

    private Policies() {
    }

    static <T> JsonCrudServlet.Policy<T> adminOnly() {
        return new JsonCrudServlet.Policy<>() {
            @Override
            public boolean canRead(HttpServletRequest request, T entity) {
                return SessionAuth.isAdmin(request);
            }

            @Override
            public boolean canCreate(HttpServletRequest request, T entity) {
                return SessionAuth.isAdmin(request);
            }

            @Override
            public boolean canUpdate(HttpServletRequest request, T current, T proposed) {
                return SessionAuth.isAdmin(request);
            }

            @Override
            public boolean canDelete(HttpServletRequest request, T current) {
                return SessionAuth.isAdmin(request);
            }
        };
    }

    static <T> JsonCrudServlet.Policy<T> readForUsersWriteForAdmins() {
        return new JsonCrudServlet.Policy<>() {
            @Override
            public boolean canRead(HttpServletRequest request, T entity) {
                return SessionAuth.isAuthenticated(request);
            }

            @Override
            public boolean canCreate(HttpServletRequest request, T entity) {
                return SessionAuth.isAdmin(request);
            }

            @Override
            public boolean canUpdate(HttpServletRequest request, T current, T proposed) {
                return SessionAuth.isAdmin(request);
            }

            @Override
            public boolean canDelete(HttpServletRequest request, T current) {
                return SessionAuth.isAdmin(request);
            }
        };
    }

    static <T> JsonCrudServlet.Policy<T> customerOwned(
            Function<T, Long> ownerId, boolean readAny, boolean allowCustomerUpdate) {
        return new JsonCrudServlet.Policy<>() {
            private boolean owns(HttpServletRequest request, T entity) {
                Long userId = SessionAuth.userId(request);
                return userId != null
                        && SessionAuth.hasRole(request, "CUSTOMER")
                        && userId.equals(ownerId.apply(entity));
            }

            @Override
            public boolean canRead(HttpServletRequest request, T entity) {
                return SessionAuth.isAdmin(request)
                        || (readAny && SessionAuth.isAuthenticated(request))
                        || owns(request, entity);
            }

            @Override
            public boolean canCreate(HttpServletRequest request, T entity) {
                return SessionAuth.isAdmin(request)
                        || SessionAuth.hasRole(request, "CUSTOMER");
            }

            @Override
            public boolean canUpdate(HttpServletRequest request, T current, T proposed) {
                return SessionAuth.isAdmin(request)
                        || (allowCustomerUpdate && owns(request, current)
                                && owns(request, proposed));
            }

            @Override
            public boolean canDelete(HttpServletRequest request, T current) {
                return SessionAuth.isAdmin(request) || owns(request, current);
            }
        };
    }

    static <T> JsonCrudServlet.Policy<T> providerOwned(
            Function<T, Long> providerId, Predicate<T> customerCanRead) {
        return new JsonCrudServlet.Policy<>() {
            private boolean owns(HttpServletRequest request, T entity) {
                Object sessionProviderId = request.getSession(false) == null ? null
                        : request.getSession(false).getAttribute(SessionAuth.PROVIDER_ID);
                return SessionAuth.hasRole(request, "PROVIDER")
                        && sessionProviderId instanceof Long id
                        && Long.valueOf(id).equals(providerId.apply(entity));
            }

            @Override
            public boolean canRead(HttpServletRequest request, T entity) {
                return SessionAuth.isAdmin(request) || owns(request, entity)
                        || (SessionAuth.hasRole(request, "CUSTOMER")
                                && customerCanRead.test(entity));
            }

            @Override
            public boolean canCreate(HttpServletRequest request, T entity) {
                return SessionAuth.isAdmin(request) || owns(request, entity);
            }

            @Override
            public boolean canUpdate(HttpServletRequest request, T current, T proposed) {
                return SessionAuth.isAdmin(request)
                        || (owns(request, current) && owns(request, proposed));
            }

            @Override
            public boolean canDelete(HttpServletRequest request, T current) {
                return SessionAuth.isAdmin(request) || owns(request, current);
            }
        };
    }

    static <T> JsonCrudServlet.Policy<T> providerProfile(
            Function<T, Long> ownerUserId, Predicate<T> customerCanRead) {
        return new JsonCrudServlet.Policy<>() {
            private boolean owns(HttpServletRequest request, T entity) {
                Long userId = SessionAuth.userId(request);
                return userId != null && SessionAuth.hasRole(request, "PROVIDER")
                        && userId.equals(ownerUserId.apply(entity));
            }

            @Override
            public boolean canRead(HttpServletRequest request, T entity) {
                return SessionAuth.isAdmin(request) || owns(request, entity)
                        || (SessionAuth.hasRole(request, "CUSTOMER")
                                && customerCanRead.test(entity));
            }

            @Override
            public boolean canCreate(HttpServletRequest request, T entity) {
                return SessionAuth.isAdmin(request) || owns(request, entity);
            }

            @Override
            public boolean canUpdate(HttpServletRequest request, T current, T proposed) {
                return SessionAuth.isAdmin(request)
                        || (owns(request, current) && owns(request, proposed));
            }

            @Override
            public boolean canDelete(HttpServletRequest request, T current) {
                return SessionAuth.isAdmin(request) || owns(request, current);
            }
        };
    }

    static long providerId(HttpServletRequest request) {
        Object value = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute(SessionAuth.PROVIDER_ID);
        if (value instanceof Long id) {
            return id;
        }
        throw new IllegalArgumentException("An active provider profile is required");
    }
}
