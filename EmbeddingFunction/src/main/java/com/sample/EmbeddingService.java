package com.sample;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

public class EmbeddingService {
    private final BedrockRuntimeClient bedrockClient = BedrockRuntimeClient.create();
    private static final String MODEL_ID = "amazon.titan-embed-text-v1";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public double[] generateEmbedding(String inputText) throws Exception {
        String requestBody = "{ \"inputText\": \"" + inputText + "\" }";

        InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId(MODEL_ID)
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(requestBody))
                .build();

        InvokeModelResponse response = bedrockClient.invokeModel(request);
        return objectMapper.readValue(response.body().asUtf8String(), double[].class);
    }
}