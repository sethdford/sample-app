package com.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

/**
 * Test class for generating embeddings using AWS Bedrock
 * This demonstrates how to replace the random embedding generation with real AI-generated embeddings
 */
public class BedrockEmbeddingTest {
    
    private BedrockEmbeddingService embeddingService;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TABLE_NAME = "UserEmbeddings";
    
    @BeforeEach
    public void setup() {
        // Initialize the embedding service
        embeddingService = new BedrockEmbeddingService();
    }
    
    @Test
    @Disabled("Requires AWS credentials with Bedrock access")
    public void testBedrockEmbeddingGeneration() {
        try {
            // Test text to generate embedding for
            String text = "This is a sample user profile text that describes interests in technology, AI, and cloud computing.";
            
            // Generate embedding using Bedrock
            List<Double> embedding = embeddingService.generateEmbedding(text);
            
            // Verify embedding properties
            assertNotNull(embedding);
            assertTrue(embedding.size() > 0, "Embedding should have values");
            
            // Print embedding size for information
            System.out.println("Generated embedding with " + embedding.size() + " dimensions");
            
            // Optional: Print a few values from the embedding
            System.out.println("Sample values: " + embedding.subList(0, Math.min(5, embedding.size())));
            
        } catch (Exception e) {
            fail("Bedrock embedding generation failed: " + e.getMessage());
        }
    }
    
    @Test
    @Disabled("Requires AWS credentials with Bedrock and DynamoDB access")
    public void testStoreBedrockEmbedding() {
        try {
            // Create a test user ID
            String userId = "bedrock-test-user-" + UUID.randomUUID().toString().substring(0, 8);
            
            // Test text to generate embedding for
            String text = "This user enjoys hiking, reading science fiction, and playing chess.";
            
            // Generate embedding using Bedrock
            List<Double> embedding = embeddingService.generateEmbedding(text);
            
            // Store in DynamoDB
            DynamoDbClient dynamoDb = DynamoDbClient.builder()
                    .region(Region.US_EAST_1) // Use your preferred region
                    .build();
            
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("user_id", AttributeValue.builder().s(userId).build());
            item.put("embedding_type", AttributeValue.builder().s("profile").build());
            item.put("embedding", AttributeValue.builder().s(embedding.toString()).build());
            item.put("text", AttributeValue.builder().s(text).build());
            item.put("created_at", AttributeValue.builder().s(java.time.Instant.now().toString()).build());
            
            PutItemRequest request = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build();
            
            dynamoDb.putItem(request);
            
            System.out.println("Stored Bedrock-generated embedding for user: " + userId);
            
        } catch (Exception e) {
            fail("Storing Bedrock embedding failed: " + e.getMessage());
        }
    }
    
    @Test
    @Disabled("Requires AWS credentials with Bedrock access")
    public void testCompareEmbeddingSimilarity() {
        try {
            // Generate embeddings for two similar texts and one different text
            String text1 = "I enjoy hiking in the mountains and camping under the stars.";
            String text2 = "My favorite activities include mountain hiking and outdoor camping.";
            String text3 = "I'm interested in quantum physics and theoretical mathematics.";
            
            List<Double> embedding1 = embeddingService.generateEmbedding(text1);
            List<Double> embedding2 = embeddingService.generateEmbedding(text2);
            List<Double> embedding3 = embeddingService.generateEmbedding(text3);
            
            // Calculate similarities
            double similarity12 = calculateCosineSimilarity(embedding1, embedding2);
            double similarity13 = calculateCosineSimilarity(embedding1, embedding3);
            
            System.out.println("Similarity between similar texts: " + similarity12);
            System.out.println("Similarity between different texts: " + similarity13);
            
            // Similar texts should have higher similarity
            assertTrue(similarity12 > similarity13, 
                    "Similarity between similar texts should be higher than between different texts");
            
        } catch (Exception e) {
            fail("Embedding comparison failed: " + e.getMessage());
        }
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
    
    /**
     * Service class for generating embeddings using AWS Bedrock
     */
    public static class BedrockEmbeddingService {
        private final BedrockRuntimeClient bedrockClient;
        private final ObjectMapper objectMapper;
        
        // Model IDs for embedding models in Bedrock
        private static final String TITAN_EMBEDDING_MODEL = "amazon.titan-embed-text-v1";
        private static final String COHERE_EMBEDDING_MODEL = "cohere.embed-english-v3";
        
        public BedrockEmbeddingService() {
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
            // Choose which model to use
            return generateTitanEmbedding(text);
            // Alternative: return generateCohereEmbedding(text);
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
    }
} 