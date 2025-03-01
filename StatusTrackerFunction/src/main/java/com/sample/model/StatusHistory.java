package com.sample.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a historical entry for a status.
 * This tracks changes to a status over time, including who made the changes and when.
 */
public class StatusHistory {
    
    private String historyId;
    private String statusId;
    private Date timestamp;
    private String changedBy;
    private String previousStage;
    private String newStage;
    private String changeReason;
    private String changeDescription;
    private Map<String, Object> changedFields;
    
    // Constructors
    public StatusHistory() {
        this.timestamp = new Date();
        this.changedFields = new HashMap<>();
    }
    
    public StatusHistory(String statusId, String changedBy, String previousStage, String newStage) {
        this();
        this.statusId = statusId;
        this.changedBy = changedBy;
        this.previousStage = previousStage;
        this.newStage = newStage;
    }
    
    // Getters and Setters
    public String getHistoryId() {
        return historyId;
    }
    
    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }
    
    public String getStatusId() {
        return statusId;
    }
    
    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getChangedBy() {
        return changedBy;
    }
    
    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }
    
    public String getPreviousStage() {
        return previousStage;
    }
    
    public void setPreviousStage(String previousStage) {
        this.previousStage = previousStage;
    }
    
    public String getNewStage() {
        return newStage;
    }
    
    public void setNewStage(String newStage) {
        this.newStage = newStage;
    }
    
    public String getChangeReason() {
        return changeReason;
    }
    
    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }
    
    public String getChangeDescription() {
        return changeDescription;
    }
    
    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
    }
    
    public Map<String, Object> getChangedFields() {
        return changedFields;
    }
    
    public void setChangedFields(Map<String, Object> changedFields) {
        this.changedFields = changedFields;
    }
    
    /**
     * Adds a changed field to the history.
     */
    public void addChangedField(String fieldName, Object oldValue, Object newValue) {
        if (this.changedFields == null) {
            this.changedFields = new HashMap<>();
        }
        
        Map<String, Object> fieldChange = new HashMap<>();
        fieldChange.put("oldValue", oldValue);
        fieldChange.put("newValue", newValue);
        
        this.changedFields.put(fieldName, fieldChange);
    }
} 