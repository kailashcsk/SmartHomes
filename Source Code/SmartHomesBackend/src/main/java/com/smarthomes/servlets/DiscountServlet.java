package com.smarthomes.servlets;

import com.smarthomes.dao.DiscountDAO;
import com.smarthomes.models.Discount;
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

public class DiscountServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DiscountServlet.class.getName());
    private DiscountDAO discountDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        discountDAO = new DiscountDAO();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        LOGGER.info("Received GET request with pathInfo: " + pathInfo);

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                getAllDiscounts(response);
            } else {
                int discountId = Integer.parseInt(pathInfo.substring(1));
                getDiscountById(discountId, response);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error in doGet: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid discount ID format: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid discount ID format");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can add discounts");
            return;
        }

        try {
            Discount discount = objectMapper.readValue(request.getReader(), Discount.class);

            if (!isValidDiscount(discount)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid discount data");
                return;
            }

            LOGGER.info("Attempting to create discount for product ID: " + discount.getProductId());
            discountDAO.createDiscount(discount);
            LOGGER.info("Discount created successfully: " + discount.getId());
            sendJsonResponse(response, HttpServletResponse.SC_CREATED, discount);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error in doPost: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error creating discount: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can update discounts");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Discount ID is required");
            return;
        }

        try {
            int discountId = Integer.parseInt(pathInfo.substring(1));
            Discount discount = objectMapper.readValue(request.getReader(), Discount.class);
            discount.setId(discountId);

            if (!isValidDiscount(discount)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid discount data");
                return;
            }

            discountDAO.updateDiscount(discount);
            sendJsonResponse(response, HttpServletResponse.SC_OK, discount);
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error updating discount: " + e.getMessage());
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid discount ID format");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isStoreManager(request)) {
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Only StoreManager can delete discounts");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Discount ID is required");
            return;
        }

        try {
            int discountId = Integer.parseInt(pathInfo.substring(1));
            discountDAO.deleteDiscount(discountId);
            sendJsonResponse(response, HttpServletResponse.SC_OK, "Discount deleted successfully");
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error deleting discount: " + e.getMessage());
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid discount ID format");
        }
    }

    private void getAllDiscounts(HttpServletResponse response) throws SQLException, IOException {
        List<Discount> discounts = discountDAO.getAllDiscounts();
        sendJsonResponse(response, HttpServletResponse.SC_OK, discounts);
    }

    private void getDiscountById(int discountId, HttpServletResponse response) throws SQLException, IOException {
        Discount discount = discountDAO.getDiscountById(discountId);
        if (discount != null) {
            sendJsonResponse(response, HttpServletResponse.SC_OK, discount);
        } else {
            sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Discount not found");
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

    private boolean isValidDiscount(Discount discount) {
        return discount.getProductId() > 0 &&
                discount.getDiscountType() != null && !discount.getDiscountType().trim().isEmpty() &&
                discount.getAmount() != null && discount.getAmount().compareTo(BigDecimal.ZERO) > 0 &&
                discount.getStartDate() != null &&
                discount.getEndDate() != null &&
                discount.getStartDate().before(discount.getEndDate());
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