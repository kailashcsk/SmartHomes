package com.smarthomes.util;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoDBDataStoreUtilities {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static final String DB_NAME = "product_reviews_db";
    private static final String COLLECTION_NAME = "reviews";
    private static final String CONNECTION_STRING = "mongodb+srv://admin:QuWUjS8lWpFwHUeE@cluster0.durfwcu.mongodb.net/";

    private static final Logger logger = Logger.getLogger(MongoDBDataStoreUtilities.class.getName());

    static {
        try {
            // Initialize MongoDB connection
            ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(CONNECTION_STRING))
                    .serverApi(serverApi)
                    .build();
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(DB_NAME);
            logger.info("MongoDB connection established successfully.");
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Failed to connect to MongoDB", e);
        }
    }

    public static Document getReviewById(ObjectId id) {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        return collection.find(new Document("_id", id)).first();
    }

    public static List<Document> getAllReviews() {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        return collection.find().into(new ArrayList<>());
    }

    public static void addReview(Document review) {
        try {
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
            collection.insertOne(review);
            logger.info("Review added successfully");
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Failed to add review", e);
            throw new RuntimeException("Failed to add review", e);
        }
    }

     public static boolean updateReview(ObjectId id, String userId, Document updatedReview) {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        
        // First, check if the review exists and belongs to the user
        Document existingReview = collection.find(new Document("_id", id).append("UserID", userId)).first();
        
        if (existingReview == null) {
            return false; // Review not found or doesn't belong to the user
        }

        // Update the review
        UpdateResult result = collection.updateOne(
            new Document("_id", id).append("UserID", userId),
            new Document("$set", updatedReview)
        );

        return result.getModifiedCount() > 0;
    }

    public static boolean deleteReview(ObjectId id, String userId) {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        
        // Delete the review only if it belongs to the user
        DeleteResult result = collection.deleteOne(
            new Document("_id", id).append("UserID", userId)
        );

        return result.getDeletedCount() > 0;
    }


    // Add more methods as needed for querying and analyzing reviews
    public static void closeConnection() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                logger.info("MongoDB connection closed successfully.");
            } catch (MongoException e) {
                logger.log(Level.SEVERE, "Failed to close MongoDB connection", e);
            }
        }
    }

    public static Map<String, Double> getAverageProductRatings() {
    MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
    
    AggregateIterable<Document> result = collection.aggregate(Arrays.asList(
        new Document("$group", new Document("_id", "$ProductID")
            .append("avgRating", new Document("$avg", "$ReviewRating")))));

    Map<String, Double> productRatings = new HashMap<>();
    for (Document doc : result) {
        productRatings.put(doc.getString("_id"), doc.getDouble("avgRating"));
    }
    
    return productRatings;
}
}