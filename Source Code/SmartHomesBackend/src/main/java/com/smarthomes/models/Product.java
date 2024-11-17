package com.smarthomes.models;

import java.io.Serializable;
import java.math.BigDecimal;

public class Product implements Serializable {
    private int id;
    private int categoryId;
    private String name;
    private BigDecimal price;
    private String description;
    private String manufacturerName;
    private double averageRating;

    // Default constructor
    public Product() {
    }

    // Constructor with all fields except id
    public Product(int categoryId, String name, BigDecimal price, String description, String manufacturerName) {
        this.categoryId = categoryId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.manufacturerName = manufacturerName;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public double getAverageRating() {
        return averageRating;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", manufacturerName='" + manufacturerName + '\'' +
                '}';
    }
}