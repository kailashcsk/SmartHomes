package com.smarthomes.servlets;

import com.smarthomes.dao.UserDAO;
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

//@WebServlet("/api/users/*")
public class UserManagementServlet extends HttpServlet {
    private UserDAO userDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isSalesman(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied. Salesman only.");
            return;
        }

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            getAllUsers(response);
        } else {
            try {
                int userId = Integer.parseInt(pathInfo.substring(1));
                getUserById(response, userId);
            } catch (NumberFormatException e) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isSalesman(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied. Salesman only.");
            return;
        }

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "User ID is required");
            return;
        }

        try {
            int userId = Integer.parseInt(pathInfo.substring(1));
            User existingUser = userDAO.getUserById(userId);

            if (existingUser == null) {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            User updatedFields = objectMapper.readValue(request.getReader(), User.class);
            updatedFields.setId(userId);

            // Only update fields that are not null in updatedFields
            if (updatedFields.getName() != null) {
                existingUser.setName(updatedFields.getName());
            }
            if (updatedFields.getEmail() != null) {
                existingUser.setEmail(updatedFields.getEmail());
            }
            if (updatedFields.getRole() != null) {
                existingUser.setRole(updatedFields.getRole());
            }
            if (updatedFields.getPassword() != null) {
                existingUser.setPassword(updatedFields.getPassword());
            }

            userDAO.updateUser(existingUser);

            // Remove sensitive information before sending response
            existingUser.setPassword(null);
            sendJsonResponse(response, HttpServletResponse.SC_OK, existingUser);
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error updating user: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isSalesman(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied. Salesman only.");
            return;
        }

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "User ID is required");
            return;
        }

        try {
            int userId = Integer.parseInt(pathInfo.substring(1));
            deleteUser(response, userId);
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
        }
    }

    private void getAllUsers(HttpServletResponse response) throws IOException {
        try {
            List<User> users = userDAO.getAllUsers();
            sendJsonResponse(response, HttpServletResponse.SC_OK, users);
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving users");
        }
    }

    private void getUserById(HttpServletResponse response, int userId) throws IOException {
        try {
            User user = userDAO.getUserById(userId);
            if (user != null) {
                sendJsonResponse(response, HttpServletResponse.SC_OK, user);
            } else {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
            }
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving user");
        }
    }

    private void deleteUser(HttpServletResponse response, int userId) throws IOException {
        try {
            userDAO.deleteUser(userId);
            sendJsonResponse(response, HttpServletResponse.SC_NO_CONTENT, null);
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting user");
        }
    }

    private boolean isSalesman(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            User user = JwtUtil.verifyToken(token);
            return user != null && user.getRole() == User.Role.SALESMAN;
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