package com.smarthomes.models;

import java.io.Serializable;
import java.math.BigDecimal;

public class Warranty implements Serializable {
    private int id;
    private int productId;
    private int duration;
    private BigDecimal price;
    private String description;

    // Default constructor
    public Warranty() {
    }

    // Constructor with all fields except id
    public Warranty(int productId, int duration, BigDecimal price, String description) {
        this.productId = productId;
        this.duration = duration;
        this.price = price;
        this.description = description;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Warranty{" +
                "id=" + id +
                ", productId=" + productId +
                ", duration=" + duration +
                ", price=" + price +
                ", description='" + description + '\'' +
                '}';
    }
}