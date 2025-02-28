package com.sample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.BedrockEmbeddingTest.BedrockEmbeddingService;
import com.sample.UserEmbeddingLambdaTest.UserEmbeddingLambda;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Test class for UserEmbeddingLambda with AWS Bedrock integration
 * This demonstrates how to replace the random embedding generation with real AI-generated embeddings
 */
public class BedrockUserEmbeddingTest {
    
    private BedrockUserEmbeddingLambda lambda;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @BeforeEach
    public void setup() {
        // Create DynamoDB client
        DynamoDbClient dynamoDb = DynamoDbClient.builder()
                .region(Region.US_EAST_1) // Use your preferred region
                .build();
        
        // Initialize the lambda with Bedrock embedding service
        lambda = new BedrockUserEmbeddingLambda(dynamoDb);
    }
    
    @Test
    @Disabled("Requires AWS credentials with Bedrock and DynamoDB access")
    public void testHandleRequestWithBedrock() {
        try {
            // Create a test user ID
            String userId = "bedrock-lambda-test-" + UUID.randomUUID().toString().substring(0, 8);
            
            // Create request event
            Map<String, Object> event = new HashMap<>();
            Map<String, String> body = new HashMap<>();
            body.put("user_id", userId);
            body.put("text", "This user enjoys hiking, reading science fiction, and playing chess.");
            
            event.put("body", objectMapper.writeValueAsString(body));
            
            // Call the lambda function
            Map<String, Object> response = lambda.handleRequest(event);
            
            // Verify response
            assertEquals(200, response.get("statusCode"));
            assertTrue(response.get("body").toString().contains("Embedding stored for user: " + userId));
            
            System.out.println("Successfully stored Bedrock-generated embedding for user: " + userId);
            
        } catch (Exception e) {
            fail("Bedrock lambda test failed: " + e.getMessage());
        }
    }
    
    @Test
    @Disabled("Requires AWS credentials with Bedrock and DynamoDB access")
    public void testFindSimilarUsersWithBedrock() {
        try {
            // Create test users with different interests
            String userId1 = "bedrock-sim-user1-" + UUID.randomUUID().toString().substring(0, 8);
            String userId2 = "bedrock-sim-user2-" + UUID.randomUUID().toString().substring(0, 8);
            String userId3 = "bedrock-sim-user3-" + UUID.randomUUID().toString().substring(0, 8);
            
            // Store embeddings for each user
            lambda.storeUserEmbedding(userId1, "I enjoy outdoor activities like hiking, camping, and mountain biking.");
            lambda.storeUserEmbedding(userId2, "My hobbies include trekking, nature photography, and wilderness survival.");
            lambda.storeUserEmbedding(userId3, "I'm passionate about quantum computing, theoretical physics, and mathematics.");
            
            // Find similar users to user1
            Map<String, Object> event = new HashMap<>();
            Map<String, String> body = new HashMap<>();
            body.put("user_id", userId1);
            body.put("action", "find_similar");
            body.put("limit", "2");
            
            event.put("body", objectMapper.writeValueAsString(body));
            
            // Call the lambda function
            Map<String, Object> response = lambda.handleRequest(event);
            
            // Verify response
            assertEquals(200, response.get("statusCode"));
            
            // Parse the response body to get similar users
            String responseBody = (String) response.get("body");
            List<Map<String, Object>> similarUsers = objectMapper.readValue(responseBody, List.class);
            
            // Verify that user2 is more similar to user1 than user3
            assertEquals(2, similarUsers.size(), "Should return 2 similar users");
            
            String firstSimilarUserId = (String) similarUsers.get(0).get("userId");
            String secondSimilarUserId = (String) similarUsers.get(1).get("userId");
            
            System.out.println("Most similar user to " + userId1 + " is " + firstSimilarUserId);
            System.out.println("Second most similar user to " + userId1 + " is " + secondSimilarUserId);
            
            // User2 should be more similar to User1 than User3
            assertEquals(userId2, firstSimilarUserId, "User2 should be most similar to User1");
            assertEquals(userId3, secondSimilarUserId, "User3 should be less similar to User1");
            
        } catch (Exception e) {
            fail("Bedrock similarity search test failed: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to create a simple request event
     */
    private Map<String, Object> createRequestEvent(String userId, String text) throws Exception {
        Map<String, Object> event = new HashMap<>();
        Map<String, String> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("text", text);
        
        event.put("body", objectMapper.writeValueAsString(body));
        return event;
    }
    
    /**
     * UserEmbeddingLambda implementation that uses AWS Bedrock for embedding generation
     */
    public static class BedrockUserEmbeddingLambda extends UserEmbeddingLambda {
        private final BedrockEmbeddingService bedrockService;
        
        public BedrockUserEmbeddingLambda(DynamoDbClient dynamoDb) {
            super(dynamoDb);
            this.bedrockService = new BedrockEmbeddingService();
        }
        
        /**
         * Override the generateEmbedding method to use Bedrock instead of random values
         */
        @Override
        public List<Double> generateEmbedding(String text) {
            try {
                // Use Bedrock to generate the embedding
                return bedrockService.generateEmbedding(text);
            } catch (Exception e) {
                System.err.println("Error generating Bedrock embedding: " + e.getMessage());
                // Fall back to random embedding if Bedrock fails
                return super.generateEmbedding(text);
            }
        }
        
        /**
         * Helper method to store a user embedding
         */
        public void storeUserEmbedding(String userId, String text) throws Exception {
            List<Double> embedding = generateEmbedding(text);
            saveToDynamoDB(userId, "profile", embedding);
        }
    }
} 