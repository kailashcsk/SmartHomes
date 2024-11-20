package com.smarthomes.servlets;

import com.smarthomes.dao.ReviewDAO;
import com.smarthomes.models.Review;
import com.smarthomes.models.User;
import com.smarthomes.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReviewServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ReviewServlet.class.getName());
    private ReviewDAO reviewDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        reviewDAO = new ReviewDAO();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        String pathInfo = request.getPathInfo();
        LOGGER.info("Received GET request with pathInfo: " + pathInfo);

        try {
            String productIdParam = request.getParameter("productId");
            String userIdParam = request.getParameter("userId");

            if (productIdParam != null && userIdParam != null) {
                int productId = Integer.parseInt(productIdParam);
                int userId = Integer.parseInt(userIdParam);
                List<Review> reviews = reviewDAO.getReviewsByUserAndProduct(userId, productId);
                sendSuccessResponse(response, HttpServletResponse.SC_OK, "Reviews retrieved successfully", reviews);
            } else if (productIdParam != null) {
                int productId = Integer.parseInt(productIdParam);
                List<Review> reviews = reviewDAO.getReviewsByProduct(productId);
                sendSuccessResponse(response, HttpServletResponse.SC_OK, "Reviews retrieved successfully", reviews);
            } else if (userIdParam != null) {
                int userId = Integer.parseInt(userIdParam);
                List<Review> reviews = reviewDAO.getReviewsByUser(userId);
                sendSuccessResponse(response, HttpServletResponse.SC_OK, "Reviews retrieved successfully", reviews);
            } else {
                List<Review> reviews = reviewDAO.getAllReviews();
                sendSuccessResponse(response, HttpServletResponse.SC_OK, "Reviews retrieved successfully", reviews);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error in doGet: " + e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid parameter format: " + e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter format");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");

        User user = authenticateUser(request);
        if (user == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        try {
            Review review = objectMapper.readValue(request.getReader(), Review.class);
            review.setUserId(user.getId());

            // Validate review
            if (!validateReview(review, response)) {
                return;
            }

            // Remove the duplicate review check
            LOGGER.info("Creating review for product: " + review.getProductId());
            boolean success = reviewDAO.createReview(review);

            if (success) {
                // Fetch the created review
                List<Review> createdReview = reviewDAO.getReviewsByUserAndProduct(user.getId(), review.getProductId());
                if (!createdReview.isEmpty()) {
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("message", "Review created successfully");
                    responseMap.put("review", createdReview.get(createdReview.size() - 1)); // Get the most recent
                                                                                            // review
                    sendSuccessResponse(response, HttpServletResponse.SC_CREATED, "Review created successfully",
                            responseMap);
                } else {
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("message", "Review created successfully");
                    responseMap.put("reviewId", review.getId());
                    sendSuccessResponse(response, HttpServletResponse.SC_CREATED, "Review created successfully",
                            responseMap);
                }
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create review");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in doPost: " + e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error creating review: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        User user = authenticateUser(request);
        if (user == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        try {
            Review review = objectMapper.readValue(request.getReader(), Review.class);
            review.setUserId(user.getId()); // Ensure user ID is set correctly

            if (!validateReview(review, response)) {
                return;
            }

            if (!reviewDAO.userCanModifyReview(review.getId(), user.getId())) {
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "You can only modify your own reviews");
                return;
            }

            boolean success = reviewDAO.updateReview(review);
            if (success) {
                Review updatedReview = reviewDAO.getReviewById(review.getId());
                if (updatedReview != null) {
                    sendSuccessResponse(response, HttpServletResponse.SC_OK, "Review updated successfully",
                            updatedReview);
                } else {
                    Map<String, Object> data = new HashMap<>();
                    data.put("reviewId", review.getId());
                    sendSuccessResponse(response, HttpServletResponse.SC_OK, "Review updated successfully", data);
                }
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Review not found or update failed");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in doPut: " + e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error updating review: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        User user = authenticateUser(request);
        if (user == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        try {
            int reviewId = Integer.parseInt(request.getParameter("reviewId"));
            if (!reviewDAO.userCanModifyReview(reviewId, user.getId())) {
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "You can only delete your own reviews");
                return;
            }

            reviewDAO.deleteReview(reviewId, user.getId());
            sendSuccessResponse(response, HttpServletResponse.SC_OK, "Review deleted successfully", null);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting review: " + e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_OK, "Review deleted successfully");
        }
    }

    private boolean validateReview(Review review, HttpServletResponse response) throws IOException {
        if (review.getReviewText() == null || review.getReviewText().trim().isEmpty()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Review text is required");
            return false;
        }
        if (review.getRating() < 1 || review.getRating() > 5) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Rating must be between 1 and 5");
            return false;
        }
        if (review.getProductId() <= 0) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Valid product ID is required");
            return false;
        }
        if (review.getStoreId() <= 0) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Valid store ID is required");
            return false;
        }
        return true;
    }

    private User authenticateUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return JwtUtil.verifyToken(token);
        }
        return null;
    }

    private void sendSuccessResponse(HttpServletResponse response, int status, String message, Object data)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        // If data is a List or single object, send it directly
        objectMapper.writeValue(response.getWriter(), data);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), message);
    }
}