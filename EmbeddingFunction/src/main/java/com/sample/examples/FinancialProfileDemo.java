package com.sample.examples;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sample.service.EnhancedDynamoDBService;
import com.sample.service.FinancialInterestEmbeddingService;
import com.sample.util.JsonUtils;

/**
 * Demonstrates how to use the FinancialInterestEmbeddingService to generate embeddings
 * for clients of financial institutions like ThetaCorp and BetaCorp, focusing on
 * financial well-being, wellness, and life events.
 */
public class FinancialProfileDemo {

    public static void main(String[] args) {
        try {
            System.out.println("=== Financial Profile Embedding Demo ===");
            System.out.println("This demo showcases how to generate embeddings for financial institution clients\n");
            
            // Initialize the service
            FinancialInterestEmbeddingService embeddingService = new FinancialInterestEmbeddingService();
            
            // Create sample clients with different financial profiles
            String[] clientIds = {
                "thetacorp_client_young", 
                "betacorp_client_family", 
                "thetacorp_client_preretiree",
                "betacorp_client_retiree",
                "thetacorp_client_business"
            };
            
            String[] clientTypes = {
                "young_professional", 
                "mid_career_family", 
                "pre_retiree",
                "retiree",
                "business_owner"
            };
            
            for (int i = 0; i < clientIds.length; i++) {
                String clientId = clientIds[i];
                String clientType = clientTypes[i];
                
                System.out.println("\n=== Processing Client: " + clientId + " (" + clientType + ") ===");
                
                // Generate sample financial profile
                Map<String, Object> profileData = embeddingService.generateSampleFinancialProfile(clientType);
                
                @SuppressWarnings("unchecked")
                List<String> financialInterests = (List<String>) profileData.get("financial_interests");
                
                @SuppressWarnings("unchecked")
                List<String> lifeEvents = (List<String>) profileData.get("life_events");
                
                @SuppressWarnings("unchecked")
                List<String> wellnessConcerns = (List<String>) profileData.get("wellness_concerns");
                
                int riskTolerance = (int) profileData.get("risk_tolerance");
                int timeHorizon = (int) profileData.get("time_horizon");
                
                // Display the profile
                System.out.println("Financial Interests: " + financialInterests);
                System.out.println("Life Events: " + lifeEvents);
                System.out.println("Wellness Concerns: " + wellnessConcerns);
                System.out.println("Risk Tolerance (1-10): " + riskTolerance);
                System.out.println("Time Horizon (years): " + timeHorizon);
                
                // Generate embedding from financial profile
                System.out.println("\nGenerating financial profile embedding...");
                double[] embedding = embeddingService.generateFinancialEmbedding(
                    clientId, 
                    financialInterests, 
                    lifeEvents, 
                    wellnessConcerns, 
                    riskTolerance, 
                    timeHorizon
                );
                
                System.out.println("Embedding generated successfully!");
                System.out.println("Embedding dimensions: " + embedding.length);
                System.out.println("First 5 values: " + Arrays.toString(Arrays.copyOf(embedding, 5)) + "...");
                
                // Retrieve the embedding from DynamoDB to verify storage
                EnhancedDynamoDBService dynamoDBService = new EnhancedDynamoDBService();
                Map<String, Object> storedData = dynamoDBService.getEmbeddingWithMetadata(
                    clientId, 
                    "financial_profile"
                );
                
                if (storedData != null) {
                    System.out.println("\nSuccessfully retrieved from DynamoDB!");
                    System.out.println("Metadata: " + JsonUtils.toJson(storedData.get("metadata")));
                } else {
                    System.out.println("\nFailed to retrieve from DynamoDB.");
                }
                
                // Get product recommendations
                System.out.println("\n=== Product Recommendations ===");
                Map<String, Double> recommendations = embeddingService.recommendFinancialProducts(clientId);
                
                System.out.println("Recommended financial products for " + clientId + ":");
                for (Map.Entry<String, Double> entry : recommendations.entrySet()) {
                    System.out.println(formatProductName(entry.getKey()) + ": " 
                                      + String.format("%.2f%%", entry.getValue() * 100) + " relevance");
                }
            }
            
            // Demonstrate finding similar clients
            System.out.println("\n=== Finding Similar Financial Profiles ===");
            String targetClientId = "thetacorp_client_young";
            Map<String, Double> similarClients = embeddingService.findSimilarFinancialProfiles(targetClientId, 3);
            
            System.out.println("Clients with similar financial profiles to " + targetClientId + ":");
            for (Map.Entry<String, Double> entry : similarClients.entrySet()) {
                System.out.println(entry.getKey() + ": " + String.format("%.2f%%", entry.getValue() * 100) + " similarity");
            }
            
            // Demonstrate comparing two specific clients
            System.out.println("\n=== Comparing Two Financial Profiles ===");
            compareClients("thetacorp_client_young", "betacorp_client_family");
            
        } catch (Exception e) {
            System.err.println("Error in FinancialProfileDemo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Compares two clients based on their financial profile embeddings.
     */
    private static void compareClients(String clientId1, String clientId2) throws Exception {
        EnhancedDynamoDBService dynamoDBService = new EnhancedDynamoDBService();
        
        // Get embeddings for both clients
        double[] embedding1 = dynamoDBService.getEmbedding(clientId1, "financial_profile");
        double[] embedding2 = dynamoDBService.getEmbedding(clientId2, "financial_profile");
        
        if (embedding1 == null || embedding2 == null) {
            System.out.println("One or both client embeddings not found.");
            return;
        }
        
        // Calculate similarity
        double similarity = calculateCosineSimilarity(embedding1, embedding2);
        
        System.out.println("Similarity between " + clientId1 + " and " + clientId2 + ": " 
                          + String.format("%.2f%%", similarity * 100));
        
        // Interpret the similarity
        if (similarity > 0.8) {
            System.out.println("These clients have very similar financial profiles!");
            System.out.println("Recommendation: Consider similar financial products and services.");
        } else if (similarity > 0.5) {
            System.out.println("These clients have somewhat similar financial profiles.");
            System.out.println("Recommendation: Some products may be relevant to both, but personalization is needed.");
        } else {
            System.out.println("These clients have different financial profiles.");
            System.out.println("Recommendation: Completely different financial strategies are appropriate.");
        }
    }
    
    /**
     * Formats product IDs into readable names.
     */
    private static String formatProductName(String productId) {
        switch (productId) {
            case "retirement_target_2045":
                return "Target Retirement 2045 Fund";
            case "balanced_growth_fund":
                return "Balanced Growth Fund";
            case "tax_advantaged_bond":
                return "Tax-Advantaged Bond Fund";
            case "college_savings_529":
                return "529 College Savings Plan";
            default:
                return productId.replace("_", " ");
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