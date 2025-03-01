package com.sample.model;

/**
 * Represents the result of generating an embedding
 */
public class EmbeddingResult {
    private String embeddingId;
    private double[] embeddingVector;
    private String sourceType;
    private long timestamp;
    private int dimensions;
    
    public EmbeddingResult() {
    }
    
    public EmbeddingResult(String embeddingId, double[] embeddingVector, String sourceType) {
        this.embeddingId = embeddingId;
        this.embeddingVector = embeddingVector;
        this.sourceType = sourceType;
        this.timestamp = System.currentTimeMillis();
        this.dimensions = embeddingVector != null ? embeddingVector.length : 0;
    }
    
    public String getEmbeddingId() {
        return embeddingId;
    }
    
    public void setEmbeddingId(String embeddingId) {
        this.embeddingId = embeddingId;
    }
    
    public double[] getEmbeddingVector() {
        return embeddingVector;
    }
    
    public void setEmbeddingVector(double[] embeddingVector) {
        this.embeddingVector = embeddingVector;
        this.dimensions = embeddingVector != null ? embeddingVector.length : 0;
    }
    
    public String getSourceType() {
        return sourceType;
    }
    
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getDimensions() {
        return dimensions;
    }
} 