
package com.Eventhub.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.Eventhub.model.BookingCoupon;
import com.Eventhub.util.DBConnection;

public class BookingCouponDAO {

    // Get all booking coupons
    public List<BookingCoupon> getAllBookingCoupons() throws SQLException {
        List<BookingCoupon> coupons = new ArrayList<>();

        String sql = "SELECT * FROM booking_coupons ORDER BY id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                coupons.add(mapBookingCoupon(rs));
            }
        }

        return coupons;
    }

    // Get booking coupon by ID
    public BookingCoupon getBookingCouponById(Long id)
            throws SQLException {

        String sql = "SELECT * FROM booking_coupons WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBookingCoupon(rs);
                }
            }
        }

        return null;
    }

    // Get coupons applied to a particular booking
    public List<BookingCoupon> getByBookingId(Long bookingId)
            throws SQLException {

        List<BookingCoupon> coupons = new ArrayList<>();

        String sql =
                "SELECT * FROM booking_coupons WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    coupons.add(mapBookingCoupon(rs));
                }
            }
        }

        return coupons;
    }

    // Apply a coupon to a booking
    public boolean createBookingCoupon(BookingCoupon coupon)
            throws SQLException {

        String sql =
                "INSERT INTO booking_coupons " +
                "(booking_id, coupon_id, discount_amount) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, coupon.getBookingId());
            ps.setLong(2, coupon.getCouponId());
            ps.setBigDecimal(3, coupon.getDiscountAmount());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        coupon.setId(keys.getLong(1));
                    }
                }
                return true;
            }
        }

        return false;
    }

    // Update the discount amount
    public boolean updateBookingCoupon(BookingCoupon coupon)
            throws SQLException {

        String sql =
                "UPDATE booking_coupons SET discount_amount = ? " +
                "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, coupon.getDiscountAmount());
            ps.setLong(2, coupon.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // Remove a coupon from a booking
    public boolean deleteBookingCoupon(Long id)
            throws SQLException {

        String sql = "DELETE FROM booking_coupons WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    // Convert database row into BookingCoupon object
    private BookingCoupon mapBookingCoupon(ResultSet rs)
            throws SQLException {

        BookingCoupon coupon = new BookingCoupon();

        coupon.setId(rs.getLong("id"));
        coupon.setBookingId(rs.getLong("booking_id"));
        coupon.setCouponId(rs.getLong("coupon_id"));
        coupon.setDiscountAmount(
                rs.getBigDecimal("discount_amount"));
        coupon.setAppliedAt(rs.getTimestamp("applied_at"));

        return coupon;
    }
}
