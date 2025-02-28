package com.sample.examples;

import java.util.List;

import com.sample.examples.UserInterestMatchingExample.EmbeddingService;
import com.sample.examples.UserInterestMatchingExample.UserMatchingService;
import com.sample.examples.UserInterestMatchingExample.UserProfile;
import com.sample.examples.UserInterestMatchingExample.UserProfileMatch;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

/**
 * Demo application that shows how to match users with similar interests
 * This creates sample user profiles and finds matches between them
 */
public class UserMatchingDemo {

    private static final String TABLE_NAME = "UserEmbeddings";
    
    public static void main(String[] args) {
        System.out.println("=== User Interest Matching Demo ===");
        System.out.println("This demo creates sample user profiles and finds matches between them");
        System.out.println("using AI-generated embeddings from AWS Bedrock.");
        System.out.println();
        
        try {
            // Create DynamoDB client
            DynamoDbClient dynamoDb = DynamoDbClient.builder()
                    .region(Region.US_EAST_1) // Change to your preferred region
                    .endpointOverride(java.net.URI.create("http://localhost:8000")) // Use DynamoDB Local for testing
                    .build();
            
            // Create embedding service
            EmbeddingService embeddingService = new EmbeddingService();
            
            // Create user matching service
            UserMatchingService matchingService = new UserMatchingService(dynamoDb, embeddingService);
            
            // Create test table
            createTestTable(dynamoDb);
            
            // Create sample user profiles
            System.out.println("Creating sample user profiles...");
            UserProfile user1 = createUser(matchingService, "John", 28, "New York", 
                    "I love hiking, camping, and outdoor photography. I'm also interested in craft beer and cooking.");
            
            UserProfile user2 = createUser(matchingService, "Sarah", 32, "Colorado", 
                    "Passionate about mountain climbing, trail running, and landscape photography. I also enjoy craft breweries.");
            
            UserProfile user3 = createUser(matchingService, "Michael", 35, "California", 
                    "Software engineer who enjoys coding, AI, machine learning, and data science. I also like playing chess and reading sci-fi novels.");
            
            UserProfile user4 = createUser(matchingService, "Emily", 29, "Washington", 
                    "I'm a data scientist who loves working with AI models. In my free time, I enjoy hiking and reading science fiction.");
            
            UserProfile user5 = createUser(matchingService, "David", 31, "Texas", 
                    "Basketball player and sports enthusiast. I also enjoy watching movies, playing video games, and trying new restaurants.");
            
            // Find matches for each user
            System.out.println("\n=== Finding Matches for Each User ===");
            
            findAndDisplayMatches(matchingService, user1, "John");
            findAndDisplayMatches(matchingService, user2, "Sarah");
            findAndDisplayMatches(matchingService, user3, "Michael");
            findAndDisplayMatches(matchingService, user4, "Emily");
            findAndDisplayMatches(matchingService, user5, "David");
            
            // Clean up - delete the table
            deleteTestTable(dynamoDb);
            
            System.out.println("\nDemo completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error in demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static UserProfile createUser(UserMatchingService matchingService, String name, int age, String location, String interests) throws Exception {
        String userId = "demo-" + name.toLowerCase() + "-" + System.currentTimeMillis();
        UserProfile profile = matchingService.createUserProfile(userId, name, age, location, interests);
        System.out.println("Created profile for " + name + " (ID: " + userId + ")");
        return profile;
    }
    
    private static void findAndDisplayMatches(UserMatchingService matchingService, UserProfile user, String name) throws Exception {
        System.out.println("\nMatches for " + name + ":");
        System.out.println("-------------------");
        
        List<UserProfileMatch> matches = matchingService.findSimilarUsers(user.getUserId(), 4);
        
        if (matches.isEmpty()) {
            System.out.println("No matches found.");
            return;
        }
        
        for (int i = 0; i < matches.size(); i++) {
            UserProfileMatch match = matches.get(i);
            System.out.printf("%d. %s (%.2f%% match)\n", 
                    i + 1, match.getProfile().getName(), match.getSimilarityPercentage());
            System.out.println("   Location: " + match.getProfile().getLocation());
            System.out.println("   Interests: " + match.getProfile().getInterests());
        }
    }
    
    private static void createTestTable(DynamoDbClient dynamoDb) {
        try {
            // Check if table exists
            try {
                dynamoDb.describeTable(DescribeTableRequest.builder()
                        .tableName(TABLE_NAME)
                        .build());
                System.out.println("Table already exists: " + TABLE_NAME);
                return;
            } catch (Exception e) {
                // Table doesn't exist, create it
            }
            
            // Create table
            dynamoDb.createTable(CreateTableRequest.builder()
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
            dynamoDb.waiter().waitUntilTableExists(DescribeTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .build());
            
            System.out.println("Created table: " + TABLE_NAME);
            
        } catch (Exception e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }
    
    private static void deleteTestTable(DynamoDbClient dynamoDb) {
        try {
            dynamoDb.deleteTable(DeleteTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .build());
            
            System.out.println("Deleted table: " + TABLE_NAME);
            
        } catch (Exception e) {
            System.err.println("Error deleting table: " + e.getMessage());
        }
    }
} 