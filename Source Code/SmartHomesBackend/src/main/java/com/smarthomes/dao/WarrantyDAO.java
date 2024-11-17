package com.smarthomes.dao;

import com.smarthomes.models.Warranty;
import com.smarthomes.util.MySQLDataStoreUtilities;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WarrantyDAO {
    private static final Logger LOGGER = Logger.getLogger(WarrantyDAO.class.getName());

    public List<Warranty> getAllWarranties() throws SQLException {
        return MySQLDataStoreUtilities.getAllWarranties();
    }

    public Warranty getWarrantyById(int id) throws SQLException {
        return MySQLDataStoreUtilities.getWarrantyById(id);
    }

    public void createWarranty(Warranty warranty) throws SQLException {
        MySQLDataStoreUtilities.createWarranty(warranty);
    }

    public void updateWarranty(Warranty warranty) throws SQLException {
        MySQLDataStoreUtilities.updateWarranty(warranty);
    }

    public void deleteWarranty(int id) throws SQLException {
        MySQLDataStoreUtilities.deleteWarranty(id);
    }

    public boolean warrantyExists(int warrantyId) throws SQLException {
        return MySQLDataStoreUtilities.warrantyExists(warrantyId);
    }
}