package com.sample.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sample.model.Status;

/**
 * Unit tests for StatusTrackerService.
 */
public class StatusTrackerServiceTest {

    private StatusTrackerService statusService;
    private MockStatusDynamoDBService mockDynamoDBService;

    @BeforeEach
    public void setUp() {
        // Create a mock DynamoDB service
        mockDynamoDBService = new MockStatusDynamoDBService();
        
        // Create the service with the mock
        statusService = new StatusTrackerService(mockDynamoDBService);
    }

    @Test
    public void testCreateStatus() {
        // Prepare test data
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("clientId", "client123");
        statusData.put("advisorId", "advisor456");
        statusData.put("statusType", StatusTrackerService.STATUS_TYPE_ACCOUNT_OPENING);
        statusData.put("statusSummary", "Opening a new brokerage account for client");
        statusData.put("createdBy", "advisor456");
        statusData.put("sourceId", "WF-12345");
        statusData.put("trackingId", "ST-A7B3C-230615");
        
        // Add optional fields
        Map<String, Object> statusDetails = new HashMap<>();
        statusDetails.put("accountType", "Individual Brokerage");
        statusDetails.put("initialDeposit", 10000.00);
        statusData.put("statusDetails", statusDetails);
        
        List<String> requiredActions = new ArrayList<>();
        requiredActions.add("Complete risk assessment");
        requiredActions.add("Verify identity documents");
        statusData.put("requiredActions", requiredActions);
        
        statusData.put("priority", "High");
        
        // Execute the method
        Status status = statusService.createStatus(statusData);
        
        // Verify results
        assertNotNull(status);
        assertNotNull(status.getStatusId());
        assertEquals("client123", status.getClientId());
        assertEquals("advisor456", status.getAdvisorId());
        assertEquals(StatusTrackerService.STATUS_TYPE_ACCOUNT_OPENING, status.getStatusType());
        assertEquals(StatusTrackerService.STAGE_INITIATED, status.getCurrentStage());
        assertEquals("Opening a new brokerage account for client", status.getStatusSummary());
        assertEquals("advisor456", status.getCreatedBy());
        assertEquals("advisor456", status.getLastUpdatedBy());
        assertEquals("WF-12345", status.getSourceId());
        assertEquals("ST-A7B3C-230615", status.getTrackingId());
        
        // Verify optional fields
        assertEquals("Individual Brokerage", status.getStatusDetails().get("accountType"));
        assertEquals(10000.00, status.getStatusDetails().get("initialDeposit"));
        
        assertEquals(2, status.getRequiredActions().size());
        assertTrue(status.getRequiredActions().contains("Complete risk assessment"));
        assertTrue(status.getRequiredActions().contains("Verify identity documents"));
        
        assertEquals("High", status.getPriority());
        
        // Verify history
        assertNotNull(status.getStatusHistory());
        assertEquals(1, status.getStatusHistory().size());
        assertEquals(StatusTrackerService.STAGE_INITIATED, status.getStatusHistory().get(0).getNewStage());
        assertEquals("Status created", status.getStatusHistory().get(0).getChangeDescription());
    }

    @Test
    public void testUpdateStatus() {
        // First create a status
        Map<String, Object> createData = new HashMap<>();
        createData.put("clientId", "client123");
        createData.put("advisorId", "advisor456");
        createData.put("statusType", StatusTrackerService.STATUS_TYPE_ACCOUNT_OPENING);
        createData.put("statusSummary", "Opening a new brokerage account for client");
        createData.put("createdBy", "advisor456");
        createData.put("sourceId", "WF-12345");
        createData.put("trackingId", "ST-A7B3C-230615");
        
        Status status = statusService.createStatus(createData);
        String statusId = status.getStatusId();
        
        // Now update the status
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("currentStage", StatusTrackerService.STAGE_IN_PROGRESS);
        updateData.put("statusSummary", "Processing account opening request");
        updateData.put("updatedBy", "advisor456");
        updateData.put("changeReason", "Started processing the application");
        
        // Execute the update
        Status updatedStatus = statusService.updateStatus(statusId, updateData);
        
        // Verify results
        assertNotNull(updatedStatus);
        assertEquals(statusId, updatedStatus.getStatusId());
        assertEquals(StatusTrackerService.STAGE_IN_PROGRESS, updatedStatus.getCurrentStage());
        assertEquals("Processing account opening request", updatedStatus.getStatusSummary());
        assertEquals("advisor456", updatedStatus.getLastUpdatedBy());
        
        // Verify history
        assertNotNull(updatedStatus.getStatusHistory());
        assertEquals(2, updatedStatus.getStatusHistory().size());
        assertEquals(StatusTrackerService.STAGE_INITIATED, updatedStatus.getStatusHistory().get(0).getNewStage());
        assertEquals(StatusTrackerService.STAGE_IN_PROGRESS, updatedStatus.getStatusHistory().get(1).getNewStage());
        assertEquals(StatusTrackerService.STAGE_INITIATED, updatedStatus.getStatusHistory().get(1).getPreviousStage());
        assertEquals("Started processing the application", updatedStatus.getStatusHistory().get(1).getChangeReason());
    }

    @Test
    public void testGetStatus() {
        // First create a status
        Map<String, Object> createData = new HashMap<>();
        createData.put("clientId", "client123");
        createData.put("advisorId", "advisor456");
        createData.put("statusType", StatusTrackerService.STATUS_TYPE_ACCOUNT_OPENING);
        createData.put("statusSummary", "Opening a new brokerage account for client");
        createData.put("createdBy", "advisor456");
        createData.put("sourceId", "WF-12345");
        createData.put("trackingId", "ST-A7B3C-230615");
        
        Status status = statusService.createStatus(createData);
        String statusId = status.getStatusId();
        
        // Get the status
        Status retrievedStatus = statusService.getStatus(statusId);
        
        // Verify results
        assertNotNull(retrievedStatus);
        assertEquals(statusId, retrievedStatus.getStatusId());
        assertEquals("client123", retrievedStatus.getClientId());
        assertEquals("advisor456", retrievedStatus.getAdvisorId());
        assertEquals(StatusTrackerService.STATUS_TYPE_ACCOUNT_OPENING, retrievedStatus.getStatusType());
        assertEquals("WF-12345", retrievedStatus.getSourceId());
        assertEquals("ST-A7B3C-230615", retrievedStatus.getTrackingId());
    }

    @Test
    public void testGetStatusBySourceId() {
        // First create a status
        Map<String, Object> createData = new HashMap<>();
        createData.put("clientId", "client123");
        createData.put("advisorId", "advisor456");
        createData.put("statusType", StatusTrackerService.STATUS_TYPE_ACCOUNT_OPENING);
        createData.put("statusSummary", "Opening a new brokerage account for client");
        createData.put("createdBy", "advisor456");
        createData.put("sourceId", "WF-12345");
        createData.put("trackingId", "ST-A7B3C-230615");
        
        Status status = statusService.createStatus(createData);
        
        // Get the status by source ID
        Status retrievedStatus = statusService.getStatusBySourceId("WF-12345");
        
        // Verify results
        assertNotNull(retrievedStatus);
        assertEquals(status.getStatusId(), retrievedStatus.getStatusId());
        assertEquals("client123", retrievedStatus.getClientId());
        assertEquals("advisor456", retrievedStatus.getAdvisorId());
        assertEquals("WF-12345", retrievedStatus.getSourceId());
    }

    @Test
    public void testGetStatusByTrackingId() {
        // First create a status
        Map<String, Object> createData = new HashMap<>();
        createData.put("clientId", "client123");
        createData.put("advisorId", "advisor456");
        createData.put("statusType", StatusTrackerService.STATUS_TYPE_ACCOUNT_OPENING);
        createData.put("statusSummary", "Opening a new brokerage account for client");
        createData.put("createdBy", "advisor456");
        createData.put("trackingId", "ST-A7B3C-230615");
        
        Status status = statusService.createStatus(createData);
        
        // Get the status by tracking ID
        Status retrievedStatus = statusService.getStatusByTrackingId("ST-A7B3C-230615");
        
        // Verify results
        assertNotNull(retrievedStatus);
        assertEquals(status.getStatusId(), retrievedStatus.getStatusId());
        assertEquals("client123", retrievedStatus.getClientId());
        assertEquals("advisor456", retrievedStatus.getAdvisorId());
        assertEquals("ST-A7B3C-230615", retrievedStatus.getTrackingId());
    }

    @Test
    public void testGetClientStatuses() {
        // Create multiple statuses for the same client
        String clientId = "client123";
        
        // Create account opening status
        Map<String, Object> status1Data = new HashMap<>();
        status1Data.put("clientId", clientId);
        status1Data.put("advisorId", "advisor456");
        status1Data.put("statusType", StatusTrackerService.STATUS_TYPE_ACCOUNT_OPENING);
        status1Data.put("statusSummary", "Opening a new brokerage account");
        status1Data.put("createdBy", "advisor456");
        status1Data.put("trackingId", "ST-A7B3C-230615-1");
        statusService.createStatus(status1Data);
        
        // Create portfolio review status
        Map<String, Object> status2Data = new HashMap<>();
        status2Data.put("clientId", clientId);
        status2Data.put("advisorId", "advisor456");
        status2Data.put("statusType", StatusTrackerService.STATUS_TYPE_PORTFOLIO_REVIEW);
        status2Data.put("statusSummary", "Annual portfolio review");
        status2Data.put("createdBy", "advisor456");
        status2Data.put("trackingId", "ST-A7B3C-230615-2");
        statusService.createStatus(status2Data);
        
        // Get all client statuses
        List<Status> clientStatuses = mockDynamoDBService.getClientStatuses(clientId, null, null, null);
        
        // Verify results
        assertNotNull(clientStatuses);
        assertEquals(2, clientStatuses.size());
        
        // Get filtered client statuses
        List<Status> filteredStatuses = mockDynamoDBService.getClientStatuses(
            clientId, StatusTrackerService.STATUS_TYPE_PORTFOLIO_REVIEW, null, null);
        
        // Verify filtered results
        assertNotNull(filteredStatuses);
        assertEquals(1, filteredStatuses.size());
        assertEquals(StatusTrackerService.STATUS_TYPE_PORTFOLIO_REVIEW, filteredStatuses.get(0).getStatusType());
    }

    @Test
    public void testSearchStatuses() {
        // Create multiple statuses with different attributes
        
        // Create status 1
        Map<String, Object> status1Data = new HashMap<>();
        status1Data.put("clientId", "client123");
        status1Data.put("advisorId", "advisor456");
        status1Data.put("statusType", StatusTrackerService.STATUS_TYPE_ACCOUNT_OPENING);
        status1Data.put("statusSummary", "Opening a new brokerage account");
        status1Data.put("createdBy", "advisor456");
        status1Data.put("priority", "High");
        status1Data.put("trackingId", "ST-A7B3C-230615-1");
        statusService.createStatus(status1Data);
        
        // Create status 2
        Map<String, Object> status2Data = new HashMap<>();
        status2Data.put("clientId", "client123");
        status2Data.put("advisorId", "advisor456");
        status2Data.put("statusType", StatusTrackerService.STATUS_TYPE_PORTFOLIO_REVIEW);
        status2Data.put("statusSummary", "Annual portfolio review");
        status2Data.put("createdBy", "advisor456");
        status2Data.put("priority", "Medium");
        status2Data.put("trackingId", "ST-A7B3C-230615-2");
        statusService.createStatus(status2Data);
        
        // Create status 3
        Map<String, Object> status3Data = new HashMap<>();
        status3Data.put("clientId", "client789");
        status3Data.put("advisorId", "advisor456");
        status3Data.put("statusType", StatusTrackerService.STATUS_TYPE_FINANCIAL_PLAN);
        status3Data.put("statusSummary", "Creating retirement plan");
        status3Data.put("createdBy", "advisor456");
        status3Data.put("priority", "High");
        status3Data.put("trackingId", "ST-A7B3C-230615-3");
        statusService.createStatus(status3Data);
        
        // Search by client ID
        Map<String, Object> searchByClient = new HashMap<>();
        searchByClient.put("clientId", "client123");
        List<Status> clientResults = statusService.searchStatuses(searchByClient);
        
        // Verify client search results
        assertNotNull(clientResults);
        assertEquals(2, clientResults.size());
        
        // Search by priority
        Map<String, Object> searchByPriority = new HashMap<>();
        searchByPriority.put("priority", "High");
        List<Status> priorityResults = statusService.searchStatuses(searchByPriority);
        
        // Verify priority search results
        assertNotNull(priorityResults);
        assertEquals(2, priorityResults.size());
        
        // Search by text
        Map<String, Object> searchByText = new HashMap<>();
        searchByText.put("textSearch", "retirement");
        List<Status> textResults = statusService.searchStatuses(searchByText);
        
        // Verify text search results
        assertNotNull(textResults);
        assertEquals(1, textResults.size());
        assertEquals("client789", textResults.get(0).getClientId());
    }
} 