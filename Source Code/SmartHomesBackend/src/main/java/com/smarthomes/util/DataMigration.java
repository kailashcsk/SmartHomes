package com.smarthomes.util;

import com.smarthomes.dao.ProductDAO;
import com.smarthomes.dao.ReviewDAO;
import com.smarthomes.models.Product;
import com.smarthomes.models.Review;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataMigration {
    private static final Logger LOGGER = Logger.getLogger(DataMigration.class.getName());

    private final ProductDAO productDAO;
    private final ReviewDAO reviewDAO;

    public DataMigration() {
        this.productDAO = new ProductDAO();
        this.reviewDAO = new ReviewDAO();
    }

    public void migrateExistingData() {
        try {
            LOGGER.info("Starting data migration to Elasticsearch...");

            // Migrate products
            migrateProducts();

            // Migrate reviews
            migrateReviews();

            LOGGER.info("Data migration completed successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during data migration: " + e.getMessage(), e);
            throw new RuntimeException("Migration failed", e);
        }
    }

    private void migrateProducts() {
        try {
            LOGGER.info("Migrating products...");
            List<Product> products = productDAO.getAllProducts();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            products.forEach(product -> {
                try {
                    EmbeddingSync.syncProduct(product);
                    LOGGER.info(String.format("Migrated product %d/%d: %s",
                            successCount.incrementAndGet(), products.size(), product.getName()));
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to migrate product " + product.getId() + ": " + e.getMessage());
                    failCount.incrementAndGet();
                }
            });

            LOGGER.info(String.format("Product migration complete. Success: %d, Failed: %d",
                    successCount.get(), failCount.get()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during product migration: " + e.getMessage(), e);
            throw new RuntimeException("Product migration failed", e);
        }
    }

    private void migrateReviews() {
        try {
            LOGGER.info("Migrating reviews...");
            List<Review> reviews = reviewDAO.getAllReviews();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            reviews.forEach(review -> {
                try {
                    EmbeddingSync.syncReview(review);
                    LOGGER.info(String.format("Migrated review %d/%d",
                            successCount.incrementAndGet(), reviews.size()));
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to migrate review " + review.getId() + ": " + e.getMessage());
                    failCount.incrementAndGet();
                }
            });

            LOGGER.info(String.format("Review migration complete. Success: %d, Failed: %d",
                    successCount.get(), failCount.get()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during review migration: " + e.getMessage(), e);
            throw new RuntimeException("Review migration failed", e);
        }
    }

    public void verifyMigration() {
        try {
            LOGGER.info("Verifying migration...");

            // Verify products
            List<Product> products = productDAO.getAllProducts();
            LOGGER.info("Total products in MySQL: " + products.size());

            // Verify reviews
            List<Review> reviews = reviewDAO.getAllReviews();
            LOGGER.info("Total reviews in MySQL: " + reviews.size());

            // TODO: Add verification against Elasticsearch
            // This would involve querying Elasticsearch and comparing counts

            LOGGER.info("Migration verification complete.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error verifying migration: " + e.getMessage(), e);
            throw new RuntimeException("Migration verification failed", e);
        }
    }

    private void handleError(String operation, Exception e) {
        LOGGER.log(Level.SEVERE, "Error during " + operation + ": " + e.getMessage(), e);
        throw new RuntimeException(operation + " failed", e);
    }

    public static void main(String[] args) {
        DataMigration migration = new DataMigration();

        try {
            // Run migration
            migration.migrateExistingData();

            // Verify migration
            migration.verifyMigration();

            LOGGER.info("Migration process completed successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Migration process failed: " + e.getMessage(), e);
            System.exit(1);
        }
    }
}