package com.smarthomes.servlets;

import com.smarthomes.dao.CategoryDAO;
import com.smarthomes.models.Category;
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

public class CategoryServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CategoryServlet.class.getName());
    private CategoryDAO categoryDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        categoryDAO = new CategoryDAO();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        LOGGER.info("Received GET request with pathInfo: " + pathInfo);

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                getAllCategories(response);
            } else {
                int categoryId = Integer.parseInt(pathInfo.substring(1));
                getCategoryById(categoryId, response);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error in doGet: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid category ID format: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid category ID format");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can add categories");
            return;
        }

        try {
            Category category = objectMapper.readValue(request.getReader(), Category.class);

            if (category.getName() == null || category.getName().trim().isEmpty()) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Category name is required");
                return;
            }

            LOGGER.info("Attempting to create category: " + category.getName());
            categoryDAO.createCategory(category);
            LOGGER.info("Category created successfully: " + category.getId());
            sendJsonResponse(response, HttpServletResponse.SC_CREATED, category);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error in doPost: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error creating category: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can update categories");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Category ID is required");
            return;
        }

        try {
            int categoryId = Integer.parseInt(pathInfo.substring(1));
            Category category = objectMapper.readValue(request.getReader(), Category.class);
            category.setId(categoryId);

            if (category.getName() == null || category.getName().trim().isEmpty()) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Category name is required");
                return;
            }

            categoryDAO.updateCategory(category);
            sendJsonResponse(response, HttpServletResponse.SC_OK, category);
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error updating category: " + e.getMessage());
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid category ID format");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can delete categories");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Category ID is required");
            return;
        }

        try {
            int categoryId = Integer.parseInt(pathInfo.substring(1));
            categoryDAO.deleteCategory(categoryId);
            sendJsonResponse(response, HttpServletResponse.SC_OK, "Category deleted successfully");
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error deleting category: " + e.getMessage());
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid category ID format");
        }
    }

    private void getAllCategories(HttpServletResponse response) throws SQLException, IOException {
        List<Category> categories = categoryDAO.getAllCategories();
        sendJsonResponse(response, HttpServletResponse.SC_OK, categories);
    }

    private void getCategoryById(int categoryId, HttpServletResponse response) throws SQLException, IOException {
        Category category = categoryDAO.getCategoryById(categoryId);
        if (category != null) {
            sendJsonResponse(response, HttpServletResponse.SC_OK, category);
        } else {
            sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Category not found");
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