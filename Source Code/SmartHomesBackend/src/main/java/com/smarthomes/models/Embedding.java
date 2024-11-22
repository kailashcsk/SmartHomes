package com.smarthomes.models;

import java.util.List;

public class Embedding {
    private String id; // Used as reference in Elasticsearch
    private String type; // "product" or "review"
    private int referenceId; // product_id or review_id
    private List<Float> vector;
    private String content; // The text that was embedded

    public Embedding() {
    }

    public Embedding(String type, int referenceId, List<Float> vector, String content) {
        this.type = type;
        this.referenceId = referenceId;
        this.vector = vector;
        this.content = content;
        this.id = type + "-" + referenceId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
    }

    public List<Float> getVector() {
        return vector;
    }

    public void setVector(List<Float> vector) {
        this.vector = vector;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}