package com.sample.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sample.model.EmbeddingMetadata;

/**
 * Service for generating embeddings from user behavioral data.
 * This service processes user activity logs, clickstream data, and other behavioral signals
 * to create embeddings that represent user behavior patterns.
 */
public class BehavioralEmbeddingService {
    
    private final EmbeddingService embeddingService;
    private final EnhancedDynamoDBService dynamoDBService;
    
    // Constants for behavioral data types
    private static final String CLICK_EVENT = "click";
    private static final String VIEW_EVENT = "view";
    private static final String PURCHASE_EVENT = "purchase";
    private static final String SEARCH_EVENT = "search";
    
    // Weights for different event types
    private static final Map<String, Double> EVENT_WEIGHTS = Map.of(
        CLICK_EVENT, 1.0,
        VIEW_EVENT, 0.5,
        PURCHASE_EVENT, 3.0,
        SEARCH_EVENT, 2.0
    );
    
    public BehavioralEmbeddingService() {
        this.embeddingService = new EmbeddingService();
        this.dynamoDBService = new EnhancedDynamoDBService();
    }
    
    /**
     * Generates an embedding from user behavioral data.
     * 
     * @param userId User ID
     * @param behavioralData Map of behavioral data including events, timestamps, etc.
     * @return The generated embedding
     */
    public double[] generateBehavioralEmbedding(String userId, Map<String, Object> behavioralData) throws Exception {
        // 1. Process and normalize the behavioral data
        String processedText = processBehavioralData(behavioralData);
        
        // 2. Generate embedding using the processed text
        long startTime = System.currentTimeMillis();
        double[] embedding = embeddingService.generateEmbedding(processedText);
        long endTime = System.currentTimeMillis();
        
        // 3. Create metadata
        EmbeddingMetadata metadata = new EmbeddingMetadata("behavioral", "amazon.titan-embed-text-v1");
        metadata.addEncodingDetail("event_count", countEvents(behavioralData));
        metadata.addEncodingDetail("data_points", behavioralData.size());
        metadata.addPerformanceMetric("generation_time_ms", endTime - startTime);
        
        // 4. Store in DynamoDB with metadata
        dynamoDBService.storeEmbeddingWithMetadata(
            userId, 
            EnhancedDynamoDBService.EMBEDDING_TYPE_BEHAVIOR, 
            embedding, 
            metadata
        );
        
        return embedding;
    }
    
    /**
     * Process behavioral data into a format suitable for embedding generation.
     * 
     * @param behavioralData Map of behavioral data
     * @return Processed text representation
     */
    private String processBehavioralData(Map<String, Object> behavioralData) {
        StringBuilder processedText = new StringBuilder("User behavior: ");
        
        // Process events if present
        if (behavioralData.containsKey("events")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> events = (List<Map<String, Object>>) behavioralData.get("events");
            processEvents(events, processedText);
        }
        
        // Process page views if present
        if (behavioralData.containsKey("page_views")) {
            @SuppressWarnings("unchecked")
            List<String> pageViews = (List<String>) behavioralData.get("page_views");
            processPageViews(pageViews, processedText);
        }
        
        // Process search queries if present
        if (behavioralData.containsKey("search_queries")) {
            @SuppressWarnings("unchecked")
            List<String> searchQueries = (List<String>) behavioralData.get("search_queries");
            processSearchQueries(searchQueries, processedText);
        }
        
        return processedText.toString();
    }
    
    /**
     * Process event data into text.
     */
    private void processEvents(List<Map<String, Object>> events, StringBuilder builder) {
        Map<String, Integer> eventCounts = new HashMap<>();
        
        // Count events by type
        for (Map<String, Object> event : events) {
            String eventType = (String) event.get("type");
            eventCounts.put(eventType, eventCounts.getOrDefault(eventType, 0) + 1);
        }
        
        // Add event summary to the text
        builder.append("Performed ");
        List<String> eventDescriptions = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : eventCounts.entrySet()) {
            eventDescriptions.add(entry.getValue() + " " + entry.getKey() + " events");
        }
        
        builder.append(String.join(", ", eventDescriptions));
        builder.append(". ");
        
        // Add details about the most recent events (up to 5)
        int recentEventCount = Math.min(5, events.size());
        if (recentEventCount > 0) {
            builder.append("Recent activities: ");
            
            for (int i = 0; i < recentEventCount; i++) {
                Map<String, Object> event = events.get(i);
                String eventType = (String) event.get("type");
                String itemId = (String) event.get("item_id");
                String category = (String) event.get("category");
                
                builder.append(eventType)
                       .append(" ")
                       .append(category)
                       .append(" item ")
                       .append(itemId);
                
                if (i < recentEventCount - 1) {
                    builder.append(", ");
                }
            }
            
            builder.append(". ");
        }
    }
    
    /**
     * Process page view data into text.
     */
    private void processPageViews(List<String> pageViews, StringBuilder builder) {
        if (pageViews.isEmpty()) {
            return;
        }
        
        builder.append("Viewed pages: ");
        int viewCount = Math.min(5, pageViews.size());
        
        for (int i = 0; i < viewCount; i++) {
            builder.append(pageViews.get(i));
            
            if (i < viewCount - 1) {
                builder.append(", ");
            }
        }
        
        if (pageViews.size() > viewCount) {
            builder.append(" and ").append(pageViews.size() - viewCount).append(" more");
        }
        
        builder.append(". ");
    }
    
    /**
     * Process search query data into text.
     */
    private void processSearchQueries(List<String> searchQueries, StringBuilder builder) {
        if (searchQueries.isEmpty()) {
            return;
        }
        
        builder.append("Searched for: ");
        int queryCount = Math.min(3, searchQueries.size());
        
        for (int i = 0; i < queryCount; i++) {
            builder.append("\"").append(searchQueries.get(i)).append("\"");
            
            if (i < queryCount - 1) {
                builder.append(", ");
            }
        }
        
        if (searchQueries.size() > queryCount) {
            builder.append(" and ").append(searchQueries.size() - queryCount).append(" more queries");
        }
        
        builder.append(". ");
    }
    
    /**
     * Count the total number of events in the behavioral data.
     */
    private int countEvents(Map<String, Object> behavioralData) {
        if (!behavioralData.containsKey("events")) {
            return 0;
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> events = (List<Map<String, Object>>) behavioralData.get("events");
        return events.size();
    }
} 