
package com.Eventhub.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.Eventhub.model.Review;
import com.Eventhub.util.DBConnection;

public class ReviewDAO {

    // 1. Retrieve all reviews
    public List<Review> getAllReviews() throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                reviews.add(mapReview(resultSet));
            }
        }

        return reviews;
    }

    // 2. Retrieve a review by ID
    public Review getReviewById(Long id) throws SQLException {
        String sql = "SELECT * FROM reviews WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapReview(resultSet);
                }
            }
        }

        return null;
    }

    // 3. Retrieve reviews for a service
    public List<Review> getReviewsByServiceId(Long serviceId)
            throws SQLException {

        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE service_id = ? ORDER BY id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, serviceId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reviews.add(mapReview(resultSet));
                }
            }
        }

        return reviews;
    }

    // 4. Retrieve reviews submitted by a user
    public List<Review> getReviewsByUserId(Long userId)
            throws SQLException {

        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE user_id = ? ORDER BY id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reviews.add(mapReview(resultSet));
                }
            }
        }

        return reviews;
    }

    // 5. Create a review
    public long createReview(Review review) throws SQLException {
        validateRating(review.getRating());

        String sql = "INSERT INTO reviews " +
                "(booking_id, user_id, service_id, rating, comment) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, review.getBookingId());
            statement.setLong(2, review.getUserId());
            statement.setLong(3, review.getServiceId());
            statement.setInt(4, review.getRating());
            statement.setString(5, review.getComment());

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Creating review failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("No ID generated for the review.");
    }

    // 6. Update a review
    public boolean updateReview(Review review) throws SQLException {
        validateRating(review.getRating());

        String sql = "UPDATE reviews SET rating = ?, comment = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, review.getRating());
            statement.setString(2, review.getComment());
            statement.setLong(3, review.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // 7. Delete a review
    public boolean deleteReview(Long id) throws SQLException {
        String sql = "DELETE FROM reviews WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    // Validate the rating range
    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5.");
        }
    }

    // Convert a database row into a Review object
    private Review mapReview(ResultSet resultSet) throws SQLException {
        Review review = new Review();

        review.setId(resultSet.getLong("id"));
        review.setBookingId(resultSet.getLong("booking_id"));
        review.setUserId(resultSet.getLong("user_id"));
        review.setServiceId(resultSet.getLong("service_id"));
        review.setRating(resultSet.getInt("rating"));
        review.setComment(resultSet.getString("comment"));
        review.setCreatedAt(resultSet.getTimestamp("created_at"));
        review.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return review;
    }
}

