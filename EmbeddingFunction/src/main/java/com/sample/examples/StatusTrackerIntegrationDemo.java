package com.sample.examples;

import com.sample.EmbeddingService;
import com.sample.model.EmbeddingResult;
import com.sample.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Demo showing how to integrate the Embedding Function with the Status Tracker
 */
public class StatusTrackerIntegrationDemo {

    public static void main(String[] args) {
        System.out.println("Starting Status Tracker Integration Demo");
        
        // Create an instance of the EmbeddingService
        EmbeddingService embeddingService = new EmbeddingService();
        
        // 1. Generate embeddings for client profiles
        System.out.println("\n=== Generating Client Profile Embeddings ===");
        Map<String, Object> clientProfile = createSampleClientProfile();
        EmbeddingResult clientEmbedding = embeddingService.generateEmbedding(clientProfile);
        
        System.out.println("Client Profile: " + JsonUtils.toJson(clientProfile));
        System.out.println("Embedding Vector (first 5 dimensions): " + 
                           formatVector(clientEmbedding.getEmbeddingVector(), 5));
        
        // 2. Generate embeddings for trading patterns
        System.out.println("\n=== Generating Trading Pattern Embeddings ===");
        Map<String, Object> tradingPattern = createSampleTradingPattern();
        EmbeddingResult tradingEmbedding = embeddingService.generateEmbedding(tradingPattern);
        
        System.out.println("Trading Pattern: " + JsonUtils.toJson(tradingPattern));
        System.out.println("Embedding Vector (first 5 dimensions): " + 
                           formatVector(tradingEmbedding.getEmbeddingVector(), 5));
        
        // 3. Generate embeddings for compliance patterns
        System.out.println("\n=== Generating Compliance Pattern Embeddings ===");
        Map<String, Object> compliancePattern = createSampleCompliancePattern();
        EmbeddingResult complianceEmbedding = embeddingService.generateEmbedding(compliancePattern);
        
        System.out.println("Compliance Pattern: " + JsonUtils.toJson(compliancePattern));
        System.out.println("Embedding Vector (first 5 dimensions): " + 
                           formatVector(complianceEmbedding.getEmbeddingVector(), 5));
        
        // 4. Simulate Status Tracker integration
        System.out.println("\n=== Simulating Status Tracker Integration ===");
        
        // Create a sample status
        Map<String, Object> status = new HashMap<>();
        status.put("id", "status-" + UUID.randomUUID().toString().substring(0, 8));
        status.put("title", "Investment Portfolio Rebalancing");
        status.put("description", "Rebalancing client portfolio based on market conditions and risk profile");
        status.put("type", "FINANCIAL_ADVISORY");
        status.put("priority", "HIGH");
        status.put("createdAt", System.currentTimeMillis());
        status.put("updatedAt", System.currentTimeMillis());
        status.put("status", "IN_PROGRESS");
        
        // Add metadata with embedding information
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("clientProfileEmbeddingId", clientEmbedding.getEmbeddingId());
        metadata.put("tradingPatternEmbeddingId", tradingEmbedding.getEmbeddingId());
        metadata.put("compliancePatternEmbeddingId", complianceEmbedding.getEmbeddingId());
        metadata.put("similarityScore", 0.87);
        metadata.put("riskScore", 0.42);
        status.put("metadata", metadata);
        
        // Add steps with embedding-driven recommendations
        Map<String, Object>[] steps = createStepsWithEmbeddingInsights(
                clientEmbedding, tradingEmbedding, complianceEmbedding);
        status.put("steps", steps);
        
        // Print the status with embedding-driven insights
        System.out.println("Status with Embedding Insights: " + JsonUtils.toJson(status));
        
        // 5. Simulate updating a step based on embedding analysis
        System.out.println("\n=== Updating Step Based on Embedding Analysis ===");
        
        // Find similar clients based on embedding
        System.out.println("Finding similar clients to current client profile...");
        String[] similarClientIds = {"client-003", "client-042", "client-078"};
        double[] similarityScores = {0.92, 0.87, 0.85};
        
        // Update step with recommendations
        Map<String, Object> step = (Map<String, Object>) steps[1];
        Map<String, Object> stepMetadata = (Map<String, Object>) step.get("metadata");
        stepMetadata.put("similarClients", similarClientIds);
        stepMetadata.put("similarityScores", similarityScores);
        stepMetadata.put("recommendedStrategy", "Based on similar client outcomes, recommend 60/40 stock/bond allocation with focus on dividend stocks");
        
        // Update step status
        step.put("status", "COMPLETED");
        step.put("completedAt", System.currentTimeMillis());
        
        // Print the updated step
        System.out.println("Updated Step: " + JsonUtils.toJson(step));
        
        System.out.println("\nStatus Tracker Integration Demo completed successfully!");
    }
    
    private static Map<String, Object> createSampleClientProfile() {
        Map<String, Object> profile = new HashMap<>();
        profile.put("clientId", "client-" + UUID.randomUUID().toString().substring(0, 8));
        profile.put("name", "John Smith");
        profile.put("age", 45);
        profile.put("location", "New York, NY");
        profile.put("occupation", "Software Engineer");
        profile.put("income", 150000);
        profile.put("netWorth", 750000);
        profile.put("riskTolerance", "moderate");
        profile.put("investmentHorizon", 20);
        profile.put("investmentGoals", new String[]{"retirement", "college_funding", "home_purchase"});
        profile.put("existingInvestments", new String[]{"stocks", "bonds", "real_estate"});
        return profile;
    }
    
    private static Map<String, Object> createSampleTradingPattern() {
        Map<String, Object> pattern = new HashMap<>();
        pattern.put("clientId", "client-" + UUID.randomUUID().toString().substring(0, 8));
        pattern.put("tradingFrequency", "monthly");
        pattern.put("averageTradeSize", 5000);
        pattern.put("preferredSecurities", new String[]{"ETFs", "mutual_funds", "blue_chip_stocks"});
        pattern.put("sectorPreferences", new String[]{"technology", "healthcare", "consumer_staples"});
        pattern.put("tradingStrategy", "buy_and_hold");
        pattern.put("rebalancingFrequency", "quarterly");
        pattern.put("taxConsiderations", "tax_efficient");
        return pattern;
    }
    
    private static Map<String, Object> createSampleCompliancePattern() {
        Map<String, Object> pattern = new HashMap<>();
        pattern.put("clientId", "client-" + UUID.randomUUID().toString().substring(0, 8));
        pattern.put("kycStatus", "verified");
        pattern.put("kycLastUpdated", System.currentTimeMillis() - 90 * 24 * 60 * 60 * 1000L); // 90 days ago
        pattern.put("amlChecks", new String[]{"identity_verification", "source_of_funds", "pep_screening"});
        pattern.put("riskRating", "medium");
        pattern.put("regulatoryRestrictions", new String[]{"no_margin_trading", "no_options_trading"});
        pattern.put("complianceNotes", "Annual review due in 3 months");
        return pattern;
    }
    
    private static Map<String, Object>[] createStepsWithEmbeddingInsights(
            EmbeddingResult clientEmbedding, 
            EmbeddingResult tradingEmbedding,
            EmbeddingResult complianceEmbedding) {
        
        Map<String, Object>[] steps = new Map[5];
        
        // Step 1: Analysis
        steps[0] = new HashMap<>();
        steps[0].put("id", "step-001");
        steps[0].put("name", "Portfolio Analysis");
        steps[0].put("description", "Analyze current portfolio allocation and performance");
        steps[0].put("status", "COMPLETED");
        steps[0].put("startedAt", System.currentTimeMillis() - 48 * 60 * 60 * 1000L); // 48 hours ago
        steps[0].put("completedAt", System.currentTimeMillis() - 36 * 60 * 60 * 1000L); // 36 hours ago
        
        Map<String, Object> step1Metadata = new HashMap<>();
        step1Metadata.put("currentAllocation", "70% stocks, 20% bonds, 10% cash");
        step1Metadata.put("performanceYTD", "+8.3%");
        step1Metadata.put("riskAssessment", "Slightly higher risk than target profile");
        step1Metadata.put("embeddingInsight", "Client profile embedding indicates moderate risk tolerance with focus on growth");
        steps[0].put("metadata", step1Metadata);
        
        // Step 2: Strategy Development
        steps[1] = new HashMap<>();
        steps[1].put("id", "step-002");
        steps[1].put("name", "Strategy Development");
        steps[1].put("description", "Develop rebalancing strategy based on client profile and market conditions");
        steps[1].put("status", "IN_PROGRESS");
        steps[1].put("startedAt", System.currentTimeMillis() - 24 * 60 * 60 * 1000L); // 24 hours ago
        
        Map<String, Object> step2Metadata = new HashMap<>();
        step2Metadata.put("targetAllocation", "60% stocks, 30% bonds, 10% cash");
        step2Metadata.put("rationale", "Reducing equity exposure due to market volatility and client's approaching retirement");
        step2Metadata.put("embeddingInsight", "Trading pattern embedding suggests preference for blue-chip stocks and dividend-focused ETFs");
        steps[1].put("metadata", step2Metadata);
        
        // Step 3: Client Approval
        steps[2] = new HashMap<>();
        steps[2].put("id", "step-003");
        steps[2].put("name", "Client Approval");
        steps[2].put("description", "Present rebalancing strategy to client for approval");
        steps[2].put("status", "NOT_STARTED");
        steps[2].put("dueDate", System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L); // 7 days from now
        
        Map<String, Object> step3Metadata = new HashMap<>();
        step3Metadata.put("meetingScheduled", false);
        step3Metadata.put("preferredContactMethod", "video_call");
        step3Metadata.put("embeddingInsight", "Compliance pattern embedding indicates need for detailed documentation of client approval");
        steps[2].put("metadata", step3Metadata);
        
        // Step 4: Execution
        steps[3] = new HashMap<>();
        steps[3].put("id", "step-004");
        steps[3].put("name", "Trade Execution");
        steps[3].put("description", "Execute trades to implement the approved rebalancing strategy");
        steps[3].put("status", "NOT_STARTED");
        steps[3].put("dueDate", System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000L); // 14 days from now
        
        Map<String, Object> step4Metadata = new HashMap<>();
        step4Metadata.put("tradesToExecute", "Sell: AAPL (10%), MSFT (5%), AMZN (5%); Buy: AGG (15%), VTIP (5%)");
        step4Metadata.put("estimatedTradingCosts", "$45.00");
        step4Metadata.put("taxImplications", "Estimated capital gains: $3,200");
        step4Metadata.put("embeddingInsight", "Trading pattern embedding suggests executing trades gradually over 5 days to minimize market impact");
        steps[3].put("metadata", step4Metadata);
        
        // Step 5: Review
        steps[4] = new HashMap<>();
        steps[4].put("id", "step-005");
        steps[4].put("name", "Post-Rebalancing Review");
        steps[4].put("description", "Review portfolio after rebalancing and document results");
        steps[4].put("status", "NOT_STARTED");
        steps[4].put("dueDate", System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L); // 30 days from now
        
        Map<String, Object> step5Metadata = new HashMap<>();
        step5Metadata.put("reviewCriteria", "Portfolio alignment, performance impact, tax efficiency");
        step5Metadata.put("nextReviewDate", System.currentTimeMillis() + 90 * 24 * 60 * 60 * 1000L); // 90 days from now
        step5Metadata.put("embeddingInsight", "Client profile embedding suggests scheduling quarterly reviews based on client engagement pattern");
        steps[4].put("metadata", step5Metadata);
        
        return steps;
    }
    
    private static String formatVector(double[] vector, int dimensions) {
        if (vector == null || vector.length == 0) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        int limit = Math.min(dimensions, vector.length);
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(String.format("%.4f", vector[i]));
        }
        
        if (vector.length > dimensions) {
            sb.append(", ...");
        }
        
        sb.append("]");
        return sb.toString();
    }
} 