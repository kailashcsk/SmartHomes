package com.smarthomes.servlets;

import com.smarthomes.dao.OrderDAO;
import com.smarthomes.models.Order;
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
import java.util.logging.Level;
import java.util.logging.Logger;

//@WebServlet("/api/orders/*")
public class OrderServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(OrderServlet.class.getName());
    private OrderDAO orderDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        orderDAO = new OrderDAO();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            createOrder(request, response);
        } else {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            getAllOrders(request, response);
        } else {
            getOrderDetails(request, response);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        cancelOrder(request, response);
    }

    private void createOrder(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Order order = objectMapper.readValue(request.getReader(), Order.class);
            User user = JwtUtil.verifyToken(request.getHeader("Authorization").substring(7));
            order.setUserId(user.getId());

            orderDAO.createOrder(order);
            sendJsonResponse(response, HttpServletResponse.SC_CREATED, order);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating order: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error creating order: " + e.getMessage());
        }
    }

    private void getAllOrders(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            User user = JwtUtil.verifyToken(request.getHeader("Authorization").substring(7));
            List<Order> orders = orderDAO.getOrdersByUserId(user.getId());
            sendJsonResponse(response, HttpServletResponse.SC_OK, orders);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching orders: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error fetching orders: " + e.getMessage());
        }
    }

    private void getOrderDetails(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] splits = pathInfo.split("/");
        if (splits.length != 2) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID");
            return;
        }

        try {
            int orderId = Integer.parseInt(splits[1]);
            User user = JwtUtil.verifyToken(request.getHeader("Authorization").substring(7));
            Order order = orderDAO.getOrderByUserIdAndOrderId(user.getId(), orderId);

            if (order != null) {
                sendJsonResponse(response, HttpServletResponse.SC_OK, order);
            } else {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Order not found");
            }
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID format");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching order details: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error fetching order details: " + e.getMessage());
        }
    }

    private void cancelOrder(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] splits = pathInfo.split("/");
        if (splits.length != 2) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID");
            return;
        }

        try {
            int orderId = Integer.parseInt(splits[1]);
            User user = JwtUtil.verifyToken(request.getHeader("Authorization").substring(7));
            boolean cancelled = orderDAO.cancelOrder(user.getId(), orderId);

            if (cancelled) {
                sendJsonResponse(response, HttpServletResponse.SC_OK, "Order cancelled successfully");
            } else {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Unable to cancel order. It may be past the cancellation period or already cancelled.");
            }
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID format");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error cancelling order: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error cancelling order: " + e.getMessage());
        }
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