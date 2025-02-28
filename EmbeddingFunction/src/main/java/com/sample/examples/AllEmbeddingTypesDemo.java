package com.sample.examples;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sample.model.EmbeddingMetadata;
import com.sample.service.BehavioralEmbeddingService;
import com.sample.service.CloudWatchLogEmbeddingService;
import com.sample.service.EmbeddingService;
import com.sample.service.EmbeddingServiceFactory;
import com.sample.service.EnhancedDynamoDBService;
import com.sample.service.FinancialInterestEmbeddingService;
import com.sample.service.InterestEmbeddingService;
import com.sample.util.JsonUtils;
import com.sample.util.UserAttributeFormatter;

/**
 * Comprehensive demo that showcases all embedding types and approaches.
 * This demo creates embeddings for the same user using different data sources and methods,
 * then compares the results to show how different embedding approaches capture different aspects
 * of user data.
 */
public class AllEmbeddingTypesDemo {

    public static void main(String[] args) {
        try {
            System.out.println("=== AWS Bedrock Embedding Types Demo ===");
            System.out.println("This demo showcases different approaches to generating embeddings for user data.\n");
            
            // Get services from factory
            EmbeddingServiceFactory factory = EmbeddingServiceFactory.getInstance();
            EmbeddingService embeddingService = factory.getEmbeddingService();
            BehavioralEmbeddingService behavioralService = factory.getBehavioralEmbeddingService();
            InterestEmbeddingService interestService = factory.getInterestEmbeddingService();
            FinancialInterestEmbeddingService financialService = factory.getFinancialInterestEmbeddingService();
            CloudWatchLogEmbeddingService cloudWatchService = factory.getCloudWatchLogEmbeddingService();
            EnhancedDynamoDBService dynamoDBService = factory.getDynamoDBService();
            
            // Create a test user
            String userId = "demo_user_" + System.currentTimeMillis();
            System.out.println("Creating embeddings for user: " + userId + "\n");
            
            // 1. Raw Text Embedding
            System.out.println("=== 1. Raw Text Embedding ===");
            String rawText = "I'm a software developer interested in machine learning, cloud computing, and hiking on weekends.";
            System.out.println("Raw text: " + rawText);
            
            double[] rawTextEmbedding = embeddingService.generateEmbedding(rawText);
            
            // Store with metadata
            EmbeddingMetadata rawTextMetadata = new EmbeddingMetadata("raw_text", "amazon.titan-embed-text-v1");
            rawTextMetadata.addEncodingDetail("text_length", rawText.length());
            rawTextMetadata.addEncodingDetail("word_count", rawText.split("\\s+").length);
            
            dynamoDBService.storeEmbeddingWithMetadata(
                userId, 
                EnhancedDynamoDBService.EMBEDDING_TYPE_RAW_TEXT, 
                rawTextEmbedding, 
                rawTextMetadata
            );
            
            System.out.println("Raw text embedding generated and stored.");
            System.out.println("Dimensions: " + rawTextEmbedding.length);
            System.out.println("First 5 values: " + Arrays.toString(Arrays.copyOf(rawTextEmbedding, 5)) + "...\n");
            
            // 2. User Attributes Embedding
            System.out.println("=== 2. User Attributes Embedding ===");
            Map<String, Object> userAttributes = new HashMap<>();
            userAttributes.put("user_id", userId);
            userAttributes.put("last_login", "2023-02-24T12:30:00Z");
            userAttributes.put("errors_encountered", Arrays.asList("404", "500"));
            userAttributes.put("page_views", 25);
            userAttributes.put("subscription_plan", "Premium");
            userAttributes.put("device", "Desktop");
            
            System.out.println("User attributes: " + JsonUtils.toJson(userAttributes));
            
            // Process with UserAttributeFormatter
            UserAttributeFormatter formatter = new UserAttributeFormatter();
            Map<String, Object> encodedAttributes = formatter.encodeAttributes(userAttributes);
            String attributesPrompt = formatter.createTitanPrompt(encodedAttributes);
            
            System.out.println("Encoded attributes: " + JsonUtils.toJson(encodedAttributes));
            System.out.println("Titan prompt: " + attributesPrompt);
            
            double[] attributesEmbedding = embeddingService.generateEmbedding(attributesPrompt);
            
            // Store with metadata
            EmbeddingMetadata attributesMetadata = new EmbeddingMetadata("user_attributes", "amazon.titan-embed-text-v1");
            attributesMetadata.addEncodingDetail("attribute_count", userAttributes.size());
            attributesMetadata.addEncodingDetail("encoded_attributes", encodedAttributes);
            
            dynamoDBService.storeEmbeddingWithMetadata(
                userId, 
                EnhancedDynamoDBService.EMBEDDING_TYPE_USER_ATTRIBUTES, 
                attributesEmbedding, 
                attributesMetadata
            );
            
            System.out.println("User attributes embedding generated and stored.");
            System.out.println("Dimensions: " + attributesEmbedding.length);
            System.out.println("First 5 values: " + Arrays.toString(Arrays.copyOf(attributesEmbedding, 5)) + "...\n");
            
            // 3. Behavioral Embedding
            System.out.println("=== 3. Behavioral Embedding ===");
            Map<String, Object> behavioralData = generateSampleBehavioralData();
            System.out.println("Behavioral data sample: " + JsonUtils.toJson(behavioralData));
            
            double[] behavioralEmbedding = behavioralService.generateBehavioralEmbedding(userId, behavioralData);
            
            System.out.println("Behavioral embedding generated and stored.");
            System.out.println("Dimensions: " + behavioralEmbedding.length);
            System.out.println("First 5 values: " + Arrays.toString(Arrays.copyOf(behavioralEmbedding, 5)) + "...\n");
            
            // 4. Interest Embedding
            System.out.println("=== 4. Interest Embedding ===");
            Map<String, Object> interestData = interestService.generateSampleInterests("tech");
            
            @SuppressWarnings("unchecked")
            List<String> interests = (List<String>) interestData.get("interests");
            
            @SuppressWarnings("unchecked")
            Map<String, Integer> preferences = (Map<String, Integer>) interestData.get("preferences");
            
            System.out.println("Interests: " + interests);
            System.out.println("Preferences: " + preferences);
            
            double[] interestEmbedding = interestService.generateInterestEmbedding(userId, interests, preferences);
            
            System.out.println("Interest embedding generated and stored.");
            System.out.println("Dimensions: " + interestEmbedding.length);
            System.out.println("First 5 values: " + Arrays.toString(Arrays.copyOf(interestEmbedding, 5)) + "...\n");
            
            // 5. Financial Profile Embedding
            System.out.println("=== 5. Financial Profile Embedding ===");
            Map<String, Object> financialProfileData = financialService.generateSampleFinancialProfile("mid_career_family");
            
            @SuppressWarnings("unchecked")
            List<String> financialInterests = (List<String>) financialProfileData.get("financial_interests");
            
            @SuppressWarnings("unchecked")
            List<String> lifeEvents = (List<String>) financialProfileData.get("life_events");
            
            @SuppressWarnings("unchecked")
            List<String> wellnessConcerns = (List<String>) financialProfileData.get("wellness_concerns");
            
            int riskTolerance = (int) financialProfileData.get("risk_tolerance");
            int timeHorizon = (int) financialProfileData.get("time_horizon");
            
            System.out.println("Financial Interests: " + financialInterests);
            System.out.println("Life Events: " + lifeEvents);
            System.out.println("Wellness Concerns: " + wellnessConcerns);
            System.out.println("Risk Tolerance: " + riskTolerance);
            System.out.println("Time Horizon: " + timeHorizon);
            
            double[] financialEmbedding = financialService.generateFinancialEmbedding(
                userId, 
                financialInterests, 
                lifeEvents, 
                wellnessConcerns, 
                riskTolerance, 
                timeHorizon
            );
            
            System.out.println("Financial profile embedding generated and stored.");
            System.out.println("Dimensions: " + financialEmbedding.length);
            System.out.println("First 5 values: " + Arrays.toString(Arrays.copyOf(financialEmbedding, 5)) + "...\n");
            
            // Generate CloudWatch logs embedding
            System.out.println("\n=== CloudWatch Logs Embedding ===");
            String userId5 = "user_logs_" + System.currentTimeMillis();
            List<String> sampleLogs = cloudWatchService.generateSampleLogs(userId5, CloudWatchLogEmbeddingService.LOG_TYPE_API_GATEWAY, 50);
            sampleLogs.addAll(cloudWatchService.generateSampleLogs(userId5, CloudWatchLogEmbeddingService.LOG_TYPE_ERROR, 5));
            System.out.println("Generated " + sampleLogs.size() + " sample logs");
            System.out.println("Sample log: " + sampleLogs.get(0));
            
            // Simulate embedding generation
            double[] logsEmbedding = simulateLogsEmbedding(cloudWatchService, userId5);
            System.out.println("CloudWatch logs embedding generated with " + logsEmbedding.length + " dimensions");
            
            // Generate client effort embedding
            System.out.println("\n=== Client Effort Embedding ===");
            String userId6 = "user_effort_" + System.currentTimeMillis();
            List<String> highEffortLogs = cloudWatchService.generateHighEffortLogs(userId6, 4, 100);
            System.out.println("Generated " + highEffortLogs.size() + " high effort logs");
            System.out.println("Sample high effort log: " + highEffortLogs.get(0));
            
            // Simulate client effort embedding generation
            double[] effortEmbedding = simulateClientEffortEmbedding(cloudWatchService, userId6);
            System.out.println("Client effort embedding generated with " + effortEmbedding.length + " dimensions");
            
            // Compare all embedding types
            System.out.println("\n=== Comparing All Embedding Types ===");
            compareEmbeddings(
                new String[]{"Raw Text", "User Attributes", "Behavioral", "Interests", "Financial Profile", "CloudWatch Logs", "Client Effort"},
                new double[][]{rawTextEmbedding, attributesEmbedding, behavioralEmbedding, interestEmbedding, financialEmbedding, logsEmbedding, effortEmbedding}
            );
            
            // 7. Retrieve all embedding types for the user
            System.out.println("\n=== 7. All Embedding Types for User ===");
            List<String> embeddingTypes = dynamoDBService.getUserEmbeddingTypes(userId);
            System.out.println("Embedding types stored for user " + userId + ": " + embeddingTypes);
            
            // 8. Financial product recommendations
            System.out.println("\n=== 8. Financial Product Recommendations ===");
            Map<String, Double> recommendations = financialService.recommendFinancialProducts(userId);
            
            System.out.println("Recommended financial products:");
            for (Map.Entry<String, Double> entry : recommendations.entrySet()) {
                System.out.println(entry.getKey() + ": " + String.format("%.2f%%", entry.getValue() * 100) + " relevance");
            }
            
        } catch (Exception e) {
            System.err.println("Error in AllEmbeddingTypesDemo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generates sample behavioral data for demo purposes.
     */
    private static Map<String, Object> generateSampleBehavioralData() {
        Map<String, Object> behavioralData = new HashMap<>();
        
        // Add events
        List<Map<String, Object>> events = Arrays.asList(
            createEvent("click", "technology", "cloud-article"),
            createEvent("view", "technology", "ml-tutorial"),
            createEvent("search", "technology", "aws bedrock examples"),
            createEvent("click", "outdoor", "hiking-trails")
        );
        behavioralData.put("events", events);
        
        // Add page views
        List<String> pageViews = Arrays.asList(
            "/technology/cloud",
            "/technology/machine-learning",
            "/outdoor/hiking",
            "/account/settings"
        );
        behavioralData.put("page_views", pageViews);
        
        // Add search queries
        List<String> searchQueries = Arrays.asList(
            "aws bedrock examples",
            "machine learning tutorials",
            "best hiking trails"
        );
        behavioralData.put("search_queries", searchQueries);
        
        return behavioralData;
    }
    
    /**
     * Creates a sample event for behavioral data.
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
     * Compares all embedding types for a user.
     */
    private static void compareEmbeddings(String[] types, double[][] embeddings) throws Exception {
        // Compare each pair
        System.out.println("Similarity matrix between embedding types:");
        System.out.println("--------------------------------------------");
        System.out.printf("%-20s", "");
        for (String type : types) {
            System.out.printf("%-20s", type);
        }
        System.out.println();
        
        for (int i = 0; i < embeddings.length; i++) {
            System.out.printf("%-20s", types[i]);
            for (int j = 0; j < embeddings.length; j++) {
                double similarity = calculateCosineSimilarity(embeddings[i], embeddings[j]);
                System.out.printf("%-20s", String.format("%.4f", similarity));
            }
            System.out.println();
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
    
    /**
     * Simulates generating a CloudWatch logs embedding.
     */
    private static double[] simulateLogsEmbedding(CloudWatchLogEmbeddingService service, String userId) {
        try {
            // In a real scenario, this would call the actual CloudWatch logs
            // For demo, we'll generate a mock embedding
            double[] mockEmbedding = new double[1536];
            for (int i = 0; i < mockEmbedding.length; i++) {
                mockEmbedding[i] = (Math.sin(i * 0.1) * 0.5) + (Math.random() * 0.01);
            }
            
            // Create metadata
            EmbeddingMetadata metadata = new EmbeddingMetadata(
                EnhancedDynamoDBService.EMBEDDING_TYPE_CLOUDWATCH_LOGS, 
                "amazon.titan-embed-text-v1"
            );
            metadata.addEncodingDetail("log_count", 55);
            metadata.addEncodingDetail("error_count", 5);
            
            // Store in DynamoDB
            EnhancedDynamoDBService dynamoDBService = new EnhancedDynamoDBService();
            dynamoDBService.storeEmbeddingWithMetadata(
                userId, 
                EnhancedDynamoDBService.EMBEDDING_TYPE_CLOUDWATCH_LOGS, 
                mockEmbedding, 
                metadata
            );
            
            return mockEmbedding;
        } catch (Exception e) {
            System.err.println("Error simulating logs embedding: " + e.getMessage());
            return new double[1536]; // Return empty embedding on error
        }
    }
    
    /**
     * Simulates generating a client effort embedding.
     */
    private static double[] simulateClientEffortEmbedding(CloudWatchLogEmbeddingService service, String userId) {
        try {
            // In a real scenario, this would analyze actual logs
            // For demo, we'll generate a mock embedding
            double[] mockEmbedding = new double[1536];
            for (int i = 0; i < mockEmbedding.length; i++) {
                mockEmbedding[i] = (Math.cos(i * 0.1) * 0.5) + (Math.random() * 0.01);
            }
            
            // Create metadata with effort metrics
            EmbeddingMetadata metadata = new EmbeddingMetadata(
                EnhancedDynamoDBService.EMBEDDING_TYPE_CLIENT_EFFORT, 
                "amazon.titan-embed-text-v1"
            );
            metadata.addEncodingDetail("effort_error_count", 12);
            metadata.addEncodingDetail("effort_repeated_click_count", 8);
            metadata.addEncodingDetail("effort_back_forth_navigation_count", 15);
            metadata.addEncodingDetail("effort_channel_switch_count", 3);
            metadata.addEncodingDetail("effort_effort_score", 68.5);
            metadata.addEncodingDetail("effort_high_error_rate", "true");
            metadata.addEncodingDetail("effort_high_repeated_clicks", "true");
            metadata.addEncodingDetail("effort_high_back_forth_navigation", "true");
            metadata.addEncodingDetail("effort_high_channel_switching", "true");
            metadata.addEncodingDetail("effort_high_effort", "true");
            
            // Store in DynamoDB
            EnhancedDynamoDBService dynamoDBService = new EnhancedDynamoDBService();
            dynamoDBService.storeEmbeddingWithMetadata(
                userId, 
                EnhancedDynamoDBService.EMBEDDING_TYPE_CLIENT_EFFORT, 
                mockEmbedding, 
                metadata
            );
            
            // Display client effort analysis
            System.out.println("Client Effort Analysis:");
            System.out.println("- Overall Effort Score: 68.5/100 (HIGH EFFORT)");
            System.out.println("- Error Count: 12");
            System.out.println("- Repeated Button Clicks: 8");
            System.out.println("- Back-and-Forth Navigation: 15");
            System.out.println("- Channel Switches: 3");
            System.out.println("Recommendations:");
            System.out.println("- Improve error handling and recovery paths");
            System.out.println("- Enhance UI responsiveness for button feedback");
            System.out.println("- Simplify navigation flow to reduce back-and-forth");
            System.out.println("- Ensure consistent experience across channels");
            
            return mockEmbedding;
        } catch (Exception e) {
            System.err.println("Error simulating client effort embedding: " + e.getMessage());
            return new double[1536]; // Return empty embedding on error
        }
    }
} 