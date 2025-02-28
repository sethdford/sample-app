package com.sample.examples;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.sample.util.JsonUtils;
import com.sample.util.UserAttributeFormatter;

/**
 * Demonstrates how to use the UserAttributeFormatter to convert raw user attributes
 * into a format compatible with AWS Titan embedding models.
 */
public class UserAttributeFormatterDemo {

    public static void main(String[] args) {
        try {
            // Create sample raw user attributes
            Map<String, Object> rawAttributes = createSampleUserAttributes();
            System.out.println("Raw User Attributes:");
            System.out.println(JsonUtils.toJson(rawAttributes));
            System.out.println();
            
            // Initialize the formatter
            UserAttributeFormatter formatter = new UserAttributeFormatter();
            
            // Step 1: Convert raw attributes to encoded attributes
            Map<String, Object> encodedAttributes = formatter.encodeAttributes(rawAttributes);
            System.out.println("Encoded User Attributes:");
            System.out.println(JsonUtils.toJson(encodedAttributes));
            System.out.println();
            
            // Step 2: Convert encoded attributes to Titan-compatible text prompt
            String titanPrompt = formatter.createTitanPrompt(encodedAttributes);
            System.out.println("Titan-Compatible Text Prompt:");
            System.out.println(titanPrompt);
            
            // Example of how this would be used with the EmbeddingService
            System.out.println("\nExample usage with EmbeddingService:");
            System.out.println("double[] embedding = embeddingService.generateEmbedding(titanPrompt);");
        } catch (Exception e) {
            System.err.println("Error processing user attributes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a sample set of raw user attributes for demonstration.
     */
    private static Map<String, Object> createSampleUserAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("user_id", "user123");
        attributes.put("last_login", "2025-02-24T12:30:00Z");
        attributes.put("errors_encountered", Arrays.asList("500", "403", "Timeout"));
        attributes.put("page_views", 10);
        attributes.put("subscription_plan", "Premium");
        attributes.put("device", "Mobile");
        return attributes;
    }
} 