# Status Tracker Function

A serverless AWS Lambda function for tracking client statuses in financial services applications. This function is designed for brokerage and wealth management firms to track client interactions, financial transactions, and other important events.

## Features

- **Comprehensive Status Tracking**: Track client statuses across various financial services processes
- **Rich Metadata**: Store detailed information about each status, including history, required actions, and more
- **Sentiment Analysis**: Automatically analyze sentiment of status summaries using Amazon Comprehend
- **Advanced Search**: Search for statuses based on various criteria, including client, advisor, status type, and more
- **Client-Advisor Relationship Management**: Track statuses across client-advisor relationships
- **High-Performance Data Storage**: Utilizes Amazon DynamoDB with DAX for fast, scalable data access
- **Efficient Caching**: Implements DynamoDB Accelerator (DAX) for improved read performance

## Status Types

The service supports various status types relevant to financial services:

- Account Opening
- Portfolio Review
- Financial Plan
- Trade Execution
- Fund Transfer
- Tax Document
- Compliance Check
- Client Meeting

## Prerequisites

- Java 17 or later
- Maven 3.6 or later
- AWS CLI configured with appropriate credentials
- AWS SAM CLI (for local testing)
- Amazon VPC with at least 3 subnets for DAX deployment

## Setup

1. Clone the repository:

```bash
git clone https://github.com/yourusername/status-tracker-function.git
cd status-tracker-function
```

2. Build the project:

```bash
mvn clean package
```

3. Deploy to AWS:

```bash
aws cloudformation package \
  --template-file template.yaml \
  --s3-bucket your-deployment-bucket \
  --output-template-file packaged.yaml

aws cloudformation deploy \
  --template-file packaged.yaml \
  --stack-name status-tracker-stack \
  --capabilities CAPABILITY_IAM \
  --parameter-overrides \
    VPCID=vpc-xxxxxxxx \
    DAXSubnet1=subnet-xxxxxxxx \
    DAXSubnet2=subnet-yyyyyyyy \
    DAXSubnet3=subnet-zzzzzzzz
```

## Local Testing

You can test the function locally using the AWS SAM CLI:

```bash
sam local start-api
```

This will start a local API Gateway that you can use to test the function. The API will be available at `http://localhost:3000`.

## API Documentation

For detailed API documentation, see [API_DOCUMENTATION.md](API_DOCUMENTATION.md).

## Example Usage

### Create a Status

```bash
curl -X POST \
  http://localhost:3000/status \
  -H 'Content-Type: application/json' \
  -d '{
    "clientId": "client123",
    "advisorId": "advisor456",
    "statusType": "account_opening",
    "statusSummary": "Opening a new brokerage account for client",
    "createdBy": "advisor456",
    "sourceId": "WF-12345",
    "trackingId": "ST-A7B3C-230615",
    "statusDetails": {
      "accountType": "Individual Brokerage",
      "initialDeposit": 10000.00
    },
    "requiredActions": [
      "Complete risk assessment",
      "Verify identity documents"
    ],
    "priority": "High"
  }'
```

### Get a Status

```bash
curl -X GET http://localhost:3000/status/550e8400-e29b-41d4-a716-446655440000
```

### Get a Status by Source ID

```bash
curl -X GET http://localhost:3000/status/source?sourceId=WF-12345
```

### Get a Status by Tracking ID

```bash
curl -X GET http://localhost:3000/status/tracking?trackingId=ST-A7B3C-230615
```

### Update a Status

```bash
curl -X PUT \
  http://localhost:3000/status/550e8400-e29b-41d4-a716-446655440000 \
  -H 'Content-Type: application/json' \
  -d '{
    "currentStage": "in_progress",
    "statusSummary": "Processing account opening request",
    "updatedBy": "advisor456",
    "changeReason": "Started processing the application"
  }'
```

### List Client Statuses

```bash
curl -X GET http://localhost:3000/client/client123/statuses
```

### Search Statuses

```bash
curl -X POST \
  http://localhost:3000/statuses/search \
  -H 'Content-Type: application/json' \
  -d '{
    "priority": "High",
    "textSearch": "brokerage account"
  }'
```

## Architecture

The Status Tracker Function is built using the following technologies:

- **AWS Lambda**: Serverless compute service
- **Amazon API Gateway**: API management service
- **Amazon DynamoDB**: NoSQL database service for persistent storage
- **Amazon DynamoDB Accelerator (DAX)**: In-memory cache for DynamoDB
- **Amazon Comprehend**: Natural language processing service for sentiment analysis

### Data Storage Architecture

The application uses a sophisticated data storage approach:

1. **Primary Storage**: Amazon DynamoDB provides the primary storage for all status data
   - Uses a single table design with Global Secondary Indexes (GSIs) for efficient queries
   - Indexes for clientId, advisorId, statusType, sourceId, and trackingId enable fast lookups

2. **Caching Layer**: DynamoDB Accelerator (DAX) provides in-memory caching
   - Significantly reduces read latency from milliseconds to microseconds
   - Handles read-heavy workloads efficiently without additional coding complexity
   - Automatically manages cache invalidation when data is updated

3. **Data Access Pattern**:
   - Write operations go directly to DynamoDB
   - Read operations check the DAX cache first
   - If data is not in cache, DAX retrieves it from DynamoDB and caches it for future requests

This architecture ensures high performance, scalability, and reliability for the Status Tracker service.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 