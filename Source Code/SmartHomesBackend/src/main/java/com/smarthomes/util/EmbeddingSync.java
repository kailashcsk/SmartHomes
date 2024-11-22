package com.smarthomes.util;

import com.smarthomes.models.Embedding;
import com.smarthomes.models.Product;
import com.smarthomes.models.Review;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmbeddingSync {
    private static final Logger LOGGER = Logger.getLogger(EmbeddingSync.class.getName());

    public static void syncProduct(Product product) {
        try {
            // Generate embedding for product description
            List<Float> vector = OpenAIUtil.generateEmbedding(product.getDescription());

            // Create embedding object
            Embedding embedding = new Embedding(
                    "product",
                    product.getId(),
                    vector,
                    product.getDescription());

            // Store in Elasticsearch
            ElasticsearchUtil.indexEmbedding(embedding);

            LOGGER.info("Successfully synced product embedding for ID: " + product.getId());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to sync product embedding: " + e.getMessage(), e);
        }
    }

    public static void syncReview(Review review) {
        try {
            // Generate embedding for review text
            List<Float> vector = OpenAIUtil.generateEmbedding(review.getReviewText());

            // Create embedding object
            Embedding embedding = new Embedding(
                    "review",
                    review.getId(),
                    vector,
                    review.getReviewText());

            // Store in Elasticsearch
            ElasticsearchUtil.indexEmbedding(embedding);

            LOGGER.info("Successfully synced review embedding for ID: " + review.getId());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to sync review embedding: " + e.getMessage(), e);
        }
    }

    public static void deleteProduct(int productId) {
        try {
            ElasticsearchUtil.deleteEmbedding("product", productId);
            LOGGER.info("Successfully deleted product embedding for ID: " + productId);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete product embedding: " + e.getMessage(), e);
        }
    }

    public static void deleteReview(int reviewId) {
        try {
            ElasticsearchUtil.deleteEmbedding("review", reviewId);
            LOGGER.info("Successfully deleted review embedding for ID: " + reviewId);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete review embedding: " + e.getMessage(), e);
        }
    }
}