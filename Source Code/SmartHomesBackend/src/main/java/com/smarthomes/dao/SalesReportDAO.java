package com.smarthomes.dao;

import com.smarthomes.dto.SalesReportDTO;
import com.smarthomes.util.MySQLDataStoreUtilities;

import java.sql.SQLException;
import java.util.List;

public class SalesReportDAO {

    public List<SalesReportDTO.ProductSalesSummary> getProductSalesSummary() throws SQLException {
        return MySQLDataStoreUtilities.getProductSalesSummary();
    }

    public List<SalesReportDTO.DailySalesTransaction> getDailySalesTransactions() throws SQLException {
        return MySQLDataStoreUtilities.getDailySalesTransactions();
    }
}