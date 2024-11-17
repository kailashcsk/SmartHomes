package com.smarthomes.servlets;

import com.smarthomes.dao.SalesmanOrderDAO;
import com.smarthomes.dao.UserDAO;
import com.smarthomes.models.Order;
import com.smarthomes.models.User;
import com.smarthomes.util.JwtUtil;
import com.smarthomes.util.MySQLDataStoreUtilities;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


//@WebServlet("/api/allorders/*")
public class SalesmanOrderServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SalesmanOrderServlet.class.getName());
    private SalesmanOrderDAO salesmanOrderDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        salesmanOrderDAO = new SalesmanOrderDAO();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isSalesman(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied. Salesman only.");
            return;
        }

        try {
            Order order = objectMapper.readValue(request.getReader(), Order.class);

            // Validate that userId is provided
            if (order.getUserId() <= 0) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid user ID is required");
                return;
            }

            // Check if the user exists
            if (!MySQLDataStoreUtilities.userExistsById(order.getUserId())) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "User with ID " + order.getUserId() + " does not exist.");
                return;
            }

            // Set the order date and calculate ship date
            order.setOrderDate(new Date());
            LocalDate orderDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate shipDate = "HOME_DELIVERY".equals(order.getDeliveryMethod()) ? orderDate.plusDays(15)
                    : orderDate.plusDays(5);
            order.setShipDate(Date.from(shipDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

            // Create the order
            salesmanOrderDAO.createOrder(order);
            sendJsonResponse(response, HttpServletResponse.SC_CREATED, order);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if (!isSalesman(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied. Salesman only.");
            return;
        }

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                getAllOrders(response);
            } else {
                int orderId = Integer.parseInt(pathInfo.substring(1));
                getOrderById(orderId, response);
            }
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID format");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isSalesman(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied. Salesman only.");
            return;
        }

        try {
            int orderId = Integer.parseInt(request.getPathInfo().substring(1));
            Order updatedFields = objectMapper.readValue(request.getReader(), Order.class);
            updatedFields.setId(orderId);

            // Fetch the existing order
            Order existingOrder = salesmanOrderDAO.getOrderById(orderId);
            if (existingOrder == null) {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Order not found");
                return;
            }

            // Update only the fields that are provided in the request
            salesmanOrderDAO.updateOrder(updatedFields);
            sendJsonResponse(response, HttpServletResponse.SC_OK, "Order updated successfully");
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID format");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isSalesman(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied. Salesman only.");
            return;
        }

        try {
            int orderId = Integer.parseInt(request.getPathInfo().substring(1));
            salesmanOrderDAO.deleteOrder(orderId);
            sendJsonResponse(response, HttpServletResponse.SC_OK, "Order deleted successfully");
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID format");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private void getAllOrders(HttpServletResponse response) throws SQLException, IOException {
        List<Order> orders = salesmanOrderDAO.getAllOrders();
        sendJsonResponse(response, HttpServletResponse.SC_OK, orders);
    }

    private void getOrderById(int orderId, HttpServletResponse response) throws SQLException, IOException {
        Order order = salesmanOrderDAO.getOrderById(orderId);
        if (order != null) {
            sendJsonResponse(response, HttpServletResponse.SC_OK, order);
        } else {
            sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Order not found");
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