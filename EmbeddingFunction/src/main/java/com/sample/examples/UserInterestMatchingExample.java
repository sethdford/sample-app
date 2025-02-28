package com.sample.examples;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

/**
 * Example application that matches users with similar interests
 * This demonstrates how to use AI-generated embeddings to find users with similar interests
 */
public class UserInterestMatchingExample {

    private static final String TABLE_NAME = "UserEmbeddings";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DynamoDbClient dynamoDb = DynamoDbClient.builder()
            .region(Region.US_EAST_1) // Change to your preferred region
            .build();
    private static final EmbeddingService embeddingService = new EmbeddingService();
    private static final UserMatchingService matchingService = new UserMatchingService(dynamoDb, embeddingService);
    
    public static void main(String[] args) {
        System.out.println("=== User Interest Matching System ===");
        System.out.println("This application demonstrates how to match users with similar interests");
        System.out.println("using AI-generated embeddings from AWS Bedrock.");
        System.out.println();
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Create a new user profile");
            System.out.println("2. Find users with similar interests");
            System.out.println("3. View user profile");
            System.out.println("4. Exit");
            System.out.print("Enter your choice (1-4): ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            switch (choice) {
                case 1:
                    createUserProfile(scanner);
                    break;
                case 2:
                    findSimilarUsers(scanner);
                    break;
                case 3:
                    viewUserProfile(scanner);
                    break;
                case 4:
                    System.out.println("Exiting application. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void createUserProfile(Scanner scanner) {
        System.out.println("\n=== Create New User Profile ===");
        
        System.out.print("Enter user name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter user age: ");
        int age = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        
        System.out.print("Enter user location: ");
        String location = scanner.nextLine();
        
        System.out.println("Describe your interests (hobbies, activities, preferences, etc.):");
        String interests = scanner.nextLine();
        
        // Generate a unique user ID
        String userId = "user-" + UUID.randomUUID().toString().substring(0, 8);
        
        try {
            // Store the user profile with embedding
            UserProfile profile = matchingService.createUserProfile(userId, name, age, location, interests);
            
            System.out.println("\nUser profile created successfully!");
            System.out.println("User ID: " + profile.getUserId());
            System.out.println("Remember this ID to find similar users later.");
            
        } catch (Exception e) {
            System.err.println("Error creating user profile: " + e.getMessage());
        }
    }
    
    private static void findSimilarUsers(Scanner scanner) {
        System.out.println("\n=== Find Users with Similar Interests ===");
        
        System.out.print("Enter your user ID: ");
        String userId = scanner.nextLine();
        
        System.out.print("How many similar users would you like to find? ");
        int limit = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        
        try {
            // Find similar users
            List<UserProfileMatch> matches = matchingService.findSimilarUsers(userId, limit);
            
            if (matches.isEmpty()) {
                System.out.println("No similar users found. Try creating more user profiles.");
                return;
            }
            
            System.out.println("\nUsers with similar interests:");
            System.out.println("-----------------------------");
            
            for (int i = 0; i < matches.size(); i++) {
                UserProfileMatch match = matches.get(i);
                System.out.printf("%d. %s (%.2f%% match)\n", 
                        i + 1, match.getProfile().getName(), match.getSimilarityPercentage());
                System.out.println("   Location: " + match.getProfile().getLocation());
                System.out.println("   Interests: " + match.getProfile().getInterests());
                System.out.println();
            }
            
        } catch (Exception e) {
            System.err.println("Error finding similar users: " + e.getMessage());
        }
    }
    
    private static void viewUserProfile(Scanner scanner) {
        System.out.println("\n=== View User Profile ===");
        
        System.out.print("Enter user ID: ");
        String userId = scanner.nextLine();
        
        try {
            UserProfile profile = matchingService.getUserProfile(userId);
            
            if (profile == null) {
                System.out.println("User profile not found. Please check the ID and try again.");
                return;
            }
            
            System.out.println("\nUser Profile:");
            System.out.println("-------------");
            System.out.println("Name: " + profile.getName());
            System.out.println("Age: " + profile.getAge());
            System.out.println("Location: " + profile.getLocation());
            System.out.println("Interests: " + profile.getInterests());
            
        } catch (Exception e) {
            System.err.println("Error retrieving user profile: " + e.getMessage());
        }
    }
    
    /**
     * Service class for generating embeddings using AWS Bedrock
     */
    public static class EmbeddingService {
        private final BedrockRuntimeClient bedrockClient;
        private final ObjectMapper objectMapper;
        
        // Model IDs for embedding models in Bedrock
        private static final String TITAN_EMBEDDING_MODEL = "amazon.titan-embed-text-v1";
        private static final String COHERE_EMBEDDING_MODEL = "cohere.embed-english-v3";
        
        public EmbeddingService() {
            this.bedrockClient = BedrockRuntimeClient.builder()
                    .region(Region.US_EAST_1) // Use your preferred region
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
            this.objectMapper = new ObjectMapper();
        }
        
        /**
         * Generate embedding for text using AWS Bedrock
         * @param text The text to generate embedding for
         * @return List of embedding values
         */
        public List<Double> generateEmbedding(String text) throws Exception {
            try {
                // Choose which model to use
                return generateTitanEmbedding(text);
                // Alternative: return generateCohereEmbedding(text);
            } catch (Exception e) {
                System.err.println("Error generating Bedrock embedding: " + e.getMessage());
                // Fall back to random embedding if Bedrock fails
                return generateRandomEmbedding();
            }
        }
        
        /**
         * Generate embedding using Amazon Titan model
         */
        private List<Double> generateTitanEmbedding(String text) throws Exception {
            // Create request body for Titan model
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("inputText", text);
            
            // Invoke Bedrock model
            InvokeModelResponse response = invokeBedrockModel(TITAN_EMBEDDING_MODEL, requestBody);
            
            // Parse response
            JsonNode responseBody = objectMapper.readTree(response.body().asByteArray());
            JsonNode embeddingNode = responseBody.get("embedding");
            
            // Convert to List<Double>
            List<Double> embedding = new ArrayList<>();
            if (embeddingNode.isArray()) {
                for (JsonNode value : embeddingNode) {
                    embedding.add(value.asDouble());
                }
            }
            
            return embedding;
        }
        
        /**
         * Generate embedding using Cohere model
         */
        private List<Double> generateCohereEmbedding(String text) throws Exception {
            // Create request body for Cohere model
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode textsNode = requestBody.putArray("texts");
            textsNode.add(text);
            requestBody.put("input_type", "search_document");
            
            // Invoke Bedrock model
            InvokeModelResponse response = invokeBedrockModel(COHERE_EMBEDDING_MODEL, requestBody);
            
            // Parse response
            JsonNode responseBody = objectMapper.readTree(response.body().asByteArray());
            JsonNode embeddingsNode = responseBody.get("embeddings");
            
            // Convert to List<Double>
            List<Double> embedding = new ArrayList<>();
            if (embeddingsNode.isArray() && embeddingsNode.size() > 0) {
                JsonNode firstEmbedding = embeddingsNode.get(0);
                if (firstEmbedding.isArray()) {
                    for (JsonNode value : firstEmbedding) {
                        embedding.add(value.asDouble());
                    }
                }
            }
            
            return embedding;
        }
        
        /**
         * Invoke Bedrock model with the given request body
         */
        private InvokeModelResponse invokeBedrockModel(String modelId, JsonNode requestBody) throws Exception {
            // Convert request body to JSON string
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            
            // Create request
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .body(SdkBytes.fromUtf8String(requestBodyJson))
                    .build();
            
            // Invoke model
            return bedrockClient.invokeModel(request);
        }
        
        /**
         * Generate random embedding as fallback
         */
        private List<Double> generateRandomEmbedding() {
            java.util.Random rand = new java.util.Random();
            List<Double> embedding = new ArrayList<>();
            for (int i = 0; i < 512; i++) {
                embedding.add(rand.nextDouble() * 2 - 1); // Values between -1 and 1
            }
            return embedding;
        }
    }
    
    /**
     * Service class for matching users with similar interests
     */
    public static class UserMatchingService {
        private final DynamoDbClient dynamoDb;
        private final EmbeddingService embeddingService;
        
        public UserMatchingService(DynamoDbClient dynamoDb, EmbeddingService embeddingService) {
            this.dynamoDb = dynamoDb;
            this.embeddingService = embeddingService;
        }
        
        /**
         * Create a new user profile with embedding
         */
        public UserProfile createUserProfile(String userId, String name, int age, String location, String interests) throws Exception {
            // Create a user profile
            UserProfile profile = new UserProfile(userId, name, age, location, interests);
            
            // Create a profile text for embedding generation
            String profileText = String.format(
                    "Name: %s. Age: %d. Location: %s. Interests: %s",
                    name, age, location, interests);
            
            // Generate embedding using Bedrock
            List<Double> embedding = embeddingService.generateEmbedding(profileText);
            
            // Store in DynamoDB
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("user_id", AttributeValue.builder().s(userId).build());
            item.put("embedding_type", AttributeValue.builder().s("profile").build());
            item.put("embedding", AttributeValue.builder().s(embedding.toString()).build());
            item.put("name", AttributeValue.builder().s(name).build());
            item.put("age", AttributeValue.builder().n(String.valueOf(age)).build());
            item.put("location", AttributeValue.builder().s(location).build());
            item.put("interests", AttributeValue.builder().s(interests).build());
            item.put("created_at", AttributeValue.builder().s(java.time.Instant.now().toString()).build());
            
            PutItemRequest request = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build();
            
            dynamoDb.putItem(request);
            
            return profile;
        }
        
        /**
         * Find users with similar interests
         */
        public List<UserProfileMatch> findSimilarUsers(String userId, int limit) throws Exception {
            // Get the user's embedding
            UserEmbedding userEmbedding = getUserEmbedding(userId);
            if (userEmbedding == null) {
                throw new IllegalArgumentException("No embedding found for user: " + userId);
            }
            
            // Get all user embeddings
            List<UserEmbedding> allEmbeddings = getAllUserEmbeddings();
            
            // Calculate similarity scores
            List<SimilarityResult> results = new ArrayList<>();
            for (UserEmbedding other : allEmbeddings) {
                // Skip the user we're comparing against
                if (other.getUserId().equals(userId)) {
                    continue;
                }
                
                double similarity = calculateCosineSimilarity(userEmbedding.getEmbedding(), other.getEmbedding());
                results.add(new SimilarityResult(other.getUserId(), similarity));
            }
            
            // Sort by similarity (highest first) and limit results
            List<SimilarityResult> topResults = results.stream()
                    .sorted(Comparator.comparing(SimilarityResult::getSimilarity).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
            
            // Convert to UserProfileMatch objects
            List<UserProfileMatch> matches = new ArrayList<>();
            for (SimilarityResult result : topResults) {
                UserProfile profile = getUserProfile(result.getUserId());
                if (profile != null) {
                    // Convert similarity score to percentage (0-100%)
                    double similarityPercentage = result.getSimilarity() * 100;
                    matches.add(new UserProfileMatch(profile, similarityPercentage));
                }
            }
            
            return matches;
        }
        
        /**
         * Get a user profile from DynamoDB
         */
        public UserProfile getUserProfile(String userId) {
            GetItemResponse response = dynamoDb.getItem(GetItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(Map.of(
                            "user_id", AttributeValue.builder().s(userId).build(),
                            "embedding_type", AttributeValue.builder().s("profile").build()))
                    .build());
            
            if (!response.hasItem()) {
                return null;
            }
            
            Map<String, AttributeValue> item = response.item();
            
            String name = item.containsKey("name") ? item.get("name").s() : "Unknown";
            int age = item.containsKey("age") ? Integer.parseInt(item.get("age").n()) : 0;
            String location = item.containsKey("location") ? item.get("location").s() : "Unknown";
            String interests = item.containsKey("interests") ? item.get("interests").s() : "";
            
            return new UserProfile(userId, name, age, location, interests);
        }
        
        /**
         * Get a user's embedding from DynamoDB
         */
        private UserEmbedding getUserEmbedding(String userId) throws Exception {
            GetItemResponse response = dynamoDb.getItem(GetItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(Map.of(
                            "user_id", AttributeValue.builder().s(userId).build(),
                            "embedding_type", AttributeValue.builder().s("profile").build()))
                    .build());
            
            if (!response.hasItem() || !response.item().containsKey("embedding")) {
                return null;
            }
            
            Map<String, AttributeValue> item = response.item();
            String embeddingJson = item.get("embedding").s();
            List<Double> embedding = parseEmbeddingFromJson(embeddingJson);
            
            return new UserEmbedding(userId, "profile", embedding);
        }
        
        /**
         * Get all user embeddings from DynamoDB
         */
        private List<UserEmbedding> getAllUserEmbeddings() throws Exception {
            List<UserEmbedding> results = new ArrayList<>();
            
            ScanResponse response = dynamoDb.scan(ScanRequest.builder()
                    .tableName(TABLE_NAME)
                    .filterExpression("embedding_type = :type")
                    .expressionAttributeValues(Map.of(
                            ":type", AttributeValue.builder().s("profile").build()))
                    .build());
            
            for (Map<String, AttributeValue> item : response.items()) {
                String userId = item.get("user_id").s();
                String embeddingJson = item.get("embedding").s();
                
                try {
                    List<Double> embedding = parseEmbeddingFromJson(embeddingJson);
                    results.add(new UserEmbedding(userId, "profile", embedding));
                } catch (Exception e) {
                    System.err.println("Error parsing embedding for user " + userId + ": " + e.getMessage());
                }
            }
            
            return results;
        }
        
        /**
         * Parse embedding from JSON string
         */
        private List<Double> parseEmbeddingFromJson(String embeddingJson) throws Exception {
            // Remove brackets and split by comma
            String cleaned = embeddingJson.replace("[", "").replace("]", "").trim();
            if (cleaned.isEmpty()) {
                return new ArrayList<>();
            }
            
            String[] values = cleaned.split(",");
            List<Double> embedding = new ArrayList<>();
            
            for (String value : values) {
                embedding.add(Double.parseDouble(value.trim()));
            }
            
            return embedding;
        }
        
        /**
         * Calculate cosine similarity between two embeddings
         */
        private double calculateCosineSimilarity(List<Double> embedding1, List<Double> embedding2) {
            if (embedding1.size() != embedding2.size()) {
                throw new IllegalArgumentException("Embeddings must have the same dimension");
            }
            
            double dotProduct = 0.0;
            double norm1 = 0.0;
            double norm2 = 0.0;
            
            for (int i = 0; i < embedding1.size(); i++) {
                dotProduct += embedding1.get(i) * embedding2.get(i);
                norm1 += Math.pow(embedding1.get(i), 2);
                norm2 += Math.pow(embedding2.get(i), 2);
            }
            
            // Avoid division by zero
            if (norm1 == 0 || norm2 == 0) {
                return 0.0;
            }
            
            return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        }
    }
    
    /**
     * Class to hold user embedding data
     */
    public static class UserEmbedding {
        private final String userId;
        private final String embeddingType;
        private final List<Double> embedding;
        
        public UserEmbedding(String userId, String embeddingType, List<Double> embedding) {
            this.userId = userId;
            this.embeddingType = embeddingType;
            this.embedding = embedding;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public String getEmbeddingType() {
            return embeddingType;
        }
        
        public List<Double> getEmbedding() {
            return embedding;
        }
    }
    
    /**
     * Class to represent a user profile
     */
    public static class UserProfile {
        private final String userId;
        private final String name;
        private final int age;
        private final String location;
        private final String interests;
        
        public UserProfile(String userId, String name, int age, String location, String interests) {
            this.userId = userId;
            this.name = name;
            this.age = age;
            this.location = location;
            this.interests = interests;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public String getName() {
            return name;
        }
        
        public int getAge() {
            return age;
        }
        
        public String getLocation() {
            return location;
        }
        
        public String getInterests() {
            return interests;
        }
    }
    
    /**
     * Class to represent a user profile match with similarity score
     */
    public static class UserProfileMatch {
        private final UserProfile profile;
        private final double similarityPercentage;
        
        public UserProfileMatch(UserProfile profile, double similarityPercentage) {
            this.profile = profile;
            this.similarityPercentage = similarityPercentage;
        }
        
        public UserProfile getProfile() {
            return profile;
        }
        
        public double getSimilarityPercentage() {
            return similarityPercentage;
        }
    }
    
    /**
     * Class to hold similarity search results
     */
    public static class SimilarityResult {
        private final String userId;
        private final double similarity;
        
        public SimilarityResult(String userId, double similarity) {
            this.userId = userId;
            this.similarity = similarity;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public double getSimilarity() {
            return similarity;
        }
    }
} 