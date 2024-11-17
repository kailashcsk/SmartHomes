// package com.smarthomes.servlets;

// import com.smarthomes.models.User;
// import com.smarthomes.util.JwtUtil;
// import java.io.File;
// import java.io.FileInputStream;
// import java.io.IOException;
// import java.io.OutputStream;
// import javax.servlet.ServletException;
// import javax.servlet.annotation.WebServlet;
// import javax.servlet.http.HttpServlet;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.util.logging.Logger;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.node.ObjectNode;

// //@WebServlet("/api/images/*")
// public class ImageServlet extends HttpServlet {
//     private static final Logger LOGGER = Logger.getLogger(ImageServlet.class.getName());
//     private static final String UPLOAD_DIRECTORY = "customer_service_images";
//     private ObjectMapper objectMapper;

//     @Override
//     public void init() throws ServletException {
//         objectMapper = new ObjectMapper();
//     }

//     @Override
//     protected void doGet(HttpServletRequest request, HttpServletResponse response)
//             throws ServletException, IOException {

//         // Authentication check
//         String token = request.getHeader("Authorization");
//         if (token == null || !token.startsWith("Bearer ")) {
//             sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing token");
//             return;
//         }

//         // Verify token
//         token = token.substring(7); // Remove "Bearer " prefix
//         User user = JwtUtil.verifyToken(token);
//         if (user == null) {
//             sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
//             return;
//         }

//         String pathInfo = request.getPathInfo();
//         if (pathInfo == null || pathInfo.equals("/")) {
//             sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Image name is required");
//             return;
//         }

//         // Get the file name from the path
//         String fileName = pathInfo.substring(1);

//         // Construct the full file path
//         String applicationPath = System.getProperty("catalina.home");
//         String fullPath = applicationPath + File.separator + UPLOAD_DIRECTORY + File.separator + fileName;
//         File imageFile = new File(fullPath);

//         if (!imageFile.exists()) {
//             sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Image not found");
//             return;
//         }

//         // Set content type
//         String contentType = getServletContext().getMimeType(fileName);
//         if (contentType == null) {
//             contentType = "application/octet-stream";
//         }
//         response.setContentType(contentType);
//         response.setContentLength((int) imageFile.length());

//         // Copy the file content to response output stream
//         try (FileInputStream in = new FileInputStream(imageFile);
//                 OutputStream out = response.getOutputStream()) {
//             byte[] buffer = new byte[4096];
//             int bytesRead;
//             while ((bytesRead = in.read(buffer)) != -1) {
//                 out.write(buffer, 0, bytesRead);
//             }
//         }
//     }

//     private void sendJsonError(HttpServletResponse response, int status, String message)
//             throws IOException {
//         response.setStatus(status);
//         response.setContentType("application/json");
//         ObjectNode jsonResponse = objectMapper.createObjectNode()
//                 .put("error", message);
//         objectMapper.writeValue(response.getWriter(), jsonResponse);
//     }
// }