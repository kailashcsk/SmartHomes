package com.smarthomes.servlets;

import com.smarthomes.dao.InventoryReportDAO;
import com.smarthomes.dto.InventoryDTO;
import com.smarthomes.dto.InventoryReportDTO;
import com.smarthomes.models.User;
import com.smarthomes.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class InventoryReportServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(InventoryReportServlet.class.getName());
    private InventoryReportDAO inventoryReportDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        inventoryReportDAO = new InventoryReportDAO();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied. StoreManager role required.");
            return;
        }

        String pathInfo = request.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                getAllProductsWithInventory(response);
            } else {
                switch (pathInfo) {
                    case "/on-sale":
                        getProductsOnSale(response);
                        break;
                    case "/with-rebates":
                        getProductsWithRebates(response);
                        break;
                    case "/inventory-counts":
                        getProductInventoryCounts(response);
                        break;
                    default:
                        sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in InventoryReportServlet", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied. StoreManager role required.");
            return;
        }

        try {
            InventoryDTO inventoryItem = objectMapper.readValue(request.getReader(), InventoryDTO.class);
            
            // Get the current inventory count
            InventoryDTO existingItem = inventoryReportDAO.getInventoryById(inventoryItem.getId());
            
            if (existingItem != null) {
                // Add the new count to the existing count
                int newCount = existingItem.getInventoryCount() + inventoryItem.getInventoryCount();
                inventoryItem.setInventoryCount(newCount);
                
                inventoryReportDAO.updateInventory(inventoryItem);
                sendJsonResponse(response, HttpServletResponse.SC_OK, "Inventory updated successfully");
            } else {
                // If the item doesn't exist, create a new entry
                inventoryReportDAO.addInventory(inventoryItem);
                sendJsonResponse(response, HttpServletResponse.SC_CREATED, "New inventory item added successfully");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while adding/updating inventory", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error adding/updating inventory: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied. StoreManager role required.");
            return;
        }

        try {
            InventoryDTO inventoryItem = objectMapper.readValue(request.getReader(), InventoryDTO.class);
            
            // Update the inventory count to the exact value provided
            inventoryReportDAO.updateInventory(inventoryItem);
            sendJsonResponse(response, HttpServletResponse.SC_OK, "Inventory updated successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while updating inventory", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating inventory: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied. StoreManager role required.");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Product ID is required");
            return;
        }

        try {
            int productId = Integer.parseInt(pathInfo.substring(1));
            inventoryReportDAO.deleteInventory(productId);
            sendJsonResponse(response, HttpServletResponse.SC_OK, "Inventory deleted successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while deleting inventory", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error deleting inventory: " + e.getMessage());
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID format");
        }
    }

    private void getAllProductsWithInventory(HttpServletResponse response) throws SQLException, IOException {
        List<InventoryReportDTO> products = inventoryReportDAO.getAllProductsWithInventory();
        sendJsonResponse(response, HttpServletResponse.SC_OK, products);
    }

    private void getProductsOnSale(HttpServletResponse response) throws SQLException, IOException {
        List<Map<String, Object>> productsOnSale = inventoryReportDAO.getProductsOnSale();
        sendJsonResponse(response, HttpServletResponse.SC_OK, productsOnSale);
    }

    private void getProductsWithRebates(HttpServletResponse response) throws SQLException, IOException {
        List<Map<String, Object>> productsWithRebates = inventoryReportDAO.getProductsWithRebates();
        sendJsonResponse(response, HttpServletResponse.SC_OK, productsWithRebates);
    }

    private void getProductInventoryCounts(HttpServletResponse response) throws SQLException, IOException {
        List<Map<String, Object>> inventoryCounts = inventoryReportDAO.getProductInventoryCounts();
        sendJsonResponse(response, HttpServletResponse.SC_OK, inventoryCounts);
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