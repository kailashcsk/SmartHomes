package com.smarthomes.dao;

import com.smarthomes.dto.InventoryDTO;
import com.smarthomes.dto.InventoryReportDTO;
import com.smarthomes.util.MySQLDataStoreUtilities;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class InventoryReportDAO {

     public List<InventoryReportDTO> getAllProductsWithInventory() throws SQLException {
        return MySQLDataStoreUtilities.getAllProductsWithInventory();
    }

    public List<Map<String, Object>> getProductsOnSale() throws SQLException {
        return MySQLDataStoreUtilities.getProductsOnSale();
    }

    public List<Map<String, Object>> getProductsWithRebates() throws SQLException {
        return MySQLDataStoreUtilities.getProductsWithRebates();
    }

    public List<Map<String, Object>> getProductInventoryCounts() throws SQLException {
        return MySQLDataStoreUtilities.getProductInventoryCounts();
    }

    public InventoryDTO getInventoryById(int id) throws SQLException {
        return MySQLDataStoreUtilities.getInventoryById(id);
    }

    public void addInventory(InventoryDTO inventoryItem) throws SQLException {
        MySQLDataStoreUtilities.addInventory(inventoryItem);
    }

    public void updateInventory(InventoryDTO inventoryItem) throws SQLException {
        MySQLDataStoreUtilities.updateInventory(inventoryItem);
    }

    public void deleteInventory(int id) throws SQLException {
        MySQLDataStoreUtilities.deleteInventory(id);
    }
}