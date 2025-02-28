package com.sample.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sample.model.EmbeddingMetadata;

/**
 * Specialized service for generating embeddings from financial interests, well-being concerns,
 * and life events for clients of financial institutions like ThetaCorp and BetaCorp.
 */
public class FinancialInterestEmbeddingService {
    
    private final EmbeddingService embeddingService;
    private final EnhancedDynamoDBService dynamoDBService;
    
    // Constants for financial interest categories
    private static final List<String> FINANCIAL_CATEGORIES = List.of(
        "Retirement", "Investing", "Saving", "Debt Management", "Tax Planning", 
        "Estate Planning", "Insurance", "College Planning", "Budgeting", "Financial Literacy"
    );
    
    // Constants for life events
    private static final List<String> LIFE_EVENTS = List.of(
        "Marriage", "Divorce", "Birth of Child", "College", "New Job", "Job Loss", 
        "Buying a Home", "Selling a Home", "Retirement", "Inheritance", "Health Crisis", 
        "Starting a Business", "Caring for Aging Parents", "Relocation"
    );
    
    // Constants for financial well-being and wellness concerns
    private static final List<String> WELLNESS_CONCERNS = List.of(
        "Financial Stress", "Work-Life Balance", "Emergency Preparedness", 
        "Long-term Security", "Healthcare Costs", "Financial Independence", 
        "Debt Stress", "Retirement Readiness", "Financial Confidence"
    );
    
    public FinancialInterestEmbeddingService() {
        this.embeddingService = new EmbeddingService();
        this.dynamoDBService = new EnhancedDynamoDBService();
    }
    
    /**
     * Generates an embedding from financial interests, life events, and wellness concerns.
     * 
     * @param userId User ID
     * @param financialInterests List of financial interests
     * @param lifeEvents List of relevant life events
     * @param wellnessConcerns List of financial wellness concerns
     * @param riskTolerance Risk tolerance level (1-10)
     * @param timeHorizon Investment time horizon in years
     * @return The generated embedding
     */
    public double[] generateFinancialEmbedding(
            String userId, 
            List<String> financialInterests,
            List<String> lifeEvents,
            List<String> wellnessConcerns,
            int riskTolerance,
            int timeHorizon) throws Exception {
        
        // 1. Process financial data into a text format
        String processedText = processFinancialProfile(
            financialInterests, lifeEvents, wellnessConcerns, riskTolerance, timeHorizon);
        
        // 2. Generate embedding using the processed text
        long startTime = System.currentTimeMillis();
        double[] embedding = embeddingService.generateEmbedding(processedText);
        long endTime = System.currentTimeMillis();
        
        // 3. Create metadata
        EmbeddingMetadata metadata = new EmbeddingMetadata("financial_profile", "amazon.titan-embed-text-v1");
        metadata.addEncodingDetail("interest_count", financialInterests.size());
        metadata.addEncodingDetail("life_events_count", lifeEvents.size());
        metadata.addEncodingDetail("wellness_concerns_count", wellnessConcerns.size());
        metadata.addEncodingDetail("risk_tolerance", riskTolerance);
        metadata.addEncodingDetail("time_horizon", timeHorizon);
        metadata.addPerformanceMetric("generation_time_ms", endTime - startTime);
        
        // 4. Store in DynamoDB with metadata
        dynamoDBService.storeEmbeddingWithMetadata(
            userId, 
            "financial_profile", 
            embedding, 
            metadata
        );
        
        return embedding;
    }
    
    /**
     * Process financial profile data into a format suitable for embedding generation.
     */
    private String processFinancialProfile(
            List<String> financialInterests,
            List<String> lifeEvents,
            List<String> wellnessConcerns,
            int riskTolerance,
            int timeHorizon) {
        
        StringBuilder processedText = new StringBuilder("Financial profile: ");
        
        // Process financial interests
        if (financialInterests != null && !financialInterests.isEmpty()) {
            processedText.append("Interested in ");
            processedText.append(String.join(", ", financialInterests));
            processedText.append(". ");
        }
        
        // Process life events
        if (lifeEvents != null && !lifeEvents.isEmpty()) {
            processedText.append("Experiencing or planning for ");
            processedText.append(String.join(", ", lifeEvents));
            processedText.append(". ");
        }
        
        // Process wellness concerns
        if (wellnessConcerns != null && !wellnessConcerns.isEmpty()) {
            processedText.append("Concerned about ");
            processedText.append(String.join(", ", wellnessConcerns));
            processedText.append(". ");
        }
        
        // Add risk tolerance
        String riskLevel;
        if (riskTolerance >= 8) {
            riskLevel = "aggressive";
        } else if (riskTolerance >= 5) {
            riskLevel = "moderate";
        } else {
            riskLevel = "conservative";
        }
        processedText.append("Has ").append(riskLevel).append(" risk tolerance. ");
        
        // Add time horizon
        String horizonDescription;
        if (timeHorizon > 20) {
            horizonDescription = "very long-term";
        } else if (timeHorizon > 10) {
            horizonDescription = "long-term";
        } else if (timeHorizon > 5) {
            horizonDescription = "medium-term";
        } else {
            horizonDescription = "short-term";
        }
        processedText.append("Planning with a ").append(horizonDescription).append(" investment horizon.");
        
        return processedText.toString();
    }
    
    /**
     * Finds similar financial profiles.
     * 
     * @param userId User ID to find similar profiles for
     * @param maxResults Maximum number of results to return
     * @return Map of user IDs to similarity scores
     */
    public Map<String, Double> findSimilarFinancialProfiles(String userId, int maxResults) throws Exception {
        // Get the target user's embedding
        double[] targetEmbedding = dynamoDBService.getEmbedding(userId, "financial_profile");
        
        if (targetEmbedding == null) {
            throw new IllegalArgumentException("No financial profile embedding found for user: " + userId);
        }
        
        // TODO: In a real implementation, we would query a database of users
        // For this example, we'll just return a placeholder
        Map<String, Double> similarUsers = new HashMap<>();
        similarUsers.put("client123", 0.94);
        similarUsers.put("client456", 0.89);
        similarUsers.put("client789", 0.78);
        
        return similarUsers;
    }
    
    /**
     * Recommends financial products based on a user's financial profile.
     * 
     * @param userId User ID
     * @return List of recommended product IDs with relevance scores
     */
    public Map<String, Double> recommendFinancialProducts(String userId) throws Exception {
        // Get the user's financial profile embedding
        double[] userEmbedding = dynamoDBService.getEmbedding(userId, "financial_profile");
        
        if (userEmbedding == null) {
            throw new IllegalArgumentException("No financial profile embedding found for user: " + userId);
        }
        
        // TODO: In a real implementation, we would compare with product embeddings
        // For this example, we'll just return placeholder recommendations
        Map<String, Double> recommendations = new HashMap<>();
        recommendations.put("retirement_target_2045", 0.92);
        recommendations.put("balanced_growth_fund", 0.87);
        recommendations.put("tax_advantaged_bond", 0.76);
        recommendations.put("college_savings_529", 0.68);
        
        return recommendations;
    }
    
    /**
     * Generates sample financial profile data for demo purposes.
     * 
     * @param clientType Type of financial client to generate profile for
     * @return Map containing financial interests, life events, and wellness concerns
     */
    public Map<String, Object> generateSampleFinancialProfile(String clientType) {
        Map<String, Object> result = new HashMap<>();
        List<String> interests = new ArrayList<>();
        List<String> lifeEvents = new ArrayList<>();
        List<String> wellnessConcerns = new ArrayList<>();
        int riskTolerance = 5;
        int timeHorizon = 10;
        
        switch (clientType) {
            case "young_professional":
                interests.add("Retirement Planning");
                interests.add("Investing Basics");
                interests.add("Student Loan Repayment");
                interests.add("First-time Home Buying");
                
                lifeEvents.add("New Job");
                lifeEvents.add("Marriage");
                
                wellnessConcerns.add("Emergency Fund");
                wellnessConcerns.add("Work-Life Balance");
                wellnessConcerns.add("Financial Independence");
                
                riskTolerance = 7;
                timeHorizon = 30;
                break;
                
            case "mid_career_family":
                interests.add("College Planning");
                interests.add("Retirement Savings");
                interests.add("Life Insurance");
                interests.add("Tax Planning");
                interests.add("Estate Planning Basics");
                
                lifeEvents.add("Birth of Child");
                lifeEvents.add("Buying a Home");
                lifeEvents.add("Career Advancement");
                
                wellnessConcerns.add("Healthcare Costs");
                wellnessConcerns.add("Education Expenses");
                wellnessConcerns.add("Retirement Readiness");
                
                riskTolerance = 6;
                timeHorizon = 20;
                break;
                
            case "pre_retiree":
                interests.add("Retirement Income");
                interests.add("Social Security Optimization");
                interests.add("Healthcare Planning");
                interests.add("Estate Planning");
                interests.add("Tax-Efficient Withdrawals");
                
                lifeEvents.add("Empty Nest");
                lifeEvents.add("Caring for Aging Parents");
                lifeEvents.add("Approaching Retirement");
                
                wellnessConcerns.add("Longevity Risk");
                wellnessConcerns.add("Healthcare in Retirement");
                wellnessConcerns.add("Legacy Planning");
                
                riskTolerance = 4;
                timeHorizon = 10;
                break;
                
            case "retiree":
                interests.add("Income Generation");
                interests.add("Required Minimum Distributions");
                interests.add("Estate Planning");
                interests.add("Healthcare and Medicare");
                interests.add("Charitable Giving");
                
                lifeEvents.add("Retirement");
                lifeEvents.add("Downsizing Home");
                lifeEvents.add("Grandchildren");
                
                wellnessConcerns.add("Outliving Assets");
                wellnessConcerns.add("Healthcare Costs");
                wellnessConcerns.add("Legacy Planning");
                
                riskTolerance = 3;
                timeHorizon = 15;
                break;
                
            case "business_owner":
                interests.add("Business Succession Planning");
                interests.add("Retirement Plans for Small Business");
                interests.add("Tax Strategies");
                interests.add("Key Person Insurance");
                interests.add("Personal/Business Financial Integration");
                
                lifeEvents.add("Business Expansion");
                lifeEvents.add("Hiring Employees");
                lifeEvents.add("Exit Planning");
                
                wellnessConcerns.add("Business Continuity");
                wellnessConcerns.add("Work-Life Balance");
                wellnessConcerns.add("Retirement Security");
                
                riskTolerance = 6;
                timeHorizon = 15;
                break;
                
            default:
                interests.add("Retirement Planning");
                interests.add("Investing");
                interests.add("Budgeting");
                
                lifeEvents.add("Career Change");
                
                wellnessConcerns.add("Financial Security");
                wellnessConcerns.add("Emergency Preparedness");
                
                riskTolerance = 5;
                timeHorizon = 20;
        }
        
        result.put("financial_interests", interests);
        result.put("life_events", lifeEvents);
        result.put("wellness_concerns", wellnessConcerns);
        result.put("risk_tolerance", riskTolerance);
        result.put("time_horizon", timeHorizon);
        
        return result;
    }
} 