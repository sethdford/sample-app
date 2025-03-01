package com.sample.util;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

/**
 * Utility class for building API Gateway response objects.
 */
public class ApiGatewayResponseUtil {
    
    /**
     * Builds a successful response with the given status code and body.
     */
    public APIGatewayProxyResponseEvent buildSuccessResponse(int statusCode, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(body)
                .withIsBase64Encoded(false);
    }
    
    /**
     * Builds an error response with the given status code, error type, and message.
     */
    public APIGatewayProxyResponseEvent buildErrorResponse(int statusCode, String errorType, String message) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        
        String errorBody = String.format("{\"error\":{\"type\":\"%s\",\"message\":\"%s\"}}", 
                                         errorType, 
                                         message.replace("\"", "\\\""));
        
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(errorBody)
                .withIsBase64Encoded(false);
    }
} 