package com.sample.examples;

import java.util.List;
import java.util.Map;

import com.sample.model.EmbeddingMetadata;
import com.sample.service.CloudWatchLogEmbeddingService;
import com.sample.service.EnhancedDynamoDBService;
import com.sample.util.JsonUtils;

/**
 * Demonstrates how to use the CloudWatchLogEmbeddingService to detect client effort
 * in wealth management, brokerage, and financial advice scenarios.
 * This demo focuses on identifying friction points in client journeys related to
 * investment activities, account management, and financial advisory interactions.
 */
public class ClientEffortDemo {

    public static void main(String[] args) {
        try {
            System.out.println("=== Wealth Management Client Effort Demo ===");
            System.out.println("This demo showcases how to detect high client effort in financial services scenarios\n");
            
            // Initialize the service
            CloudWatchLogEmbeddingService service = new CloudWatchLogEmbeddingService();
            EnhancedDynamoDBService dynamoDBService = new EnhancedDynamoDBService();
            
            // Create sample clients with different effort scenarios
            String[] clientIds = {
                "wealth_client_low_effort", 
                "brokerage_client_medium_effort", 
                "advisory_client_high_effort",
                "retirement_client_very_high_effort"
            };
            
            int[] effortLevels = {1, 2, 3, 4};
            
            for (int i = 0; i < clientIds.length; i++) {
                String clientId = clientIds[i];
                int effortLevel = effortLevels[i];
                
                System.out.println("\n=== Processing Client: " + clientId + " ===");
                
                // Generate sample logs with financial services context
                List<String> clientLogs = generateFinancialServiceLogs(service, clientId, effortLevel);
                System.out.println("Generated " + clientLogs.size() + " logs for client journey");
                System.out.println("Sample log: " + clientLogs.get(0));
                
                // Simulate embedding generation and effort analysis
                System.out.println("\nAnalyzing client effort...");
                Map<String, Object> effortAnalysis = simulateClientEffortAnalysis(clientId, effortLevel);
                
                // Display client effort analysis
                displayClientEffortAnalysis(clientId, effortAnalysis);
                
                // Generate and store embedding
                double[] embedding = simulateClientEffortEmbedding(clientId, effortAnalysis);
                System.out.println("Client effort embedding generated with " + embedding.length + " dimensions");
                
                // Retrieve the embedding from DynamoDB to verify storage
                Map<String, Object> storedData = dynamoDBService.getEmbeddingWithMetadata(
                    clientId, 
                    EnhancedDynamoDBService.EMBEDDING_TYPE_CLIENT_EFFORT
                );
                
                if (storedData != null) {
                    System.out.println("\nSuccessfully retrieved from DynamoDB!");
                    System.out.println("Metadata: " + JsonUtils.toJson(storedData.get("metadata")));
                } else {
                    System.out.println("\nFailed to retrieve from DynamoDB.");
                }
            }
            
            // Compare clients based on effort
            System.out.println("\n=== Comparing Clients Based on Effort ===");
            compareClientEffort("wealth_client_low_effort", "advisory_client_high_effort");
            
            // Generate recommendations for high-effort client
            System.out.println("\n=== Recommendations for High-Effort Client ===");
            generateRecommendations("advisory_client_high_effort");
            
        } catch (Exception e) {
            System.err.println("Error in ClientEffortDemo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generates sample logs for financial services scenarios.
     */
    private static List<String> generateFinancialServiceLogs(CloudWatchLogEmbeddingService service, 
                                                           String clientId, int effortLevel) {
        // Use the service's built-in method but add our own context
        List<String> logs = service.generateHighEffortLogs(clientId, effortLevel, 50);
        
        // Add some specific financial service logs
        if (clientId.contains("wealth")) {
            logs.add("[INFO] " + System.currentTimeMillis() + " userId=" + clientId + " path=/wealth/portfolio-review method=GET statusCode=200 responseTime=345ms");
            logs.add("[INFO] " + System.currentTimeMillis() + " userId=" + clientId + " path=/wealth/asset-allocation method=GET statusCode=200 responseTime=456ms");
            logs.add("[INFO] " + System.currentTimeMillis() + " userId=" + clientId + " path=/wealth/advisor-meeting method=POST statusCode=201 responseTime=234ms");
        } else if (clientId.contains("brokerage")) {
            logs.add("[INFO] " + System.currentTimeMillis() + " userId=" + clientId + " path=/trading/stock-order method=POST statusCode=201 responseTime=567ms");
            logs.add("[INFO] " + System.currentTimeMillis() + " userId=" + clientId + " path=/trading/order-status method=GET statusCode=200 responseTime=345ms");
            logs.add("[INFO] " + System.currentTimeMillis() + " userId=" + clientId + " path=/trading/order-confirmation method=GET statusCode=404 responseTime=789ms");
            logs.add("[ERROR] " + System.currentTimeMillis() + " userId=" + clientId + " path=/trading/order-confirmation method=GET message=\"Order not found\" exception=OrderNotFoundException");
        } else if (clientId.contains("advisory")) {
            logs.add("[INFO] " + System.currentTimeMillis() + " userId=" + clientId + " path=/advisory/financial-plan method=GET statusCode=200 responseTime=678ms");
            logs.add("[INFO] " + System.currentTimeMillis() + " userId=" + clientId + " path=/advisory/goal-tracking method=GET statusCode=500 responseTime=1234ms");
            logs.add("[ERROR] " + System.currentTimeMillis() + " userId=" + clientId + " path=/advisory/goal-tracking method=GET message=\"Internal server error\" exception=DatabaseConnectionException");
            logs.add("[INFO] " + System.currentTimeMillis() + " userId=" + clientId + " path=/advisory/goal-tracking method=GET statusCode=200 responseTime=567ms");
        } else if (clientId.contains("retirement")) {
            logs.add("[INFO] " + System.currentTimeMillis() + " userId=" + clientId + " path=/retirement/calculator method=GET statusCode=200 responseTime=456ms");
            logs.add("[INFO] " + System.currentTimeMillis() + " userId=" + clientId + " path=/retirement/contribution method=POST statusCode=400 responseTime=345ms");
            logs.add("[ERROR] " + System.currentTimeMillis() + " userId=" + clientId + " path=/retirement/contribution method=POST message=\"Invalid contribution amount\" exception=ValidationException");
            logs.add("[INFO] " + System.currentTimeMillis() + " userId=" + clientId + " path=/retirement/contribution method=POST statusCode=400 responseTime=345ms");
            logs.add("[ERROR] " + System.currentTimeMillis() + " userId=" + clientId + " path=/retirement/contribution method=POST message=\"Invalid contribution amount\" exception=ValidationException");
            logs.add("[INFO] " + System.currentTimeMillis() + " userId=" + clientId + " path=/help/contact-advisor method=GET statusCode=200 responseTime=234ms");
        }
        
        return logs;
    }
    
    /**
     * Simulates client effort analysis based on logs.
     */
    private static Map<String, Object> simulateClientEffortAnalysis(String clientId, int effortLevel) {
        Map<String, Object> analysis = new java.util.HashMap<>();
        
        // Base metrics that scale with effort level
        int errorCount = effortLevel * 3;
        int repeatedClickCount = effortLevel * 2;
        int backForthCount = effortLevel * 4;
        int channelSwitchCount = effortLevel - 1;
        double effortScore = 20 + (effortLevel * 15); // 35, 50, 65, 80
        
        // Add financial service specific metrics
        if (clientId.contains("wealth")) {
            analysis.put("portfolio_view_count", 5);
            analysis.put("advisor_contact_attempts", 1);
            analysis.put("document_download_failures", 0);
            analysis.put("session_duration_minutes", 12);
            analysis.put("transaction_attempts", 2);
            analysis.put("transaction_completions", 2);
        } else if (clientId.contains("brokerage")) {
            analysis.put("portfolio_view_count", 8);
            analysis.put("advisor_contact_attempts", 0);
            analysis.put("document_download_failures", 1);
            analysis.put("session_duration_minutes", 25);
            analysis.put("transaction_attempts", 4);
            analysis.put("transaction_completions", 3);
            analysis.put("order_status_checks", 6);
        } else if (clientId.contains("advisory")) {
            analysis.put("portfolio_view_count", 3);
            analysis.put("advisor_contact_attempts", 2);
            analysis.put("document_download_failures", 2);
            analysis.put("session_duration_minutes", 35);
            analysis.put("transaction_attempts", 1);
            analysis.put("transaction_completions", 0);
            analysis.put("financial_plan_views", 4);
        } else if (clientId.contains("retirement")) {
            analysis.put("portfolio_view_count", 7);
            analysis.put("advisor_contact_attempts", 3);
            analysis.put("document_download_failures", 3);
            analysis.put("session_duration_minutes", 45);
            analysis.put("transaction_attempts", 5);
            analysis.put("transaction_completions", 2);
            analysis.put("calculator_uses", 8);
        }
        
        // Common metrics
        analysis.put("error_count", errorCount);
        analysis.put("repeated_click_count", repeatedClickCount);
        analysis.put("back_forth_navigation_count", backForthCount);
        analysis.put("channel_switch_count", channelSwitchCount);
        analysis.put("effort_score", effortScore);
        
        // Boolean flags
        analysis.put("high_error_rate", errorCount > 5);
        analysis.put("high_repeated_clicks", repeatedClickCount > 5);
        analysis.put("high_back_forth_navigation", backForthCount > 10);
        analysis.put("high_channel_switching", channelSwitchCount > 1);
        analysis.put("high_effort", effortScore > 50);
        
        return analysis;
    }
    
    /**
     * Displays client effort analysis in a readable format.
     */
    private static void displayClientEffortAnalysis(String clientId, Map<String, Object> analysis) {
        double effortScore = (double) analysis.get("effort_score");
        String effortLevel;
        
        if (effortScore < 40) {
            effortLevel = "LOW EFFORT";
        } else if (effortScore < 55) {
            effortLevel = "MEDIUM EFFORT";
        } else if (effortScore < 70) {
            effortLevel = "HIGH EFFORT";
        } else {
            effortLevel = "VERY HIGH EFFORT";
        }
        
        System.out.println("\nClient Effort Analysis for " + clientId + ":");
        System.out.println("- Overall Effort Score: " + effortScore + "/100 (" + effortLevel + ")");
        System.out.println("- Error Count: " + analysis.get("error_count"));
        System.out.println("- Repeated Button Clicks: " + analysis.get("repeated_click_count"));
        System.out.println("- Back-and-Forth Navigation: " + analysis.get("back_forth_navigation_count"));
        System.out.println("- Channel Switches: " + analysis.get("channel_switch_count"));
        
        // Display financial service specific metrics
        if (analysis.containsKey("portfolio_view_count")) {
            System.out.println("- Portfolio View Count: " + analysis.get("portfolio_view_count"));
        }
        if (analysis.containsKey("advisor_contact_attempts")) {
            System.out.println("- Advisor Contact Attempts: " + analysis.get("advisor_contact_attempts"));
        }
        if (analysis.containsKey("document_download_failures")) {
            System.out.println("- Document Download Failures: " + analysis.get("document_download_failures"));
        }
        if (analysis.containsKey("transaction_attempts")) {
            int attempts = (int) analysis.get("transaction_attempts");
            int completions = (int) analysis.get("transaction_completions");
            double successRate = attempts > 0 ? (double) completions / attempts * 100 : 0;
            System.out.println("- Transaction Success Rate: " + String.format("%.1f", successRate) + "% (" 
                              + completions + "/" + attempts + ")");
        }
        if (analysis.containsKey("order_status_checks")) {
            System.out.println("- Order Status Checks: " + analysis.get("order_status_checks"));
        }
        if (analysis.containsKey("financial_plan_views")) {
            System.out.println("- Financial Plan Views: " + analysis.get("financial_plan_views"));
        }
        if (analysis.containsKey("calculator_uses")) {
            System.out.println("- Calculator Uses: " + analysis.get("calculator_uses"));
        }
        
        System.out.println("- Session Duration: " + analysis.get("session_duration_minutes") + " minutes");
    }
    
    /**
     * Simulates generating a client effort embedding.
     */
    private static double[] simulateClientEffortEmbedding(String clientId, Map<String, Object> effortAnalysis) throws Exception {
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
        
        // Add all analysis metrics to metadata
        for (Map.Entry<String, Object> entry : effortAnalysis.entrySet()) {
            metadata.addEncodingDetail("effort_" + entry.getKey(), entry.getValue());
        }
        
        // Store in DynamoDB
        EnhancedDynamoDBService dynamoDBService = new EnhancedDynamoDBService();
        dynamoDBService.storeEmbeddingWithMetadata(
            clientId, 
            EnhancedDynamoDBService.EMBEDDING_TYPE_CLIENT_EFFORT, 
            mockEmbedding, 
            metadata
        );
        
        return mockEmbedding;
    }
    
    /**
     * Compares two clients based on their effort embeddings.
     */
    private static void compareClientEffort(String clientId1, String clientId2) throws Exception {
        EnhancedDynamoDBService dynamoDBService = new EnhancedDynamoDBService();
        
        // Get embeddings for both clients
        double[] embedding1 = dynamoDBService.getEmbedding(clientId1, EnhancedDynamoDBService.EMBEDDING_TYPE_CLIENT_EFFORT);
        double[] embedding2 = dynamoDBService.getEmbedding(clientId2, EnhancedDynamoDBService.EMBEDDING_TYPE_CLIENT_EFFORT);
        
        if (embedding1 == null || embedding2 == null) {
            System.out.println("One or both client embeddings not found.");
            return;
        }
        
        // Calculate similarity
        double similarity = calculateCosineSimilarity(embedding1, embedding2);
        
        System.out.println("Effort similarity between " + clientId1 + " and " + clientId2 + ": " 
                          + String.format("%.2f%%", similarity * 100));
        
        // Get metadata for both clients
        Map<String, Object> metadata1 = dynamoDBService.getEmbeddingWithMetadata(
            clientId1, EnhancedDynamoDBService.EMBEDDING_TYPE_CLIENT_EFFORT);
        
        Map<String, Object> metadata2 = dynamoDBService.getEmbeddingWithMetadata(
            clientId2, EnhancedDynamoDBService.EMBEDDING_TYPE_CLIENT_EFFORT);
        
        if (metadata1 != null && metadata2 != null) {
            @SuppressWarnings("unchecked")
            EmbeddingMetadata effortMetadata1 = (EmbeddingMetadata) metadata1.get("metadata");
            
            @SuppressWarnings("unchecked")
            EmbeddingMetadata effortMetadata2 = (EmbeddingMetadata) metadata2.get("metadata");
            
            double score1 = (double) effortMetadata1.getEncodingDetails().get("effort_effort_score");
            double score2 = (double) effortMetadata2.getEncodingDetails().get("effort_effort_score");
            
            System.out.println("\nEffort Score Comparison:");
            System.out.println(clientId1 + ": " + score1 + "/100");
            System.out.println(clientId2 + ": " + score2 + "/100");
            System.out.println("Difference: " + String.format("%.1f", Math.abs(score1 - score2)) + " points");
            
            if (score1 < score2) {
                System.out.println("\nInsight: " + clientId1 + " has a significantly better experience than " + clientId2);
                System.out.println("Recommendation: Analyze " + clientId2 + "'s journey to identify and address friction points.");
            } else if (score1 > score2) {
                System.out.println("\nInsight: " + clientId2 + " has a significantly better experience than " + clientId1);
                System.out.println("Recommendation: Analyze " + clientId1 + "'s journey to identify and address friction points.");
            } else {
                System.out.println("\nInsight: Both clients have similar effort levels in their journeys.");
            }
        }
    }
    
    /**
     * Generates recommendations for improving client experience.
     */
    private static void generateRecommendations(String clientId) throws Exception {
        EnhancedDynamoDBService dynamoDBService = new EnhancedDynamoDBService();
        
        // Get client metadata
        Map<String, Object> metadata = dynamoDBService.getEmbeddingWithMetadata(
            clientId, EnhancedDynamoDBService.EMBEDDING_TYPE_CLIENT_EFFORT);
        
        if (metadata == null) {
            System.out.println("Client metadata not found.");
            return;
        }
        
        @SuppressWarnings("unchecked")
        EmbeddingMetadata effortMetadata = (EmbeddingMetadata) metadata.get("metadata");
        Map<String, Object> details = effortMetadata.getEncodingDetails();
        
        System.out.println("Recommendations for improving " + clientId + "'s experience:");
        
        // Check for high error rate
        if ((boolean) details.get("effort_high_error_rate")) {
            System.out.println("\n1. Address Error Rate Issues:");
            System.out.println("   - Implement better error handling and recovery paths");
            System.out.println("   - Add clearer error messages with actionable next steps");
            System.out.println("   - Create guided workflows for complex financial transactions");
            System.out.println("   - Implement pre-validation of form fields before submission");
            
            if (clientId.contains("brokerage")) {
                System.out.println("   - Add order preview step before final submission");
                System.out.println("   - Provide real-time validation of trade parameters");
            } else if (clientId.contains("retirement")) {
                System.out.println("   - Add tooltips explaining contribution limits and rules");
                System.out.println("   - Implement a calculator for maximum allowed contributions");
            }
        }
        
        // Check for navigation issues
        if ((boolean) details.get("effort_high_back_forth_navigation")) {
            System.out.println("\n2. Improve Navigation Flow:");
            System.out.println("   - Simplify the information architecture");
            System.out.println("   - Create task-based navigation instead of product-based");
            System.out.println("   - Add breadcrumbs and clear navigation paths");
            System.out.println("   - Implement a persistent mini-dashboard with key information");
            
            if (clientId.contains("wealth") || clientId.contains("advisory")) {
                System.out.println("   - Create a unified portfolio view with drill-down capabilities");
                System.out.println("   - Add a financial plan progress tracker on main dashboard");
            }
        }
        
        // Check for UI responsiveness
        if ((boolean) details.get("effort_high_repeated_clicks")) {
            System.out.println("\n3. Enhance UI Responsiveness:");
            System.out.println("   - Improve button feedback (visual, loading indicators)");
            System.out.println("   - Optimize page load times for financial data");
            System.out.println("   - Implement progressive loading for large datasets");
            System.out.println("   - Add clear confirmation for successful actions");
            
            if (clientId.contains("brokerage")) {
                System.out.println("   - Provide immediate order acknowledgment before full processing");
                System.out.println("   - Implement real-time order status updates");
            }
        }
        
        // Check for channel consistency
        if ((boolean) details.get("effort_high_channel_switching")) {
            System.out.println("\n4. Ensure Cross-Channel Consistency:");
            System.out.println("   - Harmonize the experience across web, mobile, and advisor interactions");
            System.out.println("   - Implement seamless handoffs between channels");
            System.out.println("   - Ensure consistent data visibility across all touchpoints");
            System.out.println("   - Create a unified notification system across channels");
            
            if (clientId.contains("advisory")) {
                System.out.println("   - Enable advisor to view same screens as client during consultations");
                System.out.println("   - Create co-browsing capability for complex scenarios");
            }
        }
        
        // Additional recommendations based on client type
        if (clientId.contains("wealth")) {
            System.out.println("\n5. Wealth Management Specific Improvements:");
            System.out.println("   - Implement a simplified asset allocation visualization");
            System.out.println("   - Add one-click access to recent advisor communications");
            System.out.println("   - Create a personalized market insights feed");
        } else if (clientId.contains("brokerage")) {
            System.out.println("\n5. Brokerage Specific Improvements:");
            System.out.println("   - Streamline the order entry process");
            System.out.println("   - Add quick access to frequently traded securities");
            System.out.println("   - Implement watchlist-to-order functionality");
        } else if (clientId.contains("advisory")) {
            System.out.println("\n5. Financial Advisory Specific Improvements:");
            System.out.println("   - Create a simplified goal tracking dashboard");
            System.out.println("   - Add progress visualization for financial plans");
            System.out.println("   - Implement one-click advisor scheduling");
        } else if (clientId.contains("retirement")) {
            System.out.println("\n5. Retirement Planning Specific Improvements:");
            System.out.println("   - Simplify the contribution process");
            System.out.println("   - Add visual retirement readiness indicators");
            System.out.println("   - Implement scenario modeling with simple controls");
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