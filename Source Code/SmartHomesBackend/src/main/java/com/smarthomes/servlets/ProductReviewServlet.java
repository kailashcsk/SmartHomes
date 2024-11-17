package com.smarthomes.servlets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.List;
import java.util.stream.Collectors;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.smarthomes.util.MongoDBDataStoreUtilities;

//@WebServlet("/api/reviews")
public class ProductReviewServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Read the entire request body
            String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println("Received body: " + body); // Debug print

            if (body.isEmpty()) {
                throw new IllegalArgumentException("Request body is empty");
            }

            Document review = Document.parse(body);
            MongoDBDataStoreUtilities.addReview(review);

            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Review submitted successfully\"}");
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace for debugging
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Error submitting review: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Return all reviews if no ID is specified
                List<Document> allReviews = MongoDBDataStoreUtilities.getAllReviews();
                response.getWriter().write(gson.toJson(allReviews));
            } else {
                // Extract review ID from path
                String reviewId = pathInfo.substring(1);
                Document review = MongoDBDataStoreUtilities.getReviewById(new ObjectId(reviewId));
                
                if (review != null) {
                    response.getWriter().write(gson.toJson(review));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\": \"Review not found\"}");
                }
            }
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid review ID\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error retrieving review: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Review ID is required\"}");
                return;
            }

            String reviewId = pathInfo.substring(1);
            System.out.println("Received review ID for update: " + reviewId); // Debug log

            if (!ObjectId.isValid(reviewId)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid review ID format\"}");
                return;
            }

            ObjectId id = new ObjectId(reviewId);

            // Read the updated review data from request body
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String jsonBody = sb.toString();
            System.out.println("Received JSON body: " + jsonBody); // Debug log

            Document updatedReview = Document.parse(jsonBody);

            // Get the user ID from the request
            String userId = request.getParameter("userId");
            System.out.println("Received userId: " + userId); // Debug log

            boolean updated = MongoDBDataStoreUtilities.updateReview(id, userId, updatedReview);

            if (updated) {
                response.getWriter().write("{\"message\": \"Review updated successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Review not found or unauthorized\"}");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace for debugging
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error updating review: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Review ID is required\"}");
                return;
            }

            String reviewId = pathInfo.substring(1);
            System.out.println("Received review ID for delete: " + reviewId); // Debug log

            if (!ObjectId.isValid(reviewId)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid review ID format\"}");
                return;
            }

            ObjectId id = new ObjectId(reviewId);

            // Get the user ID from the request
            String userId = request.getParameter("userId");
            System.out.println("Received userId: " + userId); // Debug log

            boolean deleted = MongoDBDataStoreUtilities.deleteReview(id, userId);

            if (deleted) {
                response.getWriter().write("{\"message\": \"Review deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Review not found or unauthorized\"}");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace for debugging
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error deleting review: " + e.getMessage() + "\"}");
        }
    }
    
    @Override
    public void destroy() {
        MongoDBDataStoreUtilities.closeConnection();
    }
}