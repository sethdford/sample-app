package com.sample.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.util.UserAttributeFormatter;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

/**
 * Service that calls AWS Bedrock to generate embeddings for user input text.
 */
public class EmbeddingService {
    private final BedrockRuntimeClient bedrockClient;
    private static final String MODEL_ID = "amazon.titan-embed-text-v2";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final UserAttributeFormatter attributeFormatter;

    public EmbeddingService() {
        this.bedrockClient = BedrockRuntimeClient.create();
        this.attributeFormatter = new UserAttributeFormatter();
    }

    /**
     * Generates an embedding using AWS Bedrock Titan.
     *
     * @param inputText The input text to be converted into an embedding.
     * @return An array of double values representing the embedding.
     * @throws Exception If the Bedrock API call fails.
     */
    public double[] generateEmbedding(String inputText) throws Exception {
        // For Titan v2, we need to specify the embedding type and dimensions
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("inputText", inputText);
        requestMap.put("embeddingTypes", List.of("float"));
        requestMap.put("dimensions", 1024); // Default dimension for v2
        
        String requestBody = objectMapper.writeValueAsString(requestMap);

        InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId(MODEL_ID)
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(requestBody))
                .build();

        try {
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            Map<String, Object> responseMap = objectMapper.readValue(response.body().asUtf8String(), Map.class);
            
            // In v2, the embedding is in embeddingsByType.float
            if (responseMap.containsKey("embeddingsByType") && 
                ((Map<String, Object>)responseMap.get("embeddingsByType")).containsKey("float")) {
                return objectMapper.convertValue(
                    ((Map<String, Object>)responseMap.get("embeddingsByType")).get("float"), 
                    double[].class
                );
            } else if (responseMap.containsKey("embedding")) {
                // Fallback for v1 format if needed
                return objectMapper.convertValue(responseMap.get("embedding"), double[].class);
            } else {
                throw new RuntimeException("Unexpected response format from AWS Bedrock Titan");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error calling AWS Bedrock Titan: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generates an embedding from user attributes using AWS Bedrock Titan.
     * This method:
     * 1. Encodes raw user attributes into numerical features
     * 2. Converts encoded attributes into a text prompt for Titan
     * 3. Generates an embedding from the text prompt
     *
     * @param userAttributes Map of raw user attributes
     * @return An array of double values representing the embedding
     * @throws Exception If the Bedrock API call fails
     */
    public double[] generateEmbeddingFromAttributes(Map<String, Object> userAttributes) throws Exception {
        // Step 1: Encode raw attributes
        Map<String, Object> encodedAttributes = attributeFormatter.encodeAttributes(userAttributes);
        
        // Step 2: Create Titan-compatible text prompt
        String titanPrompt = attributeFormatter.createTitanPrompt(encodedAttributes);
        
        // Step 3: Generate embedding from the text prompt
        return generateEmbedding(titanPrompt);
    }

    /**
     * Generates an embedding using AWS Bedrock Titan with a specified dimension.
     * Titan v2 supports dimensions of 256, 512, and 1024.
     *
     * @param inputText The input text to be converted into an embedding.
     * @param dimensions The desired embedding dimensions (256, 512, or 1024).
     * @return An array of double values representing the embedding.
     * @throws Exception If the Bedrock API call fails.
     */
    public double[] generateEmbeddingWithDimension(String inputText, int dimensions) throws Exception {
        // Validate dimensions
        if (dimensions != 256 && dimensions != 512 && dimensions != 1024) {
            throw new IllegalArgumentException("Dimensions must be 256, 512, or 1024 for Titan v2");
        }
        
        // For Titan v2, we need to specify the embedding type and dimensions
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("inputText", inputText);
        requestMap.put("embeddingTypes", List.of("float"));
        requestMap.put("dimensions", dimensions);
        
        String requestBody = objectMapper.writeValueAsString(requestMap);

        InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId(MODEL_ID)
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(requestBody))
                .build();

        try {
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            Map<String, Object> responseMap = objectMapper.readValue(response.body().asUtf8String(), Map.class);
            
            // In v2, the embedding is in embeddingsByType.float
            if (responseMap.containsKey("embeddingsByType") && 
                ((Map<String, Object>)responseMap.get("embeddingsByType")).containsKey("float")) {
                return objectMapper.convertValue(
                    ((Map<String, Object>)responseMap.get("embeddingsByType")).get("float"), 
                    double[].class
                );
            } else {
                throw new RuntimeException("Unexpected response format from AWS Bedrock Titan");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error calling AWS Bedrock Titan: " + e.getMessage(), e);
        }
    }
}