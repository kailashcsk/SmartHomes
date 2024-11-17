package com.smarthomes.dao;

import com.smarthomes.models.Order;
import com.smarthomes.util.MySQLDataStoreUtilities;
import java.sql.SQLException;
import java.util.List;

public class SalesmanOrderDAO {

    public void createOrder(Order order) throws SQLException {
        MySQLDataStoreUtilities.createOrder(order);
    }
    
    public List<Order> getAllOrders() throws SQLException {
        return MySQLDataStoreUtilities.getAllOrders();
    }

    public Order getOrderById(int orderId) throws SQLException {
        return MySQLDataStoreUtilities.getOrderById(orderId);
    }

    public void updateOrder(Order order) throws SQLException {
        MySQLDataStoreUtilities.updateOrder(order);
    }

    public void deleteOrder(int orderId) throws SQLException {
        MySQLDataStoreUtilities.deleteOrder(orderId);
    }
}