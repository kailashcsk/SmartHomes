package com.smarthomes.dto;

public class InventoryDTO {
    private int id;
    private int inventoryCount;

    // Constructors
    public InventoryDTO() {
    }

    public InventoryDTO(int id, int inventoryCount) {
        this.id = id;
        this.inventoryCount = inventoryCount;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInventoryCount() {
        return inventoryCount;
    }

    public void setInventoryCount(int inventoryCount) {
        this.inventoryCount = inventoryCount;
    }
}