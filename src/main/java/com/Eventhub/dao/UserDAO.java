 package com.Eventhub.dao;

import com.Eventhub.model.User;
import com.Eventhub.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public User getUserById(Long id) throws SQLException {

        String sql = "SELECT id, first_name, last_name, email, password_hash, " +
                     "phone, role, status FROM users WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        }
    }

    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT id, first_name, last_name, email, password_hash, "
                + "phone, role, status FROM users WHERE email = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        }
    }

    public long createUser(User user) throws SQLException {
        String sql = "INSERT INTO users "
                + "(first_name, last_name, email, password_hash, phone, role, status) "
                + "VALUES (?, ?, ?, ?, ?, 'CUSTOMER', 'ACTIVE')";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPasswordHash());
            statement.setString(5, user.getPhone());

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Creating user failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("No ID generated for the user.");
    }

    public void updatePasswordHash(long userId, String passwordHash)
            throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }

    public boolean promoteToProvider(Long id) throws SQLException {
        String sql = "UPDATE users SET role = 'PROVIDER' "
                + "WHERE id = ? AND role = 'CUSTOMER' AND status = 'ACTIVE'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setPhone(resultSet.getString("phone"));
        user.setRole(resultSet.getString("role"));
        user.setStatus(resultSet.getString("status"));
        return user;
    }
}