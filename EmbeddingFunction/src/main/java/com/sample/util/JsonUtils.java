package com.sample.util;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for handling JSON serialization and deserialization.
 */
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts a Java object into a JSON string.
     *
     * @param obj The object to serialize
     * @return JSON string representation
     * @throws Exception if serialization fails
     */
    public static String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * Converts a JSON string into a Java object.
     *
     * @param json  JSON string
     * @param clazz Target class type
     * @param <T>   Type parameter
     * @return Deserialized Java object
     * @throws Exception if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * Creates an API Gateway response with a given status code and message.
     *
     * @param statusCode HTTP status code
     * @param body       Response body
     * @return API Gateway response
     */
    public static APIGatewayProxyResponseEvent createResponse(int statusCode, Object body) {
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody(toJson(body));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"error\": \"Failed to serialize response\"}");
        }
    }

    /**
     * Convert a Map to a JSON string
     * 
     * @param map The map to convert
     * @return JSON string representation
     */
    public static String toJson(Map<String, Object> map) {
        if (map == null) {
            return "null";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            
            sb.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Number) {
                sb.append(value);
            } else if (value instanceof Boolean) {
                sb.append(value);
            } else if (value instanceof Map) {
                sb.append(toJson((Map<String, Object>) value));
            } else if (value instanceof Map[]) {
                sb.append("[");
                Map[] maps = (Map[]) value;
                for (int i = 0; i < maps.length; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(toJson((Map<String, Object>) maps[i]));
                }
                sb.append("]");
            } else if (value instanceof String[]) {
                sb.append("[");
                String[] strings = (String[]) value;
                for (int i = 0; i < strings.length; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append("\"").append(strings[i]).append("\"");
                }
                sb.append("]");
            } else if (value instanceof double[]) {
                sb.append("[");
                double[] doubles = (double[]) value;
                for (int i = 0; i < doubles.length; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(doubles[i]);
                }
                sb.append("]");
            } else if (value == null) {
                sb.append("null");
            } else {
                sb.append("\"").append(value.toString()).append("\"");
            }
        }
        
        sb.append("}");
        return sb.toString();
    }
}