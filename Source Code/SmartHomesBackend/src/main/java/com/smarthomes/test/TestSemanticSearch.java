package com.smarthomes.test;

import com.smarthomes.util.OpenAIUtil;
import com.smarthomes.util.ElasticsearchUtil;
import com.smarthomes.dao.ProductDAO;
import com.smarthomes.dao.ReviewDAO;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TestSemanticSearch {
    private static final Logger LOGGER = Logger.getLogger(TestSemanticSearch.class.getName());

    public static void main(String[] args) {
        try {
            // Test cases
            testProductRecommendation("smart doorbell with night vision and motion detection");
            testProductRecommendation("energy efficient thermostat that works with voice control");

            testReviewSearch("had issues with battery life");
            testReviewSearch("great sound quality and easy setup");

        } catch (Exception e) {
            LOGGER.severe("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testProductRecommendation(String query) {
        try {
            LOGGER.info("\nTesting product recommendation for query: " + query);

            // Generate embedding for query
            List<Float> queryEmbedding = OpenAIUtil.generateEmbedding(query);

            // Find similar products
            List<Map<String, Object>> results = ElasticsearchUtil.findSimilarDocuments("product", queryEmbedding, 3);

            // Get full product details
            ProductDAO productDAO = new ProductDAO();
            for (Map<String, Object> result : results) {
                int productId = ((Number) result.get("referenceId")).intValue();
                double score = ((Number) result.get("score")).doubleValue();
                var product = productDAO.getProductById(productId);

                LOGGER.info(String.format("Product: %s (ID: %d)", product.getName(), product.getId()));
                LOGGER.info("Description: " + product.getDescription());
                LOGGER.info("Similarity Score: " + score);
                LOGGER.info("---");
            }
        } catch (Exception e) {
            LOGGER.severe("Product recommendation test failed: " + e.getMessage());
        }
    }

    private static void testReviewSearch(String query) {
        try {
            LOGGER.info("\nTesting review search for query: " + query);

            // Generate embedding for query
            List<Float> queryEmbedding = OpenAIUtil.generateEmbedding(query);

            // Find similar reviews
            List<Map<String, Object>> results = ElasticsearchUtil.findSimilarDocuments("review", queryEmbedding, 3);

            // Get full review details
            ReviewDAO reviewDAO = new ReviewDAO();
            for (Map<String, Object> result : results) {
                int reviewId = ((Number) result.get("referenceId")).intValue();
                double score = ((Number) result.get("score")).doubleValue();
                var review = reviewDAO.getReviewById(reviewId);

                LOGGER.info(String.format("Review ID: %d", review.getId()));
                LOGGER.info("Text: " + review.getReviewText());
                LOGGER.info("Rating: " + review.getRating());
                LOGGER.info("Similarity Score: " + score);
                LOGGER.info("---");
            }
        } catch (Exception e) {
            LOGGER.severe("Review search test failed: " + e.getMessage());
        }
    }
}