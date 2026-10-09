
package com.Eventhub.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.Eventhub.model.ServiceProvider;
import com.Eventhub.util.DBConnection;

public class ServiceProviderDAO {

    // 1. Retrieve all service providers
    public List<ServiceProvider> getAllProviders() throws SQLException {
        List<ServiceProvider> providers = new ArrayList<>();

        String sql = "SELECT * FROM service_providers ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                providers.add(mapProvider(resultSet));
            }
        }

        return providers;
    }

    // 2. Retrieve a provider by ID
    public ServiceProvider getProviderById(Long id) throws SQLException {
        String sql = "SELECT * FROM service_providers WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapProvider(resultSet);
                }
            }
        }

        return null;
    }

    // 3. Retrieve a provider by user ID
    public ServiceProvider getProviderByUserId(Long userId)
            throws SQLException {

        String sql = "SELECT * FROM service_providers WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapProvider(resultSet);
                }
            }
        }

        return null;
    }

    // 4. Create a new service provider
    public long createProvider(ServiceProvider provider) throws SQLException {
        String sql = "INSERT INTO service_providers " +
                "(user_id, business_name, description, phone, email, website, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, provider.getUserId());
            statement.setString(2, provider.getBusinessName());
            statement.setString(3, provider.getDescription());
            statement.setString(4, provider.getPhone());
            statement.setString(5, provider.getEmail());
            statement.setString(6, provider.getWebsite());

            statement.setString(7,
                    provider.getStatus() != null
                            ? provider.getStatus()
                            : "PENDING");

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Creating service provider failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("No ID generated for the service provider.");
    }

    // 5. Update a service provider
    public boolean updateProvider(ServiceProvider provider)
            throws SQLException {

        String sql = "UPDATE service_providers SET business_name = ?, " +
                "description = ?, phone = ?, email = ?, website = ?, " +
                "status = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, provider.getBusinessName());
            statement.setString(2, provider.getDescription());
            statement.setString(3, provider.getPhone());
            statement.setString(4, provider.getEmail());
            statement.setString(5, provider.getWebsite());
            statement.setString(6, provider.getStatus());
            statement.setLong(7, provider.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // 6. Delete a service provider
    public boolean deleteProvider(Long id) throws SQLException {
        String sql = "DELETE FROM service_providers WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    // Convert a database row into a ServiceProvider object
    private ServiceProvider mapProvider(ResultSet resultSet)
            throws SQLException {

        ServiceProvider provider = new ServiceProvider();

        provider.setId(resultSet.getLong("id"));
        provider.setUserId(resultSet.getLong("user_id"));
        provider.setBusinessName(resultSet.getString("business_name"));
        provider.setDescription(resultSet.getString("description"));
        provider.setPhone(resultSet.getString("phone"));
        provider.setEmail(resultSet.getString("email"));
        provider.setWebsite(resultSet.getString("website"));
        provider.setStatus(resultSet.getString("status"));
        provider.setCreatedAt(resultSet.getTimestamp("created_at"));
        provider.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return provider;
    }
}
