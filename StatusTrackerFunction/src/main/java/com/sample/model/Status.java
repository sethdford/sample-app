package com.sample.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents a status entry in the system.
 * This can be a financial transaction status, client interaction, or any other trackable event.
 */
public class Status implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Core fields
    private String statusId;
    private String clientId;
    private String advisorId;
    private String statusType;
    private String currentStage;
    private String statusSummary;
    private String createdDate;
    private String lastUpdatedDate;
    private String createdBy;
    private String lastUpdatedBy;
    private String sourceId; // ID from source system (workflow ID, order ID, etc.)
    private String trackingId; // User-friendly tracking ID for customer service
    
    // Status-specific fields
    private Map<String, Object> statusDetails;
    private List<StatusHistoryItem> statusHistory;
    private List<String> relatedDocuments;
    private List<String> requiredActions;
    private List<String> completedActions;
    private String estimatedCompletionDate;
    private String actualCompletionDate;
    private String priority;
    private String category;
    private String subCategory;
    
    // Relationship fields
    private String householdId;
    private List<String> relatedClientIds;
    private List<String> beneficiaryIds;
    private Map<String, String> relationshipTypes;
    
    // Metadata
    private Map<String, Object> metadata;
    private Map<String, String> tags;
    
    // Constructors
    public Status() {
        this.statusDetails = new HashMap<>();
        this.statusHistory = new ArrayList<>();
        this.requiredActions = new ArrayList<>();
        this.completedActions = new ArrayList<>();
        this.tags = new HashMap<>();
        this.metadata = new HashMap<>();
        this.createdDate = new Date().toString();
        this.lastUpdatedDate = new Date().toString();
        // Generate a user-friendly tracking ID
        this.trackingId = generateTrackingId();
    }
    
    // Getters and Setters
    public String getStatusId() {
        return statusId;
    }
    
    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getAdvisorId() {
        return advisorId;
    }
    
    public void setAdvisorId(String advisorId) {
        this.advisorId = advisorId;
    }
    
    public String getStatusType() {
        return statusType;
    }
    
    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }
    
    public String getCurrentStage() {
        return currentStage;
    }
    
    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }
    
    public String getStatusSummary() {
        return statusSummary;
    }
    
    public void setStatusSummary(String statusSummary) {
        this.statusSummary = statusSummary;
    }
    
    public String getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getLastUpdatedDate() {
        return lastUpdatedDate;
    }
    
    public void setLastUpdatedDate(String lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }
    
    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }
    
    public String getSourceId() {
        return sourceId;
    }
    
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    
    public String getTrackingId() {
        return trackingId;
    }
    
    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }
    
    public Map<String, Object> getStatusDetails() {
        return statusDetails;
    }
    
    public void setStatusDetails(Map<String, Object> statusDetails) {
        this.statusDetails = statusDetails;
    }
    
    public List<StatusHistoryItem> getStatusHistory() {
        return statusHistory;
    }
    
    public void setStatusHistory(List<StatusHistoryItem> statusHistory) {
        this.statusHistory = statusHistory;
    }
    
    public List<String> getRelatedDocuments() {
        return relatedDocuments;
    }
    
    public void setRelatedDocuments(List<String> relatedDocuments) {
        this.relatedDocuments = relatedDocuments;
    }
    
    public List<String> getRequiredActions() {
        return requiredActions;
    }
    
    public void setRequiredActions(List<String> requiredActions) {
        this.requiredActions = requiredActions;
    }
    
    public List<String> getCompletedActions() {
        return completedActions;
    }
    
    public void setCompletedActions(List<String> completedActions) {
        this.completedActions = completedActions;
    }
    
    public String getEstimatedCompletionDate() {
        return estimatedCompletionDate;
    }
    
    public void setEstimatedCompletionDate(String estimatedCompletionDate) {
        this.estimatedCompletionDate = estimatedCompletionDate;
    }
    
    public String getActualCompletionDate() {
        return actualCompletionDate;
    }
    
    public void setActualCompletionDate(String actualCompletionDate) {
        this.actualCompletionDate = actualCompletionDate;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getSubCategory() {
        return subCategory;
    }
    
    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }
    
    public String getHouseholdId() {
        return householdId;
    }
    
    public void setHouseholdId(String householdId) {
        this.householdId = householdId;
    }
    
    public List<String> getRelatedClientIds() {
        return relatedClientIds;
    }
    
    public void setRelatedClientIds(List<String> relatedClientIds) {
        this.relatedClientIds = relatedClientIds;
    }
    
    public List<String> getBeneficiaryIds() {
        return beneficiaryIds;
    }
    
    public void setBeneficiaryIds(List<String> beneficiaryIds) {
        this.beneficiaryIds = beneficiaryIds;
    }
    
    public Map<String, String> getRelationshipTypes() {
        return relationshipTypes;
    }
    
    public void setRelationshipTypes(Map<String, String> relationshipTypes) {
        this.relationshipTypes = relationshipTypes;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public Map<String, String> getTags() {
        return tags;
    }
    
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
    
    /**
     * Adds a detail to the status details map.
     */
    public void addStatusDetail(String key, Object value) {
        if (this.statusDetails == null) {
            this.statusDetails = new HashMap<>();
        }
        this.statusDetails.put(key, value);
    }
    
    /**
     * Adds a tag to the status.
     */
    public void addTag(String key, String value) {
        if (this.tags == null) {
            this.tags = new HashMap<>();
        }
        this.tags.put(key, value);
    }
    
    /**
     * Adds metadata to the status.
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
    
    /**
     * Updates the last updated date to the current time.
     */
    public void updateLastUpdatedDate() {
        this.lastUpdatedDate = new Date().toString();
    }
    
    /**
     * Generates a user-friendly tracking ID for customer service reference.
     * Format: ST-XXXXX-YYMMDD where XXXXX is a random alphanumeric string
     * and YYMMDD is the current date.
     */
    private String generateTrackingId() {
        // Get current date in YYMMDD format
        Calendar cal = Calendar.getInstance();
        String datePart = String.format("%02d%02d%02d", 
            cal.get(Calendar.YEAR) % 100,
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH));
        
        // Generate random alphanumeric string of length 5
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomPart = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            randomPart.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return "ST-" + randomPart.toString() + "-" + datePart;
    }
} 