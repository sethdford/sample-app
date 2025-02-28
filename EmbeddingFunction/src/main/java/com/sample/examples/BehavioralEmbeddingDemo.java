package com.sample.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sample.service.BehavioralEmbeddingService;
import com.sample.service.EnhancedDynamoDBService;
import com.sample.util.JsonUtils;

/**
 * Demonstrates how to use the BehavioralEmbeddingService to generate embeddings from user behavioral data.
 */
public class BehavioralEmbeddingDemo {

    public static void main(String[] args) {
        try {
            // Initialize the service
            BehavioralEmbeddingService embeddingService = new BehavioralEmbeddingService();
            
            // Create sample users with behavioral data
            String[] userIds = {"user1", "user2", "user3"};
            
            for (String userId : userIds) {
                // Generate sample behavioral data
                Map<String, Object> behavioralData = generateSampleBehavioralData(userId);
                
                System.out.println("\n=== Behavioral Data for " + userId + " ===");
                System.out.println(JsonUtils.toJson(behavioralData));
                
                // Generate embedding from behavioral data
                System.out.println("\nGenerating embedding...");
                double[] embedding = embeddingService.generateBehavioralEmbedding(userId, behavioralData);
                
                System.out.println("Embedding generated successfully!");
                System.out.println("Embedding dimensions: " + embedding.length);
                System.out.println("First 5 values: " + Arrays.toString(Arrays.copyOf(embedding, 5)) + "...");
                
                // Retrieve the embedding from DynamoDB to verify storage
                EnhancedDynamoDBService dynamoDBService = new EnhancedDynamoDBService();
                Map<String, Object> storedData = dynamoDBService.getEmbeddingWithMetadata(
                    userId, 
                    EnhancedDynamoDBService.EMBEDDING_TYPE_BEHAVIOR
                );
                
                if (storedData != null) {
                    System.out.println("\nSuccessfully retrieved from DynamoDB!");
                    System.out.println("Metadata: " + JsonUtils.toJson(storedData.get("metadata")));
                } else {
                    System.out.println("\nFailed to retrieve from DynamoDB.");
                }
            }
            
            // Demonstrate finding similar users based on behavioral embeddings
            System.out.println("\n=== Finding Similar Users Based on Behavior ===");
            findSimilarUsers("user1", userIds);
            
        } catch (Exception e) {
            System.err.println("Error in BehavioralEmbeddingDemo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generates sample behavioral data for a user.
     */
    private static Map<String, Object> generateSampleBehavioralData(String userId) {
        Map<String, Object> behavioralData = new HashMap<>();
        
        // Generate events based on user ID
        List<Map<String, Object>> events = new ArrayList<>();
        
        if (userId.equals("user1")) {
            // Tech enthusiast
            events.add(createEvent("click", "electronics", "laptop-15"));
            events.add(createEvent("view", "electronics", "smartphone-x"));
            events.add(createEvent("search", "electronics", "best gaming laptop"));
            events.add(createEvent("purchase", "electronics", "wireless-earbuds"));
            events.add(createEvent("click", "electronics", "tablet-pro"));
        } else if (userId.equals("user2")) {
            // Fashion enthusiast
            events.add(createEvent("click", "clothing", "summer-dress"));
            events.add(createEvent("view", "accessories", "leather-bag"));
            events.add(createEvent("search", "fashion", "trending shoes 2023"));
            events.add(createEvent("purchase", "clothing", "jeans-slim"));
            events.add(createEvent("click", "accessories", "sunglasses"));
        } else {
            // Home decor enthusiast
            events.add(createEvent("click", "home", "sofa-sectional"));
            events.add(createEvent("view", "kitchen", "coffee-maker"));
            events.add(createEvent("search", "home", "minimalist decor"));
            events.add(createEvent("purchase", "home", "area-rug"));
            events.add(createEvent("click", "garden", "outdoor-lights"));
        }
        
        behavioralData.put("events", events);
        
        // Add page views
        List<String> pageViews = new ArrayList<>();
        if (userId.equals("user1")) {
            pageViews.add("/electronics");
            pageViews.add("/deals/tech");
            pageViews.add("/product/laptop-15");
            pageViews.add("/blog/tech-trends");
        } else if (userId.equals("user2")) {
            pageViews.add("/clothing/women");
            pageViews.add("/new-arrivals");
            pageViews.add("/product/summer-dress");
            pageViews.add("/blog/fashion-week");
        } else {
            pageViews.add("/home-decor");
            pageViews.add("/inspiration/living-room");
            pageViews.add("/product/sofa-sectional");
            pageViews.add("/blog/interior-design");
        }
        
        behavioralData.put("page_views", pageViews);
        
        // Add search queries
        List<String> searchQueries = new ArrayList<>();
        if (userId.equals("user1")) {
            searchQueries.add("best gaming laptop");
            searchQueries.add("wireless earbuds review");
            searchQueries.add("smartphone comparison");
        } else if (userId.equals("user2")) {
            searchQueries.add("trending shoes 2023");
            searchQueries.add("summer fashion");
            searchQueries.add("designer bags sale");
        } else {
            searchQueries.add("minimalist decor");
            searchQueries.add("modern living room ideas");
            searchQueries.add("kitchen organization");
        }
        
        behavioralData.put("search_queries", searchQueries);
        
        return behavioralData;
    }
    
    /**
     * Creates a sample event.
     */
    private static Map<String, Object> createEvent(String type, String category, String itemId) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", type);
        event.put("category", category);
        event.put("item_id", itemId);
        event.put("timestamp", System.currentTimeMillis());
        return event;
    }
    
    /**
     * Finds users with similar behavioral patterns.
     */
    private static void findSimilarUsers(String targetUserId, String[] allUserIds) throws Exception {
        EnhancedDynamoDBService dynamoDBService = new EnhancedDynamoDBService();
        
        // Get the target user's embedding
        double[] targetEmbedding = dynamoDBService.getEmbedding(
            targetUserId, 
            EnhancedDynamoDBService.EMBEDDING_TYPE_BEHAVIOR
        );
        
        if (targetEmbedding == null) {
            System.out.println("Target user embedding not found.");
            return;
        }
        
        // Calculate similarity with other users
        Map<String, Double> similarities = new HashMap<>();
        
        for (String userId : allUserIds) {
            if (userId.equals(targetUserId)) {
                continue;
            }
            
            double[] userEmbedding = dynamoDBService.getEmbedding(
                userId, 
                EnhancedDynamoDBService.EMBEDDING_TYPE_BEHAVIOR
            );
            
            if (userEmbedding != null) {
                double similarity = calculateCosineSimilarity(targetEmbedding, userEmbedding);
                similarities.put(userId, similarity);
            }
        }
        
        // Print results
        System.out.println("Similarity scores for " + targetUserId + ":");
        for (Map.Entry<String, Double> entry : similarities.entrySet()) {
            System.out.println(entry.getKey() + ": " + String.format("%.4f", entry.getValue()));
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