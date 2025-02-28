package com.sample.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Formats user attributes into a format compatible with AWS Titan embedding models.
 * This class handles:
 * 1. Parsing raw user attributes from JSON
 * 2. Encoding attributes into numerical features
 * 3. Converting encoded attributes into a text prompt for Titan
 */
public class UserAttributeFormatter {

    // Define known categories for one-hot encoding
    private static final List<String> KNOWN_ERRORS = Arrays.asList("400", "401", "403", "404", "500", "Timeout");
    private static final List<String> KNOWN_DEVICES = Arrays.asList("Mobile", "Desktop", "Tablet");
    private static final Map<String, Integer> SUBSCRIPTION_PLANS = Map.of(
            "Free", 0,
            "Basic", 1,
            "Premium", 2,
            "Enterprise", 3
    );
    
    // Max values for scaling
    private static final int MAX_PAGE_VIEWS = 100;
    
    /**
     * Converts raw user attributes to encoded attributes suitable for ML models.
     * 
     * @param rawAttributes Map of raw user attributes
     * @return Map of encoded attributes
     */
    public Map<String, Object> encodeAttributes(Map<String, Object> rawAttributes) {
        Map<String, Object> encodedAttributes = new HashMap<>();
        
        // Process last login date
        if (rawAttributes.containsKey("last_login")) {
            String lastLoginStr = (String) rawAttributes.get("last_login");
            encodedAttributes.put("last_login_days", calculateDaysSinceLogin(lastLoginStr));
        }
        
        // Process errors encountered
        if (rawAttributes.containsKey("errors_encountered")) {
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) rawAttributes.get("errors_encountered");
            encodedAttributes.put("errors_encountered_onehot", encodeErrors(errors));
        }
        
        // Process page views
        if (rawAttributes.containsKey("page_views")) {
            int pageViews = ((Number) rawAttributes.get("page_views")).intValue();
            encodedAttributes.put("page_views_scaled", scalePageViews(pageViews));
        }
        
        // Process subscription plan
        if (rawAttributes.containsKey("subscription_plan")) {
            String plan = (String) rawAttributes.get("subscription_plan");
            encodedAttributes.put("subscription_plan_label", encodeSubscriptionPlan(plan));
        }
        
        // Process device type
        if (rawAttributes.containsKey("device")) {
            String device = (String) rawAttributes.get("device");
            encodedAttributes.put("device_onehot", encodeDevice(device));
        }
        
        return encodedAttributes;
    }
    
    /**
     * Converts encoded attributes into a text prompt for Titan embedding model.
     * 
     * @param encodedAttributes Map of encoded attributes
     * @return String formatted as a text prompt
     */
    public String createTitanPrompt(Map<String, Object> encodedAttributes) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("User profile: ");
        
        // Add each encoded attribute to the prompt
        if (encodedAttributes.containsKey("last_login_days")) {
            int days = ((Number) encodedAttributes.get("last_login_days")).intValue();
            prompt.append("Last active ").append(days).append(" days ago. ");
        }
        
        if (encodedAttributes.containsKey("errors_encountered_onehot")) {
            @SuppressWarnings("unchecked")
            List<Integer> errorCodes = (List<Integer>) encodedAttributes.get("errors_encountered_onehot");
            prompt.append("Encountered errors: ");
            List<String> activeErrors = new ArrayList<>();
            for (int i = 0; i < errorCodes.size(); i++) {
                if (errorCodes.get(i) == 1 && i < KNOWN_ERRORS.size()) {
                    activeErrors.add(KNOWN_ERRORS.get(i));
                }
            }
            prompt.append(String.join(", ", activeErrors)).append(". ");
        }
        
        if (encodedAttributes.containsKey("page_views_scaled")) {
            double scaledViews = ((Number) encodedAttributes.get("page_views_scaled")).doubleValue();
            int originalViews = (int) (scaledViews * MAX_PAGE_VIEWS);
            prompt.append("Viewed ").append(originalViews).append(" pages. ");
        }
        
        if (encodedAttributes.containsKey("subscription_plan_label")) {
            int planLabel = ((Number) encodedAttributes.get("subscription_plan_label")).intValue();
            String planName = getPlanNameFromLabel(planLabel);
            prompt.append("Has ").append(planName).append(" subscription. ");
        }
        
        if (encodedAttributes.containsKey("device_onehot")) {
            @SuppressWarnings("unchecked")
            List<Integer> deviceEncoding = (List<Integer>) encodedAttributes.get("device_onehot");
            for (int i = 0; i < deviceEncoding.size(); i++) {
                if (deviceEncoding.get(i) == 1 && i < KNOWN_DEVICES.size()) {
                    prompt.append("Uses ").append(KNOWN_DEVICES.get(i)).append(" device.");
                    break;
                }
            }
        }
        
        return prompt.toString().trim();
    }
    
    /**
     * Calculate days since last login
     */
    private int calculateDaysSinceLogin(String lastLoginStr) {
        try {
            Instant lastLogin = Instant.parse(lastLoginStr);
            LocalDateTime loginDate = LocalDateTime.ofInstant(lastLogin, ZoneId.systemDefault());
            LocalDateTime now = LocalDateTime.now();
            return (int) ChronoUnit.DAYS.between(loginDate, now);
        } catch (Exception e) {
            return 0; // Default to 0 days if parsing fails
        }
    }
    
    /**
     * Encode errors as one-hot vector
     */
    private List<Integer> encodeErrors(List<String> errors) {
        List<Integer> encoded = new ArrayList<>(Collections.nCopies(KNOWN_ERRORS.size(), 0));
        
        for (String error : errors) {
            int index = KNOWN_ERRORS.indexOf(error);
            if (index >= 0) {
                encoded.set(index, 1);
            }
        }
        
        return encoded;
    }
    
    /**
     * Scale page views to range [0,1]
     */
    private double scalePageViews(int pageViews) {
        return Math.min(1.0, (double) pageViews / MAX_PAGE_VIEWS);
    }
    
    /**
     * Encode subscription plan as integer label
     */
    private int encodeSubscriptionPlan(String plan) {
        return SUBSCRIPTION_PLANS.getOrDefault(plan, 0);
    }
    
    /**
     * Encode device type as one-hot vector
     */
    private List<Integer> encodeDevice(String device) {
        List<Integer> encoded = new ArrayList<>(Collections.nCopies(KNOWN_DEVICES.size(), 0));
        
        int index = KNOWN_DEVICES.indexOf(device);
        if (index >= 0) {
            encoded.set(index, 1);
        }
        
        return encoded;
    }
    
    /**
     * Get plan name from encoded label
     */
    private String getPlanNameFromLabel(int label) {
        for (Map.Entry<String, Integer> entry : SUBSCRIPTION_PLANS.entrySet()) {
            if (entry.getValue() == label) {
                return entry.getKey();
            }
        }
        return "Unknown";
    }
} 