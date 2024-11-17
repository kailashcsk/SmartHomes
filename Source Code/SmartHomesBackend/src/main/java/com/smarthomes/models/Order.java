package com.smarthomes.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {
    private int id;
    private int userId;
    private int storeId;
    private Date orderDate;
    private Date shipDate;
    private BigDecimal totalAmount;
    private BigDecimal shippingCost;
    private BigDecimal discountAmount;
    private BigDecimal finalSaleAmount;
    private String status;
    private String deliveryMethod;
    private String shippingAddress;
    private String creditCardNumber;
    private List<OrderItem> orderItems;

    // Constructors
    public Order() {
    }

    public Order(int userId, int storeId, Date orderDate, Date shipDate, BigDecimal totalAmount,
            BigDecimal shippingCost, BigDecimal discountAmount, BigDecimal finalSaleAmount,
            String status, String deliveryMethod, String shippingAddress, String creditCardNumber,
            List<OrderItem> orderItems) {
        this.userId = userId;
        this.storeId = storeId;
        this.orderDate = orderDate;
        this.shipDate = shipDate;
        this.totalAmount = totalAmount;
        this.shippingCost = shippingCost;
        this.discountAmount = discountAmount;
        this.finalSaleAmount = finalSaleAmount;
        this.status = status;
        this.deliveryMethod = deliveryMethod;
        this.shippingAddress = shippingAddress;
        this.creditCardNumber = creditCardNumber;
        this.orderItems = orderItems;
    }

    // Getters and setters for all fields
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getShipDate() {
        return shipDate;
    }

    public void setShipDate(Date shipDate) {
        this.shipDate = shipDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getFinalSaleAmount() {
        return finalSaleAmount;
    }

    public void setFinalSaleAmount(BigDecimal finalSaleAmount) {
        this.finalSaleAmount = finalSaleAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    // toString method
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", storeId=" + storeId +
                ", orderDate=" + orderDate +
                ", shipDate=" + shipDate +
                ", totalAmount=" + totalAmount +
                ", shippingCost=" + shippingCost +
                ", discountAmount=" + discountAmount +
                ", finalSaleAmount=" + finalSaleAmount +
                ", status='" + status + '\'' +
                ", deliveryMethod='" + deliveryMethod + '\'' +
                ", shippingAddress='" + shippingAddress + '\'' +
                ", creditCardNumber='" + creditCardNumber + '\'' +
                ", orderItems=" + orderItems +
                '}';
    }

    public static class OrderItem implements Serializable {
        private int id;
        private int orderId;
        private int productId;
        private String category;
        private int quantity;
        private BigDecimal price;

        // Constructors
        public OrderItem() {
        }

        public OrderItem(int orderId, int productId, String category, int quantity, BigDecimal price) {
            this.orderId = orderId;
            this.productId = productId;
            this.category = category;
            this.quantity = quantity;
            this.price = price;
        }

        // Getters and setters for all fields
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getOrderId() {
            return orderId;
        }

        public void setOrderId(int orderId) {
            this.orderId = orderId;
        }

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        // toString method
        @Override
        public String toString() {
            return "OrderItem{" +
                    "id=" + id +
                    ", orderId=" + orderId +
                    ", productId=" + productId +
                    ", category='" + category + '\'' +
                    ", quantity=" + quantity +
                    ", price=" + price +
                    '}';
        }
    }
}