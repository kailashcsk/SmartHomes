package com.smarthomes.servlets;

import com.smarthomes.dao.UserDAO;
import com.smarthomes.models.User;
import com.smarthomes.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

//@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    private UserDAO userDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User credentials = objectMapper.readValue(request.getReader(), User.class);

        try {
            User user = userDAO.authenticate(credentials.getEmail(), credentials.getPassword());
            if (user != null) {
                String token = JwtUtil.generateToken(user);
                response.setContentType("application/json");
                response.getWriter().write(objectMapper.writeValueAsString(new LoginResponse(token, user)));
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during authentication");
        }
    }

    private static class LoginResponse {
        public String token;
        public User user;

        public LoginResponse(String token, User user) {
            this.token = token;
            this.user = user;
        }
    }
}