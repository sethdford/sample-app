package com.sample.examples;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sample.service.CloudWatchLogEmbeddingService;

/**
 * Demonstrates how to use the CloudWatchLogEmbeddingService to generate embeddings
 * from CloudWatch logs and detect anomalies in user behavior.
 */
public class CloudWatchLogEmbeddingDemo {

    public static void main(String[] args) {
        try {
            System.out.println("=== CloudWatch Log Embedding Demo ===");
            System.out.println("This demo showcases how to generate embeddings from CloudWatch logs\n");
            
            // Initialize the service
            CloudWatchLogEmbeddingService embeddingService = new CloudWatchLogEmbeddingService();
            
            // Create sample users
            String[] userIds = {
                "user_normal_activity", 
                "user_high_error_rate", 
                "user_suspicious_activity",
                "user_api_heavy",
                "user_mobile_app"
            };
            
            // For demo purposes, we'll generate sample logs instead of fetching from CloudWatch
            System.out.println("=== Generating Sample CloudWatch Logs ===");
            for (String userId : userIds) {
                System.out.println("\nGenerating logs for user: " + userId);
                
                // Generate different log patterns based on user type
                int apiGatewayLogCount = 50;
                int lambdaLogCount = 30;
                int applicationLogCount = 40;
                int errorLogCount = 5;
                
                // Adjust log patterns for different user types
                if (userId.equals("user_high_error_rate")) {
                    errorLogCount = 25; // 5x more errors
                } else if (userId.equals("user_suspicious_activity")) {
                    // Unusual pattern of API calls and errors
                    apiGatewayLogCount = 100;
                    errorLogCount = 15;
                } else if (userId.equals("user_api_heavy")) {
                    apiGatewayLogCount = 150; // 3x more API calls
                    lambdaLogCount = 100;
                } else if (userId.equals("user_mobile_app")) {
                    applicationLogCount = 100; // More application logs, fewer API logs
                    apiGatewayLogCount = 20;
                }
                
                // Generate sample logs
                List<String> apiLogs = embeddingService.generateSampleLogs(
                    userId, 
                    CloudWatchLogEmbeddingService.LOG_TYPE_API_GATEWAY, 
                    apiGatewayLogCount
                );
                
                List<String> lambdaLogs = embeddingService.generateSampleLogs(
                    userId, 
                    CloudWatchLogEmbeddingService.LOG_TYPE_LAMBDA, 
                    lambdaLogCount
                );
                
                List<String> appLogs = embeddingService.generateSampleLogs(
                    userId, 
                    CloudWatchLogEmbeddingService.LOG_TYPE_APPLICATION, 
                    applicationLogCount
                );
                
                List<String> errorLogs = embeddingService.generateSampleLogs(
                    userId, 
                    CloudWatchLogEmbeddingService.LOG_TYPE_ERROR, 
                    errorLogCount
                );
                
                int totalLogs = apiLogs.size() + lambdaLogs.size() + appLogs.size() + errorLogs.size();
                System.out.println("Generated " + totalLogs + " log entries:");
                System.out.println("  - API Gateway logs: " + apiLogs.size());
                System.out.println("  - Lambda logs: " + lambdaLogs.size());
                System.out.println("  - Application logs: " + appLogs.size());
                System.out.println("  - Error logs: " + errorLogs.size());
                
                // Display sample logs (just a few)
                System.out.println("\nSample API Gateway log:");
                System.out.println(apiLogs.get(0));
                
                System.out.println("\nSample Lambda log:");
                System.out.println(lambdaLogs.get(0));
                
                System.out.println("\nSample Application log:");
                System.out.println(appLogs.get(0));
                
                if (!errorLogs.isEmpty()) {
                    System.out.println("\nSample Error log:");
                    System.out.println(errorLogs.get(0));
                }
            }
            
            // In a real scenario, we would use actual CloudWatch log groups
            // For this demo, we'll simulate the embedding generation process
            System.out.println("\n=== Generating Embeddings from CloudWatch Logs ===");
            
            // For demo purposes, we'll pretend we're accessing CloudWatch logs
            // In a real scenario, this would fetch actual logs from CloudWatch
            String logGroupName = "/aws/lambda/user-service";
            int hoursBack = 24;
            
            for (String userId : userIds) {
                System.out.println("\nProcessing logs for user: " + userId);
                System.out.println("Log group: " + logGroupName);
                System.out.println("Time period: Last " + hoursBack + " hours");
                
                // In a real scenario, this would generate an embedding from actual CloudWatch logs
                // For demo purposes, we'll simulate the process
                simulateEmbeddingGeneration(userId);
                
                // Analyze user behavior
                System.out.println("\n=== User Behavior Analysis ===");
                simulateUserBehaviorAnalysis(userId, logGroupName, hoursBack);
            }
            
            // Demonstrate anomaly detection
            System.out.println("\n=== Anomaly Detection ===");
            System.out.println("Comparing current behavior with historical patterns...");
            
            // Simulate anomaly detection for the suspicious user
            String suspiciousUser = "user_suspicious_activity";
            simulateAnomalyDetection(suspiciousUser, logGroupName, hoursBack);
            
            // Simulate anomaly detection for the normal user
            String normalUser = "user_normal_activity";
            simulateAnomalyDetection(normalUser, logGroupName, hoursBack);
            
        } catch (Exception e) {
            System.err.println("Error in CloudWatchLogEmbeddingDemo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Simulates the embedding generation process for demo purposes.
     */
    private static void simulateEmbeddingGeneration(String userId) {
        System.out.println("Fetching logs for user: " + userId);
        System.out.println("Processing logs into text format...");
        
        // Simulate the processed text that would be generated from logs
        String processedText;
        
        if (userId.equals("user_high_error_rate")) {
            processedText = "User activity log summary: Total log entries: 125. API paths accessed: /api/users (15 times), "
                + "/api/products (10 times), /api/orders (25 times). Status codes: 200 (75 times), 500 (25 times), "
                + "404 (15 times). Successful requests: 75, Failed requests: 40. Errors encountered: error (25 times), "
                + "exception (10 times), timeout (5 times).";
        } else if (userId.equals("user_suspicious_activity")) {
            processedText = "User activity log summary: Total log entries: 165. API paths accessed: /api/users (40 times), "
                + "/api/admin (25 times), /api/settings (35 times). Status codes: 200 (100 times), 403 (35 times), "
                + "401 (15 times). Successful requests: 100, Failed requests: 50. Errors encountered: denied (35 times), "
                + "error (15 times).";
        } else if (userId.equals("user_api_heavy")) {
            processedText = "User activity log summary: Total log entries: 250. API paths accessed: /api/products (75 times), "
                + "/api/cart (50 times), /api/checkout (25 times). Status codes: 200 (225 times), 400 (15 times), "
                + "500 (10 times). Successful requests: 225, Failed requests: 25. Errors encountered: error (10 times), "
                + "timeout (15 times).";
        } else if (userId.equals("user_mobile_app")) {
            processedText = "User activity log summary: Total log entries: 120. API paths accessed: /api/users (10 times), "
                + "/api/products (10 times). Status codes: 200 (115 times), 404 (5 times). Successful requests: 115, "
                + "Failed requests: 5. User actions: login (15 times), viewProfile (25 times), viewProduct (60 times).";
        } else {
            processedText = "User activity log summary: Total log entries: 125. API paths accessed: /api/users (15 times), "
                + "/api/products (35 times), /api/orders (25 times). Status codes: 200 (115 times), 404 (5 times), "
                + "500 (5 times). Successful requests: 115, Failed requests: 10. Errors encountered: error (5 times), "
                + "timeout (5 times).";
        }
        
        System.out.println("Processed text: " + processedText);
        System.out.println("Generating embedding using AWS Bedrock...");
        
        // Simulate the embedding generation
        double[] embedding = new double[1536]; // Typical size for Titan embeddings
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = Math.random() * 2 - 1; // Random values between -1 and 1
        }
        
        System.out.println("Embedding generated successfully!");
        System.out.println("Embedding dimensions: " + embedding.length);
        System.out.println("First 5 values: " + Arrays.toString(Arrays.copyOf(embedding, 5)) + "...");
        
        System.out.println("Storing embedding in DynamoDB...");
        System.out.println("Embedding stored with metadata.");
    }
    
    /**
     * Simulates user behavior analysis for demo purposes.
     */
    private static void simulateUserBehaviorAnalysis(String userId, String logGroupName, int hoursBack) {
        System.out.println("Analyzing behavior for user: " + userId);
        
        // Simulate behavior analysis results
        Map<String, Object> analysis;
        
        if (userId.equals("user_high_error_rate")) {
            analysis = Map.of(
                "total_logs", 125,
                "success_rate", 0.65,
                "most_common_error", "Internal Server Error",
                "most_accessed_path", "/api/orders",
                "unusual_patterns", "High error rate (25%)"
            );
        } else if (userId.equals("user_suspicious_activity")) {
            analysis = Map.of(
                "total_logs", 165,
                "success_rate", 0.60,
                "most_common_error", "Access Denied",
                "most_accessed_path", "/api/admin",
                "unusual_patterns", "Multiple failed admin access attempts"
            );
        } else if (userId.equals("user_api_heavy")) {
            analysis = Map.of(
                "total_logs", 250,
                "success_rate", 0.90,
                "most_common_error", "Timeout",
                "most_accessed_path", "/api/products",
                "unusual_patterns", "High volume of API calls"
            );
        } else if (userId.equals("user_mobile_app")) {
            analysis = Map.of(
                "total_logs", 120,
                "success_rate", 0.96,
                "most_common_error", "Not Found",
                "most_accessed_path", "/api/products",
                "unusual_patterns", "None detected"
            );
        } else {
            analysis = Map.of(
                "total_logs", 125,
                "success_rate", 0.92,
                "most_common_error", "Not Found",
                "most_accessed_path", "/api/products",
                "unusual_patterns", "None detected"
            );
        }
        
        System.out.println("Analysis results:");
        for (Map.Entry<String, Object> entry : analysis.entrySet()) {
            System.out.println("  - " + entry.getKey() + ": " + entry.getValue());
        }
    }
    
    /**
     * Simulates anomaly detection for demo purposes.
     */
    private static void simulateAnomalyDetection(String userId, String logGroupName, int hoursBack) {
        System.out.println("\nDetecting anomalies for user: " + userId);
        
        // Simulate anomaly detection results
        boolean isAnomaly = userId.equals("user_suspicious_activity") || userId.equals("user_high_error_rate");
        double similarityScore = isAnomaly ? 0.45 : 0.92;
        
        System.out.println("Similarity to historical behavior: " + String.format("%.2f", similarityScore * 100) + "%");
        System.out.println("Anomaly detected: " + isAnomaly);
        
        if (isAnomaly) {
            System.out.println("Anomaly details:");
            if (userId.equals("user_suspicious_activity")) {
                System.out.println("  - Unusual access patterns to admin endpoints");
                System.out.println("  - High rate of authorization failures (403/401 errors)");
                System.out.println("  - Accessing sensitive endpoints not previously visited");
                System.out.println("  - Recommendation: Investigate potential security breach");
            } else if (userId.equals("user_high_error_rate")) {
                System.out.println("  - Unusually high error rate (25% vs normal 5-8%)");
                System.out.println("  - Increased frequency of timeout errors");
                System.out.println("  - Recommendation: Check for service degradation or API issues");
            }
        } else {
            System.out.println("User behavior is within normal parameters.");
        }
    }
} 