# Status Tracker API Documentation

The Status Tracker API provides endpoints for tracking client statuses in financial services applications. This API is designed for brokerage and wealth management firms to track client interactions, financial transactions, and other important events.

## Base URL

```
https://api.example.com/
```

## Authentication

All API requests require authentication. Include your API key in the `Authorization` header:

```
Authorization: Bearer YOUR_API_KEY
```

## Data Storage

The Status Tracker API uses Amazon DynamoDB as its primary data store with DynamoDB Accelerator (DAX) for caching:

- **DynamoDB**: Provides persistent, highly available storage for all status data
- **DAX**: Provides microsecond response times for read operations through in-memory caching

This architecture ensures:
- High throughput for both read and write operations
- Low latency access to frequently requested data
- Automatic scaling to handle varying workloads
- High availability and durability for all status data

### Date Handling

All date fields in the Status Tracker API are represented as strings in ISO-8601 format:

- `createdDate`: When the status was created (e.g., `2023-06-15T14:30:45.123Z`)
- `lastUpdatedDate`: When the status was last updated (e.g., `2023-06-15T15:45:22.456Z`)
- `estimatedCompletionDate`: When the status is expected to be completed (e.g., `2023-07-01T00:00:00.000Z`)
- `actualCompletionDate`: When the status was actually completed (e.g., `2023-06-28T16:20:10.123Z`)

This string-based approach simplifies serialization/deserialization and ensures consistent date representation across different systems and programming languages.

### Source System Integration

The Status Tracker API provides integration with source systems through the following fields:

- `sourceId`: An identifier from the source system (e.g., workflow ID, order ID, application ID)
- `trackingId`: A user-friendly tracking ID for customer service reference
- `sourceSystemUrl`: A URL to the source system where users can view the status directly

The `sourceSystemUrl` is automatically generated based on the `sourceId` prefix, but can also be explicitly provided. For example:
- A sourceId of "WF-12345" will generate a URL like "https://workflow.example.com/view?id=WF-12345"
- A sourceId of "ORD-67890" will generate a URL like "https://orders.example.com/order?id=ORD-67890"

This allows users to easily navigate to the source system for more detailed information about the status.

## Endpoints

### Create Status

Creates a new status entry for tracking a financial transaction or client interaction.

**URL**: `/status`

**Method**: `POST`

**Request Body**:

```json
{
  "clientId": "client123",
  "advisorId": "advisor456",
  "statusType": "account_opening",
  "statusSummary": "Opening a new brokerage account for client",
  "createdBy": "advisor456",
  "sourceId": "WF-12345",
  "trackingId": "ST-A7B3C-230615",
  "statusDetails": {
    "accountType": "Individual Brokerage",
    "initialDeposit": 10000.00,
    "riskProfile": "Moderate"
  },
  "requiredActions": [
    "Complete risk assessment",
    "Verify identity documents",
    "Approve account application"
  ],
  "priority": "High",
  "category": "New Account",
  "subCategory": "Brokerage",
  "tags": {
    "clientSegment": "High Net Worth",
    "region": "Northeast"
  }
}
```

**Response** (201 Created):

```json
{
  "statusId": "550e8400-e29b-41d4-a716-446655440000",
  "clientId": "client123",
  "advisorId": "advisor456",
  "statusType": "account_opening",
  "currentStage": "initiated",
  "statusSummary": "Opening a new brokerage account for client",
  "createdDate": "2023-06-15T14:30:45.123Z",
  "lastUpdatedDate": "2023-06-15T14:30:45.123Z",
  "createdBy": "advisor456",
  "lastUpdatedBy": "advisor456",
  "sourceId": "WF-12345",
  "trackingId": "ST-A7B3C-230615",
  "sourceSystemUrl": "https://workflow.example.com/view?id=WF-12345",
  "statusDetails": {
    "accountType": "Individual Brokerage",
    "initialDeposit": 10000.00,
    "riskProfile": "Moderate"
  },
  "statusHistory": [
    {
      "timestamp": "2023-06-15T14:30:45.123Z",
      "changedBy": "advisor456",
      "previousStage": null,
      "newStage": "initiated",
      "changeDescription": "Status created"
    }
  ],
  "requiredActions": [
    "Complete risk assessment",
    "Verify identity documents",
    "Approve account application"
  ],
  "completedActions": [],
  "priority": "High",
  "category": "New Account",
  "subCategory": "Brokerage",
  "tags": {
    "clientSegment": "High Net Worth",
    "region": "Northeast"
  },
  "metadata": {
    "sentiment": "positive"
  }
}
```

### Get Status

Retrieves a specific status by ID.

**URL**: `/status/{statusId}`

**Method**: `GET`

**URL Parameters**:
- `statusId`: The ID of the status to retrieve

**Response** (200 OK):

```json
{
  "statusId": "550e8400-e29b-41d4-a716-446655440000",
  "clientId": "client123",
  "advisorId": "advisor456",
  "statusType": "account_opening",
  "currentStage": "in_progress",
  "statusSummary": "Processing account opening request",
  "createdDate": "2023-06-15T14:30:45.123Z",
  "lastUpdatedDate": "2023-06-15T15:45:22.456Z",
  "createdBy": "advisor456",
  "lastUpdatedBy": "advisor456",
  "sourceId": "WF-12345",
  "trackingId": "ST-A7B3C-230615",
  "sourceSystemUrl": "https://workflow.example.com/view?id=WF-12345",
  "statusDetails": {
    "accountType": "Individual Brokerage",
    "initialDeposit": 10000.00,
    "riskProfile": "Moderate",
    "applicationId": "APP-12345"
  },
  "statusHistory": [
    {
      "timestamp": "2023-06-15T14:30:45.123Z",
      "changedBy": "advisor456",
      "previousStage": null,
      "newStage": "initiated",
      "changeDescription": "Status created"
    },
    {
      "timestamp": "2023-06-15T15:45:22.456Z",
      "changedBy": "advisor456",
      "previousStage": "initiated",
      "newStage": "in_progress",
      "changeReason": "Started processing the application",
      "changeDescription": "Application received and under review"
    }
  ],
  "requiredActions": [
    "Complete risk assessment",
    "Verify identity documents",
    "Approve account application"
  ],
  "completedActions": [
    "Complete risk assessment"
  ],
  "priority": "High",
  "category": "New Account",
  "subCategory": "Brokerage",
  "tags": {
    "clientSegment": "High Net Worth",
    "region": "Northeast"
  },
  "metadata": {
    "sentiment": "positive"
  }
}
```

### Get Status by Source ID

Retrieves a specific status by its source ID.

**URL**: `/status/source`

**Method**: `GET`

**Query Parameters**:
- `sourceId`: The source ID of the status to retrieve

**Response** (200 OK):

```json
{
  "statusId": "550e8400-e29b-41d4-a716-446655440000",
  "clientId": "client123",
  "advisorId": "advisor456",
  "statusType": "account_opening",
  "currentStage": "in_progress",
  "statusSummary": "Processing account opening request",
  "createdDate": "2023-06-15T14:30:45.123Z",
  "lastUpdatedDate": "2023-06-15T15:45:22.456Z",
  "createdBy": "advisor456",
  "lastUpdatedBy": "advisor456",
  "sourceId": "WF-12345",
  "trackingId": "ST-A7B3C-230615",
  "sourceSystemUrl": "https://workflow.example.com/view?id=WF-12345",
  "statusDetails": {
    "accountType": "Individual Brokerage",
    "initialDeposit": 10000.00,
    "riskProfile": "Moderate",
    "applicationId": "APP-12345"
  },
  "statusHistory": [
    {
      "timestamp": "2023-06-15T14:30:45.123Z",
      "changedBy": "advisor456",
      "previousStage": null,
      "newStage": "initiated",
      "changeDescription": "Status created"
    },
    {
      "timestamp": "2023-06-15T15:45:22.456Z",
      "changedBy": "advisor456",
      "previousStage": "initiated",
      "newStage": "in_progress",
      "changeReason": "Started processing the application",
      "changeDescription": "Application received and under review"
    }
  ],
  "requiredActions": [
    "Complete risk assessment",
    "Verify identity documents",
    "Approve account application"
  ],
  "completedActions": [
    "Complete risk assessment"
  ],
  "priority": "High",
  "category": "New Account",
  "subCategory": "Brokerage",
  "tags": {
    "clientSegment": "High Net Worth",
    "region": "Northeast"
  },
  "metadata": {
    "sentiment": "positive"
  }
}
```

### Get Status by Tracking ID

Retrieves a specific status by its tracking ID.

**URL**: `/status/tracking`

**Method**: `GET`

**Query Parameters**:
- `trackingId`: The tracking ID of the status to retrieve

**Response** (200 OK):

```json
{
  "statusId": "550e8400-e29b-41d4-a716-446655440000",
  "clientId": "client123",
  "advisorId": "advisor456",
  "statusType": "account_opening",
  "currentStage": "in_progress",
  "statusSummary": "Processing account opening request",
  "createdDate": "2023-06-15T14:30:45.123Z",
  "lastUpdatedDate": "2023-06-15T15:45:22.456Z",
  "createdBy": "advisor456",
  "lastUpdatedBy": "advisor456",
  "sourceId": "WF-12345",
  "trackingId": "ST-A7B3C-230615",
  "sourceSystemUrl": "https://workflow.example.com/view?id=WF-12345",
  "statusDetails": {
    "accountType": "Individual Brokerage",
    "initialDeposit": 10000.00,
    "riskProfile": "Moderate",
    "applicationId": "APP-12345"
  },
  "statusHistory": [
    {
      "timestamp": "2023-06-15T14:30:45.123Z",
      "changedBy": "advisor456",
      "previousStage": null,
      "newStage": "initiated",
      "changeDescription": "Status created"
    },
    {
      "timestamp": "2023-06-15T15:45:22.456Z",
      "changedBy": "advisor456",
      "previousStage": "initiated",
      "newStage": "in_progress",
      "changeReason": "Started processing the application",
      "changeDescription": "Application received and under review"
    }
  ],
  "requiredActions": [
    "Complete risk assessment",
    "Verify identity documents",
    "Approve account application"
  ],
  "completedActions": [
    "Complete risk assessment"
  ],
  "priority": "High",
  "category": "New Account",
  "subCategory": "Brokerage",
  "tags": {
    "clientSegment": "High Net Worth",
    "region": "Northeast"
  },
  "metadata": {
    "sentiment": "positive"
  }
}
```

### Update Status

Updates an existing status entry.

**URL**: `/status/{statusId}`

**Method**: `PUT`

**URL Parameters**:
- `statusId`: The ID of the status to update

**Request Body**:

```json
{
  "currentStage": "in_progress",
  "statusSummary": "Processing account opening request",
  "updatedBy": "advisor456",
  "changeReason": "Started processing the application",
  "changeDescription": "Application received and under review",
  "statusDetails": {
    "applicationId": "APP-12345"
  },
  "completedActions": [
    "Complete risk assessment"
  ]
}
```

**Response** (200 OK):

```json
{
  "statusId": "550e8400-e29b-41d4-a716-446655440000",
  "clientId": "client123",
  "advisorId": "advisor456",
  "statusType": "account_opening",
  "currentStage": "in_progress",
  "statusSummary": "Processing account opening request",
  "createdDate": "2023-06-15T14:30:45.123Z",
  "lastUpdatedDate": "2023-06-15T15:45:22.456Z",
  "createdBy": "advisor456",
  "lastUpdatedBy": "advisor456",
  "sourceId": "WF-12345",
  "trackingId": "ST-A7B3C-230615",
  "sourceSystemUrl": "https://workflow.example.com/view?id=WF-12345",
  "statusDetails": {
    "accountType": "Individual Brokerage",
    "initialDeposit": 10000.00,
    "riskProfile": "Moderate",
    "applicationId": "APP-12345"
  },
  "statusHistory": [
    {
      "timestamp": "2023-06-15T14:30:45.123Z",
      "changedBy": "advisor456",
      "previousStage": null,
      "newStage": "initiated",
      "changeDescription": "Status created"
    },
    {
      "timestamp": "2023-06-15T15:45:22.456Z",
      "changedBy": "advisor456",
      "previousStage": "initiated",
      "newStage": "in_progress",
      "changeReason": "Started processing the application",
      "changeDescription": "Application received and under review"
    }
  ],
  "requiredActions": [
    "Complete risk assessment",
    "Verify identity documents",
    "Approve account application"
  ],
  "completedActions": [
    "Complete risk assessment"
  ],
  "priority": "High",
  "category": "New Account",
  "subCategory": "Brokerage",
  "tags": {
    "clientSegment": "High Net Worth",
    "region": "Northeast"
  },
  "metadata": {
    "sentiment": "positive"
  }
}
```

### List Client Statuses

Lists all statuses for a specific client.

**URL**: `/client/{clientId}/statuses`

**Method**: `GET`

**URL Parameters**:
- `clientId`: The ID of the client

**Query Parameters**:
- `statusType` (optional): Filter by status type
- `fromDate` (optional): Filter by created date (format: ISO 8601)
- `toDate` (optional): Filter by created date (format: ISO 8601)

**Response** (200 OK):

```json
[
  {
    "statusId": "550e8400-e29b-41d4-a716-446655440000",
    "clientId": "client123",
    "advisorId": "advisor456",
    "statusType": "account_opening",
    "currentStage": "in_progress",
    "statusSummary": "Processing account opening request",
    "createdDate": "2023-06-15T14:30:45.123Z",
    "lastUpdatedDate": "2023-06-15T15:45:22.456Z",
    "priority": "High",
    "category": "New Account",
    "subCategory": "Brokerage"
  },
  {
    "statusId": "550e8400-e29b-41d4-a716-446655440001",
    "clientId": "client123",
    "advisorId": "advisor456",
    "statusType": "portfolio_review",
    "currentStage": "completed",
    "statusSummary": "Annual portfolio review completed",
    "createdDate": "2023-05-10T09:15:30.789Z",
    "lastUpdatedDate": "2023-05-12T16:20:10.123Z",
    "priority": "Medium",
    "category": "Review",
    "subCategory": "Annual"
  }
]
```

### List Advisor Client Statuses

Lists all client statuses for a specific advisor.

**URL**: `/advisor/{advisorId}/client-statuses`

**Method**: `GET`

**URL Parameters**:
- `advisorId`: The ID of the advisor

**Query Parameters**:
- `statusType` (optional): Filter by status type
- `fromDate` (optional): Filter by created date (format: ISO 8601)
- `toDate` (optional): Filter by created date (format: ISO 8601)

**Response** (200 OK):

```json
{
  "client123": [
    {
      "statusId": "550e8400-e29b-41d4-a716-446655440000",
      "clientId": "client123",
      "advisorId": "advisor456",
      "statusType": "account_opening",
      "currentStage": "in_progress",
      "statusSummary": "Processing account opening request",
      "createdDate": "2023-06-15T14:30:45.123Z",
      "lastUpdatedDate": "2023-06-15T15:45:22.456Z",
      "priority": "High",
      "category": "New Account",
      "subCategory": "Brokerage"
    },
    {
      "statusId": "550e8400-e29b-41d4-a716-446655440001",
      "clientId": "client123",
      "advisorId": "advisor456",
      "statusType": "portfolio_review",
      "currentStage": "completed",
      "statusSummary": "Annual portfolio review completed",
      "createdDate": "2023-05-10T09:15:30.789Z",
      "lastUpdatedDate": "2023-05-12T16:20:10.123Z",
      "priority": "Medium",
      "category": "Review",
      "subCategory": "Annual"
    }
  ],
  "client789": [
    {
      "statusId": "550e8400-e29b-41d4-a716-446655440002",
      "clientId": "client789",
      "advisorId": "advisor456",
      "statusType": "financial_plan",
      "currentStage": "pending_review",
      "statusSummary": "Retirement plan ready for review",
      "createdDate": "2023-06-01T11:22:33.456Z",
      "lastUpdatedDate": "2023-06-05T14:25:36.789Z",
      "priority": "High",
      "category": "Planning",
      "subCategory": "Retirement"
    }
  ]
}
```

### Search Statuses

Searches for statuses based on various criteria.

**URL**: `/statuses/search`

**Method**: `POST`

**Request Body**:

```json
{
  "clientId": "client123",
  "statusType": "account_opening",
  "currentStage": "in_progress",
  "priority": "High",
  "textSearch": "brokerage account",
  "sortBy": "createdDate",
  "sortOrder": "desc"
}
```

**Response** (200 OK):

```json
[
  {
    "statusId": "550e8400-e29b-41d4-a716-446655440000",
    "clientId": "client123",
    "advisorId": "advisor456",
    "statusType": "account_opening",
    "currentStage": "in_progress",
    "statusSummary": "Processing account opening request for brokerage account",
    "createdDate": "2023-06-15T14:30:45.123Z",
    "lastUpdatedDate": "2023-06-15T15:45:22.456Z",
    "priority": "High",
    "category": "New Account",
    "subCategory": "Brokerage",
    "tags": {
      "clientSegment": "High Net Worth",
      "region": "Northeast"
    },
    "metadata": {
      "sentiment": "positive"
    }
  }
]
```

## Status Types

The API supports the following status types:

- `account_opening`: Tracking the process of opening a new account
- `portfolio_review`: Tracking portfolio review sessions
- `financial_plan`: Tracking the creation and review of financial plans
- `trade_execution`: Tracking trade execution requests
- `fund_transfer`: Tracking fund transfer requests
- `tax_document`: Tracking tax document preparation and delivery
- `compliance_check`: Tracking compliance checks and reviews
- `client_meeting`: Tracking client meetings and follow-ups

## Status Stages

The API supports the following status stages:

- `initiated`: Status has been created but no action has been taken
- `in_progress`: Status is being actively worked on
- `pending_review`: Status is waiting for review
- `pending_client_action`: Status is waiting for client action
- `pending_advisor_action`: Status is waiting for advisor action
- `pending_compliance`: Status is waiting for compliance review
- `approved`: Status has been approved
- `rejected`: Status has been rejected
- `completed`: Status has been completed
- `cancelled`: Status has been cancelled

## Error Responses

### 400 Bad Request

```json
{
  "error": {
    "type": "Bad Request",
    "message": "Invalid request body"
  }
}
```

### 404 Not Found

```json
{
  "error": {
    "type": "Not Found",
    "message": "Status not found with ID: 550e8400-e29b-41d4-a716-446655440000"
  }
}
```

### 500 Internal Server Error

```json
{
  "error": {
    "type": "Internal Server Error",
    "message": "An unexpected error occurred"
  }
}
``` 