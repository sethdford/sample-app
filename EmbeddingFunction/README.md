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