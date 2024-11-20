// models/Review.java
package com.smarthomes.models;

import java.io.Serializable;
import java.sql.Timestamp;

public class Review implements Serializable {
    private int id;
    private int productId;
    private int userId;
    private int storeId;
    private String reviewText;
    private int rating;
    private Timestamp reviewDate;
    private Timestamp lastModified;
    private String userName; // Optional: for displaying user name with review

    // Default constructor
    public Review() {
    }

    // Constructor with essential fields
    public Review(int productId, int userId, int storeId, String reviewText, int rating) {
        this.productId = productId;
        this.userId = userId;
        this.storeId = storeId;
        this.reviewText = reviewText;
        this.rating = rating;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Timestamp getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Timestamp reviewDate) {
        this.reviewDate = reviewDate;
    }

    public Timestamp getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", productId=" + productId +
                ", userId=" + userId +
                ", storeId=" + storeId +
                ", reviewText='" + reviewText + '\'' +
                ", rating=" + rating +
                ", reviewDate=" + reviewDate +
                ", lastModified=" + lastModified +
                ", userName='" + userName + '\'' +
                '}';
    }
}