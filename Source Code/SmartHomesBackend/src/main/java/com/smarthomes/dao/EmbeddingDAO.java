package com.smarthomes.dao;

import com.smarthomes.models.Embedding;
import com.smarthomes.models.Product;
import com.smarthomes.models.Review;
import com.smarthomes.util.DatabaseConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class EmbeddingDAO {
    private static final Logger LOGGER = Logger.getLogger(EmbeddingDAO.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveProductEmbedding(int productId, List<Float> embedding) throws SQLException {
        String sql = "INSERT INTO product_embeddings (product_id, embedding) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            pstmt.setString(2, objectMapper.writeValueAsString(embedding));
            
            pstmt.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving product embedding", e);
            throw new SQLException(e);
        }
    }

    public void saveReviewEmbedding(int reviewId, List<Float> embedding) throws SQLException {
        String sql = "INSERT INTO review_embeddings (review_id, embedding) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reviewId);
            pstmt.setString(2, objectMapper.writeValueAsString(embedding));
            
            pstmt.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving review embedding", e);
            throw new SQLException(e);
        }
    }

    public List<Product> findSimilarProducts(List<Float> queryEmbedding, int limit) throws SQLException {
        String sql = """
            SELECT p.*, 
                   (SELECT SQRT(SUM(POW(pe.value - qe.value, 2)))
                    FROM JSON_TABLE(pe.embedding, '$[*]' COLUMNS (value DOUBLE PATH '$'))
                         CROSS JOIN JSON_TABLE(?, '$[*]' COLUMNS (value DOUBLE PATH '$'))) as distance
            FROM product_embeddings pe
            JOIN products p ON pe.product_id = p.id
            ORDER BY distance ASC
            LIMIT ?
        """;
        
        List<Product> similarProducts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, objectMapper.writeValueAsString(queryEmbedding));
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setId(rs.getInt("id"));
                    product.setName(rs.getString("name"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setDescription(rs.getString("description"));
                    product.setCategoryId(rs.getInt("category_id"));
                    similarProducts.add(product);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding similar products", e);
            throw new SQLException(e);
        }
        return similarProducts;
    }

    public List<Review> findSimilarReviews(List<Float> queryEmbedding, int limit) throws SQLException {
        String sql = """
            SELECT r.*, 
                   (SELECT SQRT(SUM(POW(re.value - qe.value, 2)))
                    FROM JSON_TABLE(re.embedding, '$[*]' COLUMNS (value DOUBLE PATH '$'))
                         CROSS JOIN JSON_TABLE(?, '$[*]' COLUMNS (value DOUBLE PATH '$'))) as distance
            FROM review_embeddings re
            JOIN reviews r ON re.review_id = r.id
            ORDER BY distance ASC
            LIMIT ?
        """;
        
        List<Review> similarReviews = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, objectMapper.writeValueAsString(queryEmbedding));
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Review review = new Review();
                    review.setId(rs.getInt("id"));
                    review.setProductId(rs.getInt("product_id"));
                    review.setUserId(rs.getInt("user_id"));
                    review.setReviewText(rs.getString("review_text"));
                    review.setRating(rs.getInt("rating"));
                    similarReviews.add(review);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding similar reviews", e);
            throw new SQLException(e);
        }
        return similarReviews;
    }
}