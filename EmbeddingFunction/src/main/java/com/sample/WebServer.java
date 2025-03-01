package com.sample;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Simple web server for the Embedding Function UI
 */
public class WebServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        Server server = new Server(PORT);

        // Resource handler for static files
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        resourceHandler.setResourceBase(WebServer.class.getClassLoader().getResource("static").toExternalForm());

        // Servlet handler for API endpoints
        ServletContextHandler servletHandler = new ServletContextHandler();
        servletHandler.setContextPath("/api");
        
        // Add API endpoints
        servletHandler.addServlet(new ServletHolder(new EmbeddingStatsServlet()), "/stats");
        servletHandler.addServlet(new ServletHolder(new EmbeddingTypesServlet()), "/types");
        servletHandler.addServlet(new ServletHolder(new EmbeddingListServlet()), "/embeddings");
        servletHandler.addServlet(new ServletHolder(new StatusTrackerIntegrationServlet()), "/status-tracker");

        // Combine handlers
        HandlerList handlers = new HandlerList();
        handlers.addHandler(resourceHandler);
        handlers.addHandler(servletHandler);
        handlers.addHandler(new DefaultHandler());
        
        server.setHandler(handlers);

        // Start the server
        server.start();
        System.out.println("Embedding Function UI server started on port " + PORT);
        System.out.println("Open http://localhost:" + PORT + " in your browser");
        
        server.join();
    }

    /**
     * Servlet for embedding statistics
     */
    static class EmbeddingStatsServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalEmbeddings", 12458);
            stats.put("apiRequests", 845632);
            stats.put("averageResponseTime", 42);
            stats.put("successRate", 99.8);
            
            resp.setContentType("application/json");
            resp.getWriter().write(toJson(stats));
        }
    }

    /**
     * Servlet for embedding types distribution
     */
    static class EmbeddingTypesServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            Map<String, Object> types = new HashMap<>();
            types.put("Financial Profile", 35);
            types.put("Trading Pattern", 25);
            types.put("Compliance Pattern", 20);
            types.put("Client Effort", 15);
            types.put("Other", 5);
            
            resp.setContentType("application/json");
            resp.getWriter().write(toJson(types));
        }
    }

    /**
     * Servlet for embedding list
     */
    static class EmbeddingListServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            // In a real application, this would fetch embeddings from a database
            // and support pagination, filtering, etc.
            Map<String, Object> response = new HashMap<>();
            response.put("total", 5);
            response.put("page", 1);
            response.put("pageSize", 10);
            
            Map<String, Object>[] embeddings = new Map[5];
            
            embeddings[0] = createEmbedding("emb-001", "John Smith Profile", "Financial Profile", "2025-01-15", "2025-02-20", "active");
            embeddings[1] = createEmbedding("emb-002", "Sarah Johnson Profile", "Financial Profile", "2025-01-16", "2025-02-21", "active");
            embeddings[2] = createEmbedding("emb-003", "High-Frequency Trading Pattern", "Trading Pattern", "2025-01-20", "2025-02-22", "active");
            embeddings[3] = createEmbedding("emb-004", "KYC Compliance Pattern", "Compliance Pattern", "2025-01-25", "2025-02-23", "archived");
            embeddings[4] = createEmbedding("emb-005", "Mobile App Client Effort", "Client Effort", "2025-02-01", "2025-02-24", "draft");
            
            response.put("embeddings", embeddings);
            
            resp.setContentType("application/json");
            resp.getWriter().write(toJson(response));
        }
        
        private Map<String, Object> createEmbedding(String id, String name, String type, String created, String updated, String status) {
            Map<String, Object> embedding = new HashMap<>();
            embedding.put("id", id);
            embedding.put("name", name);
            embedding.put("type", type);
            embedding.put("created", created);
            embedding.put("updated", updated);
            embedding.put("status", status);
            return embedding;
        }
    }

    /**
     * Servlet for Status Tracker integration
     */
    static class StatusTrackerIntegrationServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            Map<String, Object> integration = new HashMap<>();
            integration.put("connected", true);
            integration.put("lastSync", "2025-03-01 14:32:45");
            integration.put("syncStatus", "Success");
            integration.put("apiVersion", "v2.3.1");
            
            Map<String, Object>[] workflows = new Map[3];
            
            workflows[0] = createWorkflow("Client Onboarding", "active", 
                    "Automatically generates client embeddings during onboarding process and updates Status Tracker.", 
                    245, 98.8);
            
            workflows[1] = createWorkflow("Trading Pattern Analysis", "active", 
                    "Analyzes trading patterns weekly and updates client risk profiles in Status Tracker.", 
                    52, 100.0);
            
            workflows[2] = createWorkflow("Compliance Monitoring", "active", 
                    "Daily compliance check with embedding-based anomaly detection, updates Status Tracker with alerts.", 
                    365, 99.7);
            
            integration.put("workflows", workflows);
            
            resp.setContentType("application/json");
            resp.getWriter().write(toJson(integration));
        }
        
        private Map<String, Object> createWorkflow(String name, String status, String description, int executions, double successRate) {
            Map<String, Object> workflow = new HashMap<>();
            workflow.put("name", name);
            workflow.put("status", status);
            workflow.put("description", description);
            workflow.put("executions", executions);
            workflow.put("successRate", successRate);
            return workflow;
        }
    }

    /**
     * Simple JSON serialization
     */
    private static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            
            sb.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Number) {
                sb.append(value);
            } else if (value instanceof Boolean) {
                sb.append(value);
            } else if (value instanceof Map[]) {
                sb.append("[");
                Map[] maps = (Map[]) value;
                for (int i = 0; i < maps.length; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(toJson(maps[i]));
                }
                sb.append("]");
            } else if (value instanceof Map) {
                sb.append(toJson((Map<String, Object>) value));
            } else if (value == null) {
                sb.append("null");
            }
        }
        
        sb.append("}");
        return sb.toString();
    }
} 