package com.sample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.model.Status;
import com.sample.service.MockStatusDynamoDBService;
import com.sample.service.StatusTrackerService;

/**
 * Unit tests for StatusTracker Lambda handler.
 */
public class StatusTrackerTest {

    private StatusTracker statusTracker;
    private Context mockContext;
    private ObjectMapper objectMapper;
    private MockStatusDynamoDBService mockDynamoDBService;

    @BeforeEach
    public void setUp() {
        // Create a mock DynamoDB service
        mockDynamoDBService = new MockStatusDynamoDBService();
        
        // Create a StatusTrackerService with the mock
        StatusTrackerService statusService = new StatusTrackerService(mockDynamoDBService);
        
        // Create the StatusTracker with the service
        statusTracker = new StatusTracker(statusService);
        
        mockContext = Mockito.mock(Context.class);
        
        // Create a mock LambdaLogger
        LambdaLogger mockLogger = Mockito.mock(LambdaLogger.class);
        
        // Configure the mock context to return the mock logger
        when(mockContext.getLogger()).thenReturn(mockLogger);
        
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testCreateStatus() throws Exception {
        // Prepare request
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setPath("/status");
        request.setHttpMethod("POST");
        
        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("clientId", "client123");
        requestBody.put("advisorId", "advisor456");
        requestBody.put("statusType", "account_opening");
        requestBody.put("statusSummary", "Opening a new brokerage account for client");
        requestBody.put("createdBy", "advisor456");
        requestBody.put("sourceId", "WF-12345");
        requestBody.put("trackingId", "ST-A7B3C-230615");
        
        request.setBody(objectMapper.writeValueAsString(requestBody));
        
        // Execute the handler
        APIGatewayProxyResponseEvent response = statusTracker.handleRequest(request, mockContext);
        
        // Verify response
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Parse response body
        Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
        assertNotNull(responseMap.get("statusId"));
        assertEquals("client123", responseMap.get("clientId"));
        assertEquals("advisor456", responseMap.get("advisorId"));
        assertEquals("account_opening", responseMap.get("statusType"));
        assertEquals("WF-12345", responseMap.get("sourceId"));
        assertEquals("ST-A7B3C-230615", responseMap.get("trackingId"));
    }

    @Test
    public void testGetStatus() throws Exception {
        // First create a status
        APIGatewayProxyRequestEvent createRequest = new APIGatewayProxyRequestEvent();
        createRequest.setPath("/status");
        createRequest.setHttpMethod("POST");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("clientId", "client123");
        requestBody.put("advisorId", "advisor456");
        requestBody.put("statusType", "account_opening");
        requestBody.put("statusSummary", "Opening a new brokerage account for client");
        requestBody.put("createdBy", "advisor456");
        requestBody.put("sourceId", "WF-12345");
        requestBody.put("trackingId", "ST-A7B3C-230615");
        
        createRequest.setBody(objectMapper.writeValueAsString(requestBody));
        
        APIGatewayProxyResponseEvent createResponse = statusTracker.handleRequest(createRequest, mockContext);
        Map<String, Object> createResponseMap = objectMapper.readValue(createResponse.getBody(), Map.class);
        String statusId = (String) createResponseMap.get("statusId");
        
        // Now get the status
        APIGatewayProxyRequestEvent getRequest = new APIGatewayProxyRequestEvent();
        getRequest.setPath("/status/" + statusId);
        getRequest.setHttpMethod("GET");
        
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("statusId", statusId);
        getRequest.setPathParameters(pathParams);
        
        // Execute the handler
        APIGatewayProxyResponseEvent getResponse = statusTracker.handleRequest(getRequest, mockContext);
        
        // Verify response
        assertNotNull(getResponse);
        assertEquals(200, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        
        // Parse response body
        Map<String, Object> getResponseMap = objectMapper.readValue(getResponse.getBody(), Map.class);
        assertEquals(statusId, getResponseMap.get("statusId"));
        assertEquals("client123", getResponseMap.get("clientId"));
        assertEquals("advisor456", getResponseMap.get("advisorId"));
        assertEquals("account_opening", getResponseMap.get("statusType"));
        assertEquals("WF-12345", getResponseMap.get("sourceId"));
        assertEquals("ST-A7B3C-230615", getResponseMap.get("trackingId"));
    }

    @Test
    public void testGetStatusBySourceId() throws Exception {
        // Create a status directly in the mock service
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("clientId", "client123");
        statusData.put("advisorId", "advisor456");
        statusData.put("statusType", "account_opening");
        statusData.put("statusSummary", "Opening a new brokerage account for client");
        statusData.put("createdBy", "advisor456");
        statusData.put("sourceId", "WF-12345");
        statusData.put("trackingId", "ST-A7B3C-230615");
        
        // Create the status using the service
        Status status = statusTracker.getStatusService().createStatus(statusData);
        assertNotNull(status);
        assertEquals("WF-12345", status.getSourceId());
        
        // Verify the status can be retrieved by source ID
        Status retrievedStatus = statusTracker.getStatusService().getStatusBySourceId("WF-12345");
        assertNotNull(retrievedStatus);
        assertEquals("WF-12345", retrievedStatus.getSourceId());
        assertEquals("client123", retrievedStatus.getClientId());
        assertEquals("advisor456", retrievedStatus.getAdvisorId());
    }

    @Test
    public void testGetStatusByTrackingId() throws Exception {
        // Create a status directly in the mock service
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("clientId", "client123");
        statusData.put("advisorId", "advisor456");
        statusData.put("statusType", "account_opening");
        statusData.put("statusSummary", "Opening a new brokerage account for client");
        statusData.put("createdBy", "advisor456");
        statusData.put("trackingId", "ST-A7B3C-230615");
        
        // Create the status using the service
        Status status = statusTracker.getStatusService().createStatus(statusData);
        assertNotNull(status);
        assertEquals("ST-A7B3C-230615", status.getTrackingId());
        
        // Verify the status can be retrieved by tracking ID
        Status retrievedStatus = statusTracker.getStatusService().getStatusByTrackingId("ST-A7B3C-230615");
        assertNotNull(retrievedStatus);
        assertEquals("ST-A7B3C-230615", retrievedStatus.getTrackingId());
        assertEquals("client123", retrievedStatus.getClientId());
        assertEquals("advisor456", retrievedStatus.getAdvisorId());
    }

    @Test
    public void testUpdateStatus() throws Exception {
        // First create a status
        APIGatewayProxyRequestEvent createRequest = new APIGatewayProxyRequestEvent();
        createRequest.setPath("/status");
        createRequest.setHttpMethod("POST");
        
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("clientId", "client123");
        createBody.put("advisorId", "advisor456");
        createBody.put("statusType", "account_opening");
        createBody.put("statusSummary", "Opening a new brokerage account for client");
        createBody.put("createdBy", "advisor456");
        createBody.put("sourceId", "WF-12345");
        createBody.put("trackingId", "ST-A7B3C-230615");
        
        createRequest.setBody(objectMapper.writeValueAsString(createBody));
        
        APIGatewayProxyResponseEvent createResponse = statusTracker.handleRequest(createRequest, mockContext);
        Map<String, Object> createResponseMap = objectMapper.readValue(createResponse.getBody(), Map.class);
        String statusId = (String) createResponseMap.get("statusId");
        
        // Now update the status
        APIGatewayProxyRequestEvent updateRequest = new APIGatewayProxyRequestEvent();
        updateRequest.setPath("/status/" + statusId);
        updateRequest.setHttpMethod("PUT");
        
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("statusId", statusId);
        updateRequest.setPathParameters(pathParams);
        
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("currentStage", "in_progress");
        updateBody.put("statusSummary", "Processing account opening request");
        updateBody.put("updatedBy", "advisor456");
        
        updateRequest.setBody(objectMapper.writeValueAsString(updateBody));
        
        // Execute the handler
        APIGatewayProxyResponseEvent updateResponse = statusTracker.handleRequest(updateRequest, mockContext);
        
        // Verify response
        assertNotNull(updateResponse);
        assertEquals(200, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        
        // Parse response body
        Map<String, Object> updateResponseMap = objectMapper.readValue(updateResponse.getBody(), Map.class);
        assertEquals(statusId, updateResponseMap.get("statusId"));
        assertEquals("in_progress", updateResponseMap.get("currentStage"));
        assertEquals("Processing account opening request", updateResponseMap.get("statusSummary"));
    }

    @Test
    public void testListClientStatuses() throws Exception {
        // First create multiple statuses for the same client
        String clientId = "client123";
        
        // Create account opening status
        APIGatewayProxyRequestEvent createRequest1 = new APIGatewayProxyRequestEvent();
        createRequest1.setPath("/status");
        createRequest1.setHttpMethod("POST");
        
        Map<String, Object> createBody1 = new HashMap<>();
        createBody1.put("clientId", clientId);
        createBody1.put("advisorId", "advisor456");
        createBody1.put("statusType", "account_opening");
        createBody1.put("statusSummary", "Opening a new brokerage account");
        createBody1.put("createdBy", "advisor456");
        createBody1.put("trackingId", "ST-A7B3C-230615-1");
        
        createRequest1.setBody(objectMapper.writeValueAsString(createBody1));
        statusTracker.handleRequest(createRequest1, mockContext);
        
        // Create portfolio review status
        APIGatewayProxyRequestEvent createRequest2 = new APIGatewayProxyRequestEvent();
        createRequest2.setPath("/status");
        createRequest2.setHttpMethod("POST");
        
        Map<String, Object> createBody2 = new HashMap<>();
        createBody2.put("clientId", clientId);
        createBody2.put("advisorId", "advisor456");
        createBody2.put("statusType", "portfolio_review");
        createBody2.put("statusSummary", "Annual portfolio review");
        createBody2.put("createdBy", "advisor456");
        createBody2.put("trackingId", "ST-A7B3C-230615-2");
        
        createRequest2.setBody(objectMapper.writeValueAsString(createBody2));
        statusTracker.handleRequest(createRequest2, mockContext);
        
        // Now list client statuses
        APIGatewayProxyRequestEvent listRequest = new APIGatewayProxyRequestEvent();
        listRequest.setPath("/client/" + clientId + "/statuses");
        listRequest.setHttpMethod("GET");
        
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("clientId", clientId);
        listRequest.setPathParameters(pathParams);
        
        // Execute the handler
        APIGatewayProxyResponseEvent listResponse = statusTracker.handleRequest(listRequest, mockContext);
        
        // Verify response
        assertNotNull(listResponse);
        assertEquals(200, listResponse.getStatusCode());
        assertNotNull(listResponse.getBody());
        
        // Parse response body
        List<Map<String, Object>> responseList = objectMapper.readValue(listResponse.getBody(), List.class);
        assertEquals(2, responseList.size());
    }

    @Test
    public void testSearchStatuses() throws Exception {
        // First create multiple statuses with different attributes
        
        // Create status 1
        APIGatewayProxyRequestEvent createRequest1 = new APIGatewayProxyRequestEvent();
        createRequest1.setPath("/status");
        createRequest1.setHttpMethod("POST");
        
        Map<String, Object> createBody1 = new HashMap<>();
        createBody1.put("clientId", "client123");
        createBody1.put("advisorId", "advisor456");
        createBody1.put("statusType", "account_opening");
        createBody1.put("statusSummary", "Opening a new brokerage account");
        createBody1.put("createdBy", "advisor456");
        createBody1.put("priority", "High");
        createBody1.put("trackingId", "ST-A7B3C-230615-1");
        
        createRequest1.setBody(objectMapper.writeValueAsString(createBody1));
        statusTracker.handleRequest(createRequest1, mockContext);
        
        // Create status 2
        APIGatewayProxyRequestEvent createRequest2 = new APIGatewayProxyRequestEvent();
        createRequest2.setPath("/status");
        createRequest2.setHttpMethod("POST");
        
        Map<String, Object> createBody2 = new HashMap<>();
        createBody2.put("clientId", "client789");
        createBody2.put("advisorId", "advisor456");
        createBody2.put("statusType", "financial_plan");
        createBody2.put("statusSummary", "Creating retirement plan");
        createBody2.put("createdBy", "advisor456");
        createBody2.put("priority", "Medium");
        createBody2.put("trackingId", "ST-A7B3C-230615-2");
        
        createRequest2.setBody(objectMapper.writeValueAsString(createBody2));
        statusTracker.handleRequest(createRequest2, mockContext);
        
        // Search by text
        APIGatewayProxyRequestEvent searchRequest = new APIGatewayProxyRequestEvent();
        searchRequest.setPath("/statuses/search");
        searchRequest.setHttpMethod("POST");
        
        Map<String, Object> searchBody = new HashMap<>();
        searchBody.put("textSearch", "retirement");
        
        searchRequest.setBody(objectMapper.writeValueAsString(searchBody));
        
        // Execute the handler
        APIGatewayProxyResponseEvent searchResponse = statusTracker.handleRequest(searchRequest, mockContext);
        
        // Verify response
        assertNotNull(searchResponse);
        assertEquals(200, searchResponse.getStatusCode());
        assertNotNull(searchResponse.getBody());
        
        // Parse response body
        List<Map<String, Object>> responseList = objectMapper.readValue(searchResponse.getBody(), List.class);
        assertEquals(1, responseList.size());
        assertEquals("client789", responseList.get(0).get("clientId"));
    }
} 