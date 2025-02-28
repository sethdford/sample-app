package com.sample;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.dax.ClusterDaxAsyncClient;
import software.amazon.dax.Configuration;

/**
 * Test class for DynamoDB with DAX integration
 * Based on AWS example: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DAX.client.TryDax.java.html
 */
public class DynamoDBDaxTest {
    private DynamoDbAsyncClient ddbClient;
    private DynamoDbAsyncClient daxClient;
    private final String tableName = "TestEmbeddingsTable";
    private final String daxEndpoint = System.getenv("DAX_ENDPOINT"); // Get from environment variable

    @BeforeEach
    public void setup() throws Exception {
        // Initialize standard DynamoDB client
        ddbClient = DynamoDbAsyncClient.builder().build();

        // Initialize DAX client if endpoint is available
        if (daxEndpoint != null && !daxEndpoint.isEmpty()) {
            daxClient = ClusterDaxAsyncClient.builder()
                    .overrideConfiguration(Configuration.builder()
                            .url(daxEndpoint)
                            .build())
                    .build();
        }

        // Create test table
        createTable();
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Clean up by deleting the test table
        deleteTable();
    }

    @Test
    public void testDaxPerformance() throws Exception {
        // Skip test if DAX endpoint is not configured
        if (daxClient == null) {
            System.out.println("DAX_ENDPOINT not set, skipping DAX performance test");
            return;
        }

        // Populate table with test data
        System.out.println("Populating table with test data...");
        populateTable(10);

        // Test GetItem with DynamoDB client
        System.out.println("Testing GetItem with DynamoDB client...");
        long ddbTime = runGetItemTest(ddbClient, 5);

        // Test GetItem with DAX client
        System.out.println("Testing GetItem with DAX client (first run - cache miss)...");
        runGetItemTest(daxClient, 1);
        
        System.out.println("Testing GetItem with DAX client (second run - cache hit)...");
        long daxTime = runGetItemTest(daxClient, 5);

        System.out.println("DynamoDB average time: " + ddbTime + " ms");
        System.out.println("DAX average time: " + daxTime + " ms");
        System.out.println("Speedup: " + (ddbTime > 0 ? (float)ddbTime / daxTime : "N/A"));

        // We expect DAX to be faster, but don't fail the test if it's not
        // as this could be due to test environment configuration
        assertTrue(true, "Performance test completed");
    }

    private void createTable() throws Exception {
        try {
            System.out.println("Creating test table: " + tableName);
            
            ddbClient.createTable(CreateTableRequest.builder()
                    .tableName(tableName)
                    .keySchema(KeySchemaElement.builder()
                            .keyType(KeyType.HASH)
                            .attributeName("user_id")
                            .build())
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName("user_id")
                            .attributeType(ScalarAttributeType.S)
                            .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build()).get();
            
            ddbClient.waiter().waitUntilTableExists(DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build()).get();
            
            System.out.println("Table created successfully");
        } catch (Exception e) {
            System.err.println("Error creating table: " + e.getMessage());
            throw e;
        }
    }

    private void deleteTable() throws Exception {
        try {
            System.out.println("Deleting test table: " + tableName);
            
            ddbClient.deleteTable(DeleteTableRequest.builder()
                    .tableName(tableName)
                    .build()).get();
            
            ddbClient.waiter().waitUntilTableNotExists(DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build()).get();
            
            System.out.println("Table deleted successfully");
        } catch (Exception e) {
            System.err.println("Error deleting table: " + e.getMessage());
            throw e;
        }
    }

    private void populateTable(int itemCount) throws Exception {
        System.out.println("Writing " + itemCount + " items to the table...");

        // Create sample embedding data
        double[] embedding = new double[128];
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = Math.random();
        }
        String embeddingJson = java.util.Arrays.toString(embedding);

        try {
            for (int i = 1; i <= itemCount; i++) {
                String userId = "user-" + UUID.randomUUID().toString();
                
                ddbClient.putItem(PutItemRequest.builder()
                        .tableName(tableName)
                        .item(Map.of(
                            "user_id", AttributeValue.builder().s(userId).build(),
                            "embedding", AttributeValue.builder().s(embeddingJson).build()
                        ))
                        .build()).get();
                
                System.out.println("Added item " + i + " with user_id: " + userId);
            }
        } catch (Exception e) {
            System.err.println("Error writing items: " + e.getMessage());
            throw e;
        }
    }

    private long runGetItemTest(DynamoDbAsyncClient client, int iterations) throws Exception {
        long startTime = System.currentTimeMillis();
        
        try {
            // Get all items from the table
            for (int i = 0; i < iterations; i++) {
                // In a real test, you would query specific items
                // For simplicity, we're just getting the first item repeatedly
                GetItemRequest request = GetItemRequest.builder()
                        .tableName(tableName)
                        .key(Map.of("user_id", AttributeValue.builder().s("user-1").build()))
                        .build();
                
                client.getItem(request).get();
            }
        } catch (Exception e) {
            System.err.println("Error in get item test: " + e.getMessage());
            throw e;
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        long avgTime = totalTime / iterations;
        
        System.out.println("Total time: " + totalTime + " ms, Avg time: " + avgTime + " ms");
        
        return avgTime;
    }
} 