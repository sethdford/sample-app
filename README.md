# Financial Services Embedding Application

This project contains source code and supporting files for a serverless application that uses AWS Bedrock to generate embeddings for financial customer profiles, trading patterns, and behavioral analysis. Designed specifically for financial services companies like Schwab, Fidelity, JP Morgan, and Robinhood, this application enables sophisticated customer insights, personalized recommendations, and regulatory compliance monitoring.

## Project Structure

- `EmbeddingFunction/src/main` - Code for the application's Lambda function that handles embedding generation and user matching.
- `events` - Invocation events that you can use to invoke the function.
- `EmbeddingFunction/src/test` - Unit tests for the application code.
- `template.yaml` - A template that defines the application's AWS resources.

## Architecture

The application uses several AWS resources:
- **Lambda functions** for serverless compute
- **API Gateway** for RESTful API endpoints
- **DynamoDB** with **DAX** for high-performance storage and caching of embeddings
- **AWS Bedrock** (Amazon Titan Embedding v2) for generating high-quality embeddings
- **CloudWatch** for monitoring, telemetry, and log analysis

These resources are defined in the `template.yaml` file. You can update the template to add AWS resources through the same deployment process that updates your application code.

## Core Capabilities

### 1. Financial Customer Profiling
Generate comprehensive embeddings of customer financial profiles, including:
- Investment preferences and allocations
- Risk tolerance profiles
- Financial goals (retirement, education, wealth accumulation)
- Account types (IRA, 401k, brokerage)
- Advisory service preferences

### 2. Trading Pattern Analysis
Analyze customer trading behaviors to identify:
- Trading styles (day trader, buy-and-hold, diversified)
- Preferred securities and asset classes
- Order type preferences
- Success rates and trade values
- Trading journey timelines

### 3. Compliance Monitoring
Monitor and analyze compliance patterns:
- Detect regulatory violations
- Assess compliance risk levels
- Track compliance events chronologically
- Calculate compliance rates
- Categorize by severity and type

### 4. Behavioral Analysis
Process customer behavioral data including:
- Trading events
- Offer acceptances
- Advisory interactions
- Search queries
- Page views

### 5. Client Effort Analysis
Identify friction points in customer journeys:
- Detect repeated errors and failed actions
- Identify excessive navigation patterns
- Monitor channel switching behavior
- Calculate client effort scores
- Generate recommendations for improving customer experience

## Embedding Approaches

The application supports multiple approaches for generating embeddings from different types of financial customer data:

### 1. Financial Profile Embedding

Processes financial interests, investment preferences, risk profiles, and account types to create embeddings that represent a client's financial profile.

```java
FinancialCustomerEmbeddingService financialService = new FinancialCustomerEmbeddingService();
double[] embedding = financialService.processFinancialProfile(
    clientId, 
    investmentPreferences, 
    financialGoals, 
    riskProfile, 
    accountTypes,
    advisoryServices
);
```

This approach is particularly useful for:
- Matching clients with appropriate financial advisors
- Recommending relevant financial products
- Identifying clients with similar financial profiles
- Personalizing investment recommendations
- Targeting communications around life events (retirement, college planning, etc.)

### 2. Trading Pattern Embedding

Analyzes trading logs and patterns to create embeddings that represent a client's trading behavior.

```java
CloudWatchLogEmbeddingService logService = new CloudWatchLogEmbeddingService();
Map<String, Object> analysis = logService.analyzeTradingPatterns(userId, logGroupName, hoursBack);
```

This approach helps with:
- Identifying trading styles and preferences
- Detecting anomalous trading behavior
- Providing personalized trading recommendations
- Improving trade execution services
- Compliance monitoring for trading activities

### 3. Behavioral Embedding

Processes investor activity logs, clickstream data, and other behavioral signals to create embeddings that represent investor behavior patterns.

```java
BehavioralEmbeddingService behavioralService = new BehavioralEmbeddingService();
double[] embedding = behavioralService.generateBehavioralEmbedding(userId, behavioralData);
```

Key behavioral data includes:
- Trade events (buy, sell, modify orders)
- Offer acceptance events (advisory services, account upgrades)
- Advisory interaction events (consultations, portfolio reviews)
- Search queries (stock symbols, investment strategies)
- Page views (research, account management, trading platforms)

### 4. Compliance Pattern Embedding

Analyzes compliance logs and regulatory events to create embeddings that represent compliance patterns.

```java
CloudWatchLogEmbeddingService logService = new CloudWatchLogEmbeddingService();
Map<String, Object> analysis = logService.analyzeCompliancePatterns(userId, logGroupName, hoursBack);
```

This approach helps with:
- Identifying compliance risk levels
- Detecting patterns of regulatory violations
- Monitoring KYC/AML compliance
- Tracking compliance events over time
- Generating compliance risk scores

### 5. Client Effort Embedding

Analyzes user behavior patterns from logs to identify high client effort scenarios such as repeated errors, excessive navigation, and channel switching.

```java
CloudWatchLogEmbeddingService service = new CloudWatchLogEmbeddingService();
double[] embedding = service.generateClientEffortEmbedding(userId, logGroupName, hoursBack);
Map<String, Object> effortAnalysis = service.analyzeClientEffort(logs);
```

This approach helps identify:
- Friction points in customer journeys
- UI/UX issues in trading platforms
- Process inefficiencies in account management
- Customer frustration indicators
- Opportunities for service improvement

## Enhanced DynamoDB Storage with DAX

All embedding types are stored in DynamoDB with metadata for easy retrieval and comparison, with DAX providing high-performance caching:

```java
EnhancedDynamoDBService dynamoDBService = new EnhancedDynamoDBService();
dynamoDBService.storeEmbeddingWithMetadata(userId, embeddingType, embedding, metadata);
```

The DAX client is automatically initialized if the `DAX_ENDPOINT` environment variable is set, providing:
- Microsecond response times for embedding retrieval
- Reduced load on DynamoDB
- Improved application performance
- Cost savings on DynamoDB read operations

## CloudWatch Log Analysis

The application includes sophisticated log analysis capabilities specifically designed for financial services:

### Trading Log Analysis
- Extracts trading patterns from CloudWatch logs
- Identifies trading styles based on order types and frequencies
- Calculates success rates and trade values
- Tracks chronological trading journeys

### Compliance Log Analysis
- Monitors regulatory compliance events
- Calculates compliance rates and risk levels
- Categorizes compliance events by type and severity
- Tracks violations and remediation actions

### Security Log Analysis
- Monitors authentication and authorization events
- Tracks suspicious activities and security incidents
- Identifies potential security vulnerabilities
- Monitors multi-factor authentication usage

### Account Activity Analysis
- Tracks account creation, modification, and closure events
- Monitors account funding and withdrawal activities
- Analyzes account settings changes
- Tracks beneficiary and ownership changes

## Demo Applications

The project includes several demo applications to showcase different embedding approaches:

1. **FinancialProfileDemo** - Showcases financial profile embeddings for clients of financial institutions
2. **BehavioralEmbeddingDemo** - Shows how to generate embeddings from investor behavioral data
3. **CloudWatchLogEmbeddingDemo** - Demonstrates how to analyze trading patterns and compliance events
4. **ClientEffortDemo** - Shows how to detect high client effort patterns in customer journeys
5. **TradingPatternDemo** - Illustrates how to analyze and categorize trading behaviors

To run a demo, use the following command:

```bash
java -cp target/EmbeddingFunction-1.0.jar com.sample.examples.FinancialProfileDemo
```

## Use Cases for Financial Services

### 1. Personalized Investment Recommendations
- Generate embeddings from client financial profiles
- Match with similar investment products and strategies
- Provide tailored recommendations based on risk tolerance and goals

### 2. Fraud Detection
- Identify anomalous trading patterns using embedding comparisons
- Detect unusual account activities that deviate from normal behavior
- Flag potentially fraudulent transactions for review

### 3. Client Segmentation
- Cluster clients based on embedding similarity
- Create targeted marketing campaigns for specific segments
- Develop specialized service offerings for different client groups

### 4. Advisor Matching
- Match clients with financial advisors based on embedding similarity
- Ensure compatibility between client needs and advisor expertise
- Improve client satisfaction and retention

### 5. Regulatory Compliance
- Monitor trading activities for compliance violations
- Generate compliance risk scores for clients and transactions
- Provide early warning for potential regulatory issues

### 6. Portfolio Analysis
- Compare client portfolios using embedding similarity
- Identify diversification opportunities
- Recommend portfolio adjustments based on similar clients

### 7. Client Experience Optimization
- Identify high-effort client journeys
- Optimize digital platforms based on behavioral patterns
- Reduce friction in trading and account management processes

## Development Environment

If you prefer to use an integrated development environment (IDE) to build and test your application, you can use the AWS Toolkit.  
The AWS Toolkit is an open source plug-in for popular IDEs that uses the SAM CLI to build and deploy serverless applications on AWS. The AWS Toolkit also adds a simplified step-through debugging experience for Lambda function code.

* [VS Code](https://docs.aws.amazon.com/toolkit-for-vscode/latest/userguide/welcome.html)
* [IntelliJ](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [PyCharm](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [Visual Studio](https://docs.aws.amazon.com/toolkit-for-visual-studio/latest/user-guide/welcome.html)

## Deploy the application

The Serverless Application Model Command Line Interface (SAM CLI) is an extension of the AWS CLI that adds functionality for building and testing Lambda applications. It uses Docker to run your functions in an Amazon Linux environment that matches Lambda.

### Prerequisites

* SAM CLI - [Install the SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)
* Java 17 - [Install Java 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html)
* Maven - [Install Maven](https://maven.apache.org/install.html)
* Docker - [Install Docker community edition](https://hub.docker.com/search/?type=edition&offering=community)
* AWS Account with Bedrock access

### Deployment Steps

To build and deploy your application for the first time, run the following in your shell:

```bash
sam build
sam deploy --guided
```

The first command will build the source of your application. The second command will package and deploy your application to AWS, with a series of prompts:

* **Stack Name**: The name of the stack to deploy to CloudFormation. This should be unique to your account and region.
* **AWS Region**: The AWS region you want to deploy your app to.
* **Confirm changes before deploy**: If set to yes, any change sets will be shown to you before execution for manual review.
* **Allow SAM CLI IAM role creation**: Many AWS SAM templates, including this example, create AWS IAM roles required for the AWS Lambda function(s) included to access AWS services.
* **Save arguments to samconfig.toml**: If set to yes, your choices will be saved to a configuration file inside the project.

You can find your API Gateway Endpoint URL in the output values displayed after deployment.

## Local Testing

Build your application with the `sam build` command.

```bash
sam build
```

The SAM CLI installs dependencies defined in `EmbeddingFunction/pom.xml`, creates a deployment package, and saves it in the `.aws-sam/build` folder.

Test a single function by invoking it directly with a test event:

```bash
sam local invoke EmbeddingFunction --event events/event.json
```

## API Endpoints

The application provides the following API endpoints:

- `/generateFinancialEmbedding` - Generate an embedding from financial profile data
- `/analyzeTradingPatterns` - Analyze trading patterns from log data
- `/analyzeCompliancePatterns` - Analyze compliance patterns from log data
- `/generateBehavioralEmbedding` - Generate an embedding from behavioral data
- `/analyzeClientEffort` - Analyze client effort from log data
- `/findSimilarClients` - Find clients with similar profiles based on embedding similarity

## Technical Implementation Details

### Amazon Titan Embedding v2

The application uses Amazon Titan Embedding v2, which offers several advantages:
- Support for different embedding dimensions (256, 512, or 1024)
- Improved performance for financial services use cases
- Support for more languages and longer text inputs
- Lower cost per token compared to v1

### DAX Integration

DynamoDB Accelerator (DAX) is used to provide microsecond response times:
- Automatically initialized when `DAX_ENDPOINT` environment variable is set
- Provides in-memory caching for frequently accessed embeddings
- Reduces latency for embedding retrieval operations
- Scales automatically with your application

### CloudWatch Log Analysis

The application includes sophisticated pattern matching for log analysis:
- Regular expressions for extracting trading information
- Pattern matching for compliance and security events
- Temporal analysis for detecting anomalous patterns
- Sentiment analysis for customer communications

### Embedding Metadata

All embeddings are stored with rich metadata:
- Model information (amazon.titan-embed-text-v2)
- Generation timestamp
- Performance metrics
- Source data characteristics
- Embedding dimensions and type

This metadata enables effective management and analysis of embeddings over time.
