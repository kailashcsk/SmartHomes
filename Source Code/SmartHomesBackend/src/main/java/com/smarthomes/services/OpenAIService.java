package com.smarthomes.services;

import com.smarthomes.models.Ticket;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenAIService {
    private static final Logger LOGGER = Logger.getLogger(OpenAIService.class.getName());
    private static final String OPENAI_API_KEY = "sk-DTTki29DbLaLb7w2sg-eRX3tMJcMhfuIchKUff5NiWT3BlbkFJuSL-mTUmw6gpbQPHi9pUS4asg8AH4hoL9JPFjE_mMA";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL_NAME = "gpt-4o-mini";
    private static final String OPENAI_EMBEDDING_URL = "https://api.openai.com/v1/embeddings";
    private static final String EMBEDDING_MODEL = "text-embedding-3-small";
    private static final String CHAT_MODEL = "gpt-4";

    private final HttpClient httpClient;

    public OpenAIService() {
        this.httpClient = HttpClient.newBuilder().build();
        LOGGER.info("OpenAI Service initialized with model: " + MODEL_NAME);
        }


    public Ticket.Decision analyzeImage(File imageFile, String description) throws IOException {
        LOGGER.info("Starting image analysis with model " + MODEL_NAME);
        
        try {
            // Convert image to Base64
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            LOGGER.info("Image successfully converted to Base64");

            // Create the message content
            JSONObject messageContent = new JSONObject();
            messageContent.put("role", "user");
            
            // Create content array with text and image
            JSONArray content = new JSONArray();
            
            // Add text prompt
            JSONObject textContent = new JSONObject();
            textContent.put("type", "text");
            textContent.put("text", createPrompt(description));
            content.put(textContent);
            
            // Add image data
            JSONObject imageContent = new JSONObject();
            imageContent.put("type", "image_url");
            JSONObject imageUrl = new JSONObject();
            imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
            imageContent.put("image_url", imageUrl);
            content.put(imageContent);
            
            messageContent.put("content", content);

            // Create the complete request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL_NAME);
            requestBody.put("messages", new JSONArray().put(messageContent));
            requestBody.put("max_tokens", 300);

            LOGGER.info("Request JSON structure: " + requestBody.toString(2)); // Pretty print JSON for logging

            // Create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

            // Send request and get response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            LOGGER.info("Received response from OpenAI. Status code: " + response.statusCode());
            LOGGER.info("Response body: " + response.body());

            if (response.statusCode() != 200) {
                LOGGER.severe("OpenAI API error. Status code: " + response.statusCode() + ", Response: " + response.body());
                throw new IOException("OpenAI API error: " + response.body());
            }

            JSONObject jsonResponse = new JSONObject(response.body());
            String decisionText = jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
                .toUpperCase();

            LOGGER.info("Raw decision text: " + decisionText);

            // Parse decision from response
            if (decisionText.contains("REFUND_ORDER")) {
                return Ticket.Decision.REFUND_ORDER;
            } else if (decisionText.contains("REPLACE_ORDER")) {
                return Ticket.Decision.REPLACE_ORDER;
            } else {
                return Ticket.Decision.ESCALATE_TO_HUMAN;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in OpenAI analysis: " + e.getMessage(), e);
            throw new IOException("Error analyzing image: " + e.getMessage());
        }
    }

    private String createPrompt(String description) {
        return String.format(
            "Analyze this product image and customer complaint: '%s'. " +
            "Based on the visible damage or issues, choose ONE of these exact responses: " +
            "REFUND_ORDER (for severe/unrepairable damage), " +
            "REPLACE_ORDER (for cosmetic/repairable damage), " +
            "ESCALATE_TO_HUMAN (for unclear situations). " +
            "Respond with only one of these three phrases.", 
            description
        );
    }

    /**
     * Generates embeddings for text using OpenAI's text-embedding-3-small model
     */
    public List<Double> generateEmbedding(String text) throws IOException {
        try {
            JSONObject requestBody = new JSONObject()
                .put("model", EMBEDDING_MODEL)
                .put("input", text);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_EMBEDDING_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.severe("OpenAI API error. Status code: " + response.statusCode() + ", Response: " + response.body());
                throw new IOException("OpenAI API error: " + response.body());
            }

            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray embedding = jsonResponse.getJSONArray("data")
                .getJSONObject(0)
                .getJSONArray("embedding");

            List<Double> embeddingList = new ArrayList<>();
            for (int i = 0; i < embedding.length(); i++) {
                embeddingList.add(embedding.getDouble(i));
            }

            return embeddingList;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating embedding: " + e.getMessage(), e);
            throw new IOException("Error generating embedding: " + e.getMessage());
        }
    }

    /**
     * Generates product descriptions using GPT-4
     */
    public String generateProductDescription(String category) throws IOException {
        try {
            String prompt = String.format(
                "Generate a product record for a %s product with the following format:\n" +
                "Product Name: [name]\n" +
                "Product Price: [price between $100-$500]\n" +
                "Category: %s\n" +
                "Description: [detailed description in 100 words or less]\n\n" +
                "Make it specific and marketable for a smart home product.", 
                category, category
            );

            JSONObject requestBody = new JSONObject()
                .put("model", CHAT_MODEL)
                .put("messages", new JSONArray()
                    .put(new JSONObject()
                        .put("role", "user")
                        .put("content", prompt)))
                .put("temperature", 0.7)
                .put("max_tokens", 300);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("OpenAI API error: " + response.body());
            }

            return new JSONObject(response.body())
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating product description: " + e.getMessage(), e);
            throw new IOException("Error generating product description: " + e.getMessage());
        }
    }

    /**
     * Generates product reviews using GPT-4 with category-specific keywords
     */
    public String generateProductReview(String productName, String category, boolean isPositive) throws IOException {
        try {
            String sentiment = isPositive ? "positive" : "negative";
            String keywords = getKeywordsForCategory(category, isPositive);
            
            String prompt = String.format(
                "Write a %s review for this smart home product: %s.\n" +
                "Include some of these keywords: %s.\n" +
                "Keep the review between 50-100 words and make it sound authentic.",
                sentiment, productName, keywords
            );

            JSONObject requestBody = new JSONObject()
                .put("model", CHAT_MODEL)
                .put("messages", new JSONArray()
                    .put(new JSONObject()
                        .put("role", "user")
                        .put("content", prompt)))
                .put("temperature", 0.8)
                .put("max_tokens", 200);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("OpenAI API error: " + response.body());
            }

            return new JSONObject(response.body())
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating product review: " + e.getMessage(), e);
            throw new IOException("Error generating product review: " + e.getMessage());
        }
    }

    private String getKeywordsForCategory(String category, boolean isPositive) {
        Map<String, String[]> keywords;
        switch (category) {
            case "Smart Doorbells":
                keywords = Map.of(
                    "positive", new String[]{"convenient", "secure", "real-time", "reliable", "clear video"},
                    "negative", new String[]{"glitchy", "slow alerts", "poor connection", "privacy concerns"}
                );
                break;
            case "Smart Doorlocks":
                keywords = Map.of(
                    "positive", new String[]{"secure", "convenient", "remote access", "easy install"},
                    "negative", new String[]{"battery drain", "app issues", "unreliable", "lock jams"}
                );
                break;
            case "Smart Speakers":
                keywords = Map.of(
                    "positive", new String[]{"responsive", "good sound", "versatile", "user-friendly"},
                    "negative", new String[]{"poor privacy", "limited commands", "connectivity issues"}
                );
                break;
            case "Smart Lightings":
                keywords = Map.of(
                    "positive", new String[]{"customizable", "energy-efficient", "remote control", "mood-enhancing"},
                    "negative", new String[]{"app problems", "delay", "connectivity issues", "limited brightness"}
                );
                break;
            case "Smart Thermostats":
                keywords = Map.of(
                    "positive", new String[]{"energy-saving", "easy to use", "efficient", "remote control"},
                    "negative", new String[]{"difficult setup", "temperature inaccuracy", "app bugs", "connectivity issues"}
                );
                break;
            default:
                keywords = Map.of(
                    "positive", new String[]{"convenient", "reliable", "easy to use"},
                    "negative", new String[]{"unreliable", "difficult", "issues"}
                );
                break;
        }

        String[] selectedKeywords = keywords.get(isPositive ? "positive" : "negative");
        return String.join(", ", selectedKeywords);

    }
}