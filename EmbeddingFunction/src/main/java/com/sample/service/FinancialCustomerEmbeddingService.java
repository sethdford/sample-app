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
        "Individual Stocks", "Corporate Bonds", "Municipal Bonds", "Treasury Securities", 
        "Index ETFs", "Sector ETFs", "Active Mutual Funds", "Index Mutual Funds", 
        "Options Trading", "Futures", "Forex", "Commodities",
        "Cryptocurrency", "REITs", "Target Date Funds", "ESG Investments"
    );
    
    private static final List<String> FINANCIAL_GOALS = List.of(
        "Retirement Planning", "Wealth Accumulation", "Education Funding", "Estate Planning", 
        "Tax-Efficient Investing", "Income Generation", "Capital Preservation", "Inheritance Management",
        "Charitable Giving", "Business Succession", "Early Retirement", "Financial Independence"
    );
    
    private static final List<String> RISK_PROFILES = List.of(
        "Conservative", "Moderately Conservative", "Moderate", 
        "Moderately Aggressive", "Aggressive", "Very Aggressive"
    );
    
    private static final List<String> ACCOUNT_TYPES = List.of(
        "Individual Brokerage", "Joint Brokerage", "Traditional IRA", "Roth IRA", "SEP IRA", 
        "401(k)", "403(b)", "529 College Savings", "Trust Account", "UGMA/UTMA", 
        "Margin Account", "Options Approved Account", "Cash Management Account"
    );
    
    private static final List<String> ADVISORY_SERVICES = List.of(
        "Self-Directed", "Robo-Advisory", "Hybrid Advisory", "Full-Service Advisory", 
        "Wealth Management", "Private Client Services", "Financial Planning", "Tax Planning"
    );
    
    private static final List<String> TRADING_BEHAVIORS = List.of(
        "Buy and Hold", "Dollar Cost Averaging", "Active Trading", "Day Trading", 
        "Swing Trading", "Dividend Investing", "Value Investing", "Growth Investing", 
        "Momentum Trading", "Sector Rotation", "Asset Allocation Rebalancing"
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
        EmbeddingMetadata metadata = new EmbeddingMetadata("financial_profile", "amazon.titan-embed-text-v2");
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
        EmbeddingMetadata metadata = new EmbeddingMetadata("call_transcript", "amazon.titan-embed-text-v2");
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
        EmbeddingMetadata metadata = new EmbeddingMetadata("transaction_history", "amazon.titan-embed-text-v2");
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
        
        StringBuilder processedText = new StringBuilder("Financial investor profile: ");
        
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
                    preferenceLevel = "strongly allocates to";
                } else if (level >= 5) {
                    preferenceLevel = "moderately allocates to";
                } else if (level >= 3) {
                    preferenceLevel = "slightly allocates to";
                } else {
                    preferenceLevel = "minimal allocation to";
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
        StringBuilder processedText = new StringBuilder("Investment activity history: ");
        
        for (Map<String, Object> transaction : transactions) {
            processedText.append("Activity: ");
            processedText.append("Date=").append(transaction.get("date")).append(", ");
            processedText.append("Type=").append(transaction.get("type")).append(", ");
            
            if (transaction.containsKey("amount")) {
                processedText.append("Amount=").append(transaction.get("amount")).append(", ");
            }
            
            if (transaction.containsKey("shares")) {
                processedText.append("Shares=").append(transaction.get("shares")).append(", ");
            }
            
            if (transaction.containsKey("price")) {
                processedText.append("Price=").append(transaction.get("price")).append(", ");
            }
            
            if (transaction.containsKey("symbol")) {
                processedText.append("Symbol=").append(transaction.get("symbol")).append(", ");
            }
            
            if (transaction.containsKey("assetClass")) {
                processedText.append("AssetClass=").append(transaction.get("assetClass")).append(", ");
            }
            
            if (transaction.containsKey("sector")) {
                processedText.append("Sector=").append(transaction.get("sector")).append(", ");
            }
            
            if (transaction.containsKey("accountType")) {
                processedText.append("AccountType=").append(transaction.get("accountType")).append(", ");
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
        Map<String, Object> keyTerms = new HashMap<>();
        List<String> terms = new ArrayList<>();
        terms.add("retirement planning");
        terms.add("portfolio allocation");
        terms.add("asset diversification");
        terms.add("market volatility");
        terms.add("tax-loss harvesting");
        terms.add("required minimum distributions");
        terms.add("estate planning");
        
        keyTerms.put("terms", terms);
        keyTerms.put("concerns", List.of("market volatility", "retirement income", "tax efficiency"));
        
        return keyTerms;
    }
    
    /**
     * Calculate statistics from transaction data.
     */
    private Map<String, Object> calculateTransactionStats(List<Map<String, Object>> transactions) {
        // In a real implementation, this would calculate various statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("buy_count", 12);
        stats.put("sell_count", 8);
        stats.put("deposit_count", 4);
        stats.put("withdrawal_count", 2);
        stats.put("dividend_count", 6);
        stats.put("total_buy_amount", 25000.00);
        stats.put("total_sell_amount", 18500.00);
        stats.put("total_deposit_amount", 10000.00);
        stats.put("total_withdrawal_amount", 5000.00);
        stats.put("total_dividend_amount", 1200.00);
        stats.put("most_traded_symbol", "AAPL");
        stats.put("most_active_account_type", "Roth IRA");
        stats.put("asset_allocation", Map.of(
            "Stocks", 65.0,
            "Bonds", 20.0,
            "Cash", 10.0,
            "Other", 5.0
        ));
        
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
    public Map<String, Object> generateSampleFinancialProfile(String investorType) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Integer> investmentPreferences = new HashMap<>();
        List<String> financialGoals = new ArrayList<>();
        List<String> accountTypes = new ArrayList<>();
        String riskProfile;
        String advisoryService;
        
        switch (investorType) {
            case "retiree":
                investmentPreferences.put("Municipal Bonds", 9);
                investmentPreferences.put("Dividend Stocks", 7);
                investmentPreferences.put("Index ETFs", 6);
                investmentPreferences.put("Index Mutual Funds", 8);
                investmentPreferences.put("REITs", 5);
                
                financialGoals.add("Income Generation");
                financialGoals.add("Capital Preservation");
                financialGoals.add("Estate Planning");
                
                accountTypes.add("Traditional IRA");
                accountTypes.add("Roth IRA");
                accountTypes.add("Individual Brokerage");
                
                riskProfile = "Conservative";
                advisoryService = "Full-Service Advisory";
                break;
                
            case "growth_investor":
                investmentPreferences.put("Growth Stocks", 9);
                investmentPreferences.put("Index ETFs", 7);
                investmentPreferences.put("Sector ETFs", 6);
                investmentPreferences.put("Cryptocurrency", 4);
                investmentPreferences.put("Corporate Bonds", 3);
                
                financialGoals.add("Wealth Accumulation");
                financialGoals.add("Retirement Planning");
                financialGoals.add("Tax-Efficient Investing");
                
                accountTypes.add("401(k)");
                accountTypes.add("Individual Brokerage");
                accountTypes.add("Roth IRA");
                
                riskProfile = "Moderately Aggressive";
                advisoryService = "Hybrid Advisory";
                break;
                
            case "active_trader":
                investmentPreferences.put("Options Trading", 9);
                investmentPreferences.put("Individual Stocks", 9);
                investmentPreferences.put("Sector ETFs", 6);
                investmentPreferences.put("Cryptocurrency", 7);
                investmentPreferences.put("Futures", 5);
                
                financialGoals.add("Wealth Accumulation");
                financialGoals.add("Active Income Generation");
                financialGoals.add("Market Outperformance");
                
                accountTypes.add("Margin Account");
                accountTypes.add("Options Approved Account");
                accountTypes.add("Individual Brokerage");
                
                riskProfile = "Aggressive";
                advisoryService = "Self-Directed";
                break;
                
            case "new_investor":
            default:
                investmentPreferences.put("Index ETFs", 9);
                investmentPreferences.put("Target Date Funds", 8);
                investmentPreferences.put("Individual Stocks", 5);
                investmentPreferences.put("Corporate Bonds", 4);
                
                financialGoals.add("Wealth Accumulation");
                financialGoals.add("Retirement Planning");
                financialGoals.add("Education Funding");
                
                accountTypes.add("401(k)");
                accountTypes.add("Individual Brokerage");
                
                riskProfile = "Moderate";
                advisoryService = "Robo-Advisory";
        }
        
        result.put("investment_preferences", investmentPreferences);
        result.put("financial_goals", financialGoals);
        result.put("risk_profile", riskProfile);
        result.put("account_types", accountTypes);
        result.put("advisory_service", advisoryService);
        
        return result;
    }
    
    /**
     * Generates sample transaction history for demo purposes.
     */
    public List<Map<String, Object>> generateSampleTransactions(String investorType) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        
        switch (investorType) {
            case "retiree":
                addInvestmentActivity(transactions, "2023-10-15", "BUY", 5000.00, 125.0, 40.0, "VYM", "ETF", "Equity", "Vanguard High Dividend Yield ETF", "Traditional IRA");
                addInvestmentActivity(transactions, "2023-10-15", "BUY", 5000.00, 80.0, 62.5, "BND", "ETF", "Fixed Income", "Vanguard Total Bond Market ETF", "Traditional IRA");
                addInvestmentActivity(transactions, "2023-09-20", "BUY", 2500.00, 15.0, 166.67, "JNJ", "STOCK", "Healthcare", "Johnson & Johnson", "Individual Brokerage");
                addInvestmentActivity(transactions, "2023-09-20", "BUY", 2500.00, 16.0, 156.25, "PG", "STOCK", "Consumer Staples", "Procter & Gamble", "Individual Brokerage");
                addInvestmentActivity(transactions, "2023-08-10", "DIVIDEND", 350.00, null, null, "VYM", "ETF", "Equity", "Dividend Payment", "Traditional IRA");
                addInvestmentActivity(transactions, "2023-07-15", "RMD", 4500.00, null, null, null, "CASH", null, "Required Minimum Distribution", "Traditional IRA");
                break;
                
            case "growth_investor":
                addInvestmentActivity(transactions, "2023-10-18", "BUY", 3000.00, 15.0, 200.0, "AAPL", "STOCK", "Technology", "Apple Inc.", "Individual Brokerage");
                addInvestmentActivity(transactions, "2023-10-18", "BUY", 3000.00, 10.0, 300.0, "MSFT", "STOCK", "Technology", "Microsoft Corporation", "Individual Brokerage");
                addInvestmentActivity(transactions, "2023-10-05", "BUY", 5000.00, 12.0, 416.67, "QQQ", "ETF", "Technology", "Invesco QQQ Trust", "Roth IRA");
                addInvestmentActivity(transactions, "2023-09-15", "SELL", 2800.00, 10.0, 280.0, "TSLA", "STOCK", "Automotive", "Tesla Inc.", "Individual Brokerage");
                addInvestmentActivity(transactions, "2023-08-22", "BUY", 1000.00, 0.02, 50000.0, "BTC", "CRYPTO", "Cryptocurrency", "Bitcoin", "Individual Brokerage");
                addInvestmentActivity(transactions, "2023-08-01", "401K_CONTRIBUTION", 1500.00, null, null, "VFIAX", "MUTUAL_FUND", "Equity", "Vanguard 500 Index Fund", "401(k)");
                break;
                
            case "active_trader":
                addInvestmentActivity(transactions, "2023-10-20", "BUY_OPTION", 1500.00, 5.0, 300.0, "SPY", "CALL_OPTION", "Equity", "SPY Call Option Dec 2023", "Options Approved Account");
                addInvestmentActivity(transactions, "2023-10-18", "SELL", 4200.00, 10.0, 420.0, "NVDA", "STOCK", "Technology", "NVIDIA Corporation", "Margin Account");
                addInvestmentActivity(transactions, "2023-10-15", "BUY", 4000.00, 25.0, 160.0, "AMZN", "STOCK", "Consumer Discretionary", "Amazon.com Inc.", "Margin Account");
                addInvestmentActivity(transactions, "2023-10-10", "SELL_OPTION", 2200.00, 10.0, 220.0, "AAPL", "PUT_OPTION", "Technology", "AAPL Put Option Nov 2023", "Options Approved Account");
                addInvestmentActivity(transactions, "2023-10-05", "BUY", 3000.00, 1.5, 2000.0, "ETH", "CRYPTO", "Cryptocurrency", "Ethereum", "Individual Brokerage");
                addInvestmentActivity(transactions, "2023-09-28", "MARGIN_INTEREST", -125.00, null, null, null, "FEE", null, "Margin Interest Payment", "Margin Account");
                break;
                
            case "new_investor":
            default:
                addInvestmentActivity(transactions, "2023-10-15", "BUY", 2000.00, 5.0, 400.0, "VOO", "ETF", "Equity", "Vanguard S&P 500 ETF", "Individual Brokerage");
                addInvestmentActivity(transactions, "2023-10-15", "BUY", 1000.00, 4.0, 250.0, "VTI", "ETF", "Equity", "Vanguard Total Stock Market ETF", "Individual Brokerage");
                addInvestmentActivity(transactions, "2023-09-20", "BUY", 500.00, 2.5, 200.0, "AAPL", "STOCK", "Technology", "Apple Inc.", "Individual Brokerage");
                addInvestmentActivity(transactions, "2023-09-20", "BUY", 500.00, 1.5, 333.33, "MSFT", "STOCK", "Technology", "Microsoft Corporation", "Individual Brokerage");
                addInvestmentActivity(transactions, "2023-08-10", "DEPOSIT", 5000.00, null, null, null, "CASH", null, "Initial Deposit", "Individual Brokerage");
                addInvestmentActivity(transactions, "2023-07-01", "401K_CONTRIBUTION", 1000.00, null, null, "VFIFX", "TARGET_DATE", "2050", "Vanguard Target Retirement 2050 Fund", "401(k)");
        }
        
        return transactions;
    }
    
    private void addInvestmentActivity(
            List<Map<String, Object>> transactions, 
            String date, 
            String type, 
            Double amount, 
            Double shares, 
            Double price, 
            String symbol, 
            String assetClass, 
            String sector, 
            String description,
            String accountType) {
        
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("date", date);
        transaction.put("type", type);
        
        if (amount != null) transaction.put("amount", amount);
        if (shares != null) transaction.put("shares", shares);
        if (price != null) transaction.put("price", price);
        if (symbol != null) transaction.put("symbol", symbol);
        if (assetClass != null) transaction.put("assetClass", assetClass);
        if (sector != null) transaction.put("sector", sector);
        if (description != null) transaction.put("description", description);
        if (accountType != null) transaction.put("accountType", accountType);
        
        transactions.add(transaction);
    }
    
    /**
     * Generates an embedding from customer financial profile with specified dimensions.
     * Titan v2 supports dimensions of 256, 512, and 1024.
     * 
     * @param customerId Customer ID
     * @param investmentPreferences Map of investment types to preference levels
     * @param financialGoals List of financial goals
     * @param riskProfile Customer's risk tolerance profile
     * @param accountTypes List of account types (IRA, 401k, brokerage, etc.)
     * @param dimensions The desired embedding dimensions (256, 512, or 1024)
     * @return The generated embedding
     */
    public double[] generateFinancialProfileEmbeddingWithDimension(
            String customerId, 
            Map<String, Integer> investmentPreferences,
            List<String> financialGoals,
            String riskProfile,
            List<String> accountTypes,
            int dimensions) throws Exception {
        
        // 1. Process financial profile into a text format
        String processedText = processFinancialProfile(
            investmentPreferences, financialGoals, riskProfile, accountTypes);
        
        // 2. Generate embedding using the processed text with specified dimensions
        long startTime = System.currentTimeMillis();
        double[] embedding = embeddingService.generateEmbeddingWithDimension(processedText, dimensions);
        long endTime = System.currentTimeMillis();
        
        // 3. Create metadata
        EmbeddingMetadata metadata = new EmbeddingMetadata("financial_profile", "amazon.titan-embed-text-v2");
        metadata.addEncodingDetail("investment_preferences_count", investmentPreferences.size());
        metadata.addEncodingDetail("financial_goals_count", financialGoals.size());
        metadata.addEncodingDetail("risk_profile", riskProfile);
        metadata.addEncodingDetail("account_types_count", accountTypes.size());
        metadata.addEncodingDetail("dimensions", dimensions);
        metadata.addPerformanceMetric("generation_time_ms", endTime - startTime);
        
        // 4. Store in DynamoDB with metadata
        dynamoDBService.storeEmbeddingWithMetadata(
            customerId, 
            "FINANCIAL_PROFILE_" + dimensions, 
            embedding, 
            metadata
        );
        
        return embedding;
    }
    
    /**
     * Generates an embedding from customer call transcript with sentiment analysis and specified dimensions.
     * 
     * @param customerId Customer ID
     * @param callId Unique call identifier
     * @param transcriptText The call transcript text
     * @param callCategory Category of the call (e.g., "account_issue", "investment_advice")
     * @param dimensions The desired embedding dimensions (256, 512, or 1024)
     * @return Map containing embedding and sentiment analysis
     */
    public Map<String, Object> processCallTranscriptWithDimension(
            String customerId, 
            String callId,
            String transcriptText,
            String callCategory,
            int dimensions) throws Exception {
        
        // 1. Generate embedding from transcript with specified dimensions
        long startTime = System.currentTimeMillis();
        double[] embedding = embeddingService.generateEmbeddingWithDimension(transcriptText, dimensions);
        long endTime = System.currentTimeMillis();
        
        // 2. Perform sentiment analysis
        Map<String, Object> sentimentResult = analyzeSentiment(transcriptText);
        
        // 3. Extract key financial terms and concerns
        Map<String, Object> keyTerms = extractFinancialTerms(transcriptText);
        
        // 4. Create metadata
        EmbeddingMetadata metadata = new EmbeddingMetadata("call_transcript", "amazon.titan-embed-text-v2");
        metadata.addEncodingDetail("call_id", callId);
        metadata.addEncodingDetail("call_category", callCategory);
        metadata.addEncodingDetail("transcript_length", transcriptText.length());
        metadata.addEncodingDetail("sentiment", sentimentResult.get("sentiment"));
        metadata.addEncodingDetail("dimensions", dimensions);
        metadata.addPerformanceMetric("generation_time_ms", endTime - startTime);
        
        // 5. Store in DynamoDB with metadata
        dynamoDBService.storeEmbeddingWithMetadata(
            customerId, 
            "CALL_TRANSCRIPT_" + callId + "_" + dimensions, 
            embedding, 
            metadata
        );
        
        // 6. Return results
        Map<String, Object> result = new HashMap<>();
        result.put("embedding", embedding);
        result.put("sentiment", sentimentResult);
        result.put("key_terms", keyTerms);
        result.put("dimensions", dimensions);
        
        return result;
    }
    
    /**
     * Generates an embedding from customer transaction history with specified dimensions.
     * 
     * @param customerId Customer ID
     * @param transactions List of transaction data
     * @param timeframe Timeframe of the transactions (e.g., "last_30_days", "last_quarter")
     * @param dimensions The desired embedding dimensions (256, 512, or 1024)
     * @return The generated embedding
     */
    public double[] processTransactionHistoryWithDimension(
            String customerId, 
            List<Map<String, Object>> transactions,
            String timeframe,
            int dimensions) throws Exception {
        
        // 1. Process transactions into a text format
        String processedText = processTransactions(transactions);
        
        // 2. Generate embedding using the processed text with specified dimensions
        long startTime = System.currentTimeMillis();
        double[] embedding = embeddingService.generateEmbeddingWithDimension(processedText, dimensions);
        long endTime = System.currentTimeMillis();
        
        // 3. Calculate transaction statistics
        Map<String, Object> transactionStats = calculateTransactionStats(transactions);
        
        // 4. Create metadata
        EmbeddingMetadata metadata = new EmbeddingMetadata("transaction_history", "amazon.titan-embed-text-v2");
        metadata.addEncodingDetail("transaction_count", transactions.size());
        metadata.addEncodingDetail("timeframe", timeframe);
        metadata.addEncodingDetail("transaction_stats", transactionStats);
        metadata.addEncodingDetail("dimensions", dimensions);
        metadata.addPerformanceMetric("generation_time_ms", endTime - startTime);
        
        // 5. Store in DynamoDB with metadata
        dynamoDBService.storeEmbeddingWithMetadata(
            customerId, 
            "TRANSACTION_HISTORY_" + timeframe + "_" + dimensions, 
            embedding, 
            metadata
        );
        
        return embedding;
    }
}
