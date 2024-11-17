package com.smarthomes.dao;

import com.smarthomes.models.Store;
import com.smarthomes.util.MySQLDataStoreUtilities;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StoreDAO {
    private static final Logger LOGGER = Logger.getLogger(StoreDAO.class.getName());

    public List<Store> getAllStores() throws SQLException {
        return MySQLDataStoreUtilities.getAllStores();
    }

    public Store getStoreById(int id) throws SQLException {
        return MySQLDataStoreUtilities.getStoreById(id);
    }

    public void createStore(Store store) throws SQLException {
        MySQLDataStoreUtilities.createStore(store);
    }

    public void updateStore(Store store) throws SQLException {
        MySQLDataStoreUtilities.updateStore(store);
    }

    public void deleteStore(int id) throws SQLException {
        MySQLDataStoreUtilities.deleteStore(id);
    }

    public boolean storeExists(int storeId) throws SQLException {
        return MySQLDataStoreUtilities.storeExists(storeId);
    }
}