
package com.Eventhub.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.Eventhub.model.ServiceCategory;
import com.Eventhub.util.DBConnection;

public class ServiceCategoryDAO {

    // 1. Retrieve all service categories
    public List<ServiceCategory> getAllCategories() throws SQLException {
        List<ServiceCategory> categories = new ArrayList<>();
        String sql = "SELECT id, name, description, created_at " +
                     "FROM service_categories ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                categories.add(mapCategory(resultSet));
            }
        }

        return categories;
    }

    // 2. Retrieve a category by ID
    public ServiceCategory getCategoryById(Long id) throws SQLException {
        String sql = "SELECT id, name, description, created_at " +
                     "FROM service_categories WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapCategory(resultSet);
                }
            }
        }

        return null;
    }

    // 3. Create a new category
    public long createCategory(ServiceCategory category) throws SQLException {
        String sql = "INSERT INTO service_categories (name, description) " +
                     "VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Creating service category failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("No ID generated for the category.");
    }

    // 4. Update an existing category
    public boolean updateCategory(ServiceCategory category)
            throws SQLException {

        String sql = "UPDATE service_categories " +
                     "SET name = ?, description = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setLong(3, category.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // 5. Delete a category
    public boolean deleteCategory(Long id) throws SQLException {
        String sql = "DELETE FROM service_categories WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    // Convert a database row into a ServiceCategory object
    private ServiceCategory mapCategory(ResultSet resultSet)
            throws SQLException {

        ServiceCategory category = new ServiceCategory();

        category.setId(resultSet.getLong("id"));
        category.setName(resultSet.getString("name"));
        category.setDescription(resultSet.getString("description"));
        category.setCreatedAt(resultSet.getTimestamp("created_at"));

        return category;
    }
}
