package com.smarthomes.util;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OpenAIUtil {
    private static final Logger LOGGER = Logger.getLogger(OpenAIUtil.class.getName());
    private static String API_KEY = "your-api-key";
    private static final String EMBEDDING_MODEL = "text-embedding-3-small";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/embeddings";

    public static void setApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be empty");
        }
        if (!apiKey.startsWith("sk-")) {
            throw new IllegalArgumentException("Invalid API key format. Should start with 'sk-'");
        }
        API_KEY = apiKey.trim();
        // Test the API key
        try {
            testApiKey();
            LOGGER.info("OpenAI API key validated successfully");
        } catch (IOException e) {
            API_KEY = null;
            throw new IllegalArgumentException("Invalid API key: " + e.getMessage());
        }
    }

    private static void testApiKey() throws IOException {
        List<Float> test = generateEmbedding("test");
        if (test == null || test.isEmpty()) {
            throw new IOException("Failed to generate test embedding");
        }
    }

    public static List<Float> generateEmbedding(String text) throws IOException {
        if (API_KEY == null) {
            API_KEY = System.getenv("OPENAI_API_KEY");
            if (API_KEY == null) {
                throw new IllegalStateException(
                        "OpenAI API key not set. Please set OPENAI_API_KEY environment variable or call setApiKey()");
            }
        }

        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", EMBEDDING_MODEL);
        requestBody.put("input", text);

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .post(RequestBody.create(
                        MediaType.parse("application/json"),
                        requestBody.toString()))
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body().string();
                LOGGER.severe("OpenAI API error: " + errorBody);
                throw new IOException("OpenAI API call failed: " + errorBody);
            }

            String responseBody = response.body().string();
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

            List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");
            List<Number> embedding = (List<Number>) data.get(0).get("embedding");

            return embedding.stream()
                    .map(Number::floatValue)
                    .toList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating embedding: " + e.getMessage(), e);
            throw new IOException("Failed to generate embedding", e);
        }
    }
}