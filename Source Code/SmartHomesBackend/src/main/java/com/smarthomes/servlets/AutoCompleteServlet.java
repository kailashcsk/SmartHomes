package com.smarthomes.servlets;

import com.smarthomes.util.AjaxUtility;
import com.smarthomes.models.Product;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class AutoCompleteServlet extends HttpServlet {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String searchTerm = request.getParameter("term");
        if (searchTerm != null && !searchTerm.isEmpty()) {
            List<Product> suggestions = AjaxUtility.getAutoCompleteResults(searchTerm);
            String jsonResponse = objectMapper.writeValueAsString(suggestions);
            response.setContentType("application/json");
            response.getWriter().write(jsonResponse);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}