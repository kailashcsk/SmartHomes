package com.smarthomes.dao;

import com.smarthomes.models.Review;
import com.smarthomes.util.DatabaseConnection;
import com.smarthomes.util.MySQLDataStoreUtilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReviewDAO {
    private static final Logger LOGGER = Logger.getLogger(ReviewDAO.class.getName());

    public List<Review> getAllReviews() throws SQLException {
        return MySQLDataStoreUtilities.getAllReviews();
    }

    public List<Review> getReviewsByProduct(int productId) throws SQLException {
        return MySQLDataStoreUtilities.getReviewsByProduct(productId);
    }

    public List<Review> getReviewsByUser(int userId) throws SQLException {
        return MySQLDataStoreUtilities.getReviewsByUser(userId);
    }

    public List<Review> getReviewsByUserAndProduct(int userId, int productId) throws SQLException {
        return MySQLDataStoreUtilities.getReviewsByUserAndProduct(userId, productId);
    }

    public boolean createReview(Review review) throws SQLException {
        MySQLDataStoreUtilities.createReview(review);
        return true;
    }

    public boolean updateReview(Review review) throws SQLException {
        MySQLDataStoreUtilities.updateReview(review);
        return true;
    }

    public boolean deleteReview(int reviewId, int userId) throws SQLException {
        MySQLDataStoreUtilities.deleteReview(reviewId, userId);
        return true;
    }

    public Review getReviewById(int reviewId) throws SQLException {
        String sql = "SELECT r.*, u.name as user_name FROM reviews r " +
                "JOIN users u ON r.user_id = u.id " +
                "WHERE r.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reviewId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return MySQLDataStoreUtilities.mapResultSetToReview(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting review by ID: " + e.getMessage(), e);
            throw e;
        }
        return null;
    }


    public boolean userCanModifyReview(int reviewId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reviews WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reviewId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if user can modify review: " + e.getMessage(), e);
            throw e;
        }
        return false;
    }

}