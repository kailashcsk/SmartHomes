package com.smarthomes.servlets;

import com.smarthomes.dao.WarrantyDAO;
import com.smarthomes.models.Warranty;
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

public class WarrantyServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(WarrantyServlet.class.getName());
    private WarrantyDAO warrantyDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        warrantyDAO = new WarrantyDAO();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        LOGGER.info("Received GET request with pathInfo: " + pathInfo);

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                getAllWarranties(response);
            } else {
                int warrantyId = Integer.parseInt(pathInfo.substring(1));
                getWarrantyById(warrantyId, response);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error in doGet: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid warranty ID format: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid warranty ID format");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can add warranties");
            return;
        }

        try {
            Warranty warranty = objectMapper.readValue(request.getReader(), Warranty.class);

            if (!isValidWarranty(warranty)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid warranty data");
                return;
            }

            LOGGER.info("Attempting to create warranty for product ID: " + warranty.getProductId());
            warrantyDAO.createWarranty(warranty);
            LOGGER.info("Warranty created successfully: " + warranty.getId());
            sendJsonResponse(response, HttpServletResponse.SC_CREATED, warranty);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error in doPost: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error creating warranty: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can update warranties");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Warranty ID is required");
            return;
        }

        try {
            int warrantyId = Integer.parseInt(pathInfo.substring(1));
            Warranty warranty = objectMapper.readValue(request.getReader(), Warranty.class);
            warranty.setId(warrantyId);

            if (!isValidWarranty(warranty)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid warranty data");
                return;
            }

            warrantyDAO.updateWarranty(warranty);
            sendJsonResponse(response, HttpServletResponse.SC_OK, warranty);
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error updating warranty: " + e.getMessage());
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid warranty ID format");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can delete warranties");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Warranty ID is required");
            return;
        }

        try {
            int warrantyId = Integer.parseInt(pathInfo.substring(1));
            warrantyDAO.deleteWarranty(warrantyId);
            sendJsonResponse(response, HttpServletResponse.SC_OK, "Warranty deleted successfully");
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error deleting warranty: " + e.getMessage());
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid warranty ID format");
        }
    }

    private void getAllWarranties(HttpServletResponse response) throws SQLException, IOException {
        List<Warranty> warranties = warrantyDAO.getAllWarranties();
        sendJsonResponse(response, HttpServletResponse.SC_OK, warranties);
    }

    private void getWarrantyById(int warrantyId, HttpServletResponse response) throws SQLException, IOException {
        Warranty warranty = warrantyDAO.getWarrantyById(warrantyId);
        if (warranty != null) {
            sendJsonResponse(response, HttpServletResponse.SC_OK, warranty);
        } else {
            sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Warranty not found");
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

    private boolean isValidWarranty(Warranty warranty) {
        return warranty.getProductId() > 0 &&
                warranty.getDuration() > 0 &&
                warranty.getPrice() != null && warranty.getPrice().compareTo(BigDecimal.ZERO) > 0 &&
                warranty.getDescription() != null && !warranty.getDescription().trim().isEmpty();
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