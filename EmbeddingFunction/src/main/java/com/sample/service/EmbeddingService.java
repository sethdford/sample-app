package com.sample.service;

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
    private static final String MODEL_ID = "amazon.titan-embed-text-v1";
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
        String requestBody = "{ \"inputText\": \"" + inputText + "\" }";

        InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId(MODEL_ID)
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(requestBody))
                .build();

        try {
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            return objectMapper.readValue(response.body().asUtf8String(), double[].class);
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
}