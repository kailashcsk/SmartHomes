package com.smarthomes.servlets;

import com.smarthomes.dao.SalesReportDAO;
import com.smarthomes.dto.SalesReportDTO;
import com.smarthomes.models.User;
import com.smarthomes.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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

public class SalesReportServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SalesReportServlet.class.getName());
    private SalesReportDAO salesReportDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        salesReportDAO = new SalesReportDAO();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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
            if ("/product-sales".equals(pathInfo)) {
                List<SalesReportDTO.ProductSalesSummary> summary = salesReportDAO.getProductSalesSummary();
                sendJsonResponse(response, HttpServletResponse.SC_OK, summary);
            } else if ("/daily-sales".equals(pathInfo)) {
                List<SalesReportDTO.DailySalesTransaction> transactions = salesReportDAO.getDailySalesTransactions();
                sendJsonResponse(response, HttpServletResponse.SC_OK, transactions);
            } else {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in SalesReportServlet", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private void sendJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonString = objectMapper.writeValueAsString(data);
        response.getWriter().write(jsonString);
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

    private void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), new ErrorResponse(message));
    }

    private static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}