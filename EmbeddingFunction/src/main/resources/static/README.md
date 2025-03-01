# Embedding Function UI

This is a web-based user interface for the Embedding Function, providing visualization, management, and integration capabilities for embeddings used in financial services applications.

## Features

- **Dashboard Overview**: View key metrics about embeddings and API usage
- **Embedding Visualization**: Interactive 2D/3D visualization of client embeddings
- **Embedding Management**: Create, view, update, and delete embeddings
- **Status Tracker Integration**: Configure integration with Status Tracker
- **Analytics Dashboard**: Track embedding usage and performance

## Getting Started

1. Start the Embedding Function UI server:
   ```bash
   java -cp target/user-embedding-service-1.0-SNAPSHOT.jar com.sample.WebServer
   ```

2. Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

## Technology Stack

- **Frontend**: HTML5, CSS3, JavaScript
- **Visualization**: Chart.js, D3.js
- **Backend**: Java with Jetty Server
- **API**: RESTful JSON API

## Integration with Status Tracker

The Embedding Function UI integrates with the Status Tracker application to provide:

1. Automated workflow triggers based on embedding analysis
2. Status updates with embedding-based insights
3. Step tracking with embedding-driven recommendations
4. Visual progress indicators for multi-step workflows

## Financial Services Use Cases

The UI supports specialized visualizations for financial services use cases:

- **Client Profile Analysis**: Visualize client segments based on financial attributes
- **Trading Pattern Detection**: Identify and visualize trading styles and anomalies
- **Compliance Risk Assessment**: Visualize compliance patterns and risk levels
- **Client Effort Analysis**: Visualize high-effort user journeys and friction points

## Development Roadmap

See the main README.md file for the complete UI development roadmap. 