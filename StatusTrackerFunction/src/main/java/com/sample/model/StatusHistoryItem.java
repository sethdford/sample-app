package com.sample.model;

import java.io.Serializable;

/**
 * Represents an item in the status history.
 */
public class StatusHistoryItem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String timestamp;
    private String changedBy;
    private String previousStage;
    private String newStage;
    private String changeReason;
    private String changeDescription;
    
    /**
     * Default constructor.
     */
    public StatusHistoryItem() {
    }
    
    /**
     * Constructor with required fields.
     * 
     * @param timestamp The timestamp of the change
     * @param changedBy The user who made the change
     * @param previousStage The previous stage
     * @param newStage The new stage
     */
    public StatusHistoryItem(String timestamp, String changedBy, String previousStage, String newStage) {
        this.timestamp = timestamp;
        this.changedBy = changedBy;
        this.previousStage = previousStage;
        this.newStage = newStage;
    }
    
    /**
     * Gets the timestamp.
     * 
     * @return The timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }
    
    /**
     * Sets the timestamp.
     * 
     * @param timestamp The timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Gets the user who made the change.
     * 
     * @return The user who made the change
     */
    public String getChangedBy() {
        return changedBy;
    }
    
    /**
     * Sets the user who made the change.
     * 
     * @param changedBy The user who made the change
     */
    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }
    
    /**
     * Gets the previous stage.
     * 
     * @return The previous stage
     */
    public String getPreviousStage() {
        return previousStage;
    }
    
    /**
     * Sets the previous stage.
     * 
     * @param previousStage The previous stage
     */
    public void setPreviousStage(String previousStage) {
        this.previousStage = previousStage;
    }
    
    /**
     * Gets the new stage.
     * 
     * @return The new stage
     */
    public String getNewStage() {
        return newStage;
    }
    
    /**
     * Sets the new stage.
     * 
     * @param newStage The new stage
     */
    public void setNewStage(String newStage) {
        this.newStage = newStage;
    }
    
    /**
     * Gets the change reason.
     * 
     * @return The change reason
     */
    public String getChangeReason() {
        return changeReason;
    }
    
    /**
     * Sets the change reason.
     * 
     * @param changeReason The change reason
     */
    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }
    
    /**
     * Gets the change description.
     * 
     * @return The change description
     */
    public String getChangeDescription() {
        return changeDescription;
    }
    
    /**
     * Sets the change description.
     * 
     * @param changeDescription The change description
     */
    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
    }
} 