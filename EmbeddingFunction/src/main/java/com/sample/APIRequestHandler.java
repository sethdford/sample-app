package com.sample;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.sample.model.EmbeddingRequest;
import com.sample.model.EmbeddingResponse;
import com.sample.model.UserAttributesRequest;
import com.sample.service.DynamoDBService;
import com.sample.service.EmbeddingService;
import com.sample.util.JsonUtils;

/**
 * Handles API request routing and business logic for embedding operations.
 */
public class APIRequestHandler {

    private final DynamoDBService dynamoDBService = new DynamoDBService();
    private final EmbeddingService embeddingService = new EmbeddingService();

    /**
     * Routes API requests to the correct handler.
     */
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String path = input.getPath();
        context.getLogger().log("Processing request: " + path);

        Map<String, String> headers = Map.of("Content-Type", "application/json");

        switch (path) {
            case "/generateEmbedding":
                return generateUserEmbedding(input, context);
            case "/getEmbedding":
                return getUserEmbedding(input, context);
            case "/generateEmbeddingFromAttributes":
                return generateEmbeddingFromAttributes(input, context);
            default:
                return JsonUtils.createResponse(400, "Invalid request path");
        }
    }

    /**
     * Generates an embedding using AWS Bedrock and stores it in DynamoDB.
     */
    private APIGatewayProxyResponseEvent generateUserEmbedding(APIGatewayProxyRequestEvent input, Context context) {
        try {
            EmbeddingRequest request = JsonUtils.fromJson(input.getBody(), EmbeddingRequest.class);

            // ✅ Input Validation
            if (request.getUserId() == null || request.getText() == null || request.getText().isEmpty()) {
                return JsonUtils.createResponse(400, "Missing required fields: user_id or text");
            }

            context.getLogger().log("Generating embedding for user: " + request.getUserId());

            // ✅ AWS Bedrock Call
            double[] embedding;
            try {
                embedding = embeddingService.generateEmbedding(request.getText());
            } catch (Exception e) {
                context.getLogger().log("Error calling AWS Bedrock: " + e.getMessage());
                return JsonUtils.createResponse(500, "Failed to generate embedding from AWS Bedrock.");
            }

            // ✅ Store in DynamoDB
            dynamoDBService.storeEmbedding(request.getUserId(), embedding);
            return JsonUtils.createResponse(200, new EmbeddingResponse("Embedding stored successfully."));

        } catch (Exception e) {
            context.getLogger().log("Error in generateUserEmbedding: " + e.getMessage());
            return JsonUtils.createResponse(500, "Error generating embedding.");
        }
    }

    /**
     * Retrieves a stored embedding from DynamoDB.
     */
    private APIGatewayProxyResponseEvent getUserEmbedding(APIGatewayProxyRequestEvent input, Context context) {
        try {
            EmbeddingRequest request = JsonUtils.fromJson(input.getBody(), EmbeddingRequest.class);

            // ✅ Input Validation
            if (request.getUserId() == null || request.getUserId().isEmpty()) {
                return JsonUtils.createResponse(400, "Missing required field: user_id");
            }

            context.getLogger().log("Fetching embedding for user: " + request.getUserId());

            // ✅ Retrieve from DynamoDB
            double[] embedding = dynamoDBService.getEmbedding(request.getUserId());

            if (embedding == null) {
                return JsonUtils.createResponse(404, "Embedding not found");
            }

            return JsonUtils.createResponse(200, new EmbeddingResponse(embedding));

        } catch (Exception e) {
            context.getLogger().log("Error in getUserEmbedding: " + e.getMessage());
            return JsonUtils.createResponse(500, "Error retrieving embedding.");
        }
    }
    
    /**
     * Generates an embedding from user attributes using AWS Bedrock and stores it in DynamoDB.
     */
    private APIGatewayProxyResponseEvent generateEmbeddingFromAttributes(APIGatewayProxyRequestEvent input, Context context) {
        try {
            UserAttributesRequest request = JsonUtils.fromJson(input.getBody(), UserAttributesRequest.class);

            // ✅ Input Validation
            if (request.getUserId() == null || request.getUserId().isEmpty()) {
                return JsonUtils.createResponse(400, "Missing required field: user_id");
            }
            
            if (request.getAttributes() == null || request.getAttributes().isEmpty()) {
                return JsonUtils.createResponse(400, "Missing required field: attributes");
            }

            context.getLogger().log("Generating embedding from attributes for user: " + request.getUserId());

            // ✅ AWS Bedrock Call
            double[] embedding;
            try {
                embedding = embeddingService.generateEmbeddingFromAttributes(request.getAttributes());
            } catch (Exception e) {
                context.getLogger().log("Error calling AWS Bedrock: " + e.getMessage());
                return JsonUtils.createResponse(500, "Failed to generate embedding from AWS Bedrock.");
            }

            // ✅ Store in DynamoDB
            dynamoDBService.storeEmbedding(request.getUserId(), embedding);
            return JsonUtils.createResponse(200, new EmbeddingResponse("Embedding from attributes stored successfully."));

        } catch (Exception e) {
            context.getLogger().log("Error in generateEmbeddingFromAttributes: " + e.getMessage());
            return JsonUtils.createResponse(500, "Error generating embedding from attributes.");
        }
    }
}