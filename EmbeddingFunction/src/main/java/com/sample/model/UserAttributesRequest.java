package com.sample.model;

import java.util.Map;

/**
 * Request model for generating embeddings from user attributes.
 */
public class UserAttributesRequest {
    private String userId;
    private Map<String, Object> attributes;

    // Default constructor for Jackson
    public UserAttributesRequest() {
    }

    public UserAttributesRequest(String userId, Map<String, Object> attributes) {
        this.userId = userId;
        this.attributes = attributes;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
} 