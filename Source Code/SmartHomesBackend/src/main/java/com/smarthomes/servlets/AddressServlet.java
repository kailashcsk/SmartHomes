package com.smarthomes.servlets;

import com.smarthomes.dao.AddressDAO;
import com.smarthomes.models.Address;
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

//@WebServlet("/api/address/*")
public class AddressServlet extends HttpServlet {
    private AddressDAO addressDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        addressDAO = new AddressDAO();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        User user = JwtUtil.verifyToken(request.getHeader("Authorization").substring(7));

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Address> addresses = addressDAO.getAddressesByUserId(user.getId());
                sendJsonResponse(response, HttpServletResponse.SC_OK, addresses);
            } else {
                int addressId = Integer.parseInt(pathInfo.substring(1));
                Address address = addressDAO.getAddressByIdAndUserId(addressId, user.getId());
                if (address != null) {
                    sendJsonResponse(response, HttpServletResponse.SC_OK, address);
                } else {
                    sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Address not found");
                }
            }
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = JwtUtil.verifyToken(request.getHeader("Authorization").substring(7));
        Address address = objectMapper.readValue(request.getReader(), Address.class);
        address.setUserId(user.getId());

        try {
            addressDAO.createAddress(address);
            sendJsonResponse(response, HttpServletResponse.SC_CREATED, address);
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error creating address: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Address ID is required");
            return;
        }

        int addressId = Integer.parseInt(pathInfo.substring(1));
        User user = JwtUtil.verifyToken(request.getHeader("Authorization").substring(7));
        Address updatedAddress = objectMapper.readValue(request.getReader(), Address.class);
        updatedAddress.setId(addressId);
        updatedAddress.setUserId(user.getId());

        try {
            Address existingAddress = addressDAO.getAddressByIdAndUserId(addressId, user.getId());
            if (existingAddress == null) {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Address not found");
                return;
            }

            // Update only non-null fields
            if (updatedAddress.getStreet() != null)
                existingAddress.setStreet(updatedAddress.getStreet());
            if (updatedAddress.getCity() != null)
                existingAddress.setCity(updatedAddress.getCity());
            if (updatedAddress.getState() != null)
                existingAddress.setState(updatedAddress.getState());
            if (updatedAddress.getZipCode() != null)
                existingAddress.setZipCode(updatedAddress.getZipCode());

            addressDAO.updateAddress(existingAddress);
            sendJsonResponse(response, HttpServletResponse.SC_OK, existingAddress);
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error updating address: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Address ID is required");
            return;
        }

        int addressId = Integer.parseInt(pathInfo.substring(1));
        User user = JwtUtil.verifyToken(request.getHeader("Authorization").substring(7));

        try {
            addressDAO.deleteAddress(addressId, user.getId());
            sendJsonResponse(response, HttpServletResponse.SC_OK, "Address deleted successfully");
        } catch (SQLException e) {
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error deleting address: " + e.getMessage());
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