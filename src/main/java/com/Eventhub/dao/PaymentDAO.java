
package com.Eventhub.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.Eventhub.model.Payment;
import com.Eventhub.util.DBConnection;

public class PaymentDAO {

    // 1. Retrieve all payments
    public List<Payment> getAllPayments() throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                payments.add(mapPayment(resultSet));
            }
        }

        return payments;
    }

    // 2. Retrieve a payment by ID
    public Payment getPaymentById(Long id) throws SQLException {
        String sql = "SELECT * FROM payments WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapPayment(resultSet);
                }
            }
        }

        return null;
    }

    // 3. Retrieve payments for a booking
    public List<Payment> getPaymentsByBookingId(Long bookingId)
            throws SQLException {

        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE booking_id = ? ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, bookingId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    payments.add(mapPayment(resultSet));
                }
            }
        }

        return payments;
    }

    // 4. Create a payment record
    public long createPayment(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments " +
                "(booking_id, amount, payment_method, transaction_id, status, paid_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, payment.getBookingId());
            statement.setBigDecimal(2, payment.getAmount());
            statement.setString(3, payment.getPaymentMethod());
            statement.setString(4, payment.getTransactionId());
            statement.setString(5,
                    payment.getStatus() != null
                            ? payment.getStatus()
                            : "PENDING");
            statement.setTimestamp(6, payment.getPaidAt());

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Creating payment failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("No ID generated for the payment.");
    }

    // 5. Update payment status
    public boolean updatePaymentStatus(Long id, String status)
            throws SQLException {

        String sql = "UPDATE payments SET status = ?, " +
                "paid_at = CASE WHEN ? = 'SUCCESS' " +
                "THEN CURRENT_TIMESTAMP ELSE paid_at END WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setString(2, status);
            statement.setLong(3, id);

            return statement.executeUpdate() > 0;
        }
    }

    // 6. Update payment details
    public boolean updatePayment(Payment payment) throws SQLException {
        String sql = "UPDATE payments SET booking_id = ?, amount = ?, " +
                "payment_method = ?, transaction_id = ?, status = ?, " +
                "paid_at = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, payment.getBookingId());
            statement.setBigDecimal(2, payment.getAmount());
            statement.setString(3, payment.getPaymentMethod());
            statement.setString(4, payment.getTransactionId());
            statement.setString(5, payment.getStatus());
            statement.setTimestamp(6, payment.getPaidAt());
            statement.setLong(7, payment.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // 7. Delete a payment record
    public boolean deletePayment(Long id) throws SQLException {
        String sql = "DELETE FROM payments WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    // Convert a database row into a Payment object
    private Payment mapPayment(ResultSet resultSet) throws SQLException {
        Payment payment = new Payment();

        payment.setId(resultSet.getLong("id"));
        payment.setBookingId(resultSet.getLong("booking_id"));
        payment.setAmount(resultSet.getBigDecimal("amount"));
        payment.setPaymentMethod(resultSet.getString("payment_method"));
        payment.setTransactionId(resultSet.getString("transaction_id"));
        payment.setStatus(resultSet.getString("status"));
        payment.setPaidAt(resultSet.getTimestamp("paid_at"));
        payment.setCreatedAt(resultSet.getTimestamp("created_at"));

        return payment;
    }
}
