package com.sample.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.model.Status;
import com.sample.model.StatusHistoryItem;

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.dax.ClusterDaxAsyncClient;
import software.amazon.dax.Configuration;

/**
 * Service for managing Status objects in DynamoDB with DAX caching.
 */
public class StatusDynamoDBService {
    private DynamoDbAsyncClient daxClient = null;
    private final DynamoDbClient dynamoDb;
    private static final String TABLE_NAME = "StatusTracker";
    private static final String DAX_ENDPOINT = System.getenv("DAX_ENDPOINT");
    private final ObjectMapper objectMapper;

    /**
     * Default constructor that initializes AWS clients.
     */
    public StatusDynamoDBService() {
        // Initialize DynamoDB client
        this.dynamoDb = DynamoDbClient.create();
        this.objectMapper = new ObjectMapper();
        
        // Initialize DAX client if endpoint is available
        try {
            if (DAX_ENDPOINT != null && !DAX_ENDPOINT.isEmpty()) {
                daxClient = ClusterDaxAsyncClient.builder()
                        .overrideConfiguration(Configuration.builder()
                                .url(DAX_ENDPOINT)
                                .build())
                        .build();
            }
        } catch (IOException e) {
            System.err.println("Error initializing DAX client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stores a status in DynamoDB and DAX.
     */
    public void putStatus(Status status) throws JsonProcessingException {
        Map<String, AttributeValue> item = convertStatusToItem(status);

        // Store in DynamoDB (permanent storage)
        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build());

        // Store in DAX (cache) if available
        if (daxClient != null) {
            try {
                daxClient.putItem(PutItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .item(item)
                        .build());
            } catch (Exception e) {
                System.err.println("Error storing in DAX: " + e.getMessage());
            }
        }
    }

    /**
     * Retrieves a status by its ID.
     */
    public Status getStatus(String statusId) throws Exception {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("statusId", AttributeValue.builder().s(statusId).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .build();

        // Attempt to fetch from DAX (cache) if available
        Map<String, AttributeValue> item = null;
        if (daxClient != null) {
            try {
                GetItemResponse response = daxClient.getItem(request).get();
                item = response.item();
            } catch (Exception e) {
                System.err.println("Error fetching from DAX: " + e.getMessage());
            }
        }
        
        if (item == null || item.isEmpty()) {
            // Fallback to DynamoDB
            GetItemResponse response = dynamoDb.getItem(request);
            item = response.item();
        }

        return (item != null && !item.isEmpty()) ? convertItemToStatus(item) : null;
    }

    /**
     * Retrieves a status by its source ID.
     */
    public Status getStatusBySourceId(String sourceId) throws Exception {
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":sourceId", AttributeValue.builder().s(sourceId).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("SourceIdIndex")
                .keyConditionExpression("sourceId = :sourceId")
                .expressionAttributeValues(expressionValues)
                .build();

        QueryResponse response = dynamoDb.query(queryRequest);
        
        if (response.items() != null && !response.items().isEmpty()) {
            return convertItemToStatus(response.items().get(0));
        }
        
        return null;
    }

    /**
     * Retrieves a status by its tracking ID.
     */
    public Status getStatusByTrackingId(String trackingId) throws Exception {
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":trackingId", AttributeValue.builder().s(trackingId).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("TrackingIdIndex")
                .keyConditionExpression("trackingId = :trackingId")
                .expressionAttributeValues(expressionValues)
                .build();

        QueryResponse response = dynamoDb.query(queryRequest);
        
        if (response.items() != null && !response.items().isEmpty()) {
            return convertItemToStatus(response.items().get(0));
        }
        
        return null;
    }

    /**
     * Retrieves all statuses for a specific client.
     */
    public List<Status> getClientStatuses(String clientId, String statusType) throws Exception {
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":clientId", AttributeValue.builder().s(clientId).build());

        QueryRequest.Builder queryRequestBuilder = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("ClientIdIndex")
                .keyConditionExpression("clientId = :clientId");

        // Add filter for status type if provided
        if (statusType != null && !statusType.isEmpty()) {
            expressionValues.put(":statusType", AttributeValue.builder().s(statusType).build());
            queryRequestBuilder.filterExpression("statusType = :statusType");
        }

        QueryRequest queryRequest = queryRequestBuilder
                .expressionAttributeValues(expressionValues)
                .build();

        QueryResponse response = dynamoDb.query(queryRequest);
        
        List<Status> statuses = new ArrayList<>();
        for (Map<String, AttributeValue> item : response.items()) {
            statuses.add(convertItemToStatus(item));
        }
        
        return statuses;
    }

    /**
     * Retrieves all client statuses for a specific advisor.
     */
    public Map<String, List<Status>> getAdvisorClientStatuses(String advisorId, String statusType) throws Exception {
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":advisorId", AttributeValue.builder().s(advisorId).build());

        QueryRequest.Builder queryRequestBuilder = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("AdvisorIdIndex")
                .keyConditionExpression("advisorId = :advisorId");

        // Add filter for status type if provided
        if (statusType != null && !statusType.isEmpty()) {
            expressionValues.put(":statusType", AttributeValue.builder().s(statusType).build());
            queryRequestBuilder.filterExpression("statusType = :statusType");
        }

        QueryRequest queryRequest = queryRequestBuilder
                .expressionAttributeValues(expressionValues)
                .build();

        QueryResponse response = dynamoDb.query(queryRequest);
        
        // Group by client ID
        Map<String, List<Status>> advisorClientStatuses = new HashMap<>();
        for (Map<String, AttributeValue> item : response.items()) {
            Status status = convertItemToStatus(item);
            String clientId = status.getClientId();
            
            if (!advisorClientStatuses.containsKey(clientId)) {
                advisorClientStatuses.put(clientId, new ArrayList<>());
            }
            
            advisorClientStatuses.get(clientId).add(status);
        }
        
        return advisorClientStatuses;
    }

    /**
     * Searches for statuses based on various criteria.
     */
    public List<Status> searchStatuses(Map<String, Object> searchCriteria) throws Exception {
        // Start with a base query
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        List<String> filterExpressions = new ArrayList<>();
        
        // Build filter expressions based on search criteria
        if (searchCriteria.containsKey("clientId")) {
            String clientId = (String) searchCriteria.get("clientId");
            expressionValues.put(":clientId", AttributeValue.builder().s(clientId).build());
            filterExpressions.add("clientId = :clientId");
        }
        
        if (searchCriteria.containsKey("advisorId")) {
            String advisorId = (String) searchCriteria.get("advisorId");
            expressionValues.put(":advisorId", AttributeValue.builder().s(advisorId).build());
            filterExpressions.add("advisorId = :advisorId");
        }
        
        if (searchCriteria.containsKey("statusType")) {
            String statusType = (String) searchCriteria.get("statusType");
            expressionValues.put(":statusType", AttributeValue.builder().s(statusType).build());
            filterExpressions.add("statusType = :statusType");
        }
        
        if (searchCriteria.containsKey("currentStage")) {
            String currentStage = (String) searchCriteria.get("currentStage");
            expressionValues.put(":currentStage", AttributeValue.builder().s(currentStage).build());
            filterExpressions.add("currentStage = :currentStage");
        }
        
        if (searchCriteria.containsKey("priority")) {
            String priority = (String) searchCriteria.get("priority");
            expressionValues.put(":priority", AttributeValue.builder().s(priority).build());
            filterExpressions.add("priority = :priority");
        }
        
        // Combine filter expressions
        String filterExpression = String.join(" AND ", filterExpressions);
        
        // Create scan request
        QueryRequest.Builder queryRequestBuilder = QueryRequest.builder()
                .tableName(TABLE_NAME);
        
        // Determine which index to use based on search criteria
        if (searchCriteria.containsKey("clientId")) {
            queryRequestBuilder.indexName("ClientIdIndex")
                              .keyConditionExpression("clientId = :clientId");
            
            // Remove clientId from filter expression since it's now part of the key condition
            filterExpressions.removeIf(expr -> expr.startsWith("clientId ="));
            filterExpression = String.join(" AND ", filterExpressions);
        } else if (searchCriteria.containsKey("advisorId")) {
            queryRequestBuilder.indexName("AdvisorIdIndex")
                              .keyConditionExpression("advisorId = :advisorId");
            
            // Remove advisorId from filter expression since it's now part of the key condition
            filterExpressions.removeIf(expr -> expr.startsWith("advisorId ="));
            filterExpression = String.join(" AND ", filterExpressions);
        } else if (searchCriteria.containsKey("statusType")) {
            queryRequestBuilder.indexName("StatusTypeIndex")
                              .keyConditionExpression("statusType = :statusType");
            
            // Remove statusType from filter expression since it's now part of the key condition
            filterExpressions.removeIf(expr -> expr.startsWith("statusType ="));
            filterExpression = String.join(" AND ", filterExpressions);
        }
        
        // Add filter expression if not empty
        if (!filterExpression.isEmpty()) {
            queryRequestBuilder.filterExpression(filterExpression);
        }
        
        // Add expression attribute values if not empty
        if (!expressionValues.isEmpty()) {
            queryRequestBuilder.expressionAttributeValues(expressionValues);
        }
        
        QueryRequest queryRequest = queryRequestBuilder.build();
        QueryResponse response = dynamoDb.query(queryRequest);
        
        List<Status> statuses = new ArrayList<>();
        for (Map<String, AttributeValue> item : response.items()) {
            Status status = convertItemToStatus(item);
            
            // Apply additional filtering for text search
            if (searchCriteria.containsKey("textSearch")) {
                String textSearch = ((String) searchCriteria.get("textSearch")).toLowerCase();
                if (status.getStatusSummary() == null || 
                    !status.getStatusSummary().toLowerCase().contains(textSearch)) {
                    continue;
                }
            }
            
            // Apply additional filtering for sentiment
            if (searchCriteria.containsKey("sentiment")) {
                String sentiment = (String) searchCriteria.get("sentiment");
                if (status.getMetadata() == null || 
                    !sentiment.equals(status.getMetadata().get("sentiment"))) {
                    continue;
                }
            }
            
            // Apply additional filtering for tags
            if (searchCriteria.containsKey("tag")) {
                Map<String, String> tagSearch = (Map<String, String>) searchCriteria.get("tag");
                boolean matchesTags = true;
                
                for (Map.Entry<String, String> tagEntry : tagSearch.entrySet()) {
                    String tagKey = tagEntry.getKey();
                    String tagValue = tagEntry.getValue();
                    
                    if (status.getTags() == null || 
                        !tagValue.equals(status.getTags().get(tagKey))) {
                        matchesTags = false;
                        break;
                    }
                }
                
                if (!matchesTags) {
                    continue;
                }
            }
            
            statuses.add(status);
        }
        
        // Sort results if specified
        if (searchCriteria.containsKey("sortBy")) {
            String sortBy = (String) searchCriteria.get("sortBy");
            boolean ascending = true;
            
            if (searchCriteria.containsKey("sortOrder")) {
                ascending = "asc".equalsIgnoreCase((String) searchCriteria.get("sortOrder"));
            }
            
            // Sort based on the specified field
            final boolean finalAscending = ascending;
            if ("createdDate".equals(sortBy)) {
                statuses.sort((s1, s2) -> {
                    int result = s1.getCreatedDate().compareTo(s2.getCreatedDate());
                    return finalAscending ? result : -result;
                });
            } else if ("lastUpdatedDate".equals(sortBy)) {
                statuses.sort((s1, s2) -> {
                    int result = s1.getLastUpdatedDate().compareTo(s2.getLastUpdatedDate());
                    return finalAscending ? result : -result;
                });
            } else if ("priority".equals(sortBy)) {
                statuses.sort((s1, s2) -> {
                    int result = compareStrings(s1.getPriority(), s2.getPriority());
                    return finalAscending ? result : -result;
                });
            }
        }
        
        return statuses;
    }

    /**
     * Helper method to compare strings, handling nulls.
     */
    private int compareStrings(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return -1;
        if (s2 == null) return 1;
        return s1.compareTo(s2);
    }

    /**
     * Converts a Status object to a DynamoDB item.
     */
    private Map<String, AttributeValue> convertStatusToItem(Status status) throws JsonProcessingException {
        Map<String, AttributeValue> item = new HashMap<>();
        
        // Add all fields to the item
        item.put("statusId", AttributeValue.builder().s(status.getStatusId()).build());
        
        if (status.getClientId() != null) {
            item.put("clientId", AttributeValue.builder().s(status.getClientId()).build());
        }
        
        if (status.getAdvisorId() != null) {
            item.put("advisorId", AttributeValue.builder().s(status.getAdvisorId()).build());
        }
        
        if (status.getStatusType() != null) {
            item.put("statusType", AttributeValue.builder().s(status.getStatusType()).build());
        }
        
        if (status.getCurrentStage() != null) {
            item.put("currentStage", AttributeValue.builder().s(status.getCurrentStage()).build());
        }
        
        if (status.getStatusSummary() != null) {
            item.put("statusSummary", AttributeValue.builder().s(status.getStatusSummary()).build());
        }
        
        if (status.getCreatedDate() != null) {
            item.put("createdDate", AttributeValue.builder().s(status.getCreatedDate().toString()).build());
        }
        
        if (status.getLastUpdatedDate() != null) {
            item.put("lastUpdatedDate", AttributeValue.builder().s(status.getLastUpdatedDate().toString()).build());
        }
        
        if (status.getCreatedBy() != null) {
            item.put("createdBy", AttributeValue.builder().s(status.getCreatedBy()).build());
        }
        
        if (status.getLastUpdatedBy() != null) {
            item.put("lastUpdatedBy", AttributeValue.builder().s(status.getLastUpdatedBy()).build());
        }
        
        if (status.getSourceId() != null) {
            item.put("sourceId", AttributeValue.builder().s(status.getSourceId()).build());
        }
        
        if (status.getTrackingId() != null) {
            item.put("trackingId", AttributeValue.builder().s(status.getTrackingId()).build());
        }
        
        if (status.getSourceSystemUrl() != null) {
            item.put("sourceSystemUrl", AttributeValue.builder().s(status.getSourceSystemUrl()).build());
        }
        
        // Convert complex objects to JSON strings
        if (status.getStatusDetails() != null) {
            item.put("statusDetails", AttributeValue.builder().s(objectMapper.writeValueAsString(status.getStatusDetails())).build());
        }
        
        if (status.getStatusHistory() != null) {
            item.put("statusHistory", AttributeValue.builder().s(objectMapper.writeValueAsString(status.getStatusHistory())).build());
        }
        
        if (status.getRelatedDocuments() != null) {
            item.put("relatedDocuments", AttributeValue.builder().s(objectMapper.writeValueAsString(status.getRelatedDocuments())).build());
        }
        
        if (status.getRequiredActions() != null) {
            item.put("requiredActions", AttributeValue.builder().s(objectMapper.writeValueAsString(status.getRequiredActions())).build());
        }
        
        if (status.getCompletedActions() != null) {
            item.put("completedActions", AttributeValue.builder().s(objectMapper.writeValueAsString(status.getCompletedActions())).build());
        }
        
        if (status.getEstimatedCompletionDate() != null) {
            item.put("estimatedCompletionDate", AttributeValue.builder().s(status.getEstimatedCompletionDate().toString()).build());
        }
        
        if (status.getActualCompletionDate() != null) {
            item.put("actualCompletionDate", AttributeValue.builder().s(status.getActualCompletionDate().toString()).build());
        }
        
        if (status.getPriority() != null) {
            item.put("priority", AttributeValue.builder().s(status.getPriority()).build());
        }
        
        if (status.getCategory() != null) {
            item.put("category", AttributeValue.builder().s(status.getCategory()).build());
        }
        
        if (status.getSubCategory() != null) {
            item.put("subCategory", AttributeValue.builder().s(status.getSubCategory()).build());
        }
        
        if (status.getHouseholdId() != null) {
            item.put("householdId", AttributeValue.builder().s(status.getHouseholdId()).build());
        }
        
        if (status.getRelatedClientIds() != null) {
            item.put("relatedClientIds", AttributeValue.builder().s(objectMapper.writeValueAsString(status.getRelatedClientIds())).build());
        }
        
        if (status.getBeneficiaryIds() != null) {
            item.put("beneficiaryIds", AttributeValue.builder().s(objectMapper.writeValueAsString(status.getBeneficiaryIds())).build());
        }
        
        if (status.getRelationshipTypes() != null) {
            item.put("relationshipTypes", AttributeValue.builder().s(objectMapper.writeValueAsString(status.getRelationshipTypes())).build());
        }
        
        if (status.getMetadata() != null) {
            item.put("metadata", AttributeValue.builder().s(objectMapper.writeValueAsString(status.getMetadata())).build());
        }
        
        if (status.getTags() != null) {
            item.put("tags", AttributeValue.builder().s(objectMapper.writeValueAsString(status.getTags())).build());
        }
        
        return item;
    }

    /**
     * Converts a DynamoDB item to a Status object.
     */
    private Status convertItemToStatus(Map<String, AttributeValue> item) throws Exception {
        Status status = new Status();
        
        if (item.containsKey("statusId")) {
            status.setStatusId(item.get("statusId").s());
        }
        
        if (item.containsKey("clientId")) {
            status.setClientId(item.get("clientId").s());
        }
        
        if (item.containsKey("advisorId")) {
            status.setAdvisorId(item.get("advisorId").s());
        }
        
        if (item.containsKey("statusType")) {
            status.setStatusType(item.get("statusType").s());
        }
        
        if (item.containsKey("currentStage")) {
            status.setCurrentStage(item.get("currentStage").s());
        }
        
        if (item.containsKey("statusSummary")) {
            status.setStatusSummary(item.get("statusSummary").s());
        }
        
        if (item.containsKey("createdDate")) {
            // Store as String directly
            status.setCreatedDate(item.get("createdDate").s());
        }
        
        if (item.containsKey("lastUpdatedDate")) {
            // Store as String directly
            status.setLastUpdatedDate(item.get("lastUpdatedDate").s());
        }
        
        if (item.containsKey("createdBy")) {
            status.setCreatedBy(item.get("createdBy").s());
        }
        
        if (item.containsKey("lastUpdatedBy")) {
            status.setLastUpdatedBy(item.get("lastUpdatedBy").s());
        }
        
        if (item.containsKey("sourceId")) {
            status.setSourceId(item.get("sourceId").s());
        }
        
        if (item.containsKey("trackingId")) {
            status.setTrackingId(item.get("trackingId").s());
        }
        
        if (item.containsKey("sourceSystemUrl")) {
            status.setSourceSystemUrl(item.get("sourceSystemUrl").s());
        }
        
        // Convert JSON strings to complex objects
        if (item.containsKey("statusDetails")) {
            status.setStatusDetails(objectMapper.readValue(item.get("statusDetails").s(), Map.class));
        }
        
        if (item.containsKey("statusHistory")) {
            status.setStatusHistory(objectMapper.readValue(item.get("statusHistory").s(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, StatusHistoryItem.class)));
        }
        
        if (item.containsKey("relatedDocuments")) {
            status.setRelatedDocuments(objectMapper.readValue(item.get("relatedDocuments").s(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
        }
        
        if (item.containsKey("requiredActions")) {
            status.setRequiredActions(objectMapper.readValue(item.get("requiredActions").s(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
        }
        
        if (item.containsKey("completedActions")) {
            status.setCompletedActions(objectMapper.readValue(item.get("completedActions").s(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
        }
        
        if (item.containsKey("estimatedCompletionDate")) {
            // Store as String directly
            status.setEstimatedCompletionDate(item.get("estimatedCompletionDate").s());
        }
        
        if (item.containsKey("actualCompletionDate")) {
            // Store as String directly
            status.setActualCompletionDate(item.get("actualCompletionDate").s());
        }
        
        if (item.containsKey("priority")) {
            status.setPriority(item.get("priority").s());
        }
        
        if (item.containsKey("category")) {
            status.setCategory(item.get("category").s());
        }
        
        if (item.containsKey("subCategory")) {
            status.setSubCategory(item.get("subCategory").s());
        }
        
        if (item.containsKey("householdId")) {
            status.setHouseholdId(item.get("householdId").s());
        }
        
        if (item.containsKey("relatedClientIds")) {
            status.setRelatedClientIds(objectMapper.readValue(item.get("relatedClientIds").s(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
        }
        
        if (item.containsKey("beneficiaryIds")) {
            status.setBeneficiaryIds(objectMapper.readValue(item.get("beneficiaryIds").s(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
        }
        
        if (item.containsKey("relationshipTypes")) {
            status.setRelationshipTypes(objectMapper.readValue(item.get("relationshipTypes").s(), Map.class));
        }
        
        if (item.containsKey("metadata")) {
            status.setMetadata(objectMapper.readValue(item.get("metadata").s(), Map.class));
        }
        
        if (item.containsKey("tags")) {
            status.setTags(objectMapper.readValue(item.get("tags").s(), Map.class));
        }
        
        return status;
    }
} 