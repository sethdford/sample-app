package com.sample.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.settings.Settings;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.functionscore.ScriptScoreQueryBuilder;
import org.opensearch.script.Script;
import org.opensearch.script.ScriptType;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;

import com.sample.examples.UserInterestMatchingExample.EmbeddingService;
import com.sample.examples.UserInterestMatchingExample.UserEmbedding;
import com.sample.examples.UserInterestMatchingExample.UserProfile;
import com.sample.examples.UserInterestMatchingExample.UserProfileMatch;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

/**
 * Service class for matching users with similar interests using OpenSearch k-NN
 * This version uses the OpenSearch High-Level REST Client
 */
public class SimpleOpenSearchUserMatchingService {
    
    private static final String DYNAMODB_TABLE_NAME = "UserEmbeddings";
    private static final String OPENSEARCH_INDEX_NAME = "user_embeddings";
    
    private final DynamoDbClient dynamoDb;
    private final EmbeddingService embeddingService;
    private final RestHighLevelClient openSearchClient;
    
    /**
     * Constructor for SimpleOpenSearchUserMatchingService
     * @param dynamoDb DynamoDB client
     * @param embeddingService Embedding service for generating embeddings
     * @param openSearchEndpoint OpenSearch endpoint URL
     * @param username OpenSearch username (if using authentication)
     * @param password OpenSearch password (if using authentication)
     */
    public SimpleOpenSearchUserMatchingService(DynamoDbClient dynamoDb, EmbeddingService embeddingService, 
            String openSearchEndpoint, String username, String password) {
        this.dynamoDb = dynamoDb;
        this.embeddingService = embeddingService;
        
        // Create OpenSearch client with authentication if provided
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            
            this.openSearchClient = new RestHighLevelClient(
                    RestClient.builder(HttpHost.create(openSearchEndpoint))
                            .setHttpClientConfigCallback(httpClientBuilder -> 
                                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)));
        } else {
            this.openSearchClient = new RestHighLevelClient(
                    RestClient.builder(HttpHost.create(openSearchEndpoint)));
        }
    }
    
    /**
     * Initialize the OpenSearch index with k-NN settings
     * @param dimensions Number of dimensions in the embedding vectors
     * @throws IOException If there's an error creating the index
     */
    public void initializeOpenSearchIndex(int dimensions) throws IOException {
        // Check if index already exists
        GetIndexRequest getIndexRequest = new GetIndexRequest(OPENSEARCH_INDEX_NAME);
        boolean indexExists = openSearchClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        
        if (indexExists) {
            System.out.println("Index already exists: " + OPENSEARCH_INDEX_NAME);
            return;
        }
        
        // Create index with k-NN settings
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(OPENSEARCH_INDEX_NAME);
        
        // Configure k-NN settings
        createIndexRequest.settings(Settings.builder()
                .put("index.knn", true)
                .put("index.knn.space_type", "cosinesimil") // Use cosine similarity
                .put("index.knn.algo_param.ef_search", 100) // Tune for better recall
                .put("index.knn.algo_param.ef_construction", 512) // Tune for better index quality
                .put("index.knn.algo_param.m", 16) // Tune for better performance
                .build());
        
        // Define mapping with k-NN field using a Map
        Map<String, Object> userIdField = new HashMap<>();
        userIdField.put("type", "keyword");
        
        Map<String, Object> embeddingTypeField = new HashMap<>();
        embeddingTypeField.put("type", "keyword");
        
        Map<String, Object> embeddingVectorField = new HashMap<>();
        embeddingVectorField.put("type", "knn_vector");
        embeddingVectorField.put("dimension", dimensions);
        
        Map<String, Object> nameField = new HashMap<>();
        nameField.put("type", "text");
        
        Map<String, Object> ageField = new HashMap<>();
        ageField.put("type", "integer");
        
        Map<String, Object> locationField = new HashMap<>();
        locationField.put("type", "text");
        
        Map<String, Object> interestsField = new HashMap<>();
        interestsField.put("type", "text");
        
        Map<String, Object> createdAtField = new HashMap<>();
        createdAtField.put("type", "date");
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("user_id", userIdField);
        properties.put("embedding_type", embeddingTypeField);
        properties.put("embedding_vector", embeddingVectorField);
        properties.put("name", nameField);
        properties.put("age", ageField);
        properties.put("location", locationField);
        properties.put("interests", interestsField);
        properties.put("created_at", createdAtField);
        
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        
        createIndexRequest.mapping(mapping);
        
        // Create the index
        openSearchClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        System.out.println("Created index with k-NN settings: " + OPENSEARCH_INDEX_NAME);
    }
    
    /**
     * Create a new user profile with embedding and store in both DynamoDB and OpenSearch
     */
    public UserProfile createUserProfile(String userId, String name, int age, String location, String interests) throws Exception {
        // Create a user profile
        UserProfile profile = new UserProfile(userId, name, age, location, interests);
        
        // Create a profile text for embedding generation
        String profileText = String.format(
                "Name: %s. Age: %d. Location: %s. Interests: %s",
                name, age, location, interests);
        
        // Generate embedding using Bedrock
        List<Double> embedding = embeddingService.generateEmbedding(profileText);
        
        // Store in DynamoDB
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("user_id", AttributeValue.builder().s(userId).build());
        item.put("embedding_type", AttributeValue.builder().s("profile").build());
        item.put("embedding", AttributeValue.builder().s(embedding.toString()).build());
        item.put("name", AttributeValue.builder().s(name).build());
        item.put("age", AttributeValue.builder().n(String.valueOf(age)).build());
        item.put("location", AttributeValue.builder().s(location).build());
        item.put("interests", AttributeValue.builder().s(interests).build());
        item.put("created_at", AttributeValue.builder().s(java.time.Instant.now().toString()).build());
        
        PutItemRequest request = PutItemRequest.builder()
                .tableName(DYNAMODB_TABLE_NAME)
                .item(item)
                .build();
        
        dynamoDb.putItem(request);
        
        // Store in OpenSearch
        Map<String, Object> document = new HashMap<>();
        document.put("user_id", userId);
        document.put("embedding_type", "profile");
        document.put("embedding_vector", embedding);
        document.put("name", name);
        document.put("age", age);
        document.put("location", location);
        document.put("interests", interests);
        document.put("created_at", java.time.Instant.now().toString());
        
        IndexRequest indexRequest = new IndexRequest(OPENSEARCH_INDEX_NAME)
                .id(userId)
                .source(document);
        
        openSearchClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println("Indexed document in OpenSearch: " + userId);
        
        return profile;
    }
    
    /**
     * Find users with similar interests using OpenSearch k-NN search
     */
    public List<UserProfileMatch> findSimilarUsers(String userId, int limit) throws Exception {
        // Get the user's embedding from DynamoDB
        UserEmbedding userEmbedding = getUserEmbedding(userId);
        if (userEmbedding == null) {
            throw new IllegalArgumentException("No embedding found for user: " + userId);
        }
        
        // Create k-NN query using script score
        SearchRequest searchRequest = new SearchRequest(OPENSEARCH_INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        
        // Use script score query for k-NN search
        Map<String, Object> params = new HashMap<>();
        params.put("query_vector", userEmbedding.getEmbedding());
        params.put("field", "embedding_vector");
        
        String scriptSource = 
                "double dotProduct = 0.0;" +
                "for (int i = 0; i < params.query_vector.length; i++) {" +
                "  dotProduct += params.query_vector[i] * doc[params.field][i];" +
                "}" +
                "double magnitude1 = 0.0;" +
                "for (int i = 0; i < params.query_vector.length; i++) {" +
                "  magnitude1 += params.query_vector[i] * params.query_vector[i];" +
                "}" +
                "magnitude1 = Math.sqrt(magnitude1);" +
                "double magnitude2 = 0.0;" +
                "for (int i = 0; i < doc[params.field].length; i++) {" +
                "  magnitude2 += doc[params.field][i] * doc[params.field][i];" +
                "}" +
                "magnitude2 = Math.sqrt(magnitude2);" +
                "double cosineSimilarity = dotProduct / (magnitude1 * magnitude2);" +
                "return cosineSimilarity;";
        
        Script script = new Script(ScriptType.INLINE, "painless", scriptSource, params);
        ScriptScoreQueryBuilder scriptScoreQueryBuilder = QueryBuilders.scriptScoreQuery(
                QueryBuilders.matchAllQuery(), script);
        
        searchSourceBuilder.query(scriptScoreQueryBuilder);
        searchSourceBuilder.size(limit + 1); // +1 because we'll filter out the user itself
        searchRequest.source(searchSourceBuilder);
        
        // Execute the search
        SearchResponse searchResponse = openSearchClient.search(searchRequest, RequestOptions.DEFAULT);
        
        // Process results
        List<UserProfileMatch> matches = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, Object> sourceMap = hit.getSourceAsMap();
            String hitUserId = (String) sourceMap.get("user_id");
            
            // Skip the user we're comparing against
            if (hitUserId.equals(userId)) {
                continue;
            }
            
            // Get user profile
            UserProfile profile = getUserProfile(hitUserId);
            if (profile != null) {
                // Get similarity score (cosine similarity)
                float score = hit.getScore();
                // Convert to percentage (0-100%)
                double similarityPercentage = score * 100;
                matches.add(new UserProfileMatch(profile, similarityPercentage));
            }
            
            // Limit results
            if (matches.size() >= limit) {
                break;
            }
        }
        
        return matches;
    }
    
    /**
     * Get a user profile from DynamoDB
     */
    public UserProfile getUserProfile(String userId) {
        GetItemResponse response = dynamoDb.getItem(GetItemRequest.builder()
                .tableName(DYNAMODB_TABLE_NAME)
                .key(Map.of(
                        "user_id", AttributeValue.builder().s(userId).build(),
                        "embedding_type", AttributeValue.builder().s("profile").build()))
                .build());
        
        if (!response.hasItem()) {
            return null;
        }
        
        Map<String, AttributeValue> item = response.item();
        
        String name = item.containsKey("name") ? item.get("name").s() : "Unknown";
        int age = item.containsKey("age") ? Integer.parseInt(item.get("age").n()) : 0;
        String location = item.containsKey("location") ? item.get("location").s() : "Unknown";
        String interests = item.containsKey("interests") ? item.get("interests").s() : "";
        
        return new UserProfile(userId, name, age, location, interests);
    }
    
    /**
     * Get a user's embedding from DynamoDB
     */
    private UserEmbedding getUserEmbedding(String userId) throws Exception {
        GetItemResponse response = dynamoDb.getItem(GetItemRequest.builder()
                .tableName(DYNAMODB_TABLE_NAME)
                .key(Map.of(
                        "user_id", AttributeValue.builder().s(userId).build(),
                        "embedding_type", AttributeValue.builder().s("profile").build()))
                .build());
        
        if (!response.hasItem() || !response.item().containsKey("embedding")) {
            return null;
        }
        
        Map<String, AttributeValue> item = response.item();
        String embeddingJson = item.get("embedding").s();
        List<Double> embedding = parseEmbeddingFromJson(embeddingJson);
        
        return new UserEmbedding(userId, "profile", embedding);
    }
    
    /**
     * Parse embedding from JSON string
     */
    private List<Double> parseEmbeddingFromJson(String embeddingJson) throws Exception {
        // Remove brackets and split by comma
        String cleaned = embeddingJson.replace("[", "").replace("]", "").trim();
        if (cleaned.isEmpty()) {
            return new ArrayList<>();
        }
        
        String[] values = cleaned.split(",");
        List<Double> embedding = new ArrayList<>();
        
        for (String value : values) {
            embedding.add(Double.parseDouble(value.trim()));
        }
        
        return embedding;
    }
    
    /**
     * Close the OpenSearch client
     */
    public void close() throws IOException {
        if (openSearchClient != null) {
            openSearchClient.close();
        }
    }
    
    /**
     * Delete the OpenSearch index
     */
    public void deleteIndex() throws IOException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(OPENSEARCH_INDEX_NAME);
        openSearchClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        System.out.println("Deleted index: " + OPENSEARCH_INDEX_NAME);
    }
} 