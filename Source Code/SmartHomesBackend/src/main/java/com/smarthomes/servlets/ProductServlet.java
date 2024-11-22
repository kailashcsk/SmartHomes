package com.smarthomes.servlets;

import com.smarthomes.dao.ProductDAO;
import com.smarthomes.models.Product;
import com.smarthomes.models.User;
import com.smarthomes.util.AjaxUtility;
import com.smarthomes.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.smarthomes.util.EmbeddingSync;

//@WebServlet("/api/products/*")
public class ProductServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ProductServlet.class.getName());
    private ProductDAO productDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        productDAO = new ProductDAO();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        LOGGER.info("Received GET request with pathInfo: " + pathInfo);

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                getAllProducts(response);
            } else {
                String[] splits = pathInfo.split("/");
                if (splits.length < 2) {
                    sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request format");
                    return;
                }
                int productId = Integer.parseInt(splits[1]);

                if (splits.length == 2) {
                    getProductById(productId, response);
                } else if (splits.length == 3) {
                    switch (splits[2]) {
                        case "accessories":
                            getProductAccessories(productId, response);
                            break;
                        case "warranty":
                            getProductWarranty(productId, response);
                            break;
                        case "discounts":
                            getProductDiscounts(productId, response);
                            break;
                        default:
                            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
                    }
                } else {
                    sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error in doGet: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid product ID format: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID format");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can add products");
            return;
        }

        try {
            Product product = objectMapper.readValue(request.getReader(), Product.class);

            // Validate product data
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Product name is required");
                return;
            }
            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid product price is required");
                return;
            }
            if (product.getCategoryId() <= 0 || !productDAO.categoryExists(product.getCategoryId())) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid category ID");
                return;
            }
            // Add more validation as needed

            LOGGER.info("Attempting to create product: " + product.getName());
            productDAO.createProduct(product);
            LOGGER.info("Product created successfully: " + product.getId());

            // Sync with Elasticsearch
            EmbeddingSync.syncProduct(product);
            LOGGER.info("Synced product with Elasticsearch: " + product.getId());

            // Add the new product to the HashMap
            AjaxUtility.addOrUpdateProductInMap(product);
            sendJsonResponse(response, HttpServletResponse.SC_CREATED, product);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error in doPost: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error creating product: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in doPost: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unexpected error creating product: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can update products");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Product ID is required");
            return;
        }

        try {
            int productId = Integer.parseInt(pathInfo.substring(1));
            Product product = objectMapper.readValue(request.getReader(), Product.class);
            product.setId(productId);
            productDAO.updateProduct(product);

            // Sync with Elasticsearch
            EmbeddingSync.syncProduct(product);
            LOGGER.info("Synced updated product with Elasticsearch: " + product.getId());


            // Update the product in the HashMap
            AjaxUtility.addOrUpdateProductInMap(product);
            sendJsonResponse(response, HttpServletResponse.SC_OK, product);
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating product");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can delete products");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Product ID is required");
            return;
        }

        try {
            int productId = Integer.parseInt(pathInfo.substring(1));
            productDAO.deleteProduct(productId);

            // Delete from Elasticsearch
            EmbeddingSync.deleteProduct(productId);
            LOGGER.info("Deleted product from Elasticsearch: " + productId);
            
            // Remove the product from the HashMap
            AjaxUtility.removeProductFromMap(productId);
            sendJsonResponse(response, HttpServletResponse.SC_OK, "Product deleted successfully");
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting product");
        }
    }

    private void getAllProducts(HttpServletResponse response) throws SQLException, IOException {
        List<Product> products = productDAO.getAllProducts();
        sendJsonResponse(response, HttpServletResponse.SC_OK, products);
    }

    private void getProductById(int productId, HttpServletResponse response) throws SQLException, IOException {
        Product product = productDAO.getProductById(productId);
        if (product != null) {
            sendJsonResponse(response, HttpServletResponse.SC_OK, product);
        } else {
            sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Product not found");
        }
    }

    private void getProductAccessories(int productId, HttpServletResponse response) throws SQLException, IOException {
        List<Product> accessories = productDAO.getProductAccessories(productId);
        sendJsonResponse(response, HttpServletResponse.SC_OK, accessories);
    }

    private void getProductWarranty(int productId, HttpServletResponse response) throws SQLException, IOException {
        LOGGER.info("Fetching warranty for product ID: " + productId);
        List<String> warranties = productDAO.getProductWarranty(productId);
        sendJsonResponse(response, HttpServletResponse.SC_OK, warranties);
    }

    private void getProductDiscounts(int productId, HttpServletResponse response) throws SQLException, IOException {
        List<String> discounts = productDAO.getProductDiscounts(productId);
        sendJsonResponse(response, HttpServletResponse.SC_OK, discounts);
    }

    private boolean isStoreManager(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            User user = JwtUtil.verifyToken(token);
            return user != null && user.getRole() == User.Role.STOREMANAGER;
        }
        return false;
    }

    private void sendJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), data);
    }

    private void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ObjectNode jsonResponse = objectMapper.createObjectNode();
        jsonResponse.put("error", message);
        objectMapper.writeValue(response.getWriter(), jsonResponse);
    }
}