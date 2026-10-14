package com.Eventhub.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

final class SessionAuth {

    static final String USER_ID = "eventhub.userId";
    static final String ROLE = "eventhub.role";
    static final String PROVIDER_ID = "eventhub.providerId";
    static final String CSRF_TOKEN = "eventhub.csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    private SessionAuth() {
    }

    static Long userId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Long) session.getAttribute(USER_ID);
    }

    static String role(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (String) session.getAttribute(ROLE);
    }

    static boolean isAdmin(HttpServletRequest request) {
        return "ADMIN".equals(role(request));
    }

    static boolean hasRole(HttpServletRequest request, String role) {
        return role.equals(SessionAuth.role(request));
    }

    static boolean isAuthenticated(HttpServletRequest request) {
        return userId(request) != null;
    }

    static String establish(
            HttpServletRequest request, Long userId, String role, Long providerId) {
        HttpSession existing = request.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(USER_ID, userId);
        session.setAttribute(ROLE, role);
        if (providerId != null) {
            session.setAttribute(PROVIDER_ID, providerId);
        }
        session.setMaxInactiveInterval(30 * 60);
        byte[] token = new byte[32];
        RANDOM.nextBytes(token);
        String csrfToken = Base64.getUrlEncoder().withoutPadding().encodeToString(token);
        session.setAttribute(CSRF_TOKEN, csrfToken);
        return csrfToken;
    }

    static boolean hasValidCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        String expected = (String) session.getAttribute(CSRF_TOKEN);
        String supplied = request.getHeader("X-CSRF-Token");
        return expected != null && supplied != null
                && java.security.MessageDigest.isEqual(
                        expected.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        supplied.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
