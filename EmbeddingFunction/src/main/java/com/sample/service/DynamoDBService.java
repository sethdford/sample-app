package com.sample.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sample.util.JsonUtils;

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.dax.ClusterDaxAsyncClient;
import software.amazon.dax.Configuration;

public class DynamoDBService {
    private DynamoDbAsyncClient daxClient = null;
    private final DynamoDbClient dynamoDb;
    private static final String TABLE_NAME = "UserEmbeddings";
    private static final String DAX_ENDPOINT = "dax://your-dax-cluster.amazonaws.com"; // Replace with your DAX cluster

    public DynamoDBService()  {
        // Initialize DAX client (for caching) and DynamoDB client (for permanent storage)
        this.dynamoDb = DynamoDbClient.create();
       
        try {
            daxClient = ClusterDaxAsyncClient.builder()
                    .overrideConfiguration(Configuration.builder()
                            .url(DAX_ENDPOINT) // e.g. dax://my-cluster.l6fzcv.dax-clusters.us-east-1.amazonaws.com
                            .build())
                    .build();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Stores a user embedding in both DAX (cache) and DynamoDB (permanent
     * storage).
     */
    public void storeEmbedding(String userId, double[] embedding) throws Exception {
        String embeddingJson = JsonUtils.toJson(embedding);

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("user_id", AttributeValue.builder().s(userId).build());
        item.put("embedding", AttributeValue.builder().s(embeddingJson).build());

        // Store in DAX (cache)
        daxClient.putItem(PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build());

        // Store in DynamoDB (permanent storage)
        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build());
    }

    /**
     * Retrieves a user embedding from DAX first, then falls back to DynamoDB if
     * not found.
     */
    public double[] getEmbedding(String userId) throws Exception {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("user_id", AttributeValue.builder().s(userId).build()))
                .build();

        // Attempt to fetch from DAX (cache)
        Map<String, AttributeValue> item = null;
        try {
            GetItemResponse response = daxClient.getItem(request).get(); // Use .get() to wait for the CompletableFuture
            item = response.item();
        } catch (Exception e) {
            // Log the exception if needed
            System.err.println("Error fetching from DAX: " + e.getMessage());
        }
        
        if (item == null || !item.containsKey("embedding")) {
            // Fallback to DynamoDB
            item = dynamoDb.getItem(request).item();
        }

        return (item != null && item.containsKey("embedding"))
                ? JsonUtils.fromJson(item.get("embedding").s(), double[].class)
                : null;
    }
}
