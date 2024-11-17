package com.smarthomes.models;

import java.io.Serializable;

public class Category implements Serializable {
    private int id;
    private String name;

    // Default constructor
    public Category() {
    }

    // Constructor with name
    public Category(String name) {
        this.name = name;
    }

    // Constructor with all fields
    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}