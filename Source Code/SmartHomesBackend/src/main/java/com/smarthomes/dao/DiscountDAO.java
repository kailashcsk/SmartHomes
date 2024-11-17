package com.smarthomes.dao;

import com.smarthomes.models.Discount;
import com.smarthomes.util.MySQLDataStoreUtilities;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscountDAO {
    private static final Logger LOGGER = Logger.getLogger(DiscountDAO.class.getName());

    public List<Discount> getAllDiscounts() throws SQLException {
        return MySQLDataStoreUtilities.getAllDiscounts();
    }

    public Discount getDiscountById(int id) throws SQLException {
        return MySQLDataStoreUtilities.getDiscountById(id);
    }

    public void createDiscount(Discount discount) throws SQLException {
        MySQLDataStoreUtilities.createDiscount(discount);
    }

    public void updateDiscount(Discount discount) throws SQLException {
        MySQLDataStoreUtilities.updateDiscount(discount);
    }

    public void deleteDiscount(int id) throws SQLException {
        MySQLDataStoreUtilities.deleteDiscount(id);
    }

    public boolean discountExists(int discountId) throws SQLException {
        return MySQLDataStoreUtilities.discountExists(discountId);
    }
}