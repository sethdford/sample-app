package com.sample.model;

/**
 * Represents an API request for generating or retrieving embeddings.
 */
public class EmbeddingRequest {
    private String userId;
    private String text;

    // Default constructor (required for JSON deserialization)
    public EmbeddingRequest() {}

    // Constructor
    public EmbeddingRequest(String userId, String text) {
        this.userId = userId;
        this.text = text;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    // String representation (for debugging/logging)
    @Override
    public String toString() {
        return "EmbeddingRequest{" +
                "userId='" + userId + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}