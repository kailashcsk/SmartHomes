package com.smarthomes.servlets;

import com.smarthomes.dao.StoreDAO;
import com.smarthomes.models.Store;
import com.smarthomes.models.User;
import com.smarthomes.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StoreServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(StoreServlet.class.getName());
    private StoreDAO storeDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        storeDAO = new StoreDAO();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        LOGGER.info("Received GET request with pathInfo: " + pathInfo);

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                getAllStores(response);
            } else {
                int storeId = Integer.parseInt(pathInfo.substring(1));
                getStoreById(storeId, response);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error in doGet: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid store ID format: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid store ID format");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can add stores");
            return;
        }

        try {
            Store store = objectMapper.readValue(request.getReader(), Store.class);

            if (!isValidStore(store)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid store data");
                return;
            }

            LOGGER.info("Attempting to create store: " + store.getName());
            storeDAO.createStore(store);
            LOGGER.info("Store created successfully: " + store.getId());
            sendJsonResponse(response, HttpServletResponse.SC_CREATED, store);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error in doPost: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error creating store: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can update stores");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Store ID is required");
            return;
        }

        try {
            int storeId = Integer.parseInt(pathInfo.substring(1));
            Store store = objectMapper.readValue(request.getReader(), Store.class);
            store.setId(storeId);

            if (!isValidStore(store)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid store data");
                return;
            }

            storeDAO.updateStore(store);
            sendJsonResponse(response, HttpServletResponse.SC_OK, store);
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error updating store: " + e.getMessage());
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid store ID format");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can delete stores");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Store ID is required");
            return;
        }

        try {
            int storeId = Integer.parseInt(pathInfo.substring(1));
            storeDAO.deleteStore(storeId);
            sendJsonResponse(response, HttpServletResponse.SC_OK, "Store deleted successfully");
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error deleting store: " + e.getMessage());
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid store ID format");
        }
    }

    private void getAllStores(HttpServletResponse response) throws SQLException, IOException {
        List<Store> stores = storeDAO.getAllStores();
        sendJsonResponse(response, HttpServletResponse.SC_OK, stores);
    }

    private void getStoreById(int storeId, HttpServletResponse response) throws SQLException, IOException {
        Store store = storeDAO.getStoreById(storeId);
        if (store != null) {
            sendJsonResponse(response, HttpServletResponse.SC_OK, store);
        } else {
            sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Store not found");
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

    private boolean isValidStore(Store store) {
        return store.getName() != null && !store.getName().trim().isEmpty() &&
                store.getStreet() != null && !store.getStreet().trim().isEmpty() &&
                store.getCity() != null && !store.getCity().trim().isEmpty() &&
                store.getState() != null && !store.getState().trim().isEmpty() &&
                store.getZipCode() != null && !store.getZipCode().trim().isEmpty();
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