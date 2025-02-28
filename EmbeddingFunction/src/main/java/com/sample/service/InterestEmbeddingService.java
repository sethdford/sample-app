package com.sample.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sample.model.EmbeddingMetadata;

/**
 * Service for generating embeddings from user interests.
 * This service processes user interests and preferences to create embeddings
 * that represent user interests for matching and recommendation purposes.
 */
public class InterestEmbeddingService {
    
    private final EmbeddingService embeddingService;
    private final EnhancedDynamoDBService dynamoDBService;
    
    // Constants for interest categories
    private static final List<String> INTEREST_CATEGORIES = List.of(
        "Technology", "Sports", "Music", "Movies", "Books", "Travel", 
        "Food", "Fashion", "Art", "Science", "Health", "Finance"
    );
    
    public InterestEmbeddingService() {
        this.embeddingService = new EmbeddingService();
        this.dynamoDBService = new EnhancedDynamoDBService();
    }
    
    /**
     * Generates an embedding from user interests.
     * 
     * @param userId User ID
     * @param interests List of user interests
     * @param preferences Map of user preferences (category to preference level)
     * @return The generated embedding
     */
    public double[] generateInterestEmbedding(String userId, List<String> interests, 
                                             Map<String, Integer> preferences) throws Exception {
        // 1. Process interests and preferences into a text format
        String processedText = processInterestsAndPreferences(interests, preferences);
        
        // 2. Generate embedding using the processed text
        long startTime = System.currentTimeMillis();
        double[] embedding = embeddingService.generateEmbedding(processedText);
        long endTime = System.currentTimeMillis();
        
        // 3. Create metadata
        EmbeddingMetadata metadata = new EmbeddingMetadata("interests", "amazon.titan-embed-text-v1");
        metadata.addEncodingDetail("interest_count", interests.size());
        metadata.addEncodingDetail("preference_count", preferences.size());
        metadata.addPerformanceMetric("generation_time_ms", endTime - startTime);
        
        // 4. Store in DynamoDB with metadata
        dynamoDBService.storeEmbeddingWithMetadata(
            userId, 
            EnhancedDynamoDBService.EMBEDDING_TYPE_INTERESTS, 
            embedding, 
            metadata
        );
        
        return embedding;
    }
    
    /**
     * Process interests and preferences into a format suitable for embedding generation.
     * 
     * @param interests List of user interests
     * @param preferences Map of user preferences
     * @return Processed text representation
     */
    private String processInterestsAndPreferences(List<String> interests, Map<String, Integer> preferences) {
        StringBuilder processedText = new StringBuilder("User interests: ");
        
        // Process interests
        if (interests != null && !interests.isEmpty()) {
            processedText.append("Interested in ");
            processedText.append(String.join(", ", interests));
            processedText.append(". ");
        }
        
        // Process preferences by category
        if (preferences != null && !preferences.isEmpty()) {
            processedText.append("Preferences: ");
            
            List<String> preferenceTexts = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : preferences.entrySet()) {
                String category = entry.getKey();
                int level = entry.getValue();
                
                String preferenceLevel;
                if (level >= 8) {
                    preferenceLevel = "loves";
                } else if (level >= 5) {
                    preferenceLevel = "likes";
                } else if (level >= 3) {
                    preferenceLevel = "somewhat interested in";
                } else {
                    preferenceLevel = "slightly interested in";
                }
                
                preferenceTexts.add(preferenceLevel + " " + category);
            }
            
            processedText.append(String.join(", ", preferenceTexts));
            processedText.append(".");
        }
        
        return processedText.toString();
    }
    
    /**
     * Finds users with similar interests.
     * 
     * @param targetUserId User ID to find similar users for
     * @param maxResults Maximum number of results to return
     * @return Map of user IDs to similarity scores
     */
    public Map<String, Double> findSimilarInterests(String userId, int maxResults) throws Exception {
        // Get the target user's embedding
        double[] targetEmbedding = dynamoDBService.getEmbedding(
            userId, 
            EnhancedDynamoDBService.EMBEDDING_TYPE_INTERESTS
        );
        
        if (targetEmbedding == null) {
            throw new IllegalArgumentException("No interest embedding found for user: " + userId);
        }
        
        // TODO: In a real implementation, we would query a database of users
        // For this example, we'll just return a placeholder
        Map<String, Double> similarUsers = new HashMap<>();
        similarUsers.put("user123", 0.92);
        similarUsers.put("user456", 0.87);
        similarUsers.put("user789", 0.75);
        
        return similarUsers;
    }
    
    /**
     * Generates sample interests for demo purposes.
     * 
     * @param userType Type of user to generate interests for
     * @return Map containing interests and preferences
     */
    public Map<String, Object> generateSampleInterests(String userType) {
        Map<String, Object> result = new HashMap<>();
        List<String> interests = new ArrayList<>();
        Map<String, Integer> preferences = new HashMap<>();
        
        switch (userType) {
            case "tech":
                interests.add("Programming");
                interests.add("Artificial Intelligence");
                interests.add("Blockchain");
                interests.add("Virtual Reality");
                interests.add("Robotics");
                
                preferences.put("Technology", 9);
                preferences.put("Science", 7);
                preferences.put("Finance", 5);
                preferences.put("Movies", 6);
                break;
                
            case "creative":
                interests.add("Photography");
                interests.add("Graphic Design");
                interests.add("Music Production");
                interests.add("Creative Writing");
                interests.add("Film Making");
                
                preferences.put("Art", 9);
                preferences.put("Music", 8);
                preferences.put("Movies", 8);
                preferences.put("Books", 7);
                break;
                
            case "outdoor":
                interests.add("Hiking");
                interests.add("Camping");
                interests.add("Mountain Biking");
                interests.add("Rock Climbing");
                interests.add("Kayaking");
                
                preferences.put("Sports", 9);
                preferences.put("Travel", 8);
                preferences.put("Health", 7);
                preferences.put("Food", 6);
                break;
                
            default:
                interests.add("Reading");
                interests.add("Cooking");
                interests.add("Traveling");
                interests.add("Movies");
                interests.add("Music");
                
                preferences.put("Books", 7);
                preferences.put("Food", 8);
                preferences.put("Travel", 9);
                preferences.put("Movies", 6);
        }
        
        result.put("interests", interests);
        result.put("preferences", preferences);
        
        return result;
    }
} 