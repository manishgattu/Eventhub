
package com.Eventhub.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.Eventhub.model.Address;
import com.Eventhub.util.DBConnection;

public class AddressDAO {

    // 1. Retrieve all addresses for a user
    public List<Address> getAddressesByUserId(Long userId)
            throws SQLException {

        List<Address> addresses = new ArrayList<>();
        String sql = "SELECT * FROM addresses WHERE user_id = ? ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    addresses.add(mapAddress(resultSet));
                }
            }
        }

        return addresses;
    }

    // 2. Retrieve an address by ID
    public Address getAddressById(Long id) throws SQLException {
        String sql = "SELECT * FROM addresses WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAddress(resultSet);
                }
            }
        }

        return null;
    }

    // 3. Retrieve a user's default address
    public Address getDefaultAddress(Long userId) throws SQLException {
        String sql = "SELECT * FROM addresses " +
                     "WHERE user_id = ? AND is_default = TRUE LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAddress(resultSet);
                }
            }
        }

        return null;
    }

    // 4. Create a new address
    public long createAddress(Address address) throws SQLException {
        String sql = "INSERT INTO addresses " +
                "(user_id, address_type, address_line1, address_line2, " +
                "city, state, postal_code, country, is_default) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, address.getUserId());
            statement.setString(2, address.getAddressType());
            statement.setString(3, address.getAddressLine1());
            statement.setString(4, address.getAddressLine2());
            statement.setString(5, address.getCity());
            statement.setString(6, address.getState());
            statement.setString(7, address.getPostalCode());
            statement.setString(8,
                    address.getCountry() != null
                            ? address.getCountry()
                            : "India");

            statement.setBoolean(9,
                    Boolean.TRUE.equals(address.getIsDefault()));

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Creating address failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("No ID generated for the address.");
    }

    // 5. Update an address
    public boolean updateAddress(Address address) throws SQLException {
        String sql = "UPDATE addresses SET address_type = ?, " +
                "address_line1 = ?, address_line2 = ?, city = ?, state = ?, " +
                "postal_code = ?, country = ? WHERE id = ? AND user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, address.getAddressType());
            statement.setString(2, address.getAddressLine1());
            statement.setString(3, address.getAddressLine2());
            statement.setString(4, address.getCity());
            statement.setString(5, address.getState());
            statement.setString(6, address.getPostalCode());
            statement.setString(7, address.getCountry());
            statement.setLong(8, address.getId());
            statement.setLong(9, address.getUserId());

            return statement.executeUpdate() > 0;
        }
    }

    // 6. Set an address as the user's default address
    public boolean setDefaultAddress(Long addressId, Long userId)
            throws SQLException {

        String updateSql = "UPDATE addresses SET is_default = FALSE " +
                           "WHERE user_id = ?";

        String defaultSql = "UPDATE addresses SET is_default = TRUE " +
                            "WHERE id = ? AND user_id = ?";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement =
                             connection.prepareStatement(updateSql)) {
                    statement.setLong(1, userId);
                    statement.executeUpdate();
                }

                int rowsUpdated;

                try (PreparedStatement statement =
                             connection.prepareStatement(defaultSql)) {
                    statement.setLong(1, addressId);
                    statement.setLong(2, userId);
                    rowsUpdated = statement.executeUpdate();
                }

                if (rowsUpdated == 0) {
                    connection.rollback();
                    return false;
                }

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    // 7. Delete an address
    public boolean deleteAddress(Long id, Long userId) throws SQLException {
        String sql = "DELETE FROM addresses WHERE id = ? AND user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            statement.setLong(2, userId);

            return statement.executeUpdate() > 0;
        }
    }

    // Convert a database row into an Address object
    private Address mapAddress(ResultSet resultSet) throws SQLException {
        Address address = new Address();

        address.setId(resultSet.getLong("id"));
        address.setUserId(resultSet.getLong("user_id"));
        address.setAddressType(resultSet.getString("address_type"));
        address.setAddressLine1(resultSet.getString("address_line1"));
        address.setAddressLine2(resultSet.getString("address_line2"));
        address.setCity(resultSet.getString("city"));
        address.setState(resultSet.getString("state"));
        address.setPostalCode(resultSet.getString("postal_code"));
        address.setCountry(resultSet.getString("country"));
        address.setIsDefault(resultSet.getBoolean("is_default"));
        address.setCreatedAt(resultSet.getTimestamp("created_at"));

        return address;
    }
}
