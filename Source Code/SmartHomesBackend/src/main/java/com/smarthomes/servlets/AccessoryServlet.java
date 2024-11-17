package com.smarthomes.servlets;

import com.smarthomes.dao.AccessoryDAO;
import com.smarthomes.models.Accessory;
import com.smarthomes.models.User;
import com.smarthomes.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccessoryServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AccessoryServlet.class.getName());
    private AccessoryDAO accessoryDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        accessoryDAO = new AccessoryDAO();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        LOGGER.info("Received GET request with pathInfo: " + pathInfo);

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                getAllAccessories(response);
            } else {
                int accessoryId = Integer.parseInt(pathInfo.substring(1));
                getAccessoryById(accessoryId, response);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error in doGet: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid accessory ID format: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid accessory ID format");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can add accessories");
            return;
        }

        try {
            Accessory accessory = objectMapper.readValue(request.getReader(), Accessory.class);

            if (!isValidAccessory(accessory)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid accessory data");
                return;
            }

            LOGGER.info("Attempting to create accessory for product ID: " + accessory.getProductId());
            accessoryDAO.createAccessory(accessory);
            LOGGER.info("Accessory created successfully: " + accessory.getId());
            sendJsonResponse(response, HttpServletResponse.SC_CREATED, accessory);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error in doPost: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error creating accessory: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can update accessories");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Accessory ID is required");
            return;
        }

        try {
            int accessoryId = Integer.parseInt(pathInfo.substring(1));
            Accessory accessory = objectMapper.readValue(request.getReader(), Accessory.class);
            accessory.setId(accessoryId);

            if (!isValidAccessory(accessory)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid accessory data");
                return;
            }

            accessoryDAO.updateAccessory(accessory);
            sendJsonResponse(response, HttpServletResponse.SC_OK, accessory);
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error updating accessory: " + e.getMessage());
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid accessory ID format");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can delete accessories");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Accessory ID is required");
            return;
        }

        try {
            int accessoryId = Integer.parseInt(pathInfo.substring(1));
            accessoryDAO.deleteAccessory(accessoryId);
            sendJsonResponse(response, HttpServletResponse.SC_OK, "Accessory deleted successfully");
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error deleting accessory: " + e.getMessage());
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid accessory ID format");
        }
    }

    private void getAllAccessories(HttpServletResponse response) throws SQLException, IOException {
        List<Accessory> accessories = accessoryDAO.getAllAccessories();
        sendJsonResponse(response, HttpServletResponse.SC_OK, accessories);
    }

    private void getAccessoryById(int accessoryId, HttpServletResponse response) throws SQLException, IOException {
        Accessory accessory = accessoryDAO.getAccessoryById(accessoryId);
        if (accessory != null) {
            sendJsonResponse(response, HttpServletResponse.SC_OK, accessory);
        } else {
            sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Accessory not found");
        }
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

    private boolean isValidAccessory(Accessory accessory) {
        return accessory.getProductId() > 0 &&
                accessory.getName() != null && !accessory.getName().trim().isEmpty() &&
                accessory.getPrice() != null && accessory.getPrice().compareTo(BigDecimal.ZERO) > 0 &&
                accessory.getDescription() != null && !accessory.getDescription().trim().isEmpty();
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