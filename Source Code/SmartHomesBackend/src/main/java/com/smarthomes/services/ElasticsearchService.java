package com.smarthomes.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.apache.http.ssl.SSLContexts;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ElasticsearchService {
    private static final Logger LOGGER = Logger.getLogger(ElasticsearchService.class.getName());

    private final ElasticsearchClient client;
    private static final String PRODUCTS_INDEX = "products";
    private static final String REVIEWS_INDEX = "reviews";

    private static final String ELASTICSEARCH_HOSTNAME = "";
    private static final String ELASTICSEARCH_USERNAME = "elastic";
    private static final String ELASTICSEARCH_PASSWORD = "";

    public ElasticsearchService() {
        try {
            // Create the credentials provider
            BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
            credsProv.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(ELASTICSEARCH_USERNAME, ELASTICSEARCH_PASSWORD));

            // Create SSL Context
            SSLContext sslContext = SSLContexts.createDefault();

            // Create the low-level REST client
            RestClient restClient = RestClient
                    .builder(HttpHost.create(ELASTICSEARCH_HOSTNAME))
                    .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                            .setDefaultCredentialsProvider(credsProv)
                            .setSSLContext(sslContext))
                    .build();

            // Create the transport and API client
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper());
            this.client = new ElasticsearchClient(transport);

            // Initialize indices
            createIndicesIfNotExist();

            LOGGER.info("Successfully initialized Elasticsearch client");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Elasticsearch client", e);
            throw new RuntimeException("Failed to initialize Elasticsearch client", e);
        }
    }

    private void createIndicesIfNotExist() throws IOException {
        // Create products index if it doesn't exist
        if (!indexExists(PRODUCTS_INDEX)) {
            CreateIndexRequest createIndexRequest = CreateIndexRequest.of(b -> b
                    .index(PRODUCTS_INDEX)
                    .mappings(typeMappings -> typeMappings
                            .properties("name", property -> property.text(t -> t))
                            .properties("description", property -> property.text(t -> t))
                            .properties("category", property -> property.keyword(k -> k))
                            .properties("price", property -> property.double_(d -> d))
                            .properties("embedding", property -> property
                                    .denseVector(v -> v
                                            .dims(1536)
                                            .index(true)
                                            .similarity("cosine")))));

            client.indices().create(createIndexRequest);
            LOGGER.info("Created products index");
        }

        // Create reviews index if it doesn't exist
        if (!indexExists(REVIEWS_INDEX)) {
            CreateIndexRequest createIndexRequest = CreateIndexRequest.of(b -> b
                    .index(REVIEWS_INDEX)
                    .mappings(typeMappings -> typeMappings
                            .properties("productId", property -> property.keyword(k -> k))
                            .properties("rating", property -> property.integer(i -> i))
                            .properties("reviewText", property -> property.text(t -> t))
                            .properties("embedding", property -> property
                                    .denseVector(v -> v
                                            .dims(1536)
                                            .index(true)
                                            .similarity("cosine")))));

            client.indices().create(createIndexRequest);
            LOGGER.info("Created reviews index");
        }
    }

    private boolean indexExists(String indexName) throws IOException {
        return client.indices().exists(e -> e.index(indexName)).value();
    }

    public void indexProduct(String productId, Map<String, Object> product, List<Double> embedding) throws IOException {
        try {
            Map<String, Object> document = new HashMap<>(product);
            document.put("embedding", embedding);

            IndexResponse response = client.index(i -> i
                    .index(PRODUCTS_INDEX)
                    .id(productId)
                    .document(document));

            LOGGER.info("Indexed product with ID: " + response.id());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to index product", e);
            throw new IOException("Failed to index product", e);
        }
    }

    public void indexReview(String reviewId, Map<String, Object> review, List<Double> embedding) throws IOException {
        try {
            Map<String, Object> document = new HashMap<>(review);
            document.put("embedding", embedding);

            IndexResponse response = client.index(i -> i
                    .index(REVIEWS_INDEX)
                    .id(reviewId)
                    .document(document));

            LOGGER.info("Indexed review with ID: " + response.id());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to index review", e);
            throw new IOException("Failed to index review", e);
        }
    }

    public List<Map<String, Object>> searchSimilarProducts(List<Double> queryEmbedding, int limit) throws IOException {
        try {
            // Create parameters map
            Map<String, JsonData> params = new HashMap<>();
            params.put("queryVector", JsonData.of(queryEmbedding));

            SearchResponse<Map> response = client.search(s -> s
                    .index(PRODUCTS_INDEX)
                    .query(q -> q
                            .scriptScore(ss -> ss
                                    .query(innerQ -> innerQ.matchAll(m -> m))
                                    .script(sc -> sc
                                            .inline(i -> i
                                                    .source("cosineSimilarity(params.queryVector, 'embedding') + 1.0")
                                                    .params(params)))))
                    .size(limit),
                    Map.class);

            List<Map<String, Object>> results = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    results.add(hit.source());
                }
            }
            return results;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to search similar products", e);
            throw new IOException("Failed to search similar products", e);
        }
    }

    public List<Map<String, Object>> searchSimilarReviews(List<Double> queryEmbedding, int limit) throws IOException {
        try {
            // Create parameters map
            Map<String, JsonData> params = new HashMap<>();
            params.put("queryVector", JsonData.of(queryEmbedding));

            SearchResponse<Map> response = client.search(s -> s
                    .index(REVIEWS_INDEX)
                    .query(q -> q
                            .scriptScore(ss -> ss
                                    .query(innerQ -> innerQ.matchAll(m -> m))
                                    .script(sc -> sc
                                            .inline(i -> i
                                                    .source("cosineSimilarity(params.queryVector, 'embedding') + 1.0")
                                                    .params(params)))))
                    .size(limit),
                    Map.class);

            List<Map<String, Object>> results = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    results.add(hit.source());
                }
            }
            return results;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to search similar reviews", e);
            throw new IOException("Failed to search similar reviews", e);
        }
    }

    // Helper method to test connection
    public boolean testConnection() {
        try {
            boolean productsExists = indexExists(PRODUCTS_INDEX);
            boolean reviewsExists = indexExists(REVIEWS_INDEX);
            LOGGER.info("Products index exists: " + productsExists);
            LOGGER.info("Reviews index exists: " + reviewsExists);
            return true;
        } catch (IOException e) {
            LOGGER.severe("Failed to test connection: " + e.getMessage());
            return false;
        }
    }
}