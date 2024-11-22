package com.smarthomes.util;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import com.smarthomes.models.Embedding;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElasticsearchUtil {
    private static final Logger LOGGER = Logger.getLogger(ElasticsearchUtil.class.getName());
    private static ElasticsearchClient client;
    private static final String INDEX_NAME = "smarthomes_embeddings";

    static {
        try {
            RestClient restClient = RestClient.builder(
                    new HttpHost("localhost", 9200, "http")).build();

            ElasticsearchTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper());

            client = new ElasticsearchClient(transport);
            createIndexIfNotExists();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Elasticsearch", e);
        }
    }

    public static ElasticsearchClient getClient() {
        return client;
    }

    private static void createIndexIfNotExists() throws IOException {
        boolean exists = client.indices().exists(r -> r.index(INDEX_NAME)).value();
        if (!exists) {
            client.indices().create(c -> c
                    .index(INDEX_NAME)
                    .mappings(m -> m
                            .properties("type", p -> p.keyword(k -> k))
                            .properties("referenceId", p -> p.integer(i -> i))
                            .properties("content", p -> p.text(t -> t))
                            .properties("vector", p -> p.denseVector(v -> v
                                    .dims(1536)
                                    .index(true)
                                    .similarity("cosine")))));
        }
    }

    public static void indexEmbedding(Embedding embedding) throws IOException {
        try {
            // Convert embedding to a map
            Map<String, Object> document = new HashMap<>();
            document.put("type", embedding.getType());
            document.put("referenceId", embedding.getReferenceId());
            document.put("content", embedding.getContent());
            document.put("vector", embedding.getVector());

            client.index(i -> i
                    .index(INDEX_NAME)
                    .id(embedding.getId())
                    .document(document));
            LOGGER.info("Indexed embedding for " + embedding.getType() + " with ID: " + embedding.getReferenceId());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error indexing embedding: " + e.getMessage(), e);
            throw e;
        }
    }

    public static void deleteEmbedding(String type, int referenceId) throws IOException {
        try {
            client.delete(d -> d
                    .index(INDEX_NAME)
                    .id(type + "-" + referenceId));
            LOGGER.info("Deleted embedding for " + type + " with ID: " + referenceId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting embedding: " + e.getMessage(), e);
            throw e;
        }
    }

    public static List<Map<String, Object>> findSimilarDocuments(String type, List<Float> queryVector, int limit)
            throws IOException {
        var response = client.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .term(t -> t
                                                .field("type")
                                                .value(type)))))
                .size(limit),
                Map.class);

        List<Map<String, Object>> results = new ArrayList<>();
        for (Hit<Map> hit : response.hits().hits()) {
            Map<String, Object> result = hit.source();
            @SuppressWarnings("unchecked")
            List<Number> docVector = (List<Number>) result.get("vector");
            double similarity = calculateCosineSimilarity(queryVector, docVector);
            result.put("score", similarity);
            results.add(result);
        }

        // Sort by similarity score
        results.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));
        return results;
    }

    private static double calculateCosineSimilarity(List<Float> vec1, List<Number> vec2) {
        if (vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("Vectors must be of same length");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            double v1 = vec1.get(i).doubleValue();
            double v2 = vec2.get(i).doubleValue();
            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    public static boolean isRunning() {
        try {
            return client.ping().value();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking Elasticsearch status: " + e.getMessage(), e);
            return false;
        }
    }
}