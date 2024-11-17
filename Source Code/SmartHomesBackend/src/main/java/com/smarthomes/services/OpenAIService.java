package com.smarthomes.services;

import com.smarthomes.models.Ticket;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenAIService {
    private static final Logger LOGGER = Logger.getLogger(OpenAIService.class.getName());
private static final String OPENAI_API_KEY = "your-openai-api-key";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL_NAME = "gpt-4o-mini";
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
}