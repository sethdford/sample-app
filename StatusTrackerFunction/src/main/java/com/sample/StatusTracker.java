package com.sample;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.sample.model.Status;
import com.sample.service.StatusTrackerService;
import com.sample.util.ApiGatewayResponseUtil;
import com.sample.util.JsonUtil;

/**
 * Handler for requests to Lambda function.
 * This class processes API Gateway requests for the Status Tracker service,
 * which tracks financial transactions, client interactions, and provides
 * holistic views for advisors.
 */
public class StatusTracker implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final StatusTrackerService statusService;
    private final ApiGatewayResponseUtil responseUtil;
    private final JsonUtil jsonUtil;

    /**
     * Default constructor.
     */
    public StatusTracker() {
        this.statusService = new StatusTrackerService();
        this.responseUtil = new ApiGatewayResponseUtil();
        this.jsonUtil = new JsonUtil();
    }
    
    /**
     * Constructor with dependency injection for testing.
     * 
     * @param statusService The StatusTrackerService to use
     */
    public StatusTracker(StatusTrackerService statusService) {
        this.statusService = statusService;
        this.responseUtil = new ApiGatewayResponseUtil();
        this.jsonUtil = new JsonUtil();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            String path = input.getPath();
            String httpMethod = input.getHttpMethod();
            
            // Log the request
            context.getLogger().log("Processing " + httpMethod + " request for " + path);
            
            // Route the request based on the path and method
            if (path.equals("/status") && httpMethod.equals("POST")) {
                return createStatus(input, context);
            } else if (path.matches("/status/[^/]+") && httpMethod.equals("GET")) {
                return getStatus(input, context);
            } else if (path.matches("/status/[^/]+") && httpMethod.equals("PUT")) {
                return updateStatus(input, context);
            } else if (path.matches("/client/[^/]+/statuses") && httpMethod.equals("GET")) {
                return listClientStatuses(input, context);
            } else if (path.matches("/advisor/[^/]+/client-statuses") && httpMethod.equals("GET")) {
                return listAdvisorClientStatuses(input, context);
            } else if (path.matches("/statuses/search") && httpMethod.equals("POST")) {
                return searchStatuses(input, context);
            } else if (path.matches("/status/source") && httpMethod.equals("GET")) {
                return getStatusBySourceId(input, context);
            } else if (path.matches("/status/tracking") && httpMethod.equals("GET")) {
                return getStatusByTrackingId(input, context);
            } else {
                return responseUtil.buildErrorResponse(404, "Not Found", "The requested resource was not found");
            }
        } catch (Exception e) {
            context.getLogger().log("Error processing request: " + e.getMessage());
            return responseUtil.buildErrorResponse(500, "Internal Server Error", e.getMessage());
        }
    }

    /**
     * Creates a new status entry for tracking a financial transaction or client interaction.
     */
    private APIGatewayProxyResponseEvent createStatus(APIGatewayProxyRequestEvent input, Context context) {
        try {
            // Parse the request body
            String requestBody = input.getBody();
            Map<String, Object> statusData = jsonUtil.fromJson(requestBody, Map.class);
            
            // Create the status
            Status status = statusService.createStatus(statusData);
            
            // Return the created status
            return responseUtil.buildSuccessResponse(201, jsonUtil.toJson(status));
        } catch (Exception e) {
            context.getLogger().log("Error creating status: " + e.getMessage());
            return responseUtil.buildErrorResponse(400, "Bad Request", e.getMessage());
        }
    }

    /**
     * Retrieves a specific status by ID.
     */
    private APIGatewayProxyResponseEvent getStatus(APIGatewayProxyRequestEvent input, Context context) {
        try {
            // Extract the status ID from the path
            String statusId = input.getPathParameters().get("statusId");
            
            // Get the status
            Status status = statusService.getStatus(statusId);
            
            if (status == null) {
                return responseUtil.buildErrorResponse(404, "Not Found", "Status not found with ID: " + statusId);
            }
            
            // Return the status
            return responseUtil.buildSuccessResponse(200, jsonUtil.toJson(status));
        } catch (Exception e) {
            context.getLogger().log("Error getting status: " + e.getMessage());
            return responseUtil.buildErrorResponse(500, "Internal Server Error", e.getMessage());
        }
    }

    /**
     * Updates an existing status entry.
     */
    private APIGatewayProxyResponseEvent updateStatus(APIGatewayProxyRequestEvent input, Context context) {
        try {
            // Extract the status ID from the path
            String statusId = input.getPathParameters().get("statusId");
            
            // Parse the request body
            String requestBody = input.getBody();
            Map<String, Object> statusData = jsonUtil.fromJson(requestBody, Map.class);
            
            // Update the status
            Status updatedStatus = statusService.updateStatus(statusId, statusData);
            
            if (updatedStatus == null) {
                return responseUtil.buildErrorResponse(404, "Not Found", "Status not found with ID: " + statusId);
            }
            
            // Return the updated status
            return responseUtil.buildSuccessResponse(200, jsonUtil.toJson(updatedStatus));
        } catch (Exception e) {
            context.getLogger().log("Error updating status: " + e.getMessage());
            return responseUtil.buildErrorResponse(400, "Bad Request", e.getMessage());
        }
    }

    /**
     * Lists all statuses for a specific client.
     */
    private APIGatewayProxyResponseEvent listClientStatuses(APIGatewayProxyRequestEvent input, Context context) {
        try {
            // Extract the client ID from the path
            String clientId = input.getPathParameters().get("clientId");
            
            // Get query parameters for filtering
            Map<String, String> queryParams = input.getQueryStringParameters();
            String statusType = queryParams != null ? queryParams.get("statusType") : null;
            String fromDate = queryParams != null ? queryParams.get("fromDate") : null;
            String toDate = queryParams != null ? queryParams.get("toDate") : null;
            
            // Get the statuses
            List<Status> statuses = statusService.getClientStatuses(clientId, statusType, fromDate, toDate);
            
            // Return the statuses
            return responseUtil.buildSuccessResponse(200, jsonUtil.toJson(statuses));
        } catch (Exception e) {
            context.getLogger().log("Error listing client statuses: " + e.getMessage());
            return responseUtil.buildErrorResponse(500, "Internal Server Error", e.getMessage());
        }
    }

    /**
     * Lists all client statuses for a specific advisor.
     */
    private APIGatewayProxyResponseEvent listAdvisorClientStatuses(APIGatewayProxyRequestEvent input, Context context) {
        try {
            // Extract the advisor ID from the path
            String advisorId = input.getPathParameters().get("advisorId");
            
            // Get query parameters for filtering
            Map<String, String> queryParams = input.getQueryStringParameters();
            String statusType = queryParams != null ? queryParams.get("statusType") : null;
            String fromDate = queryParams != null ? queryParams.get("fromDate") : null;
            String toDate = queryParams != null ? queryParams.get("toDate") : null;
            
            // Get the statuses
            Map<String, List<Status>> clientStatuses = statusService.getAdvisorClientStatuses(advisorId, statusType, fromDate, toDate);
            
            // Return the statuses
            return responseUtil.buildSuccessResponse(200, jsonUtil.toJson(clientStatuses));
        } catch (Exception e) {
            context.getLogger().log("Error listing advisor client statuses: " + e.getMessage());
            return responseUtil.buildErrorResponse(500, "Internal Server Error", e.getMessage());
        }
    }

    /**
     * Searches for statuses based on various criteria.
     */
    private APIGatewayProxyResponseEvent searchStatuses(APIGatewayProxyRequestEvent input, Context context) {
        try {
            // Parse the request body
            String requestBody = input.getBody();
            Map<String, Object> searchCriteria = jsonUtil.fromJson(requestBody, Map.class);
            
            // Search for statuses
            List<Status> statuses = statusService.searchStatuses(searchCriteria);
            
            // Return the search results
            return responseUtil.buildSuccessResponse(200, jsonUtil.toJson(statuses));
        } catch (Exception e) {
            context.getLogger().log("Error searching statuses: " + e.getMessage());
            return responseUtil.buildErrorResponse(400, "Bad Request", e.getMessage());
        }
    }
    
    /**
     * Retrieves a status by its source ID (e.g., workflow ID, order ID).
     * This allows linking back to external systems.
     */
    private APIGatewayProxyResponseEvent getStatusBySourceId(APIGatewayProxyRequestEvent input, Context context) {
        try {
            // Get the source ID from query parameters
            Map<String, String> queryParams = input.getQueryStringParameters();
            if (queryParams == null || !queryParams.containsKey("sourceId")) {
                context.getLogger().log("Missing sourceId query parameter");
                return responseUtil.buildErrorResponse(400, "Bad Request", "sourceId query parameter is required");
            }
            
            String sourceId = queryParams.get("sourceId");
            context.getLogger().log("Looking up status with sourceId: " + sourceId);
            
            // Get the status by source ID
            Status status = statusService.getStatusBySourceId(sourceId);
            
            if (status == null) {
                context.getLogger().log("Status not found with sourceId: " + sourceId);
                return responseUtil.buildErrorResponse(404, "Not Found", "Status not found with source ID: " + sourceId);
            }
            
            context.getLogger().log("Found status with sourceId: " + sourceId + ", statusId: " + status.getStatusId());
            
            // Return the status
            return responseUtil.buildSuccessResponse(200, jsonUtil.toJson(status));
        } catch (Exception e) {
            context.getLogger().log("Error getting status by source ID: " + e.getMessage());
            return responseUtil.buildErrorResponse(500, "Internal Server Error", e.getMessage());
        }
    }
    
    /**
     * Retrieves a status by its tracking ID.
     * This allows customer service to easily look up statuses based on user-provided tracking IDs.
     */
    private APIGatewayProxyResponseEvent getStatusByTrackingId(APIGatewayProxyRequestEvent input, Context context) {
        try {
            // Get the tracking ID from query parameters
            Map<String, String> queryParams = input.getQueryStringParameters();
            if (queryParams == null || !queryParams.containsKey("trackingId")) {
                context.getLogger().log("Missing trackingId query parameter");
                return responseUtil.buildErrorResponse(400, "Bad Request", "trackingId query parameter is required");
            }
            
            String trackingId = queryParams.get("trackingId");
            context.getLogger().log("Looking up status with trackingId: " + trackingId);
            
            // Get the status by tracking ID
            Status status = statusService.getStatusByTrackingId(trackingId);
            
            if (status == null) {
                context.getLogger().log("Status not found with trackingId: " + trackingId);
                return responseUtil.buildErrorResponse(404, "Not Found", "Status not found with tracking ID: " + trackingId);
            }
            
            context.getLogger().log("Found status with trackingId: " + trackingId + ", statusId: " + status.getStatusId());
            
            // Return the status
            return responseUtil.buildSuccessResponse(200, jsonUtil.toJson(status));
        } catch (Exception e) {
            context.getLogger().log("Error getting status by tracking ID: " + e.getMessage());
            return responseUtil.buildErrorResponse(500, "Internal Server Error", e.getMessage());
        }
    }

    /**
     * Gets the status service.
     * 
     * @return The status service
     */
    public StatusTrackerService getStatusService() {
        return statusService;
    }
} 