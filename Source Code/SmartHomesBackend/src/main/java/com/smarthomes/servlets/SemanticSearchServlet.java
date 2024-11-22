package com.smarthomes.servlets;

import com.smarthomes.dao.ProductDAO;
import com.smarthomes.dao.ReviewDAO;
import com.smarthomes.models.Product;
import com.smarthomes.models.Review;
import com.smarthomes.util.OpenAIUtil;
import com.smarthomes.util.ElasticsearchUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// @WebServlet({ "/api/search/reviews/*", "/api/search/products/*" })
public class SemanticSearchServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SemanticSearchServlet.class.getName());
    private final ProductDAO productDAO = new ProductDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            String pathInfo = request.getPathInfo();
            ObjectNode requestBody = objectMapper.readValue(request.getReader(), ObjectNode.class);

            if (!requestBody.has("query")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Query parameter is required");
                return;
            }

            String query = requestBody.get("query").asText();
            List<Float> embedding = OpenAIUtil.generateEmbedding(query);

            if (pathInfo.contains("reviews")) {
                handleReviewSearch(embedding, response);
            } else if (pathInfo.contains("products")) {
                handleProductSearch(embedding, response);
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid search type");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Search error: " + e.getMessage(), e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Search failed: " + e.getMessage());
        }
    }

    private void handleReviewSearch(List<Float> embedding, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> results = ElasticsearchUtil.findSimilarDocuments("review", embedding, 5);
        List<Map<String, Object>> fullResults = new ArrayList<>();

        for (Map<String, Object> result : results) {
            try {
                int reviewId = ((Number) result.get("referenceId")).intValue();
                Review review = reviewDAO.getReviewById(reviewId);
                if (review != null) {
                    Map<String, Object> fullResult = new HashMap<>();
                    fullResult.put("review", review);
                    fullResult.put("similarity", result.get("score"));
                    fullResults.add(fullResult);
                }
            } catch (Exception e) {
                LOGGER.warning("Error fetching review details: " + e.getMessage());
            }
        }

        sendResponse(response, fullResults);
    }

    private void handleProductSearch(List<Float> embedding, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> results = ElasticsearchUtil.findSimilarDocuments("product", embedding, 5);
        List<Map<String, Object>> fullResults = new ArrayList<>();

        for (Map<String, Object> result : results) {
            try {
                int productId = ((Number) result.get("referenceId")).intValue();
                Product product = productDAO.getProductById(productId);
                if (product != null) {
                    Map<String, Object> fullResult = new HashMap<>();
                    fullResult.put("product", product);
                    fullResult.put("similarity", result.get("score"));
                    fullResults.add(fullResult);
                }
            } catch (Exception e) {
                LOGGER.warning("Error fetching product details: " + e.getMessage());
            }
        }

        sendResponse(response, fullResults);
    }

    private void sendResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), data);
    }

    private void sendError(HttpServletResponse response, int status, String message) {
        try {
            response.setStatus(status);
            response.setContentType("application/json");
            Map<String, String> error = Map.of("error", message);
            objectMapper.writeValue(response.getWriter(), error);
        } catch (IOException e) {
            LOGGER.severe("Error sending error response: " + e.getMessage());
        }
    }
}