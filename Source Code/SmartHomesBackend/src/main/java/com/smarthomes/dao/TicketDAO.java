package com.smarthomes.dao;

import com.smarthomes.models.Ticket;
import com.smarthomes.util.DatabaseConnection;
import com.smarthomes.util.MySQLDataStoreUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicketDAO {
    private static final Logger LOGGER = Logger.getLogger(TicketDAO.class.getName());

    public Ticket createTicket(Ticket ticket) throws SQLException {
        // Validate if the order is delivered before creating ticket
        if (ticket.getOrderId() != null && !isValidOrderForTicket(ticket.getUserId(), ticket.getOrderId())) {
            throw new SQLException("Invalid order ID or order is not in delivered status");
        }
        return MySQLDataStoreUtilities.createTicket(ticket);
    }

    public Ticket getTicketByNumber(String ticketNumber) throws SQLException {
        return MySQLDataStoreUtilities.getTicketByNumber(ticketNumber);
    }

    public void updateTicketDecision(String ticketNumber, Ticket.Decision decision) throws SQLException {
        MySQLDataStoreUtilities.updateTicketDecision(ticketNumber, decision);
    }

    public List<Ticket> getUserTickets(int userId) throws SQLException {
        return MySQLDataStoreUtilities.getUserTickets(userId);
    }

    public boolean isValidTicket(String ticketNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tickets WHERE ticket_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ticketNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking ticket validity: " + e.getMessage(), e);
            throw e;
        }
        return false;
    }

    public boolean isValidOrderForTicket(int userId, int orderId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders " +
                    "WHERE id = ? AND user_id = ? AND status = 'DELIVERED'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking order validity: " + e.getMessage(), e);
            throw e;
        }
        return false;
    }

    public List<DeliveredOrder> getUserDeliveredOrders(int userId) throws SQLException {
        List<DeliveredOrder> orders = new ArrayList<>();
        String sql = "SELECT o.id, o.order_date, GROUP_CONCAT(p.name) as products, " +
                    "GROUP_CONCAT(oi.quantity) as quantities " +
                    "FROM orders o " +
                    "JOIN order_items oi ON o.id = oi.order_id " +
                    "JOIN products p ON oi.product_id = p.id " +
                    "WHERE o.user_id = ? AND o.status = 'DELIVERED' " +
                    "GROUP BY o.id, o.order_date " +
                    "ORDER BY o.order_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DeliveredOrder order = new DeliveredOrder();
                    order.setOrderId(rs.getInt("id"));
                    order.setOrderDate(rs.getTimestamp("order_date"));
                    
                    // Parse concatenated products and quantities
                    String[] products = rs.getString("products").split(",");
                    String[] quantities = rs.getString("quantities").split(",");
                    List<OrderItem> items = new ArrayList<>();
                    
                    for (int i = 0; i < products.length; i++) {
                        OrderItem item = new OrderItem();
                        item.setProductName(products[i]);
                        item.setQuantity(Integer.parseInt(quantities[i]));
                        items.add(item);
                    }
                    order.setItems(items);
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    // Inner classes for delivered orders
    public static class DeliveredOrder {
        private int orderId;
        private Timestamp orderDate;
        private List<OrderItem> items;

        // Getters and setters
        public int getOrderId() { return orderId; }
        public void setOrderId(int orderId) { this.orderId = orderId; }
        public Timestamp getOrderDate() { return orderDate; }
        public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }
        public List<OrderItem> getItems() { return items; }
        public void setItems(List<OrderItem> items) { this.items = items; }
    }

    public static class OrderItem {
        private String productName;
        private int quantity;

        // Getters and setters
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}