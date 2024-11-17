package com.smarthomes.dao;

import com.smarthomes.models.Product;
import com.smarthomes.util.MySQLDataStoreUtilities;
import com.smarthomes.util.MongoDBDataStoreUtilities;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AnalyticsDAO {

    public List<Product> getTopLikedProducts(int limit) throws SQLException {
        return MySQLDataStoreUtilities.getTopLikedProducts(limit);
    }

    public List<Map<String, Object>> getTopSellingZipCodes(int limit) throws SQLException {
        return MySQLDataStoreUtilities.getTopSellingZipCodes(limit);
    }

    public List<Product> getBestSellingProducts(int limit) throws SQLException {
        return MySQLDataStoreUtilities.getBestSellingProducts(limit);
    }
}