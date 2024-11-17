package com.smarthomes.servlets;

import com.smarthomes.dao.TicketDAO;
import com.smarthomes.models.Ticket;
import com.smarthomes.models.User;
import com.smarthomes.services.OpenAIService;
import com.smarthomes.util.FileUploadUtil;
import com.smarthomes.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// @WebServlet("/api/customer-service/*")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, // 1MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 15 // 15MB
)
public class CustomerServiceServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CustomerServiceServlet.class.getName());
    private TicketDAO ticketDAO;
    private OpenAIService openAIService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        ticketDAO = new TicketDAO();
        openAIService = new OpenAIService();
        objectMapper = new ObjectMapper();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        // Test endpoint for OpenAI integration
        if (pathInfo != null && pathInfo.equals("/test-openai")) {
            handleOpenAITest(request, response);
            return;
        }
        User user = authenticateUser(request);

        if (user == null) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated");
            return;
        }

        if (pathInfo == null || pathInfo.equals("/")) {
            // Create new ticket
            handleTicketCreation(request, response, user);
        } else {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
        }
    }
   
    private User authenticateUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return JwtUtil.verifyToken(token);
        }
        return null;
    }

    private void sendJsonError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        ObjectNode jsonResponse = objectMapper.createObjectNode()
                .put("error", message);
        objectMapper.writeValue(response.getWriter(), jsonResponse);
    }

    private void handleOpenAITest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Part filePart = request.getPart("image");
            String description = request.getParameter("description");

            if (filePart == null || description == null) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Image and description required");
                return;
            }

            // Save the image and get both paths
            FileUploadUtil.FileInfo fileInfo = FileUploadUtil.saveImage(filePart);

            // Use the full file system path for OpenAI analysis
            File imageFile = new File(fileInfo.getFilePath());
            LOGGER.info("Testing OpenAI with image file: " + imageFile.getAbsolutePath());

            // Test OpenAI analysis
            Ticket.Decision decision = openAIService.analyzeImage(imageFile, description);
            LOGGER.info("OpenAI decision: " + decision);

            // Send response
            response.setContentType("application/json");
            ObjectNode jsonResponse = objectMapper.createObjectNode()
                    .put("decision", decision != null ? decision.toString() : "ESCALATE_TO_HUMAN");
            objectMapper.writeValue(response.getWriter(), jsonResponse);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error testing OpenAI integration: " + e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error testing OpenAI: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = authenticateUser(request);
        if (user == null) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated");
            return;
        }

        try {
            String pathInfo = request.getPathInfo();

            // Handle delivered orders request
            if (request.getParameter("delivered-orders") != null) {
                List<TicketDAO.DeliveredOrder> orders = ticketDAO.getUserDeliveredOrders(user.getId());
                response.setContentType("application/json");
                objectMapper.writeValue(response.getWriter(), orders);
                return;
            }

            // Handle regular ticket requests
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Ticket> tickets = ticketDAO.getUserTickets(user.getId());
                ArrayNode ticketsArray = objectMapper.createArrayNode();
                for (Ticket ticket : tickets) {
                    ticketsArray.add(convertTicketToJson(ticket, request));
                }
                response.setContentType("application/json");
                objectMapper.writeValue(response.getWriter(), ticketsArray);
            } else {
                String ticketNumber = pathInfo.substring(1);
                Ticket ticket = ticketDAO.getTicketByNumber(ticketNumber);

                if (ticket == null) {
                    sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Ticket not found");
                    return;
                }

                if (ticket.getUserId() != user.getId()) {
                    sendJsonError(response, HttpServletResponse.SC_FORBIDDEN,
                            "Not authorized to view this ticket");
                    return;
                }

                response.setContentType("application/json");
                objectMapper.writeValue(response.getWriter(), convertTicketToJson(ticket, request));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing request", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error processing request");
        }
    }

    private void handleTicketCreation(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        try {
            String description = request.getParameter("description");
            String orderIdStr = request.getParameter("orderId");
            Part filePart = request.getPart("image");

            // Validate required fields
            if (description == null || description.trim().isEmpty()) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Description is required");
                return;
            }

            if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Order ID is required");
                return;
            }

            if (filePart == null || filePart.getSize() == 0) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Image is required");
                return;
            }

            int orderId = Integer.parseInt(orderIdStr);

            // Validate if order is delivered and belongs to user
            if (!ticketDAO.isValidOrderForTicket(user.getId(), orderId)) {
                sendJsonError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Invalid order ID or order is not in delivered status");
                return;
            }

            // Save the image
            FileUploadUtil.FileInfo fileInfo = FileUploadUtil.saveImage(filePart);

            // Create ticket
            Ticket ticket = new Ticket(user.getId(), description, fileInfo.getWebPath());
            ticket.setOrderId(orderId);

            // Analyze image with OpenAI
            File imageFile = new File(fileInfo.getFilePath());
            Ticket.Decision decision = openAIService.analyzeImage(imageFile, description);

            if (decision != null) {
                ticket.setDecision(decision);
            } else {
                ticket.setDecision(Ticket.Decision.ESCALATE_TO_HUMAN);
                LOGGER.warning("OpenAI returned null decision, defaulting to ESCALATE_TO_HUMAN");
            }
            ticket.setStatus(Ticket.Status.RESOLVED);

            // Save ticket
            ticket = ticketDAO.createTicket(ticket);

            // Send response
            response.setContentType("application/json");
            ObjectNode jsonResponse = objectMapper.createObjectNode()
                    .put("ticketNumber", ticket.getTicketNumber())
                    .put("status", ticket.getStatus().toString())
                    .put("decision", ticket.getDecision().toString());
            objectMapper.writeValue(response.getWriter(), jsonResponse);

        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID format");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating ticket", e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error creating ticket: " + e.getMessage());
        }
    }

    private ObjectNode convertTicketToJson(Ticket ticket, HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" +
                request.getServerName() + ":" +
                request.getServerPort() +
                request.getContextPath();

        ObjectNode ticketJson = objectMapper.createObjectNode()
                .put("id", ticket.getId())
                .put("ticketNumber", ticket.getTicketNumber())
                .put("userId", ticket.getUserId())
                .put("orderId", ticket.getOrderId())
                .put("description", ticket.getDescription())
                .put("imagePath", baseUrl + "/" + ticket.getImagePath())
                .put("status", ticket.getStatus().toString());

        if (ticket.getDecision() != null) {
            ticketJson.put("decision", ticket.getDecision().toString());
        }

        ticketJson.put("createdAt", ticket.getCreatedAt().getTime())
                .put("updatedAt", ticket.getUpdatedAt().getTime());

        return ticketJson;
    }
}