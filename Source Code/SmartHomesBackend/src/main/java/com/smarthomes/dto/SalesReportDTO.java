package com.smarthomes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalesReportDTO {
    public static class ProductSalesSummary {
        private int productId;
        private String productName;
        private BigDecimal productPrice;
        private int itemsSold;
        private BigDecimal totalSales;

        // Getters and setters
        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public BigDecimal getProductPrice() {
            return productPrice;
        }

        public void setProductPrice(BigDecimal productPrice) {
            this.productPrice = productPrice;
        }

        public int getItemsSold() {
            return itemsSold;
        }

        public void setItemsSold(int itemsSold) {
            this.itemsSold = itemsSold;
        }

        public BigDecimal getTotalSales() {
            return totalSales;
        }

        public void setTotalSales(BigDecimal totalSales) {
            this.totalSales = totalSales;
        }
    }

    public static class DailySalesTransaction {
        private String saleDate;
        private int productId;
        private String productName;
        private int itemsSold;
        private BigDecimal totalSales;

        // Getters and setters
        public String getSaleDate() {
            return saleDate;
        }

        public void setSaleDate(String saleDate) {
            this.saleDate = saleDate;
        }

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getItemsSold() {
            return itemsSold;
        }

        public void setItemsSold(int itemsSold) {
            this.itemsSold = itemsSold;
        }

        public BigDecimal getTotalSales() {
            return totalSales;
        }

        public void setTotalSales(BigDecimal totalSales) {
            this.totalSales = totalSales;
        }
    }
}