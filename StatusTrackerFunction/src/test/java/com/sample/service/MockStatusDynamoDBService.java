package com.sample.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.sample.model.Status;

/**
 * Mock implementation of StatusDynamoDBService for testing.
 * Uses in-memory storage instead of actual DynamoDB.
 */
public class MockStatusDynamoDBService extends StatusDynamoDBService {

    private final Map<String, Status> statusMap = new ConcurrentHashMap<>();
    private final Map<String, List<Status>> clientStatusMap = new ConcurrentHashMap<>();
    private final Map<String, List<Status>> advisorStatusMap = new ConcurrentHashMap<>();
    private final Map<String, List<Status>> statusTypeMap = new ConcurrentHashMap<>();
    private final Map<String, Status> sourceIdMap = new ConcurrentHashMap<>();
    private final Map<String, Status> trackingIdMap = new ConcurrentHashMap<>();

    public MockStatusDynamoDBService() {
        // Override constructor to avoid actual DynamoDB client initialization
        // Do not call super() to avoid initializing real DynamoDB clients
    }

    @Override
    public void putStatus(Status status) {
        // Store in main map
        statusMap.put(status.getStatusId(), status);
        
        // Index by client ID
        clientStatusMap.computeIfAbsent(status.getClientId(), k -> new ArrayList<>())
                      .add(status);
        
        // Index by advisor ID
        advisorStatusMap.computeIfAbsent(status.getAdvisorId(), k -> new ArrayList<>())
                       .add(status);
        
        // Index by status type
        statusTypeMap.computeIfAbsent(status.getStatusType(), k -> new ArrayList<>())
                    .add(status);
        
        // Index by source ID if present
        if (status.getSourceId() != null && !status.getSourceId().isEmpty()) {
            sourceIdMap.put(status.getSourceId(), status);
        }
        
        // Index by tracking ID if present
        if (status.getTrackingId() != null && !status.getTrackingId().isEmpty()) {
            trackingIdMap.put(status.getTrackingId(), status);
        }
    }

    @Override
    public Status getStatus(String statusId) {
        return statusMap.get(statusId);
    }

    @Override
    public Status getStatusBySourceId(String sourceId) throws Exception {
        // In the mock implementation, we don't need to throw exceptions
        return sourceIdMap.get(sourceId);
    }

    @Override
    public Status getStatusByTrackingId(String trackingId) throws Exception {
        // In the mock implementation, we don't need to throw exceptions
        return trackingIdMap.get(trackingId);
    }

    /**
     * Retrieves all statuses for a specific client with date filtering support.
     */
    public List<Status> getClientStatuses(String clientId, String statusType, String fromDate, String toDate) {
        List<Status> clientStatuses = clientStatusMap.getOrDefault(clientId, new ArrayList<>());
        
        if (statusType != null && !statusType.isEmpty()) {
            clientStatuses = clientStatuses.stream()
                .filter(status -> status.getStatusType().equals(statusType))
                .collect(Collectors.toList());
        }
        
        // Date filtering would be implemented here in a production environment
        // For now, we'll just return all statuses
        
        return clientStatuses;
    }
    
    @Override
    public List<Status> getClientStatuses(String clientId, String statusType) {
        return getClientStatuses(clientId, statusType, null, null);
    }

    @Override
    public Map<String, List<Status>> getAdvisorClientStatuses(String advisorId, String statusType) {
        List<Status> advisorStatuses = advisorStatusMap.getOrDefault(advisorId, new ArrayList<>());
        
        if (statusType != null && !statusType.isEmpty()) {
            advisorStatuses = advisorStatuses.stream()
                .filter(status -> status.getStatusType().equals(statusType))
                .collect(Collectors.toList());
        }
        
        // Group by client ID
        Map<String, List<Status>> clientStatuses = new HashMap<>();
        for (Status status : advisorStatuses) {
            String clientId = status.getClientId();
            if (!clientStatuses.containsKey(clientId)) {
                clientStatuses.put(clientId, new ArrayList<>());
            }
            clientStatuses.get(clientId).add(status);
        }
        
        return clientStatuses;
    }

    @Override
    public List<Status> searchStatuses(Map<String, Object> searchCriteria) {
        List<Status> allStatuses = new ArrayList<>(statusMap.values());
        
        // Apply filters based on search criteria
        if (searchCriteria.containsKey("clientId")) {
            String clientId = (String) searchCriteria.get("clientId");
            allStatuses = allStatuses.stream()
                .filter(status -> status.getClientId().equals(clientId))
                .collect(Collectors.toList());
        }
        
        if (searchCriteria.containsKey("advisorId")) {
            String advisorId = (String) searchCriteria.get("advisorId");
            allStatuses = allStatuses.stream()
                .filter(status -> status.getAdvisorId().equals(advisorId))
                .collect(Collectors.toList());
        }
        
        if (searchCriteria.containsKey("statusType")) {
            String statusType = (String) searchCriteria.get("statusType");
            allStatuses = allStatuses.stream()
                .filter(status -> status.getStatusType().equals(statusType))
                .collect(Collectors.toList());
        }
        
        if (searchCriteria.containsKey("currentStage")) {
            String currentStage = (String) searchCriteria.get("currentStage");
            allStatuses = allStatuses.stream()
                .filter(status -> status.getCurrentStage().equals(currentStage))
                .collect(Collectors.toList());
        }
        
        if (searchCriteria.containsKey("priority")) {
            String priority = (String) searchCriteria.get("priority");
            allStatuses = allStatuses.stream()
                .filter(status -> priority.equals(status.getPriority()))
                .collect(Collectors.toList());
        }
        
        if (searchCriteria.containsKey("textSearch")) {
            String textSearch = ((String) searchCriteria.get("textSearch")).toLowerCase();
            allStatuses = allStatuses.stream()
                .filter(status -> {
                    String summary = status.getStatusSummary() != null ? status.getStatusSummary().toLowerCase() : "";
                    return summary.contains(textSearch);
                })
                .collect(Collectors.toList());
        }
        
        return allStatuses;
    }
} 