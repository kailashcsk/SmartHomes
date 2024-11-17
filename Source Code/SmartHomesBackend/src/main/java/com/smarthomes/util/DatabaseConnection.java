package com.smarthomes.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DatabaseConfig.getConnection();
            LOGGER.info("Database connection obtained successfully");
            return conn;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to obtain database connection", e);
            throw e;
        }
    }
}