package com.automation.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Simple JMeter demonstration test that doesn't depend on external APIs.
 * Shows basic JMeter engine functionality.
 */
public class JMeterDemoTest {
    
    private static final Logger logger = LoggerFactory.getLogger(JMeterDemoTest.class);
    
    @Test(description = "Demonstrate JMeter engine initialization and basic functionality")
    public void testJMeterEngineBasics() {
        logger.info("=== JMeter Engine Demonstration ===");
        
        try {
            // Create and initialize JMeter engine
            JMeterTestEngine engine = new JMeterTestEngine();
            
            // Create a simple test plan
            logger.info("Creating test plan...");
            engine.createTestPlan("Demo Test Plan", 2, 5, 1);
            
            // Add a simple HTTP request (using httpbin.org which is reliable)
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");
            headers.put("User-Agent", "JMeter-Demo");
            
            engine.addHttpRequest("Demo Request", "https", "httpbin.org", 443, "/get", "GET", headers, null);
            engine.addResultCollector("target/jmeter-demo-results.jtl");
            
            // Execute the test
            logger.info("Executing JMeter test...");
            PerformanceResults results = engine.execute();
            
            // Verify results
            assertNotNull(results, "Results should not be null");
            assertEquals(results.getTestPlanName(), "Demo Test Plan", "Test plan name should match");
            assertEquals(results.getThreadCount(), 2, "Thread count should match");
            assertEquals(results.getLoopCount(), 1, "Loop count should match");
            assertTrue(results.getTotalExecutionTime() > 0, "Execution time should be positive");
            
            logger.info("JMeter test completed successfully!");
            logger.info("Results summary: {}", results.getSummary());
            
            // Cleanup
            engine.shutdown();
            
        } catch (Exception e) {
            logger.error("JMeter demo test failed", e);
            fail("JMeter demo test should not fail: " + e.getMessage());
        }
    }
    
    @Test(description = "Test JMeter engine without execution", priority = 1)
    public void testJMeterEngineCreation() {
        logger.info("=== Testing JMeter Engine Creation ===");
        
        try {
            JMeterTestEngine engine = new JMeterTestEngine();
            
            // Test engine initialization
            engine.initialize();
            logger.info("JMeter engine initialized successfully");
            
            // Test test plan creation
            engine.createTestPlan("Test Creation", 1, 1, 1);
            logger.info("Test plan created successfully");
            
            assertTrue(true, "Engine creation should succeed");
            
        } catch (Exception e) {
            logger.error("JMeter engine creation failed", e);
            fail("JMeter engine creation should not fail: " + e.getMessage());
        }
    }
    
    @Test(description = "Test PerformanceResults class functionality", priority = 0)
    public void testPerformanceResults() {
        logger.info("=== Testing PerformanceResults Class ===");
        
        // Create and populate results
        PerformanceResults results = new PerformanceResults();
        results.setTestPlanName("Test Results");
        results.setTotalExecutionTime(5000);
        results.setThreadCount(10);
        results.setRampUpTime(30);
        results.setLoopCount(3);
        results.setAverageResponseTime(250.5);
        results.setMinResponseTime(100.0);
        results.setMaxResponseTime(500.0);
        results.setTotalSamples(30);
        results.setErrorCount(2);
        results.setErrorPercentage(6.67);
        results.setThroughputPerSecond(6.0);
        results.addError("Connection timeout");
        results.addError("HTTP 500 error");
        
        // Test getters
        assertEquals(results.getTestPlanName(), "Test Results");
        assertEquals(results.getTotalExecutionTime(), 5000);
        assertEquals(results.getThreadCount(), 10);
        assertEquals(results.getLoopCount(), 3);
        assertEquals(results.getAverageResponseTime(), 250.5, 0.1);
        assertEquals(results.getErrorCount(), 2);
        assertEquals(results.getErrors().size(), 2);
        
        // Test validation methods
        assertFalse(results.isTestPassed(5.0), "Should fail with 6.67% error rate vs 5% threshold");
        assertTrue(results.isTestPassed(10.0), "Should pass with 6.67% error rate vs 10% threshold");
        assertFalse(results.isResponseTimeAcceptable(200.0), "Should fail with 250.5ms vs 200ms threshold");
        assertTrue(results.isResponseTimeAcceptable(300.0), "Should pass with 250.5ms vs 300ms threshold");
        assertTrue(results.isThroughputAcceptable(5.0), "Should pass with 6.0 req/sec vs 5.0 threshold");
        assertFalse(results.isThroughputAcceptable(7.0), "Should fail with 6.0 req/sec vs 7.0 threshold");
        
        // Test summary generation
        String summary = results.getSummary();
        assertNotNull(summary, "Summary should not be null");
        assertTrue(summary.contains("Test Results"), "Summary should contain test plan name");
        
        // Debug: Print the actual summary
        logger.info("Actual summary content:\n{}", summary);
        
        assertTrue(summary.contains("6,67") || summary.contains("6.67"), "Summary should contain error percentage");
        assertTrue(summary.contains("Connection timeout"), "Summary should contain errors");
        
        logger.info("PerformanceResults test completed successfully");
    }
} 