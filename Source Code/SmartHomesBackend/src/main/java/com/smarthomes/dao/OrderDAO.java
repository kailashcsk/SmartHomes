package com.smarthomes.dao;

import com.smarthomes.models.Order;
import com.smarthomes.util.MySQLDataStoreUtilities;
import java.sql.SQLException;
import java.util.List;

public class OrderDAO {
    public void createOrder(Order order) throws SQLException {
        MySQLDataStoreUtilities.createOrder(order);
    }

    public List<Order> getOrdersByUserId(int userId) throws SQLException {
        return MySQLDataStoreUtilities.getOrdersByUserId(userId);
    }

    public Order getOrderByUserIdAndOrderId(int userId, int orderId) throws SQLException {
        return MySQLDataStoreUtilities.getOrderByUserIdAndOrderId(userId, orderId);
    }

    public boolean cancelOrder(int userId, int orderId) throws SQLException {
        return MySQLDataStoreUtilities.cancelOrder(userId, orderId);
    }
}