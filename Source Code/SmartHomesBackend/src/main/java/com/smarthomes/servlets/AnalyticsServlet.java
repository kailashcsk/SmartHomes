package com.smarthomes.servlets;

import com.smarthomes.dao.AnalyticsDAO;
import com.smarthomes.models.Product;
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

//@WebServlet("/api/analytics/*")
public class AnalyticsServlet extends HttpServlet {
    private AnalyticsDAO analyticsDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        analyticsDAO = new AnalyticsDAO();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if ("/trending-products".equals(pathInfo)) {
                List<Product> trendingProducts = analyticsDAO.getTopLikedProducts(5);
                sendJsonResponse(response, HttpServletResponse.SC_OK, trendingProducts);
            } else if ("/top-zipcodes".equals(pathInfo)) {
                List<Map<String, Object>> topZipCodes = analyticsDAO.getTopSellingZipCodes(5);
                sendJsonResponse(response, HttpServletResponse.SC_OK, topZipCodes);
            } else if ("/best-selling-products".equals(pathInfo)) {
                List<Product> bestSellingProducts = analyticsDAO.getBestSellingProducts(5);
                sendJsonResponse(response, HttpServletResponse.SC_OK, bestSellingProducts);
            } else {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint");
            }
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
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