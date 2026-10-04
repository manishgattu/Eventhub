 package com.Eventhub.controller;

import java.io.IOException;

import com.Eventhub.dao.UserDAO;
import com.Eventhub.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

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

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"message\":\"Invalid user ID\"}"
            );
        }
    }
}