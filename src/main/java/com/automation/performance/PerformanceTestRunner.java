package com.automation.performance;

import com.automation.api.config.ApiConfig;
import com.automation.api.models.request.CreateUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Performance test runner that integrates JMeter with our API testing framework.
 * Provides easy-to-use methods for running performance tests on API endpoints.
 */
public class PerformanceTestRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceTestRunner.class);
    private final ApiConfig apiConfig;
    private final ObjectMapper objectMapper;
    private final Faker faker;
    
    public PerformanceTestRunner() {
        this.apiConfig = ApiConfig.getInstance();
        this.objectMapper = new ObjectMapper();
        this.faker = new Faker();
    }
    
    /**
     * Generate a random user for testing.
     */
    private CreateUserRequest generateRandomUser() {
        return CreateUserRequest.builder()
            .name(faker.name().fullName())
            .username(faker.name().username())
            .email(faker.internet().emailAddress())
            .phone(faker.phoneNumber().phoneNumber())
            .website(faker.internet().domainName())
            .build();
    }
    
    /**
     * Run a simple load test on GET endpoint.
     */
    public PerformanceResults runGetLoadTest(String endpoint, int users, int rampUpSeconds, int loops) {
        return runGetLoadTest(endpoint, users, rampUpSeconds, loops, null);
    }
    
    /**
     * Run a load test on GET endpoint with query parameters.
     */
    public PerformanceResults runGetLoadTest(String endpoint, int users, int rampUpSeconds, int loops,
                                           Map<String, String> queryParams) {
        logger.info("Starting GET load test for endpoint: {}", endpoint);
        
        try {
            JMeterTestEngine engine = new JMeterTestEngine();
            
            // Prepare endpoint with query parameters
            String fullEndpoint = endpoint;
            if (queryParams != null && !queryParams.isEmpty()) {
                StringBuilder queryString = new StringBuilder("?");
                for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                    queryString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                fullEndpoint = endpoint + queryString.toString().replaceAll("&$", "");
            }
            
            // Parse API base URL
            String baseUrl = apiConfig.getBaseUrl();
            String protocol = baseUrl.startsWith("https") ? "https" : "http";
            String serverName = baseUrl.replace("https://", "").replace("http://", "");
            int port = protocol.equals("https") ? 443 : 80;
            
            // Create headers
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");
            headers.put("User-Agent", "PerformanceTestRunner");
            
            // Execute test
            PerformanceResults results = engine
                .createTestPlan("GET Load Test - " + endpoint, users, rampUpSeconds, loops)
                .addHttpRequest("GET " + endpoint, protocol, serverName, port, fullEndpoint, "GET", headers, null)
                .addResultCollector("target/jmeter-get-results.jtl")
                .execute();
            
            logger.info("GET load test completed for endpoint: {}", endpoint);
            return results;
            
        } catch (Exception e) {
            logger.error("GET load test failed for endpoint: {}", endpoint, e);
            throw new RuntimeException("GET load test failed", e);
        }
    }
    
    /**
     * Run a load test on POST endpoint with JSON payload.
     */
    public PerformanceResults runPostLoadTest(String endpoint, Object requestBody, 
                                            int users, int rampUpSeconds, int loops) {
        logger.info("Starting POST load test for endpoint: {}", endpoint);
        
        try {
            JMeterTestEngine engine = new JMeterTestEngine();
            
            // Convert request body to JSON
            String jsonBody = null;
            if (requestBody != null) {
                jsonBody = objectMapper.writeValueAsString(requestBody);
            }
            
            // Parse API base URL
            String baseUrl = apiConfig.getBaseUrl();
            String protocol = baseUrl.startsWith("https") ? "https" : "http";
            String serverName = baseUrl.replace("https://", "").replace("http://", "");
            int port = protocol.equals("https") ? 443 : 80;
            
            // Create headers
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/json");
            headers.put("User-Agent", "PerformanceTestRunner");
            
            // Execute test
            PerformanceResults results = engine
                .createTestPlan("POST Load Test - " + endpoint, users, rampUpSeconds, loops)
                .addHttpRequest("POST " + endpoint, protocol, serverName, port, endpoint, "POST", headers, jsonBody)
                .addResultCollector("target/jmeter-post-results.jtl")
                .execute();
            
            logger.info("POST load test completed for endpoint: {}", endpoint);
            return results;
            
        } catch (Exception e) {
            logger.error("POST load test failed for endpoint: {}", endpoint, e);
            throw new RuntimeException("POST load test failed", e);
        }
    }
    
    /**
     * Run a comprehensive API stress test with multiple endpoints.
     */
    public PerformanceResults runApiStressTest(int users, int rampUpSeconds, int loops) {
        logger.info("Starting comprehensive API stress test");
        
        try {
            JMeterTestEngine engine = new JMeterTestEngine();
            
            // Parse API base URL
            String baseUrl = apiConfig.getBaseUrl();
            String protocol = baseUrl.startsWith("https") ? "https" : "http";
            String serverName = baseUrl.replace("https://", "").replace("http://", "");
            int port = protocol.equals("https") ? 443 : 80;
            
            // Create headers
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/json");
            headers.put("User-Agent", "PerformanceTestRunner");
            
            // Generate test user data
            CreateUserRequest testUser = generateRandomUser();
            String userJson = objectMapper.writeValueAsString(testUser);
            
            // Create comprehensive test plan
            engine.createTestPlan("API Stress Test", users, rampUpSeconds, loops)
                  // Test GET /users
                  .addHttpRequest("GET All Users", protocol, serverName, port, "/users", "GET", headers, null)
                  // Test GET /users/1
                  .addHttpRequest("GET User by ID", protocol, serverName, port, "/users/1", "GET", headers, null)
                  // Test POST /users
                  .addHttpRequest("POST Create User", protocol, serverName, port, "/users", "POST", headers, userJson)
                  // Test PUT /users/1
                  .addHttpRequest("PUT Update User", protocol, serverName, port, "/users/1", "PUT", headers, userJson)
                  .addResultCollector("target/jmeter-stress-results.jtl");
            
            PerformanceResults results = engine.execute();
            
            logger.info("API stress test completed");
            return results;
            
        } catch (Exception e) {
            logger.error("API stress test failed", e);
            throw new RuntimeException("API stress test failed", e);
        }
    }
    
    /**
     * Run a spike test to check system behavior under sudden load increases.
     */
    public PerformanceResults runSpikeTest(String endpoint, int normalUsers, int spikeUsers, int spikeDuration) {
        logger.info("Starting spike test for endpoint: {}", endpoint);
        
        try {
            // First run normal load
            logger.info("Running normal load with {} users", normalUsers);
            PerformanceResults normalResults = runGetLoadTest(endpoint, normalUsers, 30, 5);
            
            // Then run spike load
            logger.info("Running spike load with {} users for {} loops", spikeUsers, spikeDuration);
            PerformanceResults spikeResults = runGetLoadTest(endpoint, spikeUsers, 5, spikeDuration);
            
            // Combine results (simplified - in real scenario you'd want more sophisticated merging)
            PerformanceResults combinedResults = new PerformanceResults();
            combinedResults.setTestPlanName("Spike Test - " + endpoint);
            combinedResults.setTotalExecutionTime(normalResults.getTotalExecutionTime() + spikeResults.getTotalExecutionTime());
            combinedResults.setThreadCount(Math.max(normalResults.getThreadCount(), spikeResults.getThreadCount()));
            
            logger.info("Spike test completed for endpoint: {}", endpoint);
            return combinedResults;
            
        } catch (Exception e) {
            logger.error("Spike test failed for endpoint: {}", endpoint, e);
            throw new RuntimeException("Spike test failed", e);
        }
    }
    
    /**
     * Run endurance test to check system stability over time.
     */
    public PerformanceResults runEnduranceTest(String endpoint, int users, int durationMinutes) {
        logger.info("Starting endurance test for endpoint: {} - Duration: {} minutes", endpoint, durationMinutes);
        
        try {
            // Calculate loops based on duration (approximate)
            int loops = durationMinutes * 10; // Assume each loop takes ~6 seconds
            
            PerformanceResults results = runGetLoadTest(endpoint, users, 60, loops);
            results.setTestPlanName("Endurance Test - " + endpoint);
            
            logger.info("Endurance test completed for endpoint: {}", endpoint);
            return results;
            
        } catch (Exception e) {
            logger.error("Endurance test failed for endpoint: {}", endpoint, e);
            throw new RuntimeException("Endurance test failed", e);
        }
    }
    
    /**
     * Validate performance results against SLA criteria.
     */
    public boolean validatePerformanceSLA(PerformanceResults results, double maxErrorRate, 
                                        double maxResponseTime, double minThroughput) {
        logger.info("Validating performance results against SLA");
        
        boolean errorRateOk = results.isTestPassed(maxErrorRate);
        boolean responseTimeOk = results.isResponseTimeAcceptable(maxResponseTime);
        boolean throughputOk = results.isThroughputAcceptable(minThroughput);
        
        boolean overallPassed = errorRateOk && responseTimeOk && throughputOk;
        
        logger.info("SLA Validation Results:");
        logger.info("  Error Rate: {} (max: {}) - {}", 
                   String.format("%.2f%%", results.getErrorPercentage()), 
                   String.format("%.2f%%", maxErrorRate),
                   errorRateOk ? "PASS" : "FAIL");
        logger.info("  Response Time: {} ms (max: {} ms) - {}", 
                   String.format("%.2f", results.getAverageResponseTime()), 
                   String.format("%.2f", maxResponseTime),
                   responseTimeOk ? "PASS" : "FAIL");
        logger.info("  Throughput: {} req/sec (min: {} req/sec) - {}", 
                   String.format("%.2f", results.getThroughputPerSecond()), 
                   String.format("%.2f", minThroughput),
                   throughputOk ? "PASS" : "FAIL");
        logger.info("  Overall SLA: {}", overallPassed ? "PASS" : "FAIL");
        
        return overallPassed;
    }
} 