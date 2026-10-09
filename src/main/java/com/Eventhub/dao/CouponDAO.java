
package com.Eventhub.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.Eventhub.model.Coupon;
import com.Eventhub.util.DBConnection;

public class CouponDAO {

    // 1. Retrieve all coupons
    public List<Coupon> getAllCoupons() throws SQLException {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT * FROM coupons ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                coupons.add(mapCoupon(resultSet));
            }
        }

        return coupons;
    }

    // 2. Retrieve a coupon by ID
    public Coupon getCouponById(Long id) throws SQLException {
        String sql = "SELECT * FROM coupons WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapCoupon(resultSet);
                }
            }
        }

        return null;
    }

    // 3. Retrieve a coupon by code
    public Coupon getCouponByCode(String code) throws SQLException {
        String sql = "SELECT * FROM coupons WHERE code = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapCoupon(resultSet);
                }
            }
        }

        return null;
    }

    // 4. Retrieve active coupons
    public List<Coupon> getActiveCoupons() throws SQLException {
        List<Coupon> coupons = new ArrayList<>();

        String sql = "SELECT * FROM coupons " +
                "WHERE status = 'ACTIVE' " +
                "AND valid_from <= CURRENT_DATE " +
                "AND valid_until >= CURRENT_DATE " +
                "AND (usage_limit IS NULL OR used_count < usage_limit) " +
                "ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                coupons.add(mapCoupon(resultSet));
            }
        }

        return coupons;
    }

    // 5. Create a coupon
    public long createCoupon(Coupon coupon) throws SQLException {
        String sql = "INSERT INTO coupons " +
                "(code, description, discount_type, discount_value, " +
                "minimum_amount, maximum_discount, valid_from, valid_until, " +
                "usage_limit, used_count, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, coupon.getCode());
            statement.setString(2, coupon.getDescription());
            statement.setString(3, coupon.getDiscountType());
            statement.setBigDecimal(4, coupon.getDiscountValue());
            statement.setBigDecimal(5, coupon.getMinimumAmount());
            statement.setBigDecimal(6, coupon.getMaximumDiscount());
            statement.setDate(7, coupon.getValidFrom());
            statement.setDate(8, coupon.getValidUntil());

            if (coupon.getUsageLimit() != null) {
                statement.setInt(9, coupon.getUsageLimit());
            } else {
                statement.setNull(9, java.sql.Types.INTEGER);
            }

            statement.setInt(10,
                    coupon.getUsedCount() != null ? coupon.getUsedCount() : 0);

            statement.setString(11,
                    coupon.getStatus() != null ? coupon.getStatus() : "ACTIVE");

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Creating coupon failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("No ID generated for the coupon.");
    }

    // 6. Update coupon details
    public boolean updateCoupon(Coupon coupon) throws SQLException {
        String sql = "UPDATE coupons SET description = ?, discount_type = ?, " +
                "discount_value = ?, minimum_amount = ?, maximum_discount = ?, " +
                "valid_from = ?, valid_until = ?, usage_limit = ?, " +
                "status = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, coupon.getDescription());
            statement.setString(2, coupon.getDiscountType());
            statement.setBigDecimal(3, coupon.getDiscountValue());
            statement.setBigDecimal(4, coupon.getMinimumAmount());
            statement.setBigDecimal(5, coupon.getMaximumDiscount());
            statement.setDate(6, coupon.getValidFrom());
            statement.setDate(7, coupon.getValidUntil());

            if (coupon.getUsageLimit() != null) {
                statement.setInt(8, coupon.getUsageLimit());
            } else {
                statement.setNull(8, java.sql.Types.INTEGER);
            }

            statement.setString(9, coupon.getStatus());
            statement.setLong(10, coupon.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // 7. Update coupon status
    public boolean updateCouponStatus(Long id, String status)
            throws SQLException {

        String sql = "UPDATE coupons SET status = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setLong(2, id);

            return statement.executeUpdate() > 0;
        }
    }

    // 8. Delete a coupon
    public boolean deleteCoupon(Long id) throws SQLException {
        String sql = "DELETE FROM coupons WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    // Convert a database row into a Coupon object
    private Coupon mapCoupon(ResultSet resultSet) throws SQLException {
        Coupon coupon = new Coupon();

        coupon.setId(resultSet.getLong("id"));
        coupon.setCode(resultSet.getString("code"));
        coupon.setDescription(resultSet.getString("description"));
        coupon.setDiscountType(resultSet.getString("discount_type"));
        coupon.setDiscountValue(resultSet.getBigDecimal("discount_value"));
        coupon.setMinimumAmount(resultSet.getBigDecimal("minimum_amount"));
        coupon.setMaximumDiscount(resultSet.getBigDecimal("maximum_discount"));
        coupon.setValidFrom(resultSet.getDate("valid_from"));
        coupon.setValidUntil(resultSet.getDate("valid_until"));

        int usageLimit = resultSet.getInt("usage_limit");
        coupon.setUsageLimit(resultSet.wasNull() ? null : usageLimit);

        coupon.setUsedCount(resultSet.getInt("used_count"));
        coupon.setStatus(resultSet.getString("status"));
        coupon.setCreatedAt(resultSet.getTimestamp("created_at"));

        return coupon;
    }
}
