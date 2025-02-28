package com.sample.model;

import java.util.Arrays;

/**
 * Represents an API response containing an embedding or a status message.
 */
public class EmbeddingResponse {
    private Object embedding;  // Can store either an array (for embeddings) or a string message.

    // Default constructor (required for JSON deserialization)
    public EmbeddingResponse() {}

    // Constructor for a response message
    public EmbeddingResponse(String message) {
        this.embedding = message;
    }

    // Constructor for an embedding array response
    public EmbeddingResponse(double[] embedding) {
        this.embedding = embedding;
    }

    // Getter
    public Object getEmbedding() {
        return embedding;
    }

    // Setter
    public void setEmbedding(Object embedding) {
        this.embedding = embedding;
    }

    // String representation (for debugging/logging)
    @Override
    public String toString() {
        return "EmbeddingResponse{" +
                "embedding=" + (embedding instanceof double[] ? Arrays.toString((double[]) embedding) : embedding) +
                '}';
    }
}