package com.sample.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sample.model.EmbeddingMetadata;

// Remove CloudWatch imports and use mock implementations
// import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
// import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
// import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
// import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsResponse;
// import software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent;
// import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
// import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsResponse;
// import software.amazon.awssdk.services.cloudwatchlogs.model.OutputLogEvent;

/**
 * Service for generating embeddings from CloudWatch logs.
 * This service extracts user behavior patterns from log data and generates embeddings
 * that can be used for anomaly detection, user journey analysis, and system monitoring.
 * It also identifies high client effort patterns such as repeated errors, excessive navigation,
 * channel switching, and repeated button clicks.
 */
public class CloudWatchLogEmbeddingService {
    
    private final EmbeddingService embeddingService;
    private final EnhancedDynamoDBService dynamoDBService;
    // Use a mock implementation instead of the actual CloudWatchLogsClient
    // private final CloudWatchLogsClient cloudWatchLogsClient;
    
    // Constants for log event types
    public static final String LOG_TYPE_API_GATEWAY = "api_gateway";
    public static final String LOG_TYPE_LAMBDA = "lambda";
    public static final String LOG_TYPE_APPLICATION = "application";
    public static final String LOG_TYPE_ERROR = "error";
    
    // Constants for embedding type
    public static final String EMBEDDING_TYPE_CLOUDWATCH = "cloudwatch_logs";
    public static final String EMBEDDING_TYPE_CLIENT_EFFORT = "client_effort";
    
    // Patterns for extracting information from logs
    private static final Pattern USER_ID_PATTERN = Pattern.compile("userId[\"']?\\s*[:=]\\s*[\"']?([\\w-]+)[\"']?", Pattern.CASE_INSENSITIVE);
    private static final Pattern API_PATH_PATTERN = Pattern.compile("\"?path\"?\\s*[:=]\\s*\"?(/[\\w/]+)\"?", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTTP_METHOD_PATTERN = Pattern.compile("\"?httpMethod\"?\\s*[:=]\\s*\"?(GET|POST|PUT|DELETE|PATCH)\"?", Pattern.CASE_INSENSITIVE);
    private static final Pattern STATUS_CODE_PATTERN = Pattern.compile("\"?statusCode\"?\\s*[:=]\\s*\"?(\\d{3})\"?", Pattern.CASE_INSENSITIVE);
    private static final Pattern ERROR_PATTERN = Pattern.compile("\\b(error|exception|failed|timeout|denied)\\b", Pattern.CASE_INSENSITIVE);
    
    // Patterns for detecting high client effort
    private static final Pattern BUTTON_CLICK_PATTERN = Pattern.compile("\\b(click|button|submit|tap)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAVIGATION_PATTERN = Pattern.compile("\\b(navigate|page|view|screen|back|forward)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern CHANNEL_SWITCH_PATTERN = Pattern.compile("\\b(channel|switch|mobile|web|app|desktop|device)\\b", Pattern.CASE_INSENSITIVE);
    
    // Thresholds for high client effort detection
    private static final int ERROR_THRESHOLD = 3; // Number of errors in a session
    private static final int NAVIGATION_THRESHOLD = 10; // Number of back-and-forth navigations
    private static final int BUTTON_CLICK_THRESHOLD = 5; // Number of repeated button clicks
    private static final int CHANNEL_SWITCH_THRESHOLD = 2; // Number of channel switches
    private static final long TIME_WINDOW_MS = 300000; // 5 minutes in milliseconds
    
    /**
     * Constructor that initializes the service with default clients.
     */
    public CloudWatchLogEmbeddingService() {
        this.embeddingService = new EmbeddingService();
        this.dynamoDBService = new EnhancedDynamoDBService();
        // Use a mock implementation instead of the actual CloudWatchLogsClient
        // this.cloudWatchLogsClient = CloudWatchLogsClient.create();
    }
    
    /**
     * Constructor that allows for dependency injection (useful for testing).
     */
    public CloudWatchLogEmbeddingService(EmbeddingService embeddingService, 
                                        EnhancedDynamoDBService dynamoDBService) {
        this.embeddingService = embeddingService;
        this.dynamoDBService = dynamoDBService;
        // Use a mock implementation instead of the actual CloudWatchLogsClient
        // this.cloudWatchLogsClient = cloudWatchLogsClient;
    }
    
    /**
     * Generates an embedding from CloudWatch logs for a specific user.
     * 
     * @param userId User ID to filter logs for
     * @param logGroupName CloudWatch log group name
     * @param hoursBack Number of hours to look back for logs
     * @return The generated embedding
     */
    public double[] generateUserLogEmbedding(String userId, String logGroupName, int hoursBack) throws Exception {
        // 1. Fetch logs for the user
        List<LogEvent> userLogs = fetchUserLogs(userId, logGroupName, hoursBack);
        
        if (userLogs.isEmpty()) {
            throw new IllegalArgumentException("No logs found for user: " + userId);
        }
        
        // 2. Process logs into a format suitable for embedding
        String processedText = processUserLogs(userLogs);
        
        // 3. Generate embedding
        long startTime = System.currentTimeMillis();
        double[] embedding = embeddingService.generateEmbedding(processedText);
        long endTime = System.currentTimeMillis();
        
        // 4. Create metadata
        EmbeddingMetadata metadata = new EmbeddingMetadata(EMBEDDING_TYPE_CLOUDWATCH, "amazon.titan-embed-text-v1");
        metadata.addEncodingDetail("log_group", logGroupName);
        metadata.addEncodingDetail("hours_back", hoursBack);
        metadata.addEncodingDetail("log_count", userLogs.size());
        metadata.addEncodingDetail("timestamp", System.currentTimeMillis());
        metadata.addPerformanceMetric("generation_time_ms", endTime - startTime);
        
        // 5. Store in DynamoDB
        dynamoDBService.storeEmbeddingWithMetadata(
            userId, 
            EMBEDDING_TYPE_CLOUDWATCH, 
            embedding, 
            metadata
        );
        
        return embedding;
    }
    
    /**
     * Fetches logs for a specific user from CloudWatch.
     * This is a mock implementation that doesn't actually call CloudWatch.
     */
    private List<LogEvent> fetchUserLogs(String userId, String logGroupName, int hoursBack) {
        // In a real implementation, this would call CloudWatch Logs API
        // For now, we'll generate sample logs
        List<String> apiLogs = generateSampleLogs(userId, LOG_TYPE_API_GATEWAY, 50);
        List<String> lambdaLogs = generateSampleLogs(userId, LOG_TYPE_LAMBDA, 30);
        List<String> appLogs = generateSampleLogs(userId, LOG_TYPE_APPLICATION, 40);
        List<String> errorLogs = generateSampleLogs(userId, LOG_TYPE_ERROR, 5);
        
        // Convert to LogEvent objects
        List<LogEvent> allEvents = new ArrayList<>();
        
        long currentTime = System.currentTimeMillis();
        long timeIncrement = hoursBack * 3600000L / 125; // Spread events over the time period
        
        for (int i = 0; i < apiLogs.size(); i++) {
            allEvents.add(new LogEvent(apiLogs.get(i), currentTime - (i * timeIncrement)));
        }
        
        for (int i = 0; i < lambdaLogs.size(); i++) {
            allEvents.add(new LogEvent(lambdaLogs.get(i), currentTime - (i * timeIncrement)));
        }
        
        for (int i = 0; i < appLogs.size(); i++) {
            allEvents.add(new LogEvent(appLogs.get(i), currentTime - (i * timeIncrement)));
        }
        
        for (int i = 0; i < errorLogs.size(); i++) {
            allEvents.add(new LogEvent(errorLogs.get(i), currentTime - (i * timeIncrement)));
        }
        
        return allEvents;
    }
    
    /**
     * Process user logs into a format suitable for embedding generation.
     */
    private String processUserLogs(List<LogEvent> logs) {
        StringBuilder processedText = new StringBuilder("User activity log summary: ");
        
        // Extract key information from logs
        Map<String, Integer> apiPathCounts = new HashMap<>();
        Map<String, Integer> statusCodeCounts = new HashMap<>();
        Map<String, Integer> errorCounts = new HashMap<>();
        int successfulRequests = 0;
        int failedRequests = 0;
        
        for (LogEvent log : logs) {
            String message = log.getMessage();
            
            // Extract API path if present
            Matcher pathMatcher = API_PATH_PATTERN.matcher(message);
            if (pathMatcher.find()) {
                String path = pathMatcher.group(1);
                apiPathCounts.put(path, apiPathCounts.getOrDefault(path, 0) + 1);
            }
            
            // Extract status code if present
            Matcher statusMatcher = STATUS_CODE_PATTERN.matcher(message);
            if (statusMatcher.find()) {
                String statusCode = statusMatcher.group(1);
                statusCodeCounts.put(statusCode, statusCodeCounts.getOrDefault(statusCode, 0) + 1);
                
                // Count successful vs failed requests
                if (statusCode.startsWith("2")) {
                    successfulRequests++;
                } else if (statusCode.startsWith("4") || statusCode.startsWith("5")) {
                    failedRequests++;
                }
            }
            
            // Extract errors if present
            Matcher errorMatcher = ERROR_PATTERN.matcher(message);
            if (errorMatcher.find()) {
                String error = errorMatcher.group(1).toLowerCase();
                errorCounts.put(error, errorCounts.getOrDefault(error, 0) + 1);
            }
        }
        
        // Build the processed text
        processedText.append("Total log entries: ").append(logs.size()).append(". ");
        
        // Add API path information
        if (!apiPathCounts.isEmpty()) {
            processedText.append("API paths accessed: ");
            for (Map.Entry<String, Integer> entry : apiPathCounts.entrySet()) {
                processedText.append(entry.getKey()).append(" (").append(entry.getValue()).append(" times), ");
            }
            processedText.setLength(processedText.length() - 2); // Remove trailing comma and space
            processedText.append(". ");
        }
        
        // Add status code information
        if (!statusCodeCounts.isEmpty()) {
            processedText.append("Status codes: ");
            for (Map.Entry<String, Integer> entry : statusCodeCounts.entrySet()) {
                processedText.append(entry.getKey()).append(" (").append(entry.getValue()).append(" times), ");
            }
            processedText.setLength(processedText.length() - 2); // Remove trailing comma and space
            processedText.append(". ");
        }
        
        // Add success/failure information
        processedText.append("Successful requests: ").append(successfulRequests)
                    .append(", Failed requests: ").append(failedRequests).append(". ");
        
        // Add error information
        if (!errorCounts.isEmpty()) {
            processedText.append("Errors encountered: ");
            for (Map.Entry<String, Integer> entry : errorCounts.entrySet()) {
                processedText.append(entry.getKey()).append(" (").append(entry.getValue()).append(" times), ");
            }
            processedText.setLength(processedText.length() - 2); // Remove trailing comma and space
            processedText.append(". ");
        }
        
        // Add chronological sequence of the most recent events (up to 10)
        int recentLogsCount = Math.min(logs.size(), 10);
        if (recentLogsCount > 0) {
            processedText.append("Recent activity sequence: ");
            
            // Sort logs by timestamp (most recent logs first)
            logs.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
            
            for (int i = 0; i < recentLogsCount; i++) {
                LogEvent log = logs.get(i);
                String message = log.getMessage();
                
                // Extract key information for the sequence
                String path = extractPattern(API_PATH_PATTERN, message, "unknown_path");
                String method = extractPattern(HTTP_METHOD_PATTERN, message, "");
                String statusCode = extractPattern(STATUS_CODE_PATTERN, message, "");
                
                if (!method.isEmpty() && !statusCode.isEmpty()) {
                    processedText.append(method).append(" ").append(path)
                                .append(" (").append(statusCode).append("), ");
                } else {
                    // For non-API logs, just add a summary
                    if (message.length() > 50) {
                        message = message.substring(0, 47) + "...";
                    }
                    processedText.append("Log: ").append(message).append(", ");
                }
            }
            
            processedText.setLength(processedText.length() - 2); // Remove trailing comma and space
        }
        
        return processedText.toString();
    }
    
    /**
     * Helper method to extract a pattern from text.
     */
    private String extractPattern(Pattern pattern, String text, String defaultValue) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : defaultValue;
    }
    
    /**
     * Analyzes user behavior based on CloudWatch logs.
     * 
     * @param userId User ID to analyze
     * @param logGroupName CloudWatch log group name
     * @param hoursBack Number of hours to look back for logs
     * @return Map containing behavior analysis results
     */
    public Map<String, Object> analyzeUserBehavior(String userId, String logGroupName, int hoursBack) throws Exception {
        // 1. Fetch logs for the user
        List<LogEvent> userLogs = fetchUserLogs(userId, logGroupName, hoursBack);
        
        if (userLogs.isEmpty()) {
            throw new IllegalArgumentException("No logs found for user: " + userId);
        }
        
        // 2. Analyze logs
        Map<String, Object> analysis = new HashMap<>();
        
        // Basic metrics
        analysis.put("total_logs", userLogs.size());
        analysis.put("time_period_hours", hoursBack);
        
        // Extract API paths and count occurrences
        Map<String, Integer> apiPaths = new HashMap<>();
        Map<String, Integer> statusCodes = new HashMap<>();
        Map<String, Integer> errorTypes = new HashMap<>();
        List<Map<String, Object>> userJourney = new ArrayList<>();
        
        for (LogEvent log : userLogs) {
            String message = log.getMessage();
            
            // Extract API path
            Matcher pathMatcher = API_PATH_PATTERN.matcher(message);
            if (pathMatcher.find()) {
                String path = pathMatcher.group(1);
                apiPaths.put(path, apiPaths.getOrDefault(path, 0) + 1);
            }
            
            // Extract status code
            Matcher statusMatcher = STATUS_CODE_PATTERN.matcher(message);
            if (statusMatcher.find()) {
                String statusCode = statusMatcher.group(1);
                statusCodes.put(statusCode, statusCodes.getOrDefault(statusCode, 0) + 1);
            }
            
            // Extract errors
            Matcher errorMatcher = ERROR_PATTERN.matcher(message);
            if (errorMatcher.find()) {
                String error = errorMatcher.group(1).toLowerCase();
                errorTypes.put(error, errorTypes.getOrDefault(error, 0) + 1);
            }
            
            // Build user journey (for the 20 most recent events)
            if (userJourney.size() < 20) {
                Map<String, Object> journeyStep = new HashMap<>();
                journeyStep.put("timestamp", log.getTimestamp());
                
                String path = extractPattern(API_PATH_PATTERN, message, null);
                if (path != null) {
                    journeyStep.put("path", path);
                }
                
                String method = extractPattern(HTTP_METHOD_PATTERN, message, null);
                if (method != null) {
                    journeyStep.put("method", method);
                }
                
                String statusCode = extractPattern(STATUS_CODE_PATTERN, message, null);
                if (statusCode != null) {
                    journeyStep.put("status_code", statusCode);
                }
                
                if (!journeyStep.isEmpty()) {
                    userJourney.add(journeyStep);
                }
            }
        }
        
        // Add analysis results
        analysis.put("api_paths", apiPaths);
        analysis.put("status_codes", statusCodes);
        analysis.put("error_types", errorTypes);
        
        // Sort user journey by timestamp
        userJourney.sort((a, b) -> Long.compare((Long) a.get("timestamp"), (Long) b.get("timestamp")));
        analysis.put("user_journey", userJourney);
        
        // Calculate success rate
        int successfulRequests = statusCodes.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("2"))
            .mapToInt(Map.Entry::getValue)
            .sum();
        
        int totalRequests = statusCodes.values().stream().mapToInt(Integer::intValue).sum();
        double successRate = totalRequests > 0 ? (double) successfulRequests / totalRequests : 0;
        analysis.put("success_rate", successRate);
        
        return analysis;
    }
    
    /**
     * Detects anomalies in user behavior by comparing current logs with historical embedding.
     * 
     * @param userId User ID to analyze
     * @param logGroupName CloudWatch log group name
     * @param hoursBack Number of hours to look back for current logs
     * @return Map containing anomaly detection results
     */
    public Map<String, Object> detectAnomalies(String userId, String logGroupName, int hoursBack) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        // 1. Get historical embedding for the user
        double[] historicalEmbedding = dynamoDBService.getEmbedding(userId, EMBEDDING_TYPE_CLOUDWATCH);
        
        // 2. Generate current embedding
        double[] currentEmbedding = generateUserLogEmbedding(userId, logGroupName, hoursBack);
        
        // 3. Compare embeddings
        double similarity = calculateCosineSimilarity(historicalEmbedding, currentEmbedding);
        result.put("similarity_score", similarity);
        
        // 4. Determine if there's an anomaly
        boolean isAnomaly = similarity < 0.7; // Threshold can be adjusted
        result.put("is_anomaly", isAnomaly);
        
        // 5. Get detailed behavior analysis
        Map<String, Object> behaviorAnalysis = analyzeUserBehavior(userId, logGroupName, hoursBack);
        result.put("behavior_analysis", behaviorAnalysis);
        
        return result;
    }
    
    /**
     * Calculates cosine similarity between two embeddings.
     */
    private double calculateCosineSimilarity(double[] embedding1, double[] embedding2) {
        if (embedding1 == null || embedding2 == null || embedding1.length != embedding2.length) {
            return 0.0;
        }
        
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
     * Generates sample CloudWatch log data for demo purposes.
     * 
     * @param userId User ID to generate logs for
     * @param logType Type of logs to generate
     * @param count Number of log entries to generate
     * @return List of sample log entries
     */
    public List<String> generateSampleLogs(String userId, String logType, int count) {
        List<String> logs = new ArrayList<>();
        
        switch (logType) {
            case LOG_TYPE_API_GATEWAY:
                logs.addAll(generateSampleApiGatewayLogs(userId, count));
                break;
            case LOG_TYPE_LAMBDA:
                logs.addAll(generateSampleLambdaLogs(userId, count));
                break;
            case LOG_TYPE_APPLICATION:
                logs.addAll(generateSampleApplicationLogs(userId, count));
                break;
            case LOG_TYPE_ERROR:
                logs.addAll(generateSampleErrorLogs(userId, count));
                break;
            default:
                throw new IllegalArgumentException("Unsupported log type: " + logType);
        }
        
        return logs;
    }
    
    /**
     * Generates sample API Gateway logs.
     */
    private List<String> generateSampleApiGatewayLogs(String userId, int count) {
        List<String> logs = new ArrayList<>();
        String[] paths = {"/api/users", "/api/products", "/api/orders", "/api/cart", "/api/checkout"};
        String[] methods = {"GET", "POST", "PUT", "DELETE"};
        String[] statusCodes = {"200", "201", "400", "401", "403", "404", "500"};
        
        for (int i = 0; i < count; i++) {
            String path = paths[i % paths.length];
            String method = methods[i % methods.length];
            String statusCode = i % 10 == 0 ? statusCodes[i % statusCodes.length] : "200"; // 10% error rate
            
            StringBuilder log = new StringBuilder();
            log.append("{")
               .append("\"requestId\":\"req-").append(System.currentTimeMillis()).append("\",")
               .append("\"path\":\"").append(path).append("\",")
               .append("\"httpMethod\":\"").append(method).append("\",")
               .append("\"statusCode\":\"").append(statusCode).append("\",")
               .append("\"responseLatency\":").append(50 + (int)(Math.random() * 200)).append(",")
               .append("\"userId\":\"").append(userId).append("\",")
               .append("\"userAgent\":\"Mozilla/5.0\",")
               .append("\"timestamp\":").append(System.currentTimeMillis())
               .append("}");
            
            logs.add(log.toString());
        }
        
        return logs;
    }
    
    /**
     * Generates sample Lambda logs.
     */
    private List<String> generateSampleLambdaLogs(String userId, int count) {
        List<String> logs = new ArrayList<>();
        String[] functions = {"UserService", "ProductService", "OrderService", "NotificationService"};
        String[] actions = {"getUser", "createUser", "updateUser", "getProducts", "createOrder", "processPayment"};
        
        for (int i = 0; i < count; i++) {
            String function = functions[i % functions.length];
            String action = actions[i % actions.length];
            boolean isError = i % 15 == 0; // ~7% error rate
            
            StringBuilder log = new StringBuilder();
            log.append("START RequestId: ").append(System.currentTimeMillis()).append("\n");
            log.append("2023-06-15T12:34:56.789Z\t").append(System.currentTimeMillis()).append("\t");
            
            if (isError) {
                log.append("ERROR\t")
                   .append("Error executing ").append(action).append(" in ").append(function).append(": ")
                   .append("Resource not found or access denied\n")
                   .append("For userId: ").append(userId);
            } else {
                log.append("INFO\t")
                   .append("Successfully executed ").append(action).append(" in ").append(function).append("\n")
                   .append("Processing time: ").append(50 + (int)(Math.random() * 150)).append(" ms\n")
                   .append("For userId: ").append(userId);
            }
            
            logs.add(log.toString());
        }
        
        return logs;
    }
    
    /**
     * Generates sample application logs.
     */
    private List<String> generateSampleApplicationLogs(String userId, int count) {
        List<String> logs = new ArrayList<>();
        String[] actions = {"login", "logout", "viewProfile", "updateProfile", "addToCart", "checkout", "viewOrder"};
        
        for (int i = 0; i < count; i++) {
            String action = actions[i % actions.length];
            
            StringBuilder log = new StringBuilder();
            log.append("[").append(Instant.now()).append("] ")
               .append("[INFO] ")
               .append("User action: ").append(action).append(" ")
               .append("userId: ").append(userId).append(" ")
               .append("sessionId: sess-").append(System.currentTimeMillis()).append(" ")
               .append("ip: 192.168.").append(1 + (int)(Math.random() * 254)).append(".")
                                     .append(1 + (int)(Math.random() * 254));
            
            logs.add(log.toString());
        }
        
        return logs;
    }
    
    /**
     * Generates sample error logs.
     */
    private List<String> generateSampleErrorLogs(String userId, int count) {
        List<String> logs = new ArrayList<>();
        String[] errorTypes = {"NullPointerException", "ResourceNotFoundException", "AccessDeniedException", 
                              "TimeoutException", "ValidationException"};
        String[] components = {"UserService", "OrderService", "PaymentService", "InventoryService"};
        
        for (int i = 0; i < count; i++) {
            String errorType = errorTypes[i % errorTypes.length];
            String component = components[i % components.length];
            
            StringBuilder log = new StringBuilder();
            log.append("[").append(Instant.now()).append("] ")
               .append("[ERROR] ")
               .append(errorType).append(": Error in ").append(component).append(" ")
               .append("for userId: ").append(userId).append("\n")
               .append("Stack trace: com.sample.service.").append(component).append(".processRequest(")
               .append(component).append(".java:").append(100 + (int)(Math.random() * 900)).append(")");
            
            logs.add(log.toString());
        }
        
        return logs;
    }
    
    /**
     * Generates an embedding specifically focused on client effort patterns.
     * This embedding captures patterns of high effort such as repeated errors,
     * excessive navigation, channel switching, and repeated button clicks.
     * 
     * @param userId User ID to filter logs for
     * @param logGroupName CloudWatch log group name
     * @param hoursBack Number of hours to look back for logs
     * @return The generated client effort embedding
     */
    public double[] generateClientEffortEmbedding(String userId, String logGroupName, int hoursBack) throws Exception {
        // 1. Fetch logs for the user
        List<LogEvent> userLogs = fetchUserLogs(userId, logGroupName, hoursBack);
        
        if (userLogs.isEmpty()) {
            throw new IllegalArgumentException("No logs found for user: " + userId);
        }
        
        // 2. Analyze client effort patterns
        Map<String, Object> effortAnalysis = analyzeClientEffort(userLogs);
        
        // 3. Process logs into a format suitable for embedding with focus on effort
        String processedText = processClientEffortLogs(userLogs, effortAnalysis);
        
        // 4. Generate embedding
        long startTime = System.currentTimeMillis();
        double[] embedding = embeddingService.generateEmbedding(processedText);
        long endTime = System.currentTimeMillis();
        
        // 5. Create metadata
        EmbeddingMetadata metadata = new EmbeddingMetadata(EMBEDDING_TYPE_CLIENT_EFFORT, "amazon.titan-embed-text-v1");
        metadata.addEncodingDetail("log_group", logGroupName);
        metadata.addEncodingDetail("hours_back", hoursBack);
        metadata.addEncodingDetail("log_count", userLogs.size());
        metadata.addEncodingDetail("timestamp", System.currentTimeMillis());
        metadata.addPerformanceMetric("generation_time_ms", endTime - startTime);
        
        // Add effort metrics to metadata
        for (Map.Entry<String, Object> entry : effortAnalysis.entrySet()) {
            if (entry.getValue() instanceof Number) {
                metadata.addEncodingDetail("effort_" + entry.getKey(), entry.getValue());
            }
        }
        
        // 6. Store in DynamoDB
        dynamoDBService.storeEmbeddingWithMetadata(
            userId, 
            EMBEDDING_TYPE_CLIENT_EFFORT, 
            embedding, 
            metadata
        );
        
        return embedding;
    }
    
    /**
     * Analyzes client effort patterns in user logs.
     * 
     * @param logs List of user log events
     * @return Map containing client effort analysis results
     */
    public Map<String, Object> analyzeClientEffort(List<LogEvent> logs) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Sort logs by timestamp (oldest first)
        logs.sort(Comparator.comparing(LogEvent::getTimestamp));
        
        // Count errors
        int errorCount = 0;
        for (LogEvent log : logs) {
            if (ERROR_PATTERN.matcher(log.getMessage()).find()) {
                errorCount++;
            }
        }
        analysis.put("error_count", errorCount);
        analysis.put("high_error_rate", errorCount >= ERROR_THRESHOLD);
        
        // Detect repeated button clicks
        Map<String, List<Long>> buttonClicks = new HashMap<>();
        for (LogEvent log : logs) {
            Matcher buttonMatcher = BUTTON_CLICK_PATTERN.matcher(log.getMessage());
            if (buttonMatcher.find()) {
                String buttonAction = buttonMatcher.group(1).toLowerCase();
                
                // Extract more context to identify the specific button
                String context = extractContext(log.getMessage(), buttonMatcher.start(), 30);
                String buttonKey = buttonAction + "_" + context.hashCode();
                
                if (!buttonClicks.containsKey(buttonKey)) {
                    buttonClicks.put(buttonKey, new ArrayList<>());
                }
                buttonClicks.get(buttonKey).add(log.getTimestamp());
            }
        }
        
        // Count repeated button clicks within time window
        int repeatedClickCount = 0;
        for (List<Long> timestamps : buttonClicks.values()) {
            repeatedClickCount += countRepeatedActions(timestamps, TIME_WINDOW_MS, BUTTON_CLICK_THRESHOLD);
        }
        analysis.put("repeated_click_count", repeatedClickCount);
        analysis.put("high_repeated_clicks", repeatedClickCount > 0);
        
        // Detect back-and-forth navigation
        List<NavigationEvent> navigationEvents = new ArrayList<>();
        for (LogEvent log : logs) {
            Matcher navMatcher = NAVIGATION_PATTERN.matcher(log.getMessage());
            if (navMatcher.find()) {
                // Extract path if available
                String path = null;
                Matcher pathMatcher = API_PATH_PATTERN.matcher(log.getMessage());
                if (pathMatcher.find()) {
                    path = pathMatcher.group(1);
                } else {
                    // Try to extract some context for the navigation
                    path = extractContext(log.getMessage(), navMatcher.start(), 30);
                }
                
                navigationEvents.add(new NavigationEvent(path, log.getTimestamp()));
            }
        }
        
        // Detect back-and-forth navigation patterns
        int backForthCount = detectBackAndForthNavigation(navigationEvents);
        analysis.put("back_forth_navigation_count", backForthCount);
        analysis.put("high_back_forth_navigation", backForthCount >= NAVIGATION_THRESHOLD);
        
        // Detect channel switching
        List<ChannelEvent> channelEvents = new ArrayList<>();
        for (LogEvent log : logs) {
            Matcher channelMatcher = CHANNEL_SWITCH_PATTERN.matcher(log.getMessage());
            if (channelMatcher.find()) {
                String channel = channelMatcher.group(1).toLowerCase();
                // Try to determine the actual channel
                if (log.getMessage().toLowerCase().contains("mobile")) {
                    channel = "mobile";
                } else if (log.getMessage().toLowerCase().contains("web")) {
                    channel = "web";
                } else if (log.getMessage().toLowerCase().contains("app")) {
                    channel = "app";
                } else if (log.getMessage().toLowerCase().contains("desktop")) {
                    channel = "desktop";
                }
                
                channelEvents.add(new ChannelEvent(channel, log.getTimestamp()));
            }
        }
        
        // Count channel switches
        int channelSwitchCount = countChannelSwitches(channelEvents);
        analysis.put("channel_switch_count", channelSwitchCount);
        analysis.put("high_channel_switching", channelSwitchCount >= CHANNEL_SWITCH_THRESHOLD);
        
        // Calculate overall client effort score (0-100)
        double effortScore = calculateClientEffortScore(
            errorCount, 
            repeatedClickCount, 
            backForthCount, 
            channelSwitchCount
        );
        analysis.put("effort_score", effortScore);
        analysis.put("high_effort", effortScore >= 50);
        
        return analysis;
    }
    
    /**
     * Processes logs into a format suitable for client effort embedding generation.
     * 
     * @param logs List of user log events
     * @param effortAnalysis Results of client effort analysis
     * @return Processed text for embedding generation
     */
    private String processClientEffortLogs(List<LogEvent> logs, Map<String, Object> effortAnalysis) {
        StringBuilder processedText = new StringBuilder("User client effort analysis: ");
        
        // Add overall effort score
        double effortScore = (double) effortAnalysis.get("effort_score");
        processedText.append("Overall client effort score: ").append(String.format("%.1f", effortScore)).append("/100. ");
        
        // Add high effort indicators
        boolean highEffort = (boolean) effortAnalysis.get("high_effort");
        if (highEffort) {
            processedText.append("HIGH CLIENT EFFORT DETECTED. ");
        }
        
        // Add error information
        int errorCount = (int) effortAnalysis.get("error_count");
        boolean highErrorRate = (boolean) effortAnalysis.get("high_error_rate");
        processedText.append("Errors encountered: ").append(errorCount);
        if (highErrorRate) {
            processedText.append(" (HIGH ERROR RATE)");
        }
        processedText.append(". ");
        
        // Add repeated click information
        int repeatedClickCount = (int) effortAnalysis.get("repeated_click_count");
        boolean highRepeatedClicks = (boolean) effortAnalysis.get("high_repeated_clicks");
        processedText.append("Repeated button clicks: ").append(repeatedClickCount);
        if (highRepeatedClicks) {
            processedText.append(" (EXCESSIVE CLICKING DETECTED)");
        }
        processedText.append(". ");
        
        // Add navigation information
        int backForthCount = (int) effortAnalysis.get("back_forth_navigation_count");
        boolean highBackForthNavigation = (boolean) effortAnalysis.get("high_back_forth_navigation");
        processedText.append("Back-and-forth navigation: ").append(backForthCount);
        if (highBackForthNavigation) {
            processedText.append(" (EXCESSIVE NAVIGATION DETECTED)");
        }
        processedText.append(". ");
        
        // Add channel switching information
        int channelSwitchCount = (int) effortAnalysis.get("channel_switch_count");
        boolean highChannelSwitching = (boolean) effortAnalysis.get("high_channel_switching");
        processedText.append("Channel switches: ").append(channelSwitchCount);
        if (highChannelSwitching) {
            processedText.append(" (FREQUENT CHANNEL SWITCHING DETECTED)");
        }
        processedText.append(". ");
        
        // Add chronological sequence of high-effort events
        List<LogEvent> highEffortLogs = new ArrayList<>();
        for (LogEvent log : logs) {
            String message = log.getMessage();
            if (ERROR_PATTERN.matcher(message).find() || 
                BUTTON_CLICK_PATTERN.matcher(message).find() || 
                NAVIGATION_PATTERN.matcher(message).find() || 
                CHANNEL_SWITCH_PATTERN.matcher(message).find()) {
                highEffortLogs.add(log);
            }
        }
        
        // Sort by timestamp and take the most recent 15 high-effort events
        highEffortLogs.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        int recentLogsCount = Math.min(highEffortLogs.size(), 15);
        
        if (recentLogsCount > 0) {
            processedText.append("Recent high-effort events: ");
            
            for (int i = 0; i < recentLogsCount; i++) {
                LogEvent log = highEffortLogs.get(i);
                String message = log.getMessage();
                
                // Create a summary of the high-effort event
                StringBuilder eventSummary = new StringBuilder();
                
                if (ERROR_PATTERN.matcher(message).find()) {
                    eventSummary.append("ERROR: ");
                }
                
                if (BUTTON_CLICK_PATTERN.matcher(message).find()) {
                    eventSummary.append("CLICK: ");
                }
                
                if (NAVIGATION_PATTERN.matcher(message).find()) {
                    eventSummary.append("NAVIGATION: ");
                }
                
                if (CHANNEL_SWITCH_PATTERN.matcher(message).find()) {
                    eventSummary.append("CHANNEL: ");
                }
                
                // Add a summary of the message
                if (message.length() > 50) {
                    eventSummary.append(message.substring(0, 47)).append("...");
                } else {
                    eventSummary.append(message);
                }
                
                processedText.append(eventSummary).append(", ");
            }
            
            // Remove trailing comma and space
            processedText.setLength(processedText.length() - 2);
        }
        
        return processedText.toString();
    }
    
    /**
     * Extracts context around a specific position in a string.
     * 
     * @param text The text to extract context from
     * @param position The position to extract context around
     * @param length The maximum length of context to extract
     * @return The extracted context
     */
    private String extractContext(String text, int position, int length) {
        int start = Math.max(0, position - length / 2);
        int end = Math.min(text.length(), position + length / 2);
        return text.substring(start, end).trim();
    }
    
    /**
     * Counts repeated actions within a time window.
     * 
     * @param timestamps List of timestamps when actions occurred
     * @param timeWindowMs Time window in milliseconds
     * @param threshold Minimum number of actions to consider as repeated
     * @return Number of instances where actions were repeated within the time window
     */
    private int countRepeatedActions(List<Long> timestamps, long timeWindowMs, int threshold) {
        int repeatedCount = 0;
        
        if (timestamps.size() < threshold) {
            return 0;
        }
        
        // Sort timestamps
        Collections.sort(timestamps);
        
        // Sliding window to find repeated actions
        for (int i = 0; i <= timestamps.size() - threshold; i++) {
            long startTime = timestamps.get(i);
            long endTime = timestamps.get(i + threshold - 1);
            
            if (endTime - startTime <= timeWindowMs) {
                repeatedCount++;
                i += threshold - 1; // Skip ahead to avoid double counting
            }
        }
        
        return repeatedCount;
    }
    
    /**
     * Detects back-and-forth navigation patterns.
     * 
     * @param events List of navigation events
     * @return Count of back-and-forth navigation patterns
     */
    private int detectBackAndForthNavigation(List<NavigationEvent> events) {
        int backForthCount = 0;
        
        if (events.size() < 3) {
            return 0;
        }
        
        // Look for A -> B -> A patterns
        for (int i = 0; i < events.size() - 2; i++) {
            String pathA = events.get(i).getPath();
            String pathB = events.get(i + 1).getPath();
            String pathC = events.get(i + 2).getPath();
            
            // Check if A -> B -> A pattern (back to the same page)
            if (pathA != null && pathA.equals(pathC) && !pathA.equals(pathB)) {
                backForthCount++;
                i += 2; // Skip ahead to avoid double counting
            }
        }
        
        return backForthCount;
    }
    
    /**
     * Counts channel switches.
     * 
     * @param events List of channel events
     * @return Count of channel switches
     */
    private int countChannelSwitches(List<ChannelEvent> events) {
        int switchCount = 0;
        
        if (events.size() < 2) {
            return 0;
        }
        
        String currentChannel = events.get(0).getChannel();
        
        for (int i = 1; i < events.size(); i++) {
            String nextChannel = events.get(i).getChannel();
            
            if (!currentChannel.equals(nextChannel)) {
                switchCount++;
                currentChannel = nextChannel;
            }
        }
        
        return switchCount;
    }
    
    /**
     * Calculates an overall client effort score based on various metrics.
     * 
     * @param errorCount Number of errors
     * @param repeatedClickCount Number of repeated button clicks
     * @param backForthCount Number of back-and-forth navigations
     * @param channelSwitchCount Number of channel switches
     * @return Client effort score (0-100)
     */
    private double calculateClientEffortScore(int errorCount, int repeatedClickCount, 
                                             int backForthCount, int channelSwitchCount) {
        // Normalize each component to a 0-25 scale
        double errorScore = Math.min(25, (errorCount / (double) ERROR_THRESHOLD) * 25);
        double clickScore = Math.min(25, (repeatedClickCount / (double) BUTTON_CLICK_THRESHOLD) * 25);
        double navigationScore = Math.min(25, (backForthCount / (double) NAVIGATION_THRESHOLD) * 25);
        double channelScore = Math.min(25, (channelSwitchCount / (double) CHANNEL_SWITCH_THRESHOLD) * 25);
        
        // Sum the components for a total score out of 100
        return errorScore + clickScore + navigationScore + channelScore;
    }
    
    /**
     * Generates sample logs with high client effort patterns.
     * 
     * @param userId User ID to generate logs for
     * @param effortLevel Level of client effort to simulate (1-5, where 5 is highest)
     * @param count Number of log entries to generate
     * @return List of sample log entries
     */
    public List<String> generateHighEffortLogs(String userId, int effortLevel, int count) {
        List<String> logs = new ArrayList<>();
        
        // Adjust error rate based on effort level (5-25%)
        int errorRate = 5 * effortLevel;
        
        // Generate API Gateway logs with errors
        String[] paths = {"/api/users", "/api/products", "/api/orders", "/api/cart", "/api/checkout"};
        String[] methods = {"GET", "POST", "PUT", "DELETE"};
        String[] statusCodes = {"400", "401", "403", "404", "500", "503"};
        
        // Simulate back-and-forth navigation based on effort level
        List<String> navigationSequence = new ArrayList<>();
        if (effortLevel >= 3) {
            // Add more back-and-forth for higher effort levels
            navigationSequence.add("/api/products");
            navigationSequence.add("/api/products/123");
            navigationSequence.add("/api/products");
            navigationSequence.add("/api/cart");
            navigationSequence.add("/api/products");
            navigationSequence.add("/api/cart");
            navigationSequence.add("/api/checkout");
            navigationSequence.add("/api/cart");
            navigationSequence.add("/api/checkout");
        } else {
            // Less back-and-forth for lower effort levels
            navigationSequence.add("/api/products");
            navigationSequence.add("/api/products/123");
            navigationSequence.add("/api/cart");
            navigationSequence.add("/api/checkout");
        }
        
        // Generate logs with navigation patterns
        for (int i = 0; i < count / 2; i++) {
            String path = navigationSequence.get(i % navigationSequence.size());
            String method = "GET";
            String statusCode = (i % 100 < errorRate) ? statusCodes[i % statusCodes.length] : "200";
            
            StringBuilder log = new StringBuilder();
            log.append("{")
               .append("\"requestId\":\"req-").append(System.currentTimeMillis()).append("\",")
               .append("\"path\":\"").append(path).append("\",")
               .append("\"httpMethod\":\"").append(method).append("\",")
               .append("\"statusCode\":\"").append(statusCode).append("\",")
               .append("\"responseLatency\":").append(50 + (int)(Math.random() * 200)).append(",")
               .append("\"userId\":\"").append(userId).append("\",")
               .append("\"userAgent\":\"Mozilla/5.0\",")
               .append("\"timestamp\":").append(System.currentTimeMillis())
               .append("}");
            
            logs.add(log.toString());
        }
        
        // Generate button click logs
        String[] buttons = {"submit", "search", "add_to_cart", "checkout", "apply_filter"};
        for (int i = 0; i < count / 4; i++) {
            String button = buttons[i % buttons.length];
            
            // Simulate repeated clicks for higher effort levels
            int repeats = 1;
            if (effortLevel >= 4 && i % 5 == 0) {
                repeats = 3 + effortLevel; // More repeats for higher effort
            } else if (effortLevel >= 2 && i % 10 == 0) {
                repeats = 2 + effortLevel;
            }
            
            for (int j = 0; j < repeats; j++) {
                StringBuilder log = new StringBuilder();
                log.append("[").append(Instant.now()).append("] ")
                   .append("[INFO] ")
                   .append("User action: button_click ")
                   .append("button: ").append(button).append(" ")
                   .append("userId: ").append(userId).append(" ")
                   .append("sessionId: sess-").append(System.currentTimeMillis()).append(" ")
                   .append("timestamp: ").append(System.currentTimeMillis());
                
                logs.add(log.toString());
                
                // Small delay between logs to simulate rapid clicking
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }
        
        // Generate channel switching logs
        if (effortLevel >= 2) {
            String[] channels = {"web", "mobile", "app", "desktop"};
            int switchCount = Math.min(channels.length, effortLevel);
            
            for (int i = 0; i < switchCount; i++) {
                StringBuilder log = new StringBuilder();
                log.append("[").append(Instant.now()).append("] ")
                   .append("[INFO] ")
                   .append("Channel switch detected: ")
                   .append("from: ").append(channels[i % channels.length]).append(" ")
                   .append("to: ").append(channels[(i + 1) % channels.length]).append(" ")
                   .append("userId: ").append(userId).append(" ")
                   .append("sessionId: sess-").append(System.currentTimeMillis()).append(" ")
                   .append("timestamp: ").append(System.currentTimeMillis());
                
                logs.add(log.toString());
            }
        }
        
        // Generate error logs
        int errorCount = (count / 4) * errorRate / 100;
        String[] errorTypes = {"ValidationException", "TimeoutException", "ResourceNotFoundException"};
        String[] components = {"UserService", "OrderService", "PaymentService", "InventoryService"};
        
        for (int i = 0; i < errorCount; i++) {
            String errorType = errorTypes[i % errorTypes.length];
            String component = components[i % components.length];
            
            StringBuilder log = new StringBuilder();
            log.append("[").append(Instant.now()).append("] ")
               .append("[ERROR] ")
               .append(errorType).append(": Error in ").append(component).append(" ")
               .append("for userId: ").append(userId).append("\n")
               .append("Stack trace: com.sample.service.").append(component).append(".processRequest(")
               .append(component).append(".java:").append(100 + (int)(Math.random() * 900)).append(")");
            
            logs.add(log.toString());
        }
        
        return logs;
    }
    
    /**
     * Helper class to represent a navigation event.
     */
    private static class NavigationEvent {
        private final String path;
        private final long timestamp;
        
        public NavigationEvent(String path, long timestamp) {
            this.path = path;
            this.timestamp = timestamp;
        }
        
        public String getPath() {
            return path;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * Helper class to represent a channel event.
     */
    private static class ChannelEvent {
        private final String channel;
        private final long timestamp;
        
        public ChannelEvent(String channel, long timestamp) {
            this.channel = channel;
            this.timestamp = timestamp;
        }
        
        public String getChannel() {
            return channel;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * Simple class to replace FilteredLogEvent from CloudWatch SDK
     */
    private static class LogEvent {
        private final String message;
        private final long timestamp;
        
        public LogEvent(String message, long timestamp) {
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public String getMessage() {
            return message;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
} 