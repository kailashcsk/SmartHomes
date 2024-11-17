package com.smarthomes.dao;

import com.smarthomes.models.Accessory;
import com.smarthomes.util.MySQLDataStoreUtilities;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccessoryDAO {
    private static final Logger LOGGER = Logger.getLogger(AccessoryDAO.class.getName());

    public List<Accessory> getAllAccessories() throws SQLException {
        return MySQLDataStoreUtilities.getAllAccessories();
    }

    public Accessory getAccessoryById(int id) throws SQLException {
        return MySQLDataStoreUtilities.getAccessoryById(id);
    }

    public void createAccessory(Accessory accessory) throws SQLException {
        MySQLDataStoreUtilities.createAccessory(accessory);
    }

    public void updateAccessory(Accessory accessory) throws SQLException {
        MySQLDataStoreUtilities.updateAccessory(accessory);
    }

    public void deleteAccessory(int id) throws SQLException {
        MySQLDataStoreUtilities.deleteAccessory(id);
    }

    public boolean accessoryExists(int accessoryId) throws SQLException {
        return MySQLDataStoreUtilities.accessoryExists(accessoryId);
    }
}