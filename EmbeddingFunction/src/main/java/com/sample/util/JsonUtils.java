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
}