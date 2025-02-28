package com.sample.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores metadata about embeddings, such as source information, encoding details, and performance metrics.
 */
public class EmbeddingMetadata {
    private String sourceType;
    private String modelId;
    private Map<String, Object> encodingDetails;
    private Map<String, Object> performanceMetrics;
    private long timestamp;

    // Default constructor for Jackson
    public EmbeddingMetadata() {
        this.encodingDetails = new HashMap<>();
        this.performanceMetrics = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    public EmbeddingMetadata(String sourceType, String modelId) {
        this();
        this.sourceType = sourceType;
        this.modelId = modelId;
    }

    // Getters and setters
    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Map<String, Object> getEncodingDetails() {
        return encodingDetails;
    }

    public void setEncodingDetails(Map<String, Object> encodingDetails) {
        this.encodingDetails = encodingDetails;
    }

    public void addEncodingDetail(String key, Object value) {
        this.encodingDetails.put(key, value);
    }

    public Map<String, Object> getPerformanceMetrics() {
        return performanceMetrics;
    }

    public void setPerformanceMetrics(Map<String, Object> performanceMetrics) {
        this.performanceMetrics = performanceMetrics;
    }

    public void addPerformanceMetric(String key, Object value) {
        this.performanceMetrics.put(key, value);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 