package com.sample.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sample.model.EmbeddingMetadata;
import com.sample.util.JsonUtils;

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.dax.ClusterDaxAsyncClient;
import software.amazon.dax.Configuration;

/**
 * Enhanced DynamoDB service that supports storing different types of embeddings with metadata.
 */
public class EnhancedDynamoDBService {
    private DynamoDbAsyncClient daxClient = null;
    private final DynamoDbClient dynamoDb;
    private static final String TABLE_NAME = "UserEmbeddings";
    private static final String DAX_ENDPOINT = System.getenv("DAX_ENDPOINT");

    // Embedding types
    public static final String EMBEDDING_TYPE_RAW_TEXT = "raw_text";
    public static final String EMBEDDING_TYPE_USER_ATTRIBUTES = "user_attributes";
    public static final String EMBEDDING_TYPE_INTERESTS = "interests";
    public static final String EMBEDDING_TYPE_BEHAVIOR = "behavior";
    public static final String EMBEDDING_TYPE_FINANCIAL_PROFILE = "financial_profile";
    public static final String EMBEDDING_TYPE_CLOUDWATCH_LOGS = "cloudwatch_logs";
    public static final String EMBEDDING_TYPE_CLIENT_EFFORT = "client_effort";

    public EnhancedDynamoDBService() {
        // Initialize DAX client (for caching) and DynamoDB client (for permanent storage)
        this.dynamoDb = DynamoDbClient.create();
       
        try {
            if (DAX_ENDPOINT != null && !DAX_ENDPOINT.isEmpty()) {
                daxClient = ClusterDaxAsyncClient.builder()
                        .overrideConfiguration(Configuration.builder()
                                .url(DAX_ENDPOINT)
                                .build())
                        .build();
            }
        } catch (IOException e) {
            System.err.println("Error initializing DAX client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stores a user embedding with metadata in DynamoDB.
     * 
     * @param userId User ID
     * @param embeddingType Type of embedding (raw_text, user_attributes, etc.)
     * @param embedding The embedding vector
     * @param metadata Additional metadata about the embedding
     */
    public void storeEmbeddingWithMetadata(String userId, String embeddingType, double[] embedding, 
                                          EmbeddingMetadata metadata) throws Exception {
        String embeddingJson = JsonUtils.toJson(embedding);
        String metadataJson = JsonUtils.toJson(metadata);

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("user_id", AttributeValue.builder().s(userId).build());
        item.put("embedding_type", AttributeValue.builder().s(embeddingType).build());
        item.put("embedding", AttributeValue.builder().s(embeddingJson).build());
        item.put("metadata", AttributeValue.builder().s(metadataJson).build());
        item.put("created_at", AttributeValue.builder().s(String.valueOf(System.currentTimeMillis())).build());

        // Store in DynamoDB (permanent storage)
        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build());

        // Store in DAX (cache) if available
        if (daxClient != null) {
            try {
                daxClient.putItem(PutItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .item(item)
                        .build());
            } catch (Exception e) {
                System.err.println("Error storing in DAX: " + e.getMessage());
            }
        }
    }

    /**
     * Retrieves a specific embedding type for a user.
     * 
     * @param userId User ID
     * @param embeddingType Type of embedding to retrieve
     * @return The embedding vector or null if not found
     */
    public double[] getEmbedding(String userId, String embeddingType) throws Exception {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("user_id", AttributeValue.builder().s(userId).build());
        key.put("embedding_type", AttributeValue.builder().s(embeddingType).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .build();

        // Attempt to fetch from DAX (cache) if available
        Map<String, AttributeValue> item = null;
        if (daxClient != null) {
            try {
                GetItemResponse response = daxClient.getItem(request).get();
                item = response.item();
            } catch (Exception e) {
                System.err.println("Error fetching from DAX: " + e.getMessage());
            }
        }
        
        if (item == null || !item.containsKey("embedding")) {
            // Fallback to DynamoDB
            GetItemResponse response = dynamoDb.getItem(request);
            item = response.item();
        }

        return (item != null && item.containsKey("embedding"))
                ? JsonUtils.fromJson(item.get("embedding").s(), double[].class)
                : null;
    }

    /**
     * Retrieves embedding with its metadata.
     * 
     * @param userId User ID
     * @param embeddingType Type of embedding to retrieve
     * @return Map containing the embedding and metadata, or null if not found
     */
    public Map<String, Object> getEmbeddingWithMetadata(String userId, String embeddingType) throws Exception {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("user_id", AttributeValue.builder().s(userId).build());
        key.put("embedding_type", AttributeValue.builder().s(embeddingType).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .build();

        // Attempt to fetch from DAX (cache) if available
        Map<String, AttributeValue> item = null;
        if (daxClient != null) {
            try {
                GetItemResponse response = daxClient.getItem(request).get();
                item = response.item();
            } catch (Exception e) {
                System.err.println("Error fetching from DAX: " + e.getMessage());
            }
        }
        
        if (item == null || !item.containsKey("embedding")) {
            // Fallback to DynamoDB
            GetItemResponse response = dynamoDb.getItem(request);
            item = response.item();
        }

        if (item != null && item.containsKey("embedding") && item.containsKey("metadata")) {
            Map<String, Object> result = new HashMap<>();
            result.put("embedding", JsonUtils.fromJson(item.get("embedding").s(), double[].class));
            result.put("metadata", JsonUtils.fromJson(item.get("metadata").s(), EmbeddingMetadata.class));
            result.put("created_at", item.get("created_at").s());
            return result;
        }
        
        return null;
    }

    /**
     * Gets all embedding types for a specific user.
     * 
     * @param userId User ID
     * @return List of embedding types available for the user
     */
    public List<String> getUserEmbeddingTypes(String userId) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":userId", AttributeValue.builder().s(userId).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .keyConditionExpression("user_id = :userId")
                .expressionAttributeValues(expressionValues)
                .projectionExpression("embedding_type")
                .build();

        QueryResponse response = dynamoDb.query(queryRequest);
        
        List<String> embeddingTypes = new ArrayList<>();
        for (Map<String, AttributeValue> item : response.items()) {
            if (item.containsKey("embedding_type")) {
                embeddingTypes.add(item.get("embedding_type").s());
            }
        }
        
        return embeddingTypes;
    }
} 