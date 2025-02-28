package com.sample;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

/**
 * Test class for UserEmbeddingLambda
 * Tests the functionality of storing user embeddings in DynamoDB
 */
public class UserEmbeddingLambdaTest {
    
    private TestableUserEmbeddingLambda lambda;
    private static final String TABLE_NAME = "UserEmbeddings";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @BeforeEach
    public void setup() {
        // Create lambda with a testable implementation
        lambda = new TestableUserEmbeddingLambda();
    }
    
    @Test
    public void testHandleRequest_Success() {
        // Prepare test data
        String userId = "user-" + UUID.randomUUID().toString();
        String text = "This is a sample text for embedding generation";
        
        // Create request event
        Map<String, Object> event = new HashMap<>();
        Map<String, String> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("text", text);
        
        try {
            event.put("body", objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            fail("Failed to create test event: " + e.getMessage());
        }
        
        // Call the lambda function
        Map<String, Object> response = lambda.handleRequest(event);
        
        // Verify response
        assertEquals(200, response.get("statusCode"));
        assertTrue(response.get("body").toString().contains("Embedding stored for user: " + userId));
        
        // Verify DynamoDB call was made with correct parameters
        PutItemRequest lastRequest = lambda.getLastPutItemRequest();
        assertNotNull(lastRequest, "PutItemRequest should not be null");
        assertEquals(TABLE_NAME, lastRequest.tableName());
        
        Map<String, AttributeValue> item = lastRequest.item();
        assertEquals(userId, item.get("user_id").s());
        assertEquals("profile", item.get("embedding_type").s());
        assertTrue(item.containsKey("embedding"));
        assertTrue(item.containsKey("created_at"));
    }
    
    @Test
    public void testHandleRequest_InvalidInput() {
        // Test with missing user_id
        Map<String, Object> event = new HashMap<>();
        Map<String, String> body = new HashMap<>();
        body.put("text", "Sample text");
        
        try {
            event.put("body", objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            fail("Failed to create test event: " + e.getMessage());
        }
        
        Map<String, Object> response = lambda.handleRequest(event);
        
        // Verify error response
        assertEquals(500, response.get("statusCode"));
        assertTrue(response.get("body").toString().contains("Error"));
    }
    
    @Test
    public void testGenerateEmbedding() {
        // Test the embedding generation functionality
        String text = "This is a test text";
        List<Double> embedding = lambda.generateEmbedding(text);
        
        // Verify embedding properties
        assertNotNull(embedding);
        assertEquals(512, embedding.size());
        
        // In a real test, you might verify the embedding values against expected values
        // from a known model output, but for this example we just check basic properties
    }
    
    @Test
    public void testSimilaritySearch() {
        try {
            // Create test users with embeddings
            String userId1 = "user1";
            String userId2 = "user2";
            String userId3 = "user3";
            
            // Create similar embeddings for user1 and user2, different for user3
            List<Double> embedding1 = createTestEmbedding(0.1);
            List<Double> embedding2 = createTestEmbedding(0.2); // Similar to embedding1
            List<Double> embedding3 = createTestEmbedding(0.9); // Different from embedding1
            
            // Store the embeddings in our test lambda
            lambda.addTestEmbedding(userId1, "profile", embedding1);
            lambda.addTestEmbedding(userId2, "profile", embedding2);
            lambda.addTestEmbedding(userId3, "profile", embedding3);
            
            // Perform similarity search
            List<SimilarityResult> results = lambda.findSimilarUsers(userId1, 2);
            
            // Verify results
            assertEquals(2, results.size(), "Should return 2 similar users");
            
            // User2 should be more similar to User1 than User3
            assertEquals(userId2, results.get(0).getUserId(), "User2 should be most similar to User1");
            assertEquals(userId3, results.get(1).getUserId(), "User3 should be less similar to User1");
            
            // Verify similarity scores
            assertTrue(results.get(0).getSimilarity() > results.get(1).getSimilarity(), 
                    "User2 similarity score should be higher than User3");
        } catch (Exception e) {
            fail("Similarity search test failed: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to create test embeddings with controlled similarity
     */
    private List<Double> createTestEmbedding(double baseFactor) {
        List<Double> embedding = new ArrayList<>();
        for (int i = 0; i < 512; i++) {
            embedding.add(baseFactor + (i * 0.001)); // Create deterministic values
        }
        return embedding;
    }
    
    /**
     * Integration test with real DynamoDB (local)
     * Note: This test requires DynamoDB Local to be running
     */
    @Test
    @Disabled("Requires DynamoDB Local to be running")
    public void integrationTest() {
        // Create real DynamoDB client (pointing to local DynamoDB)
        DynamoDbClient realDynamoDb = DynamoDbClient.builder()
                .endpointOverride(java.net.URI.create("http://localhost:8000"))
                .build();
        
        // Create test table
        try {
            realDynamoDb.createTable(CreateTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .keySchema(
                            KeySchemaElement.builder().attributeName("user_id").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("embedding_type").keyType(KeyType.RANGE).build())
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("user_id").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("embedding_type").attributeType(ScalarAttributeType.S).build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build());
            
            // Wait for table to be created
            realDynamoDb.waiter().waitUntilTableExists(DescribeTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .build());
            
        } catch (Exception e) {
            System.out.println("Table might already exist: " + e.getMessage());
        }
        
        try {
            // Create lambda with real DynamoDB client
            UserEmbeddingLambda realLambda = new UserEmbeddingLambda(realDynamoDb);
            
            // Prepare test data
            String userId = "integration-test-user";
            String text = "Integration test text";
            
            // Create request event
            Map<String, Object> event = new HashMap<>();
            Map<String, String> body = new HashMap<>();
            body.put("user_id", userId);
            body.put("text", text);
            event.put("body", objectMapper.writeValueAsString(body));
            
            // Call the lambda function
            Map<String, Object> response = realLambda.handleRequest(event);
            
            // Verify response
            assertEquals(200, response.get("statusCode"));
            
            // Verify data was stored in DynamoDB
            GetItemResponse getItemResponse = realDynamoDb.getItem(GetItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(Map.of(
                            "user_id", AttributeValue.builder().s(userId).build(),
                            "embedding_type", AttributeValue.builder().s("profile").build()))
                    .build());
            
            assertTrue(getItemResponse.hasItem());
            assertEquals(userId, getItemResponse.item().get("user_id").s());
            
        } catch (Exception e) {
            fail("Integration test failed: " + e.getMessage());
        } finally {
            // Clean up - delete the table
            try {
                realDynamoDb.deleteTable(DeleteTableRequest.builder()
                        .tableName(TABLE_NAME)
                        .build());
            } catch (Exception e) {
                System.out.println("Error deleting table: " + e.getMessage());
            }
        }
    }
    
    /**
     * Integration test for similarity search with real DynamoDB
     */
    @Test
    @Disabled("Requires DynamoDB Local to be running")
    public void integrationTestSimilaritySearch() {
        // Create real DynamoDB client (pointing to local DynamoDB)
        DynamoDbClient realDynamoDb = DynamoDbClient.builder()
                .endpointOverride(java.net.URI.create("http://localhost:8000"))
                .build();
        
        // Create test table
        try {
            realDynamoDb.createTable(CreateTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .keySchema(
                            KeySchemaElement.builder().attributeName("user_id").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("embedding_type").keyType(KeyType.RANGE).build())
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("user_id").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("embedding_type").attributeType(ScalarAttributeType.S).build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build());
            
            // Wait for table to be created
            realDynamoDb.waiter().waitUntilTableExists(DescribeTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .build());
            
        } catch (Exception e) {
            System.out.println("Table might already exist: " + e.getMessage());
        }
        
        try {
            // Create lambda with real DynamoDB client
            UserEmbeddingLambda realLambda = new UserEmbeddingLambda(realDynamoDb);
            
            // Create test users with embeddings
            String userId1 = "sim-test-user1";
            String userId2 = "sim-test-user2";
            String userId3 = "sim-test-user3";
            
            // Create and store embeddings
            List<Double> embedding1 = createTestEmbedding(0.1);
            List<Double> embedding2 = createTestEmbedding(0.2); // Similar to embedding1
            List<Double> embedding3 = createTestEmbedding(0.9); // Different from embedding1
            
            realLambda.saveToDynamoDB(userId1, "profile", embedding1);
            realLambda.saveToDynamoDB(userId2, "profile", embedding2);
            realLambda.saveToDynamoDB(userId3, "profile", embedding3);
            
            // Perform similarity search
            List<SimilarityResult> results = realLambda.findSimilarUsers(userId1, 2);
            
            // Verify results
            assertEquals(2, results.size(), "Should return 2 similar users");
            assertEquals(userId2, results.get(0).getUserId(), "User2 should be most similar to User1");
            
        } catch (Exception e) {
            fail("Integration test failed: " + e.getMessage());
        } finally {
            // Clean up - delete the table
            try {
                realDynamoDb.deleteTable(DeleteTableRequest.builder()
                        .tableName(TABLE_NAME)
                        .build());
            } catch (Exception e) {
                System.out.println("Error deleting table: " + e.getMessage());
            }
        }
    }
    
    /**
     * Testable version of UserEmbeddingLambda that captures the DynamoDB requests
     */
    public static class TestableUserEmbeddingLambda extends UserEmbeddingLambda {
        private PutItemRequest lastPutItemRequest;
        private Map<String, Map<String, List<Double>>> testEmbeddings = new HashMap<>();
        
        public TestableUserEmbeddingLambda() {
            super(null); // No actual DynamoDB client needed
        }
        
        @Override
        protected void saveToDynamoDB(String userId, String embeddingType, List<Double> embedding) {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("user_id", AttributeValue.builder().s(userId).build());
            item.put("embedding_type", AttributeValue.builder().s(embeddingType).build());
            item.put("embedding", AttributeValue.builder().s(embedding.toString()).build());
            item.put("created_at", AttributeValue.builder().s(Instant.now().toString()).build());

            lastPutItemRequest = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build();
            
            // Store in our test map for similarity search testing
            addTestEmbedding(userId, embeddingType, embedding);
        }
        
        public void addTestEmbedding(String userId, String embeddingType, List<Double> embedding) {
            if (!testEmbeddings.containsKey(userId)) {
                testEmbeddings.put(userId, new HashMap<>());
            }
            testEmbeddings.get(userId).put(embeddingType, embedding);
        }
        
        @Override
        protected List<UserEmbedding> getAllUserEmbeddings(String embeddingType) {
            List<UserEmbedding> result = new ArrayList<>();
            
            for (Map.Entry<String, Map<String, List<Double>>> entry : testEmbeddings.entrySet()) {
                String userId = entry.getKey();
                Map<String, List<Double>> userEmbeddings = entry.getValue();
                
                if (userEmbeddings.containsKey(embeddingType)) {
                    result.add(new UserEmbedding(userId, embeddingType, userEmbeddings.get(embeddingType)));
                }
            }
            
            return result;
        }
        
        public PutItemRequest getLastPutItemRequest() {
            return lastPutItemRequest;
        }
    }
    
    /**
     * UserEmbeddingLambda implementation for testing
     */
    public static class UserEmbeddingLambda {
        private final DynamoDbClient dynamoDb;
        private static final String TABLE_NAME = "UserEmbeddings";
        private static final ObjectMapper objectMapper = new ObjectMapper();

        public UserEmbeddingLambda() {
            this.dynamoDb = DynamoDbClient.create();
        }
        
        public UserEmbeddingLambda(DynamoDbClient dynamoDb) {
            this.dynamoDb = dynamoDb;
        }

        public Map<String, Object> handleRequest(Map<String, Object> event) {
            try {
                Map<String, String> body = parseBody(event.get("body"));
                String userId = body.get("user_id");
                if (userId == null) {
                    throw new IllegalArgumentException("user_id is required");
                }
                
                String text = body.get("text");
                String action = body.get("action");
                
                if ("find_similar".equals(action)) {
                    int limit = 10;
                    if (body.containsKey("limit")) {
                        limit = Integer.parseInt(body.get("limit"));
                    }
                    
                    List<SimilarityResult> similarUsers = findSimilarUsers(userId, limit);
                    return response(200, objectMapper.writeValueAsString(similarUsers));
                } else {
                    // Default action: store embedding
                    List<Double> embedding = generateEmbedding(text);
                    saveToDynamoDB(userId, "profile", embedding);
                    return response(200, "Embedding stored for user: " + userId);
                }
            } catch (Exception e) {
                return response(500, "Error: " + e.getMessage());
            }
        }

        private Map<String, String> parseBody(Object body) throws Exception {
            return objectMapper.readValue(body.toString(), Map.class);
        }

        public List<Double> generateEmbedding(String text) {
            // In a real implementation, this would call a model to generate embeddings
            // For testing, we generate random values
            java.util.Random rand = new java.util.Random();
            List<Double> embedding = new ArrayList<>();
            for (int i = 0; i < 512; i++) {
                embedding.add(rand.nextDouble() * 2 - 1); // Values between -1 and 1
            }
            return embedding;
        }

        protected void saveToDynamoDB(String userId, String embeddingType, List<Double> embedding) {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("user_id", AttributeValue.builder().s(userId).build());
            item.put("embedding_type", AttributeValue.builder().s(embeddingType).build());
            item.put("embedding", AttributeValue.builder().s(embedding.toString()).build());
            item.put("created_at", AttributeValue.builder().s(Instant.now().toString()).build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build();

            if (dynamoDb != null) {
                dynamoDb.putItem(request);
            }
        }
        
        /**
         * Find users with similar embeddings to the specified user
         * @param userId The user ID to find similar users for
         * @param limit Maximum number of similar users to return
         * @return List of similar users with similarity scores
         */
        public List<SimilarityResult> findSimilarUsers(String userId, int limit) throws Exception {
            // Get the user's embedding
            List<Double> userEmbedding = getUserEmbedding(userId, "profile");
            if (userEmbedding == null) {
                throw new IllegalArgumentException("No embedding found for user: " + userId);
            }
            
            // Get all user embeddings
            List<UserEmbedding> allEmbeddings = getAllUserEmbeddings("profile");
            
            // Calculate similarity scores
            List<SimilarityResult> results = new ArrayList<>();
            for (UserEmbedding other : allEmbeddings) {
                // Skip the user we're comparing against
                if (other.getUserId().equals(userId)) {
                    continue;
                }
                
                double similarity = calculateCosineSimilarity(userEmbedding, other.getEmbedding());
                results.add(new SimilarityResult(other.getUserId(), similarity));
            }
            
            // Sort by similarity (highest first) and limit results
            return results.stream()
                    .sorted(Comparator.comparing(SimilarityResult::getSimilarity).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
        }
        
        /**
         * Get a user's embedding from DynamoDB
         */
        protected List<Double> getUserEmbedding(String userId, String embeddingType) throws Exception {
            if (dynamoDb == null) {
                return null; // For testing
            }
            
            GetItemResponse response = dynamoDb.getItem(GetItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(Map.of(
                            "user_id", AttributeValue.builder().s(userId).build(),
                            "embedding_type", AttributeValue.builder().s(embeddingType).build()))
                    .build());
            
            if (!response.hasItem() || !response.item().containsKey("embedding")) {
                return null;
            }
            
            String embeddingJson = response.item().get("embedding").s();
            return parseEmbeddingFromJson(embeddingJson);
        }
        
        /**
         * Get all user embeddings from DynamoDB
         */
        protected List<UserEmbedding> getAllUserEmbeddings(String embeddingType) {
            if (dynamoDb == null) {
                return new ArrayList<>(); // For testing
            }
            
            List<UserEmbedding> results = new ArrayList<>();
            
            ScanResponse response = dynamoDb.scan(ScanRequest.builder()
                    .tableName(TABLE_NAME)
                    .filterExpression("embedding_type = :type")
                    .expressionAttributeValues(Map.of(
                            ":type", AttributeValue.builder().s(embeddingType).build()))
                    .build());
            
            for (Map<String, AttributeValue> item : response.items()) {
                String userId = item.get("user_id").s();
                String embeddingJson = item.get("embedding").s();
                
                try {
                    List<Double> embedding = parseEmbeddingFromJson(embeddingJson);
                    results.add(new UserEmbedding(userId, embeddingType, embedding));
                } catch (Exception e) {
                    System.err.println("Error parsing embedding for user " + userId + ": " + e.getMessage());
                }
            }
            
            return results;
        }
        
        /**
         * Parse embedding from JSON string
         */
        private List<Double> parseEmbeddingFromJson(String embeddingJson) throws Exception {
            // Remove brackets and split by comma
            String cleaned = embeddingJson.replace("[", "").replace("]", "").trim();
            if (cleaned.isEmpty()) {
                return new ArrayList<>();
            }
            
            String[] values = cleaned.split(",");
            List<Double> embedding = new ArrayList<>();
            
            for (String value : values) {
                embedding.add(Double.parseDouble(value.trim()));
            }
            
            return embedding;
        }
        
        /**
         * Calculate cosine similarity between two embeddings
         */
        private double calculateCosineSimilarity(List<Double> embedding1, List<Double> embedding2) {
            if (embedding1.size() != embedding2.size()) {
                throw new IllegalArgumentException("Embeddings must have the same dimension");
            }
            
            double dotProduct = 0.0;
            double norm1 = 0.0;
            double norm2 = 0.0;
            
            for (int i = 0; i < embedding1.size(); i++) {
                dotProduct += embedding1.get(i) * embedding2.get(i);
                norm1 += Math.pow(embedding1.get(i), 2);
                norm2 += Math.pow(embedding2.get(i), 2);
            }
            
            // Avoid division by zero
            if (norm1 == 0 || norm2 == 0) {
                return 0.0;
            }
            
            return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        }

        private Map<String, Object> response(int statusCode, String message) {
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("statusCode", statusCode);
            responseMap.put("body", message);
            return responseMap;
        }
    }
    
    /**
     * Class to hold user embedding data
     */
    public static class UserEmbedding {
        private final String userId;
        private final String embeddingType;
        private final List<Double> embedding;
        
        public UserEmbedding(String userId, String embeddingType, List<Double> embedding) {
            this.userId = userId;
            this.embeddingType = embeddingType;
            this.embedding = embedding;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public String getEmbeddingType() {
            return embeddingType;
        }
        
        public List<Double> getEmbedding() {
            return embedding;
        }
    }
    
    /**
     * Class to hold similarity search results
     */
    public static class SimilarityResult {
        private final String userId;
        private final double similarity;
        
        public SimilarityResult(String userId, double similarity) {
            this.userId = userId;
            this.similarity = similarity;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public double getSimilarity() {
            return similarity;
        }
    }
} 