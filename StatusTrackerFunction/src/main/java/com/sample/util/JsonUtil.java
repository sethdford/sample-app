package com.sample.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for JSON serialization and deserialization.
 */
public class JsonUtil {
    
    private final ObjectMapper objectMapper;
    
    /**
     * Default constructor that initializes the ObjectMapper.
     */
    public JsonUtil() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Converts an object to a JSON string.
     */
    public String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
    
    /**
     * Converts a JSON string to an object of the specified class.
     */
    public <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }
} 