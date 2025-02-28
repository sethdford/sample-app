package com.sample.service;

/**
 * Factory class for creating different embedding services.
 * This provides a centralized way to instantiate the appropriate embedding service
 * based on the type of data being processed.
 */
public class EmbeddingServiceFactory {
    
    // Singleton instance
    private static EmbeddingServiceFactory instance;
    
    // Service instances (lazy loaded)
    private EmbeddingService embeddingService;
    private BehavioralEmbeddingService behavioralEmbeddingService;
    private InterestEmbeddingService interestEmbeddingService;
    private FinancialInterestEmbeddingService financialInterestEmbeddingService;
    private CloudWatchLogEmbeddingService cloudWatchLogEmbeddingService;
    private EnhancedDynamoDBService dynamoDBService;
    
    // Private constructor for singleton pattern
    private EmbeddingServiceFactory() {
    }
    
    /**
     * Gets the singleton instance of the factory.
     */
    public static synchronized EmbeddingServiceFactory getInstance() {
        if (instance == null) {
            instance = new EmbeddingServiceFactory();
        }
        return instance;
    }
    
    /**
     * Gets the basic embedding service for raw text.
     */
    public EmbeddingService getEmbeddingService() {
        if (embeddingService == null) {
            embeddingService = new EmbeddingService();
        }
        return embeddingService;
    }
    
    /**
     * Gets the behavioral embedding service for user activity data.
     */
    public BehavioralEmbeddingService getBehavioralEmbeddingService() {
        if (behavioralEmbeddingService == null) {
            behavioralEmbeddingService = new BehavioralEmbeddingService();
        }
        return behavioralEmbeddingService;
    }
    
    /**
     * Gets the interest embedding service for user interests and preferences.
     */
    public InterestEmbeddingService getInterestEmbeddingService() {
        if (interestEmbeddingService == null) {
            interestEmbeddingService = new InterestEmbeddingService();
        }
        return interestEmbeddingService;
    }
    
    /**
     * Gets the financial interest embedding service for financial institution clients.
     */
    public FinancialInterestEmbeddingService getFinancialInterestEmbeddingService() {
        if (financialInterestEmbeddingService == null) {
            financialInterestEmbeddingService = new FinancialInterestEmbeddingService();
        }
        return financialInterestEmbeddingService;
    }
    
    /**
     * Gets the CloudWatch log embedding service for analyzing user behavior from logs.
     */
    public CloudWatchLogEmbeddingService getCloudWatchLogEmbeddingService() {
        if (cloudWatchLogEmbeddingService == null) {
            cloudWatchLogEmbeddingService = new CloudWatchLogEmbeddingService();
        }
        return cloudWatchLogEmbeddingService;
    }
    
    /**
     * Gets the enhanced DynamoDB service for storing embeddings.
     */
    public EnhancedDynamoDBService getDynamoDBService() {
        if (dynamoDBService == null) {
            dynamoDBService = new EnhancedDynamoDBService();
        }
        return dynamoDBService;
    }
    
    /**
     * Gets the appropriate service for the given data type.
     * 
     * @param dataType The type of data to process
     * @return The appropriate embedding service
     */
    public Object getServiceForDataType(String dataType) {
        switch (dataType.toLowerCase()) {
            case "text":
                return getEmbeddingService();
            case "user_attributes":
                return getEmbeddingService(); // Uses the UserAttributeFormatter
            case "behavior":
                return getBehavioralEmbeddingService();
            case "interests":
                return getInterestEmbeddingService();
            case "financial_profile":
                return getFinancialInterestEmbeddingService();
            case "cloudwatch_logs":
                return getCloudWatchLogEmbeddingService();
            case "client_effort":
                return getCloudWatchLogEmbeddingService();
            default:
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
        }
    }
} 