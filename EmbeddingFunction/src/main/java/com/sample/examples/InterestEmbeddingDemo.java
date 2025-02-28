package com.sample.examples;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sample.service.EnhancedDynamoDBService;
import com.sample.service.InterestEmbeddingService;
import com.sample.util.JsonUtils;

/**
 * Demonstrates how to use the InterestEmbeddingService to generate embeddings from user interests.
 */
public class InterestEmbeddingDemo {

    public static void main(String[] args) {
        try {
            // Initialize the service
            InterestEmbeddingService embeddingService = new InterestEmbeddingService();
            
            // Create sample users with different interest profiles
            String[] userIds = {"tech_user", "creative_user", "outdoor_user", "general_user"};
            String[] userTypes = {"tech", "creative", "outdoor", "general"};
            
            for (int i = 0; i < userIds.length; i++) {
                String userId = userIds[i];
                String userType = userTypes[i];
                
                // Generate sample interests and preferences
                Map<String, Object> interestData = embeddingService.generateSampleInterests(userType);
                
                @SuppressWarnings("unchecked")
                List<String> interests = (List<String>) interestData.get("interests");
                
                @SuppressWarnings("unchecked")
                Map<String, Integer> preferences = (Map<String, Integer>) interestData.get("preferences");
                
                System.out.println("\n=== Interest Profile for " + userId + " ===");
                System.out.println("Interests: " + interests);
                System.out.println("Preferences: " + preferences);
                
                // Generate embedding from interests and preferences
                System.out.println("\nGenerating embedding...");
                double[] embedding = embeddingService.generateInterestEmbedding(userId, interests, preferences);
                
                System.out.println("Embedding generated successfully!");
                System.out.println("Embedding dimensions: " + embedding.length);
                System.out.println("First 5 values: " + Arrays.toString(Arrays.copyOf(embedding, 5)) + "...");
                
                // Retrieve the embedding from DynamoDB to verify storage
                EnhancedDynamoDBService dynamoDBService = new EnhancedDynamoDBService();
                Map<String, Object> storedData = dynamoDBService.getEmbeddingWithMetadata(
                    userId, 
                    EnhancedDynamoDBService.EMBEDDING_TYPE_INTERESTS
                );
                
                if (storedData != null) {
                    System.out.println("\nSuccessfully retrieved from DynamoDB!");
                    System.out.println("Metadata: " + JsonUtils.toJson(storedData.get("metadata")));
                } else {
                    System.out.println("\nFailed to retrieve from DynamoDB.");
                }
            }
            
            // Demonstrate finding similar users based on interests
            System.out.println("\n=== Finding Similar Users Based on Interests ===");
            String targetUserId = "tech_user";
            Map<String, Double> similarUsers = embeddingService.findSimilarInterests(targetUserId, 3);
            
            System.out.println("Users similar to " + targetUserId + ":");
            for (Map.Entry<String, Double> entry : similarUsers.entrySet()) {
                System.out.println(entry.getKey() + ": " + String.format("%.4f", entry.getValue()));
            }
            
            // Demonstrate comparing two specific users
            System.out.println("\n=== Comparing Two Users ===");
            compareUsers("tech_user", "creative_user");
            
        } catch (Exception e) {
            System.err.println("Error in InterestEmbeddingDemo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Compares two users based on their interest embeddings.
     */
    private static void compareUsers(String userId1, String userId2) throws Exception {
        EnhancedDynamoDBService dynamoDBService = new EnhancedDynamoDBService();
        
        // Get embeddings for both users
        double[] embedding1 = dynamoDBService.getEmbedding(
            userId1, 
            EnhancedDynamoDBService.EMBEDDING_TYPE_INTERESTS
        );
        
        double[] embedding2 = dynamoDBService.getEmbedding(
            userId2, 
            EnhancedDynamoDBService.EMBEDDING_TYPE_INTERESTS
        );
        
        if (embedding1 == null || embedding2 == null) {
            System.out.println("One or both user embeddings not found.");
            return;
        }
        
        // Calculate similarity
        double similarity = calculateCosineSimilarity(embedding1, embedding2);
        
        System.out.println("Similarity between " + userId1 + " and " + userId2 + ": " 
                          + String.format("%.4f", similarity));
        
        // Interpret the similarity
        if (similarity > 0.8) {
            System.out.println("These users have very similar interests!");
        } else if (similarity > 0.5) {
            System.out.println("These users have somewhat similar interests.");
        } else {
            System.out.println("These users have different interests.");
        }
    }
    
    /**
     * Calculates cosine similarity between two embeddings.
     */
    private static double calculateCosineSimilarity(double[] embedding1, double[] embedding2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            norm1 += Math.pow(embedding1[i], 2);
            norm2 += Math.pow(embedding2[i], 2);
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
} 