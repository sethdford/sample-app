# User Embedding Application

This project contains source code and supporting files for a serverless application that uses AWS Bedrock to generate embeddings for user profiles and find similar users based on interests. You can deploy it with the SAM CLI.

## Project Structure

- `EmbeddingFunction/src/main` - Code for the application's Lambda function that handles embedding generation and user matching.
- `events` - Invocation events that you can use to invoke the function.
- `EmbeddingFunction/src/test` - Unit tests for the application code.
- `template.yaml` - A template that defines the application's AWS resources.

## Architecture

The application uses several AWS resources:
- **Lambda functions** for serverless compute
- **API Gateway** for RESTful API endpoints
- **DynamoDB** for storing user profiles and embeddings
- **DAX** for caching embeddings and improving performance
- **AWS Bedrock** for generating high-quality embeddings
- **CloudWatch** for monitoring and telemetry

These resources are defined in the `template.yaml` file. You can update the template to add AWS resources through the same deployment process that updates your application code.

## Features

- Generate embeddings for user profiles using AWS Bedrock
- Store user profiles and embeddings in DynamoDB
- Find users with similar interests based on embedding similarity
- Comprehensive telemetry dashboard for monitoring performance
- Optional OpenSearch integration for faster similarity search with large datasets
- User attribute formatting for AWS Titan embedding models
- Multiple embedding approaches for different types of user data

## Embedding Approaches

The application supports multiple approaches for generating embeddings from different types of user data:

### 1. Raw Text Embedding

The most basic approach, which takes raw text input and generates embeddings directly using AWS Bedrock's Titan model.

```java
EmbeddingService embeddingService = new EmbeddingService();
double[] embedding = embeddingService.generateEmbedding("User's raw text description");
```

### 2. User Attributes Embedding

Converts structured user attributes (like login time, page views, subscription plan) into a format suitable for embedding generation.

```java
UserAttributeFormatter formatter = new UserAttributeFormatter();
Map<String, Object> encodedAttributes = formatter.encodeAttributes(userAttributes);
String titanPrompt = formatter.createTitanPrompt(encodedAttributes);
double[] embedding = embeddingService.generateEmbedding(titanPrompt);
```

### 3. Behavioral Embedding

Processes user activity logs, clickstream data, and other behavioral signals to create embeddings that represent user behavior patterns.

```java
BehavioralEmbeddingService behavioralService = new BehavioralEmbeddingService();
double[] embedding = behavioralService.generateBehavioralEmbedding(userId, behavioralData);
```

### 4. Interest Embedding

Processes user interests and preferences to create embeddings for matching and recommendation purposes.

```java
InterestEmbeddingService interestService = new InterestEmbeddingService();
double[] embedding = interestService.generateInterestEmbedding(userId, interests, preferences);
```

### 5. Financial Profile Embedding

Specialized for financial institutions like ThetaCorp and BetaCorp, this approach processes financial interests, life events, and wellness concerns to create embeddings that represent a client's financial profile.

```java
FinancialInterestEmbeddingService financialService = new FinancialInterestEmbeddingService();
double[] embedding = financialService.generateFinancialEmbedding(
    clientId, 
    financialInterests, 
    lifeEvents, 
    wellnessConcerns, 
    riskTolerance, 
    timeHorizon
);
```

This approach is particularly useful for:
- Matching clients with appropriate financial advisors
- Recommending relevant financial products
- Identifying clients with similar financial profiles
- Personalizing financial wellness content
- Targeting communications around life events (retirement, college planning, etc.)

### Enhanced DynamoDB Storage

All embedding types are stored in DynamoDB with metadata for easy retrieval and comparison:

```java
EnhancedDynamoDBService dynamoDBService = new EnhancedDynamoDBService();
dynamoDBService.storeEmbeddingWithMetadata(userId, embeddingType, embedding, metadata);
```

### Factory Pattern

The application uses a factory pattern to create the appropriate embedding service based on the data type:

```java
EmbeddingServiceFactory factory = EmbeddingServiceFactory.getInstance();
Object service = factory.getServiceForDataType("financial_profile");
```

### 6. Client Effort Embedding

This approach analyzes user behavior patterns from logs to identify high client effort scenarios such as repeated errors, excessive navigation, button clicking, and channel switching. It helps identify friction points in user journeys.

```java
// Generate client effort embedding
CloudWatchLogEmbeddingService service = new CloudWatchLogEmbeddingService();
double[] embedding = service.generateClientEffortEmbedding("user123", "ApplicationLogs", 24);

// Analyze client effort patterns
Map<String, Object> effortAnalysis = service.analyzeClientEffort(logs);
System.out.println("Effort Score: " + effortAnalysis.get("effort_score"));
System.out.println("High Effort: " + effortAnalysis.get("high_effort"));
```

## User Attribute Formatting

The application includes a `UserAttributeFormatter` utility that converts raw user attributes into a format compatible with AWS Titan embedding models. This process involves:

1. **Encoding raw attributes** - Converting timestamps, categorical variables, and numerical values into normalized formats
2. **Creating Titan-compatible text prompts** - Transforming encoded attributes into natural language text that Titan can process

### Example

Raw user attributes:
```json
{
  "user_id": "user123",
  "last_login": "2025-02-24T12:30:00Z",
  "errors_encountered": ["500", "403", "Timeout"],
  "page_views": 10,
  "subscription_plan": "Premium",
  "device": "Mobile"
}
```

Encoded attributes:
```json
{
  "last_login_days": 5,
  "errors_encountered_onehot": [1, 1, 1, 0, 0, 0],
  "page_views_scaled": 0.1,
  "subscription_plan_label": 2,
  "device_onehot": [1, 0, 0]
}
```

Titan-compatible text prompt:
```
User profile: Last active 5 days ago. Encountered errors: 500, 403, Timeout. Viewed 10 pages. Has Premium subscription. Uses Mobile device.
```

### API Endpoints

The application provides the following API endpoints:

- `/generateEmbedding` - Generate an embedding from raw text
- `/getEmbedding` - Retrieve a stored embedding
- `/generateEmbeddingFromAttributes` - Generate an embedding from user attributes

## Demo Applications

The project includes several demo applications to showcase different embedding approaches:

1. **UserAttributeFormatterDemo** - Demonstrates how to format user attributes for embedding generation
2. **BehavioralEmbeddingDemo** - Shows how to generate embeddings from user behavioral data
3. **InterestEmbeddingDemo** - Illustrates embedding generation from user interests and preferences
4. **FinancialProfileDemo** - Showcases financial profile embeddings for clients of financial institutions
5. **AllEmbeddingTypesDemo** - Comprehensive demo that showcases all embedding types and compares them
6. **CloudWatchLogEmbeddingDemo** - Shows how to generate embeddings from CloudWatch logs
7. **ClientEffortDemo** - Demonstrates how to detect high client effort patterns and generate recommendations

To run a demo, use the following command:

```bash
java -cp target/EmbeddingFunction-1.0.jar com.sample.examples.FinancialProfileDemo
```

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

The SAM CLI can also emulate your application's API:

```bash
sam local start-api
curl http://localhost:3000/generateEmbedding
```

## Monitoring and Telemetry

The application includes a CloudWatch dashboard for monitoring performance metrics:

- Embedding generation time
- Query performance
- Lambda function metrics (invocations, errors, duration)
- DynamoDB latency
- DAX cache performance

You can access the dashboard from the CloudWatch console or via the URL provided in the stack outputs.

## Cleanup

To delete the application that you created, use the AWS CLI:

```bash
sam delete --stack-name <your-stack-name>
```

## Resources

See the [AWS SAM developer guide](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/what-is-sam.html) for an introduction to SAM specification, the SAM CLI, and serverless application concepts.

For more information about AWS Bedrock, see the [AWS Bedrock documentation](https://docs.aws.amazon.com/bedrock/latest/userguide/what-is-bedrock.html).
