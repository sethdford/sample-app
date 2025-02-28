package com.sample.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sample.model.EmbeddingMetadata;

/**
 * Service for generating embeddings from financial customer data.
 * This service processes customer financial behaviors, call transcripts, and transaction history
 * to create embeddings for personalization and recommendation purposes.
 */
public class FinancialCustomerEmbeddingService {
    
    private final EmbeddingService embeddingService;
    private final EnhancedDynamoDBService dynamoDBService;
    
    // Constants for financial categories
    private static final List<String> INVESTMENT_CATEGORIES = List.of(
        "Stocks", "Bonds", "ETFs", "Mutual Funds", "Options", "Futures", 
        "Crypto", "Retirement", "College Savings", "Real Estate"
    );
    
    private static final List<String> FINANCIAL_GOALS = List.of(
        "Retirement", "Wealth Building", "College Funding", "Home Purchase", 
        "Debt Reduction", "Tax Optimization", "Estate Planning", "Income Generation"
    );
    
    private static final List<String> RISK_PROFILES = List.of(
        "Conservative", "Moderately Conservative", "Moderate", 
        "Moderately Aggressive", "Aggressive"
    );
    
    public FinancialCustomerEmbeddingService() {
        this.embeddingService = new EmbeddingService();
        this.dynamoDBService = new EnhancedDynamoDBService();
    }
    
    /**
     * Generates an embedding from customer financial profile.
     * 
     * @param customerId Customer ID
     * @param investmentPreferences Map of investment types to preference levels
     * @param financialGoals List of financial goals
     * @param riskProfile Customer's risk tolerance profile
     * @param accountTypes List of account types (IRA, 401k, brokerage, etc.)
     * @return The generated embedding
     */
    public double[] generateFinancialProfileEmbedding(
            String customerId, 
            Map<String, Integer> investmentPreferences,
            List<String> financialGoals,
            String riskProfile,
            List<String> accountTypes) throws Exception {
        
        // 1. Process financial profile into a text format
        String processedText = processFinancialProfile(
            investmentPreferences, financialGoals, riskProfile, accountTypes);
        
        // 2. Generate embedding using the processed text
        long startTime = System.currentTimeMillis();
        double[] embedding = embeddingService.generateEmbedding(processedText);
        long endTime = System.currentTimeMillis();
        
        // 3. Create metadata
        EmbeddingMetadata metadata = new EmbeddingMetadata("financial_profile", "amazon.titan-embed-text-v1");
        metadata.addEncodingDetail("investment_preferences_count", investmentPreferences.size());
        metadata.addEncodingDetail("financial_goals_count", financialGoals.size());
        metadata.addEncodingDetail("risk_profile", riskProfile);
        metadata.addEncodingDetail("account_types_count", accountTypes.size());
        metadata.addPerformanceMetric("generation_time_ms", endTime - startTime);
        
        // 4. Store in DynamoDB with metadata
        dynamoDBService.storeEmbeddingWithMetadata(
            customerId, 
            "FINANCIAL_PROFILE", 
            embedding, 
            metadata
        );
        
        return embedding;
    }
    
    /**
     * Generates an embedding from customer call transcript with sentiment analysis.
     * 
     * @param customerId Customer ID
     * @param callId Unique call identifier
     * @param transcriptText The call transcript text
     * @param callCategory Category of the call (e.g., "account_issue", "investment_advice")
     * @return Map containing embedding and sentiment analysis
     */
    public Map<String, Object> processCallTranscript(
            String customerId, 
            String callId,
            String transcriptText,
            String callCategory) throws Exception {
        
        // 1. Generate embedding from transcript
        long startTime = System.currentTimeMillis();
        double[] embedding = embeddingService.generateEmbedding(transcriptText);
        long endTime = System.currentTimeMillis();
        
        // 2. Perform sentiment analysis
        Map<String, Object> sentimentResult = analyzeSentiment(transcriptText);
        
        // 3. Extract key financial terms and concerns
        Map<String, Object> keyTerms = extractFinancialTerms(transcriptText);
        
        // 4. Create metadata
        EmbeddingMetadata metadata = new EmbeddingMetadata("call_transcript", "amazon.titan-embed-text-v1");
        metadata.addEncodingDetail("call_id", callId);
        metadata.addEncodingDetail("call_category", callCategory);
        metadata.addEncodingDetail("transcript_length", transcriptText.length());
        metadata.addEncodingDetail("sentiment", sentimentResult.get("sentiment"));
        metadata.addPerformanceMetric("generation_time_ms", endTime - startTime);
        
        // 5. Store in DynamoDB with metadata
        dynamoDBService.storeEmbeddingWithMetadata(
            customerId, 
            "CALL_TRANSCRIPT_" + callId, 
            embedding, 
            metadata
        );
        
        // 6. Return results
        Map<String, Object> result = new HashMap<>();
        result.put("embedding", embedding);
        result.put("sentiment", sentimentResult);
        result.put("key_terms", keyTerms);
        
        return result;
    }
    
    /**
     * Generates an embedding from customer transaction history.
     * 
     * @param customerId Customer ID
     * @param transactions List of transaction data
     * @param timeframe Timeframe of the transactions (e.g., "last_30_days", "last_quarter")
     * @return The generated embedding
     */
    public double[] processTransactionHistory(
            String customerId, 
            List<Map<String, Object>> transactions,
            String timeframe) throws Exception {
        
        // 1. Process transactions into a text format
        String processedText = processTransactions(transactions);
        
        // 2. Generate embedding using the processed text
        long startTime = System.currentTimeMillis();
        double[] embedding = embeddingService.generateEmbedding(processedText);
        long endTime = System.currentTimeMillis();
        
        // 3. Calculate transaction statistics
        Map<String, Object> transactionStats = calculateTransactionStats(transactions);
        
        // 4. Create metadata
        EmbeddingMetadata metadata = new EmbeddingMetadata("transaction_history", "amazon.titan-embed-text-v1");
        metadata.addEncodingDetail("transaction_count", transactions.size());
        metadata.addEncodingDetail("timeframe", timeframe);
        metadata.addEncodingDetail("transaction_stats", transactionStats);
        metadata.addPerformanceMetric("generation_time_ms", endTime - startTime);
        
        // 5. Store in DynamoDB with metadata
        dynamoDBService.storeEmbeddingWithMetadata(
            customerId, 
            "TRANSACTION_HISTORY_" + timeframe, 
            embedding, 
            metadata
        );
        
        return embedding;
    }
    
    /**
     * Process financial profile into a format suitable for embedding generation.
     */
    private String processFinancialProfile(
            Map<String, Integer> investmentPreferences,
            List<String> financialGoals,
            String riskProfile,
            List<String> accountTypes) {
        
        StringBuilder processedText = new StringBuilder("Financial customer profile: ");
        
        // Process risk profile
        processedText.append("Risk profile: ").append(riskProfile).append(". ");
        
        // Process investment preferences
        if (investmentPreferences != null && !investmentPreferences.isEmpty()) {
            processedText.append("Investment preferences: ");
            
            List<String> preferenceTexts = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : investmentPreferences.entrySet()) {
                String category = entry.getKey();
                int level = entry.getValue();
                
                String preferenceLevel;
                if (level >= 8) {
                    preferenceLevel = "strongly prefers";
                } else if (level >= 5) {
                    preferenceLevel = "moderately interested in";
                } else if (level >= 3) {
                    preferenceLevel = "somewhat interested in";
                } else {
                    preferenceLevel = "minimal interest in";
                }
                
                preferenceTexts.add(preferenceLevel + " " + category);
            }
            
            processedText.append(String.join(", ", preferenceTexts));
            processedText.append(". ");
        }
        
        // Process financial goals
        if (financialGoals != null && !financialGoals.isEmpty()) {
            processedText.append("Financial goals: ");
            processedText.append(String.join(", ", financialGoals));
            processedText.append(". ");
        }
        
        // Process account types
        if (accountTypes != null && !accountTypes.isEmpty()) {
            processedText.append("Account types: ");
            processedText.append(String.join(", ", accountTypes));
            processedText.append(".");
        }
        
        return processedText.toString();
    }
    
    /**
     * Process transactions into a format suitable for embedding generation.
     */
    private String processTransactions(List<Map<String, Object>> transactions) {
        StringBuilder processedText = new StringBuilder("Transaction history: ");
        
        for (Map<String, Object> transaction : transactions) {
            processedText.append("Transaction: ");
            processedText.append("Date=").append(transaction.get("date")).append(", ");
            processedText.append("Type=").append(transaction.get("type")).append(", ");
            processedText.append("Amount=").append(transaction.get("amount")).append(", ");
            
            if (transaction.containsKey("symbol")) {
                processedText.append("Symbol=").append(transaction.get("symbol")).append(", ");
            }
            
            if (transaction.containsKey("category")) {
                processedText.append("Category=").append(transaction.get("category")).append(", ");
            }
            
            if (transaction.containsKey("description")) {
                processedText.append("Description=").append(transaction.get("description"));
            }
            
            processedText.append(". ");
        }
        
        return processedText.toString();
    }
    
    /**
     * Analyze sentiment of call transcript.
     */
    private Map<String, Object> analyzeSentiment(String text) {
        // In a real implementation, this would call AWS Comprehend or another sentiment analysis service
        // For this example, we'll return a placeholder
        Map<String, Object> sentiment = new HashMap<>();
        sentiment.put("sentiment", "POSITIVE");
        sentiment.put("positive_score", 0.85);
        sentiment.put("negative_score", 0.05);
        sentiment.put("neutral_score", 0.10);
        sentiment.put("mixed_score", 0.00);
        
        return sentiment;
    }
    
    /**
     * Extract key financial terms from text.
     */
    private Map<String, Object> extractFinancialTerms(String text) {
        // In a real implementation, this would use NLP to extract key terms
        // For this example, we'll return a placeholder
        Map<String, Object> keyTerms = new HashMap<>();
        List<String> terms = new ArrayList<>();
        terms.add("retirement");
        terms.add("portfolio");
        terms.add("investment");
        terms.add("market volatility");
        
        keyTerms.put("terms", terms);
        keyTerms.put("concerns", List.of("market volatility", "retirement planning"));
        
        return keyTerms;
    }
    
    /**
     * Calculate statistics from transaction data.
     */
    private Map<String, Object> calculateTransactionStats(List<Map<String, Object>> transactions) {
        // In a real implementation, this would calculate various statistics
        // For this example, we'll return a placeholder
        Map<String, Object> stats = new HashMap<>();
        stats.put("buy_count", 12);
        stats.put("sell_count", 8);
        stats.put("total_buy_amount", 25000.00);
        stats.put("total_sell_amount", 18500.00);
        stats.put("most_traded_symbol", "AAPL");
        
        return stats;
    }
    
    /**
     * Find similar customers based on financial profile.
     */
    public Map<String, Double> findSimilarCustomers(String customerId, int maxResults) throws Exception {
        // Get the target customer's embedding
        double[] targetEmbedding = dynamoDBService.getEmbedding(
            customerId, 
            "FINANCIAL_PROFILE"
        );
        
        if (targetEmbedding == null) {
            throw new IllegalArgumentException("No financial profile embedding found for customer: " + customerId);
        }
        
        // TODO: In a real implementation, we would query a database of customers
        // For this example, we'll just return a placeholder
        Map<String, Double> similarCustomers = new HashMap<>();
        similarCustomers.put("customer123", 0.92);
        similarCustomers.put("customer456", 0.87);
        similarCustomers.put("customer789", 0.75);
        
        return similarCustomers;
    }
    
    /**
     * Generates sample financial profile for demo purposes.
     */
    public Map<String, Object> generateSampleFinancialProfile(String customerType) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Integer> investmentPreferences = new HashMap<>();
        List<String> financialGoals = new ArrayList<>();
        List<String> accountTypes = new ArrayList<>();
        String riskProfile;
        
        switch (customerType) {
            case "conservative_retiree":
                investmentPreferences.put("Bonds", 9);
                investmentPreferences.put("Dividend Stocks", 7);
                investmentPreferences.put("ETFs", 6);
                investmentPreferences.put("Mutual Funds", 8);
                investmentPreferences.put("Options", 1);
                
                financialGoals.add("Income Generation");
                financialGoals.add("Wealth Preservation");
                financialGoals.add("Estate Planning");
                
                accountTypes.add("IRA");
                accountTypes.add("Roth IRA");
                accountTypes.add("Brokerage");
                
                riskProfile = "Conservative";
                break;
                
            case "growth_investor":
                investmentPreferences.put("Growth Stocks", 9);
                investmentPreferences.put("ETFs", 7);
                investmentPreferences.put("Options", 6);
                investmentPreferences.put("Crypto", 5);
                investmentPreferences.put("Bonds", 3);
                
                financialGoals.add("Wealth Building");
                financialGoals.add("Retirement");
                financialGoals.add("Tax Optimization");
                
                accountTypes.add("401k");
                accountTypes.add("Brokerage");
                accountTypes.add("Roth IRA");
                
                riskProfile = "Moderately Aggressive";
                break;
                
            case "active_trader":
                investmentPreferences.put("Options", 9);
                investmentPreferences.put("Futures", 8);
                investmentPreferences.put("Stocks", 9);
                investmentPreferences.put("ETFs", 6);
                investmentPreferences.put("Crypto", 7);
                
                financialGoals.add("Short-term Gains");
                financialGoals.add("Market Timing");
                financialGoals.add("Portfolio Hedging");
                
                accountTypes.add("Margin Account");
                accountTypes.add("Brokerage");
                accountTypes.add("Options Account");
                
                riskProfile = "Aggressive";
                break;
                
            case "new_investor":
            default:
                investmentPreferences.put("ETFs", 8);
                investmentPreferences.put("Index Funds", 9);
                investmentPreferences.put("Stocks", 6);
                investmentPreferences.put("Bonds", 5);
                investmentPreferences.put("Crypto", 4);
                
                financialGoals.add("Wealth Building");
                financialGoals.add("Retirement");
                financialGoals.add("Home Purchase");
                
                accountTypes.add("401k");
                accountTypes.add("Brokerage");
                
                riskProfile = "Moderate";
        }
        
        result.put("investment_preferences", investmentPreferences);
        result.put("financial_goals", financialGoals);
        result.put("risk_profile", riskProfile);
        result.put("account_types", accountTypes);
        
        return result;
    }
    
    /**
     * Generates sample transaction history for demo purposes.
     */
    public List<Map<String, Object>> generateSampleTransactions(String customerType) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        
        switch (customerType) {
            case "conservative_retiree":
                addTransaction(transactions, "2023-10-15", "BUY", 5000.00, "VYM", "ETF", "Vanguard High Dividend Yield ETF");
                addTransaction(transactions, "2023-10-15", "BUY", 5000.00, "BND", "ETF", "Vanguard Total Bond Market ETF");
                addTransaction(transactions, "2023-09-20", "BUY", 2500.00, "JNJ", "STOCK", "Johnson & Johnson");
                addTransaction(transactions, "2023-09-20", "BUY", 2500.00, "PG", "STOCK", "Procter & Gamble");
                addTransaction(transactions, "2023-08-10", "DIVIDEND", 350.00, "VYM", "ETF", "Dividend Payment");
                break;
                
            case "growth_investor":
                addTransaction(transactions, "2023-10-18", "BUY", 3000.00, "AAPL", "STOCK", "Apple Inc.");
                addTransaction(transactions, "2023-10-18", "BUY", 3000.00, "MSFT", "STOCK", "Microsoft Corporation");
                addTransaction(transactions, "2023-10-05", "BUY", 5000.00, "QQQ", "ETF", "Invesco QQQ Trust");
                addTransaction(transactions, "2023-09-15", "SELL", 2800.00, "TSLA", "STOCK", "Tesla Inc.");
                addTransaction(transactions, "2023-08-22", "BUY", 1000.00, "BTC", "CRYPTO", "Bitcoin");
                break;
                
            case "active_trader":
                addTransaction(transactions, "2023-10-20", "BUY_OPTION", 1500.00, "SPY", "CALL_OPTION", "SPY Call Option Dec 2023");
                addTransaction(transactions, "2023-10-18", "SELL", 4200.00, "NVDA", "STOCK", "NVIDIA Corporation");
                addTransaction(transactions, "2023-10-15", "BUY", 4000.00, "AMZN", "STOCK", "Amazon.com Inc.");
                addTransaction(transactions, "2023-10-10", "SELL_OPTION", 2200.00, "AAPL", "PUT_OPTION", "AAPL Put Option Nov 2023");
                addTransaction(transactions, "2023-10-05", "BUY", 3000.00, "ETH", "CRYPTO", "Ethereum");
                break;
                
            case "new_investor":
            default:
                addTransaction(transactions, "2023-10-15", "BUY", 2000.00, "VOO", "ETF", "Vanguard S&P 500 ETF");
                addTransaction(transactions, "2023-10-15", "BUY", 1000.00, "VTI", "ETF", "Vanguard Total Stock Market ETF");
                addTransaction(transactions, "2023-09-20", "BUY", 500.00, "AAPL", "STOCK", "Apple Inc.");
                addTransaction(transactions, "2023-09-20", "BUY", 500.00, "MSFT", "STOCK", "Microsoft Corporation");
                addTransaction(transactions, "2023-08-10", "DEPOSIT", 5000.00, null, "CASH", "Initial Deposit");
        }
        
        return transactions;
    }
    
    private void addTransaction(List<Map<String, Object>> transactions, String date, String type, 
                               double amount, String symbol, String category, String description) {
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("date", date);
        transaction.put("type", type);
        transaction.put("amount", amount);
        if (symbol != null) transaction.put("symbol", symbol);
        transaction.put("category", category);
        transaction.put("description", description);
        
        transactions.add(transaction);
    }
}
