package com.smarthomes.dao;

import com.smarthomes.models.User;
import com.smarthomes.util.MySQLDataStoreUtilities;

import java.sql.SQLException;
import java.util.List;

public class UserDAO {

    public void createUser(User user) throws SQLException {
        MySQLDataStoreUtilities.createUser(user);
    }

    public User getUserByEmail(String email) throws SQLException {
        return MySQLDataStoreUtilities.getUserByEmail(email);
    }

    public User getUserById(int id) throws SQLException {
        return MySQLDataStoreUtilities.getUserById(id);
    }

    public void updateUser(User user) throws SQLException {
        MySQLDataStoreUtilities.updateUser(user);
    }

    public void deleteUser(int id) throws SQLException {
        MySQLDataStoreUtilities.deleteUser(id);
    }

    public List<User> getAllUsers() throws SQLException {
        return MySQLDataStoreUtilities.getAllUsers();
    }

    public boolean userExists(String email) throws SQLException {
        return MySQLDataStoreUtilities.userExists(email);
    }

    public User authenticate(String email, String password) throws SQLException {
        return MySQLDataStoreUtilities.authenticate(email, password);
    }
}