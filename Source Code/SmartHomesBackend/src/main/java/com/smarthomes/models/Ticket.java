package com.smarthomes.models;

import java.io.Serializable;
import java.sql.Timestamp;

public class Ticket implements Serializable {
    private int id;
    private String ticketNumber;
    private int userId;
    private Integer orderId;  // Changed to Integer to allow null
    private String description;
    private String imagePath;
    private Status status;
    private Decision decision;
    private Timestamp createdAt;
    private Timestamp updatedAt;


    public enum Status {
        PENDING, IN_REVIEW, RESOLVED
    }
    
    public enum Decision {
        REFUND_ORDER, REPLACE_ORDER, ESCALATE_TO_HUMAN
    }

    // Default constructor
    public Ticket() {
        this.status = Status.PENDING;
    }

    // Constructor with essential fields
    public Ticket(int userId, String description, String imagePath) {
        this.userId = userId;
        this.description = description;
        this.imagePath = imagePath;
        this.status = Status.PENDING;
        this.ticketNumber = generateTicketNumber();
    }

    private String generateTicketNumber() {
        // Format: CST-YYYYMMDD-XXXXX (where X is random alphanumeric)
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        String randomStr = java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return String.format("CST-%s-%s", dateStr, randomStr);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Updated getter and setter for orderId
    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", ticketNumber='" + ticketNumber + '\'' +
                ", userId=" + userId +
                ", orderId=" + orderId +
                ", description='" + description + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", status=" + status +
                ", decision=" + decision +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}