package com.sample;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.model.EmbeddingResult;
import com.sample.util.JsonUtils;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

public class EmbeddingService {
    private final BedrockRuntimeClient bedrockClient;
    private static final String MODEL_ID = "amazon.titan-embed-text-v1";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Random random = new Random();
    private static final int EMBEDDING_DIMENSIONS = 1536;
    private boolean useRealModel = false;

    public EmbeddingService() {
        // In demo mode, we don't need to create a real Bedrock client
        this.bedrockClient = useRealModel ? BedrockRuntimeClient.create() : null;
    }

    /**
     * Generate an embedding for the given input text
     * 
     * @param inputText The text to generate an embedding for
     * @return The embedding vector
     */
    public double[] generateEmbedding(String inputText) {
        if (useRealModel) {
            try {
                String requestBody = "{ \"inputText\": \"" + inputText + "\" }";

                InvokeModelRequest request = InvokeModelRequest.builder()
                        .modelId(MODEL_ID)
                        .contentType("application/json")
                        .accept("application/json")
                        .body(SdkBytes.fromUtf8String(requestBody))
                        .build();

                InvokeModelResponse response = bedrockClient.invokeModel(request);
                return objectMapper.readValue(response.body().asUtf8String(), double[].class);
            } catch (Exception e) {
                System.err.println("Error generating embedding: " + e.getMessage());
                return generateMockEmbedding();
            }
        } else {
            // For demo purposes, generate a mock embedding
            return generateMockEmbedding();
        }
    }

    /**
     * Generate an embedding for the given input object
     * 
     * @param input The object to generate an embedding for
     * @return The embedding result
     */
    public EmbeddingResult generateEmbedding(Map<String, Object> input) {
        // Convert the input object to a string representation
        String inputText = JsonUtils.toJson(input);
        
        // Generate the embedding vector
        double[] embeddingVector = generateEmbedding(inputText);
        
        // Determine the source type based on the input
        String sourceType = determineSourceType(input);
        
        // Create a unique ID for the embedding
        String embeddingId = "emb-" + UUID.randomUUID().toString().substring(0, 8);
        
        // Return the embedding result
        return new EmbeddingResult(embeddingId, embeddingVector, sourceType);
    }
    
    /**
     * Determine the source type based on the input
     * 
     * @param input The input object
     * @return The source type
     */
    private String determineSourceType(Map<String, Object> input) {
        if (input.containsKey("clientId") && input.containsKey("riskTolerance")) {
            return "CLIENT_PROFILE";
        } else if (input.containsKey("tradingFrequency") || input.containsKey("tradingStrategy")) {
            return "TRADING_PATTERN";
        } else if (input.containsKey("kycStatus") || input.containsKey("amlChecks")) {
            return "COMPLIANCE_PATTERN";
        } else if (input.containsKey("navigationPath") || input.containsKey("effortScore")) {
            return "CLIENT_EFFORT";
        } else {
            return "UNKNOWN";
        }
    }
    
    /**
     * Generate a mock embedding vector for demo purposes
     * 
     * @return A random embedding vector
     */
    private double[] generateMockEmbedding() {
        double[] embedding = new double[EMBEDDING_DIMENSIONS];
        for (int i = 0; i < EMBEDDING_DIMENSIONS; i++) {
            embedding[i] = (random.nextDouble() * 2 - 1) * 0.1; // Random values between -0.1 and 0.1
        }
        
        // Normalize the embedding vector
        double sum = 0;
        for (double value : embedding) {
            sum += value * value;
        }
        double magnitude = Math.sqrt(sum);
        for (int i = 0; i < EMBEDDING_DIMENSIONS; i++) {
            embedding[i] /= magnitude;
        }
        
        return embedding;
    }
}