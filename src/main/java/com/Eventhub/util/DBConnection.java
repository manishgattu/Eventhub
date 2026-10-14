package com.Eventhub.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/event_booking_system";
    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";

    public static Connection getConnection() throws SQLException {
        String url = getEnvironmentVariable("DB_URL", DEFAULT_URL);
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (user == null || user.isBlank()) {
            throw new SQLException(
                    "DB_USER is not set. Configure the database username "
                            + "as an environment variable before connecting.");
        }
        if (password == null || password.isBlank()) {
            throw new SQLException(
                    "DB_PASSWORD is not set. Configure the database credentials "
                            + "as environment variables before connecting.");
        }

        try {
            Class.forName(MYSQL_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MySQL Connector/J is missing from the application runtime.", e);
        }

        return DriverManager.getConnection(url, user, password);
    }

    private static String getEnvironmentVariable(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}