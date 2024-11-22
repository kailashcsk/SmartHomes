package com.smarthomes.util;

import com.smarthomes.dao.ProductDAO;
import com.smarthomes.dao.ReviewDAO;
import com.smarthomes.models.Product;
import com.smarthomes.models.Review;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SmartHomeDataGenerator {
    private static final Logger LOGGER = Logger.getLogger(SmartHomeDataGenerator.class.getName());

    // Category definitions
    private static final Map<Integer, CategoryInfo> CATEGORIES = new HashMap<>();
    static {
        CATEGORIES.put(1, new CategoryInfo("Smart Doorbells", 149.99, 299.99,
                new String[] { "SecureView", "BellGuard", "DoorEye" },
                new String[] { "convenient", "secure", "real-time", "reliable", "clear video" },
                new String[] { "glitchy", "slow alerts", "poor connection", "privacy concerns" }));

        CATEGORIES.put(2, new CategoryInfo("Smart Doorlocks", 199.99, 399.99,
                new String[] { "SafeGuard", "LockMaster", "SecureEntry" },
                new String[] { "secure", "convenient", "remote access", "easy install" },
                new String[] { "battery drain", "app issues", "unreliable", "lock jams" }));

        CATEGORIES.put(3, new CategoryInfo("Smart Speakers", 49.99, 199.99,
                new String[] { "EchoTech", "SoundMaster", "VoiceHub" },
                new String[] { "responsive", "good sound", "versatile", "user-friendly" },
                new String[] { "poor privacy", "limited commands", "connectivity issues" }));

        CATEGORIES.put(4, new CategoryInfo("Smart Lightings", 29.99, 149.99,
                new String[] { "LumaPro", "BrightLife", "SmartGlow" },
                new String[] { "customizable", "energy-efficient", "remote control", "mood-enhancing" },
                new String[] { "app problems", "delay", "connectivity issues", "limited brightness" }));

        CATEGORIES.put(5, new CategoryInfo("Smart Thermostats", 99.99, 299.99,
                new String[] { "TempMaster", "ClimatePro", "ThermoSmart" },
                new String[] { "energy-saving", "easy to use", "efficient", "remote control" },
                new String[] { "difficult setup", "temperature inaccuracy", "app bugs", "connectivity issues" }));
    }

    private final ProductDAO productDAO;
    private final ReviewDAO reviewDAO;
    private Random random;

    public SmartHomeDataGenerator() {
        this.productDAO = new ProductDAO();
        this.reviewDAO = new ReviewDAO();
        this.random = new Random();
    }

    public void generateData() {
        try {
            LOGGER.info("Starting data generation...");

            // Generate 2 products for each category
            for (Map.Entry<Integer, CategoryInfo> entry : CATEGORIES.entrySet()) {
                int categoryId = entry.getKey();
                CategoryInfo info = entry.getValue();

                generateProductsForCategory(categoryId, info);
            }

            LOGGER.info("Data generation completed successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating data: " + e.getMessage(), e);
        }
    }

    private void generateProductsForCategory(int categoryId, CategoryInfo info) {
        try {
            // Generate 2 products per category
            for (int i = 0; i < 2; i++) {
                // Create product
                Product product = generateProduct(categoryId, info);
                productDAO.createProduct(product);
                LOGGER.info("Created product: " + product.getName());

                // Generate embeddings and sync with Elasticsearch
                EmbeddingSync.syncProduct(product);
                LOGGER.info("Synced product embeddings: " + product.getId());

                // Generate 5 reviews for this product
                generateReviewsForProduct(product, info);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating products for category " + categoryId, e);
        }
    }

    private Product generateProduct(int categoryId, CategoryInfo info) {
        String name = getRandomBrandName(info.brandNames) + " " + (random.nextBoolean() ? "Pro" : "Elite");
        double price = info.minPrice + (random.nextDouble() * (info.maxPrice - info.minPrice));
        String description = generateProductDescription(name, info);

        Product product = new Product();
        product.setCategoryId(categoryId);
        product.setName(name);
        product.setPrice(BigDecimal.valueOf(Math.round(price * 100.0) / 100.0));
        product.setDescription(description);
        product.setManufacturerName(info.brandNames[0] + " Technologies");

        return product;
    }

    private void generateReviewsForProduct(Product product, CategoryInfo info) {
        try {
            // Generate 3 positive reviews
            for (int i = 0; i < 3; i++) {
                Review review = generateReview(product, info, true);
                reviewDAO.createReview(review);
                EmbeddingSync.syncReview(review);
                LOGGER.info("Created positive review for product: " + product.getName());
            }

            // Generate 2 negative reviews
            for (int i = 0; i < 2; i++) {
                Review review = generateReview(product, info, false);
                reviewDAO.createReview(review);
                EmbeddingSync.syncReview(review);
                LOGGER.info("Created negative review for product: " + product.getName());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating reviews for product " + product.getId(), e);
        }
    }

    private Review generateReview(Product product, CategoryInfo info, boolean positive) {
        Review review = new Review();
        review.setProductId(product.getId());
        review.setUserId(11); // Default test user
        review.setStoreId(1); // Default store
        review.setReviewText(generateReviewText(product.getName(), info, positive));
        review.setRating(positive ? 4 + random.nextInt(2) : 1 + random.nextInt(2));
        return review;
    }

    private String generateProductDescription(String productName, CategoryInfo info) {
        StringBuilder description = new StringBuilder();
        description.append("The ").append(productName).append(" is a premium ").append(info.name.toLowerCase());
        description.append(" designed for modern smart homes. ");

        // Add positive features
        description.append("Featuring ");
        List<String> features = new ArrayList<>(Arrays.asList(info.positiveFeatures));
        Collections.shuffle(features);
        for (int i = 0; i < Math.min(3, features.size()); i++) {
            if (i > 0)
                description.append(i == 2 ? ", and " : ", ");
            description.append(features.get(i));
        }
        description.append(" capabilities. ");

        description.append("Perfect for users seeking a reliable and intuitive smart home experience.");
        return description.toString();
    }

    private String generateReviewText(String productName, CategoryInfo info, boolean positive) {
        StringBuilder review = new StringBuilder();

        if (positive) {
            review.append("I'm very impressed with the ").append(productName).append(". ");
            List<String> features = new ArrayList<>(Arrays.asList(info.positiveFeatures));
            Collections.shuffle(features);
            for (int i = 0; i < Math.min(2, features.size()); i++) {
                review.append("The ").append(features.get(i)).append(" feature is exceptional. ");
            }
            review.append("Highly recommended!");
        } else {
            review.append("Not satisfied with the ").append(productName).append(". ");
            List<String> issues = new ArrayList<>(Arrays.asList(info.negativeFeatures));
            Collections.shuffle(issues);
            for (int i = 0; i < Math.min(2, issues.size()); i++) {
                review.append("Had issues with ").append(issues.get(i)).append(". ");
            }
            review.append("Needs improvement.");
        }

        return review.toString();
    }

    private String getRandomBrandName(String[] brandNames) {
        return brandNames[random.nextInt(brandNames.length)];
    }

    private static class CategoryInfo {
        String name;
        double minPrice;
        double maxPrice;
        String[] brandNames;
        String[] positiveFeatures;
        String[] negativeFeatures;

        CategoryInfo(String name, double minPrice, double maxPrice, String[] brandNames,
                String[] positiveFeatures, String[] negativeFeatures) {
            this.name = name;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.brandNames = brandNames;
            this.positiveFeatures = positiveFeatures;
            this.negativeFeatures = negativeFeatures;
        }
    }

    public static void main(String[] args) {
        SmartHomeDataGenerator generator = new SmartHomeDataGenerator();
        generator.generateData();
    }
}