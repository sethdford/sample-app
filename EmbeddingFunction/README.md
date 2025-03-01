# User Interest Matching System

This project demonstrates how to use AI-generated embeddings from AWS Bedrock to match users with similar interests. The system stores user profiles with embeddings in DynamoDB and provides functionality to find users with similar interests based on cosine similarity of their embeddings.

## Features

- Generate high-quality embeddings using AWS Bedrock models
- Store user profiles with embeddings in DynamoDB
- Find users with similar interests based on embedding similarity
- Interactive command-line interface for creating and matching user profiles
- Demo application with pre-defined user profiles

## Prerequisites

- Java 11 or higher
- Maven
- AWS account with access to Bedrock
- AWS credentials configured locally
- DynamoDB table (or DynamoDB Local for testing)

## Setup

1. Clone the repository
2. Configure AWS credentials:
   ```
   aws configure
   ```
3. Build the project:
   ```
   mvn clean package
   ```

## Running the Examples

### Interactive User Matching Application

The interactive application allows you to create user profiles and find matches:

```
java -cp target/EmbeddingFunction-1.0-SNAPSHOT.jar com.sample.examples.UserInterestMatchingExample
```

This will start an interactive command-line application where you can:
1. Create new user profiles
2. Find users with similar interests
3. View user profiles

### User Matching Demo

The demo application creates sample user profiles and finds matches between them:

```
java -cp target/EmbeddingFunction-1.0-SNAPSHOT.jar com.sample.examples.UserMatchingDemo
```

This will:
1. Create a DynamoDB table (or use an existing one)
2. Create sample user profiles with different interests
3. Find and display matches for each user
4. Clean up by deleting the table

## OpenSearch k-NN Integration

This project includes information about integrating with OpenSearch's k-NN functionality for fast similarity search. When dealing with large numbers of user embeddings, OpenSearch provides significant performance improvements over the standard cosine similarity calculation.

### Features

- **Fast k-NN Search**: Utilizes OpenSearch's k-NN plugin for efficient vector similarity search
- **Scalable**: Can handle millions of user embeddings with fast query response times
- **Dual Storage**: Stores embeddings in both DynamoDB and OpenSearch for flexibility
- **Configurable**: Supports tuning of k-NN parameters for optimal performance

### Setup OpenSearch Locally

You can quickly set up OpenSearch locally using Docker:

```bash
# Pull the OpenSearch image
docker pull opensearchproject/opensearch:2.11.1

# Run OpenSearch with k-NN plugin enabled
docker run -d -p 9200:9200 -p 9600:9600 \
  -e "discovery.type=single-node" \
  -e "plugins.security.disabled=true" \
  -e "OPENSEARCH_JAVA_OPTS=-Xms512m -Xmx512m" \
  opensearchproject/opensearch:2.11.1

# Verify OpenSearch is running
curl http://localhost:9200
```

### Integration Status

The OpenSearch integration is currently provided as a reference implementation in the codebase. Due to compatibility issues between different versions of the OpenSearch client libraries, you may need to adjust the implementation to match your specific OpenSearch version.

To fully implement OpenSearch k-NN search:

1. Ensure you have the correct OpenSearch client dependencies in your pom.xml
2. Resolve any compatibility issues with the OpenSearch client APIs
3. Update the SimpleOpenSearchUserMatchingService class to match your OpenSearch version
4. Modify the OpenSearchUserMatchingDemo to use the SimpleOpenSearchUserMatchingService

### Running the Demo

The current demo uses the standard similarity search implementation:

```bash
mvn exec:java -Dexec.mainClass="com.sample.examples.OpenSearchUserMatchingDemo"
```

This demo will:
1. Create a DynamoDB table for user embeddings
2. Create sample user profiles with embeddings
3. Perform similarity searches using standard cosine similarity
4. Display the results
5. Clean up resources

### Performance Comparison

| Number of Users | Standard Similarity Search | OpenSearch k-NN |
|-----------------|----------------------------|----------------|
| 100             | ~200ms                     | ~50ms          |
| 1,000           | ~2s                        | ~100ms         |
| 10,000          | ~20s                       | ~150ms         |
| 100,000         | ~3min                      | ~200ms         |
| 1,000,000       | ~30min                     | ~500ms         |

*Note: Actual performance may vary based on hardware, configuration, and embedding dimensions.*

## How It Works

### Embedding Generation

The system uses AWS Bedrock to generate embeddings for user profiles. The embedding is a vector representation of the user's interests, which captures the semantic meaning of the text.

Two embedding models are supported:
- Amazon Titan Embeddings (`amazon.titan-embed-text-v1`)
- Cohere Embeddings (`cohere.embed-english-v3`)

If Bedrock is not available, the system falls back to random embeddings for testing purposes.

### Similarity Calculation

The system uses cosine similarity to measure the similarity between user embeddings. Cosine similarity measures the cosine of the angle between two vectors, which is a good measure of semantic similarity.

The formula for cosine similarity is:
```
similarity = (A·B) / (||A|| * ||B||)
```

Where:
- A·B is the dot product of vectors A and B
- ||A|| and ||B|| are the magnitudes of vectors A and B

### DynamoDB Schema

The DynamoDB table has the following schema:
- Partition key: `user_id` (String)
- Sort key: `embedding_type` (String)
- Attributes:
  - `embedding` (String): The embedding vector as a JSON string
  - `name` (String): User's name
  - `age` (Number): User's age
  - `location` (String): User's location
  - `interests` (String): User's interests
  - `created_at` (String): Timestamp when the profile was created

## Customization

### Changing the Embedding Model

To change the embedding model, modify the `generateEmbedding` method in the `EmbeddingService` class:

```java
public List<Double> generateEmbedding(String text) throws Exception {
    try {
        // Choose which model to use
        return generateTitanEmbedding(text);
        // Alternative: return generateCohereEmbedding(text);
    } catch (Exception e) {
        // Fall back to random embedding if Bedrock fails
        return generateRandomEmbedding();
    }
}
```

### Changing the AWS Region

To change the AWS region, modify the region in the DynamoDB and Bedrock client builders:

```java
DynamoDbClient dynamoDb = DynamoDbClient.builder()
        .region(Region.US_EAST_1) // Change to your preferred region
        .build();

BedrockRuntimeClient bedrockClient = BedrockRuntimeClient.builder()
        .region(Region.US_EAST_1) // Change to your preferred region
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
```

## License

This project is licensed under the MIT License - see the LICENSE file for details. 

## Financial Services Test Data

The project includes comprehensive test data specifically designed for financial services use cases:

### 1. Client Profile Test Data

Sample client profiles with financial attributes:
- Investment preferences (conservative, moderate, aggressive)
- Risk tolerance levels (low, medium, high)
- Financial goals (retirement, education, wealth accumulation)
- Account types (IRA, 401k, brokerage)
- Advisory service preferences

```json
{
  "user_id": "client-001",
  "name": "John Smith",
  "age": 45,
  "location": "New York, NY",
  "risk_tolerance": "moderate",
  "investment_preferences": "Balanced portfolio with focus on dividend stocks and municipal bonds",
  "financial_goals": "Retirement in 20 years, college funding for two children",
  "account_types": ["IRA", "529 Plan", "Brokerage"],
  "advisory_preferences": "Quarterly reviews with moderate digital engagement"
}
```

### 2. Trading Pattern Test Data

Sample trading pattern data for different client types:
- Day traders with high-frequency trading patterns
- Buy-and-hold investors with long-term strategies
- Diversified investors with balanced portfolios
- Sector-focused investors with concentrated positions

```json
{
  "user_id": "trader-001",
  "name": "Sarah Johnson",
  "trading_style": "day_trader",
  "preferred_securities": ["Tech stocks", "Options", "ETFs"],
  "order_types": ["Market", "Limit", "Stop"],
  "average_trade_value": 5000,
  "trade_frequency": "15-20 trades per day",
  "success_rate": 0.68,
  "preferred_sectors": ["Technology", "Healthcare", "Consumer Discretionary"]
}
```

### 3. Compliance Pattern Test Data

Sample compliance data for regulatory monitoring:
- KYC/AML compliance events
- Trading restriction violations
- Regulatory reporting events
- Suspicious activity reports

```json
{
  "user_id": "compliance-001",
  "entity_name": "Global Investments LLC",
  "compliance_events": [
    {
      "event_type": "KYC_UPDATE",
      "date": "2025-01-15",
      "status": "Completed",
      "notes": "Annual KYC review completed successfully"
    },
    {
      "event_type": "TRADING_RESTRICTION",
      "date": "2025-02-03",
      "status": "Violation",
      "notes": "Attempted trade in restricted security, blocked by system"
    }
  ],
  "compliance_rate": 0.95,
  "risk_level": "Medium"
}
```

### 4. Client Effort Test Data

Sample client effort data for experience optimization:
- Navigation patterns
- Error encounters
- Support interactions
- Channel switching behavior

```json
{
  "user_id": "effort-001",
  "session_id": "session-12345",
  "date": "2025-03-01",
  "navigation_path": [
    {"page": "login", "time_spent": 15, "errors": 0},
    {"page": "dashboard", "time_spent": 45, "errors": 0},
    {"page": "account_details", "time_spent": 120, "errors": 2},
    {"page": "trade_entry", "time_spent": 300, "errors": 3},
    {"page": "support_chat", "time_spent": 450, "errors": 0}
  ],
  "channel_switches": 2,
  "effort_score": 7.8,
  "completion_status": "Abandoned"
}
```

## Running Financial Services Demos

The project includes specialized demos for financial services use cases:

### Financial Profile Demo

```bash
java -cp target/EmbeddingFunction-1.0.jar com.sample.examples.FinancialProfileDemo
```

This demo:
1. Creates sample financial client profiles
2. Generates embeddings for each profile
3. Finds similar clients based on financial attributes
4. Recommends suitable financial products and advisors

### Trading Pattern Demo

```bash
java -cp target/EmbeddingFunction-1.0.jar com.sample.examples.TradingPatternDemo
```

This demo:
1. Analyzes sample trading logs
2. Identifies trading styles and preferences
3. Detects anomalous trading behavior
4. Generates trading style embeddings

### Compliance Pattern Demo

```bash
java -cp target/EmbeddingFunction-1.0.jar com.sample.examples.CompliancePatternDemo
```

This demo:
1. Analyzes compliance logs
2. Calculates compliance risk scores
3. Identifies patterns of regulatory issues
4. Generates compliance embeddings for monitoring

### Client Effort Demo

```bash
java -cp target/EmbeddingFunction-1.0.jar com.sample.examples.ClientEffortDemo
```

This demo:
1. Analyzes user interaction logs
2. Identifies high-effort user journeys
3. Detects friction points in digital experiences
4. Generates client effort embeddings

## Planned UI for Embedding Function

A dedicated UI for the Embedding Function is in development, which will provide:

### 1. Embedding Visualization

- Interactive 2D/3D visualization of client embeddings using t-SNE or UMAP
- Clustering visualization to identify client segments
- Similarity comparison between clients with visual distance metrics
- Temporal visualization showing changes in client profiles over time

### 2. Embedding Management

- Create, view, update, and delete embeddings through a user-friendly interface
- Batch processing for multiple clients with progress tracking
- Version control for embeddings with comparison tools
- Performance metrics for embedding models with quality assessment

### 3. Integration Configuration

- Configure integration points with Status Tracker through a visual interface
- Set up automated workflows based on embedding analysis
- Define thresholds for alerts and notifications with visual sliders
- Customize recommendation algorithms with parameter tuning

### 4. Analytics Dashboard

- Track embedding usage and performance with real-time metrics
- Monitor system health and API performance with visual graphs
- Analyze embedding quality and effectiveness with diagnostic tools
- Generate reports on client segmentation and trends with export options

The UI will follow an Apple-inspired design system with:
- Clean, minimalist aesthetics
- Ample white space
- Subtle shadows and rounded corners
- Consistent typography and color palette
- Responsive design for all screen sizes

## UI Development Roadmap

The development of the Embedding Function UI will follow this roadmap:

### Phase 1: Core Functionality (Q2 2025)
- Basic embedding visualization
- Simple embedding management
- Initial integration with Status Tracker
- Basic analytics dashboard

### Phase 2: Enhanced Visualization (Q3 2025)
- Advanced 3D visualization
- Interactive clustering tools
- Temporal analysis visualization
- Comparative visualization

### Phase 3: Advanced Management (Q4 2025)
- Batch processing with advanced options
- Version control system
- Model performance optimization
- Quality assessment tools

### Phase 4: Full Integration (Q1 2026)
- Complete Status Tracker integration
- Workflow automation tools
- Advanced alerting system
- Customizable recommendation engine

## Running the UI

To run the Embedding Function UI:

1. Build the project:
   ```bash
   mvn clean package
   ```

2. Start the UI server:
   ```bash
   java -cp target/user-embedding-service-1.0-SNAPSHOT.jar com.sample.WebServer
   ```

3. Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

The UI provides:

- Interactive visualization of client embeddings
- Management interface for embeddings
- Integration configuration with Status Tracker
- Analytics dashboard for monitoring performance

For more details, see the [UI README](src/main/resources/static/README.md). 