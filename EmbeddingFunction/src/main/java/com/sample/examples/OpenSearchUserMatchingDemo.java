package com.sample.examples;

import java.util.List;
import java.util.UUID;

import com.sample.examples.UserInterestMatchingExample.EmbeddingService;
import com.sample.examples.UserInterestMatchingExample.UserProfile;
import com.sample.examples.UserInterestMatchingExample.UserProfileMatch;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

/**
 * Demo application that shows how to use fast similarity search
 */
public class OpenSearchUserMatchingDemo {

    private static final String DYNAMODB_TABLE_NAME = "UserEmbeddings";
    private static final Region AWS_REGION = Region.US_EAST_1; // Change to your AWS region
    
    public static void main(String[] args) {
        try {
            // Create DynamoDB client
            DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                    .region(AWS_REGION)
                    .credentialsProvider(ProfileCredentialsProvider.create())
                    .build();
            
            // Create embedding service
            EmbeddingService embeddingService = new EmbeddingService();
            
            // Create user matching service
            UserInterestMatchingExample.UserMatchingService matchingService = 
                    new UserInterestMatchingExample.UserMatchingService(dynamoDbClient, embeddingService);
            
            // Setup DynamoDB table
            setupDynamoDbTable(dynamoDbClient);
            
            // Create sample user profiles
            System.out.println("Creating sample user profiles...");
            UserProfile alice = createUserProfile(matchingService, "Alice", 28, "New York", 
                    "hiking, photography, travel, cooking, yoga");
            
            UserProfile bob = createUserProfile(matchingService, "Bob", 32, "San Francisco", 
                    "hiking, mountain biking, outdoor adventures, photography");
            
            UserProfile charlie = createUserProfile(matchingService, "Charlie", 25, "Chicago", 
                    "gaming, programming, artificial intelligence, virtual reality");
            
            UserProfile diana = createUserProfile(matchingService, "Diana", 30, "Seattle", 
                    "reading, writing, poetry, art galleries, museums");
            
            UserProfile eve = createUserProfile(matchingService, "Eve", 27, "Boston", 
                    "programming, data science, machine learning, hiking");
            
            // Find similar users for Alice
            System.out.println("\nFinding similar users for Alice...");
            List<UserProfileMatch> aliceMatches = matchingService.findSimilarUsers(alice.getUserId(), 3);
            displayMatches(alice, aliceMatches);
            
            // Find similar users for Charlie
            System.out.println("\nFinding similar users for Charlie...");
            List<UserProfileMatch> charlieMatches = matchingService.findSimilarUsers(charlie.getUserId(), 3);
            displayMatches(charlie, charlieMatches);
            
            // Find similar users for Eve
            System.out.println("\nFinding similar users for Eve...");
            List<UserProfileMatch> eveMatches = matchingService.findSimilarUsers(eve.getUserId(), 3);
            displayMatches(eve, eveMatches);
            
            // Clean up
            System.out.println("\nCleaning up...");
            deleteDynamoDbTable(dynamoDbClient);
            
            System.out.println("Demo completed successfully!");
            
            // Note about OpenSearch
            System.out.println("\n-------------------------------------------------");
            System.out.println("NOTE: For production use with large datasets, consider using OpenSearch k-NN.");
            System.out.println("OpenSearch provides significantly faster similarity search for large embedding collections.");
            System.out.println("See the README for instructions on setting up OpenSearch.");
            System.out.println("-------------------------------------------------");
            
        } catch (Exception e) {
            System.err.println("Error in demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a user profile with a unique ID
     */
    private static UserProfile createUserProfile(UserInterestMatchingExample.UserMatchingService matchingService, 
            String name, int age, String location, String interests) throws Exception {
        String userId = UUID.randomUUID().toString();
        UserProfile profile = matchingService.createUserProfile(userId, name, age, location, interests);
        System.out.println("Created profile for " + name + " (ID: " + userId + ")");
        return profile;
    }
    
    /**
     * Display matches for a user
     */
    private static void displayMatches(UserProfile user, List<UserProfileMatch> matches) {
        System.out.println("Matches for " + user.getName() + ":");
        if (matches.isEmpty()) {
            System.out.println("  No matches found");
        } else {
            for (UserProfileMatch match : matches) {
                System.out.printf("  %s (%.2f%% similar) - Interests: %s%n", 
                        match.getProfile().getName(),
                        match.getSimilarityPercentage(),
                        match.getProfile().getInterests());
            }
        }
    }
    
    /**
     * Setup DynamoDB table for user embeddings
     */
    private static void setupDynamoDbTable(DynamoDbClient dynamoDbClient) {
        try {
            // Check if table exists
            try {
                dynamoDbClient.describeTable(b -> b.tableName(DYNAMODB_TABLE_NAME));
                System.out.println("Table already exists: " + DYNAMODB_TABLE_NAME);
                return;
            } catch (ResourceNotFoundException e) {
                // Table doesn't exist, create it
            }
            
            // Create table with composite primary key (user_id, embedding_type)
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(DYNAMODB_TABLE_NAME)
                    .keySchema(
                            KeySchemaElement.builder().attributeName("user_id").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("embedding_type").keyType(KeyType.RANGE).build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("user_id").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("embedding_type").attributeType(ScalarAttributeType.S).build()
                    )
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build();
            
            dynamoDbClient.createTable(createTableRequest);
            System.out.println("Created table: " + DYNAMODB_TABLE_NAME);
            
            // Wait for table to be active
            boolean tableActive = false;
            while (!tableActive) {
                String status = dynamoDbClient.describeTable(b -> b.tableName(DYNAMODB_TABLE_NAME))
                        .table()
                        .tableStatus()
                        .toString();
                
                tableActive = "ACTIVE".equals(status);
                if (!tableActive) {
                    System.out.println("Waiting for table to be active...");
                    Thread.sleep(1000);
                }
            }
            
            System.out.println("Table is active: " + DYNAMODB_TABLE_NAME);
            
        } catch (Exception e) {
            System.err.println("Error setting up DynamoDB table: " + e.getMessage());
            throw new RuntimeException("Failed to setup DynamoDB table", e);
        }
    }
    
    /**
     * Delete DynamoDB table
     */
    private static void deleteDynamoDbTable(DynamoDbClient dynamoDbClient) {
        try {
            DeleteTableRequest deleteTableRequest = DeleteTableRequest.builder()
                    .tableName(DYNAMODB_TABLE_NAME)
                    .build();
            
            dynamoDbClient.deleteTable(deleteTableRequest);
            System.out.println("Deleted table: " + DYNAMODB_TABLE_NAME);
        } catch (Exception e) {
            System.err.println("Error deleting DynamoDB table: " + e.getMessage());
        }
    }
} 