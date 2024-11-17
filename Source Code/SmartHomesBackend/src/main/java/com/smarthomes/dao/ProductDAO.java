package com.smarthomes.dao;

import com.smarthomes.models.Product;
import com.smarthomes.util.DatabaseConnection;
import com.smarthomes.util.MySQLDataStoreUtilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductDAO {
    private static final Logger LOGGER = Logger.getLogger(ProductDAO.class.getName());

    public List<Product> getAllProducts() throws SQLException {
        return MySQLDataStoreUtilities.getAllProducts();
    }

    public Product getProductById(int id) throws SQLException {
        return MySQLDataStoreUtilities.getProductById(id);
    }

    public void createProduct(Product product) throws SQLException {
        MySQLDataStoreUtilities.createProduct(product);
    }

    public void updateProduct(Product product) throws SQLException {
        MySQLDataStoreUtilities.updateProduct(product);
    }

    public void deleteProduct(int id) throws SQLException {
        MySQLDataStoreUtilities.deleteProduct(id);
    }

    public List<Product> getProductAccessories(int productId) throws SQLException {
        return MySQLDataStoreUtilities.getProductAccessories(productId);
    }

    public List<String> getProductWarranty(int productId) throws SQLException {
        return MySQLDataStoreUtilities.getProductWarranty(productId);
    }

    public List<String> getProductDiscounts(int productId) throws SQLException {
        return MySQLDataStoreUtilities.getProductDiscounts(productId);
    }

    public boolean categoryExists(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM product_categories WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if category exists: " + e.getMessage(), e);
            throw e;
        }
        return false;
    }
}