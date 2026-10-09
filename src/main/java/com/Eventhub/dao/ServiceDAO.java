
package com.Eventhub.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.Eventhub.model.Service;
import com.Eventhub.util.DBConnection;

public class ServiceDAO {

    // 1. Retrieve all services
    public List<Service> getAllServices() throws SQLException {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM services ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                services.add(mapService(resultSet));
            }
        }

        return services;
    }

    // 2. Retrieve a service by ID
    public Service getServiceById(Long id) throws SQLException {
        String sql = "SELECT * FROM services WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapService(resultSet);
                }
            }
        }

        return null;
    }

    // 3. Retrieve services by provider ID
    public List<Service> getServicesByProviderId(Long providerId)
            throws SQLException {

        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM services WHERE provider_id = ? ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, providerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    services.add(mapService(resultSet));
                }
            }
        }

        return services;
    }

    // 4. Retrieve services by category ID
    public List<Service> getServicesByCategoryId(Long categoryId)
            throws SQLException {

        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM services WHERE category_id = ? ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    services.add(mapService(resultSet));
                }
            }
        }

        return services;
    }

    // 5. Create a new service
    public long createService(Service service) throws SQLException {
        String sql = "INSERT INTO services " +
                "(provider_id, category_id, name, description, price, " +
                "price_type, capacity, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, service.getProviderId());
            statement.setLong(2, service.getCategoryId());
            statement.setString(3, service.getName());
            statement.setString(4, service.getDescription());
            statement.setBigDecimal(5, service.getPrice());
            statement.setString(6, service.getPriceType());

            if (service.getCapacity() != null) {
                statement.setInt(7, service.getCapacity());
            } else {
                statement.setNull(7, java.sql.Types.INTEGER);
            }

            statement.setString(8,
                    service.getStatus() != null ? service.getStatus() : "ACTIVE");

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Creating service failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("No ID generated for the service.");
    }

    // 6. Update a service
    public boolean updateService(Service service) throws SQLException {
        String sql = "UPDATE services SET provider_id = ?, category_id = ?, " +
                "name = ?, description = ?, price = ?, price_type = ?, " +
                "capacity = ?, status = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, service.getProviderId());
            statement.setLong(2, service.getCategoryId());
            statement.setString(3, service.getName());
            statement.setString(4, service.getDescription());
            statement.setBigDecimal(5, service.getPrice());
            statement.setString(6, service.getPriceType());

            if (service.getCapacity() != null) {
                statement.setInt(7, service.getCapacity());
            } else {
                statement.setNull(7, java.sql.Types.INTEGER);
            }

            statement.setString(8, service.getStatus());
            statement.setLong(9, service.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // 7. Delete a service
    public boolean deleteService(Long id) throws SQLException {
        String sql = "DELETE FROM services WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    // Convert a database row into a Service object
    private Service mapService(ResultSet resultSet) throws SQLException {
        Service service = new Service();

        service.setId(resultSet.getLong("id"));
        service.setProviderId(resultSet.getLong("provider_id"));
        service.setCategoryId(resultSet.getLong("category_id"));
        service.setName(resultSet.getString("name"));
        service.setDescription(resultSet.getString("description"));
        service.setPrice(resultSet.getBigDecimal("price"));
        service.setPriceType(resultSet.getString("price_type"));

        int capacity = resultSet.getInt("capacity");
        service.setCapacity(resultSet.wasNull() ? null : capacity);

        service.setStatus(resultSet.getString("status"));
        service.setCreatedAt(resultSet.getTimestamp("created_at"));
        service.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return service;
    }
}
