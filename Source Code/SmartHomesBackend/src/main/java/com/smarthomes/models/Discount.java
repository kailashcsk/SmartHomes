package com.smarthomes.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Discount implements Serializable {
    private int id;
    private int productId;
    private String discountType;
    private BigDecimal amount;
    private Date startDate;
    private Date endDate;

    // Default constructor
    public Discount() {
    }

    // Constructor with all fields except id
    public Discount(int productId, String discountType, BigDecimal amount, Date startDate, Date endDate) {
        this.productId = productId;
        this.discountType = discountType;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "Discount{" +
                "id=" + id +
                ", productId=" + productId +
                ", discountType='" + discountType + '\'' +
                ", amount=" + amount +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}