package com.smarthomes.test;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;
import java.util.*;
import java.util.logging.Logger;

import com.smarthomes.util.*;
import com.smarthomes.dao.*;
import com.smarthomes.models.*;

public class TestSemanticSearch {
    private static final Logger LOGGER = Logger.getLogger(TestSemanticSearch.class.getName());
    private ProductDAO productDAO;
    private ReviewDAO reviewDAO;

    @BeforeClass
    public static void setUpClass() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            // Use a default test key if environment variable is not set
            apiKey = "sk-ZJxbrnlrwTLdw3cc885DQsu1bbAi5STsS5w7-SIRGdT3BlbkFJkOn6MqwhU--kLhdGhBXn5jQGcJ7U1V9gEdM-dGD0gA";
        }
        OpenAIUtil.setApiKey(apiKey);
    }

    @Before
    public void setup() {
        productDAO = new ProductDAO();
        reviewDAO = new ReviewDAO();
    }

    @Test
    public void testProductRecommendation() throws Exception {
        String query = "smart doorbell with night vision and motion detection";
        List<Float> queryEmbedding = OpenAIUtil.generateEmbedding(query);
        List<Map<String, Object>> results = ElasticsearchUtil.findSimilarDocuments("product", queryEmbedding, 3);

        assertNotNull("Results should not be null", results);
        assertTrue("Should find at least one product", results.size() > 0);

        for (Map<String, Object> result : results) {
            int productId = ((Number) result.get("referenceId")).intValue();
            Product product = productDAO.getProductById(productId);
            LOGGER.info(String.format("Found product: %s", product.getName()));

            assertNotNull("Product should not be null", product);
            assertTrue("Product should have a name", product.getName() != null && !product.getName().isEmpty());
        }
    }

    @Test
    public void testReviewSearch() throws Exception {
        String query = "battery life issues";
        List<Float> queryEmbedding = OpenAIUtil.generateEmbedding(query);
        List<Map<String, Object>> results = ElasticsearchUtil.findSimilarDocuments("review", queryEmbedding, 3);

        assertNotNull("Results should not be null", results);
        assertTrue("Should find at least one review", results.size() > 0);

        for (Map<String, Object> result : results) {
            int reviewId = ((Number) result.get("referenceId")).intValue();
            Review review = reviewDAO.getReviewById(reviewId);
            LOGGER.info(String.format("Found review: %s", review.getReviewText()));

            assertNotNull("Review should not be null", review);
            assertTrue("Review should have text", review.getReviewText() != null && !review.getReviewText().isEmpty());
        }
    }
}