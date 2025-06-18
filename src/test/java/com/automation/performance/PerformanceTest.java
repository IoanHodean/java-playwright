package com.automation.performance;

import com.automation.api.models.request.CreateUserRequest;
import com.automation.api.utils.ApiTestDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Performance tests using JMeter integration.
 * Demonstrates load testing, stress testing, and performance validation.
 */
public class PerformanceTest {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceTest.class);
    private PerformanceTestRunner performanceRunner;
    
    @BeforeClass
    public void setupPerformanceTests() {
        performanceRunner = new PerformanceTestRunner();
        logger.info("Performance test setup completed");
    }
    
    @Test(priority = 1, description = "Light load test for GET /users endpoint")
    public void testGetUsersLightLoad() {
        logger.info("=== Starting Light Load Test for GET /users ===");
        
        // Light load: 5 users, 10 second ramp-up, 3 loops each
        PerformanceResults results = performanceRunner.runGetLoadTest("/users", 5, 10, 3);
        
        assertNotNull(results, "Performance results should not be null");
        assertEquals(results.getThreadCount(), 5, "Thread count should match");
        assertEquals(results.getLoopCount(), 3, "Loop count should match");
        
        logger.info("Light load test results:");
        logger.info(results.getSummary());
        
        // Validate basic performance criteria (lenient for light load)
        boolean slaPass = performanceRunner.validatePerformanceSLA(results, 
                5.0,     // Max 5% error rate
                5000.0,  // Max 5 second response time
                1.0      // Min 1 req/sec throughput
        );
        
        assertTrue(slaPass, "Light load test should meet SLA requirements");
    }
    
    @Test(priority = 2, description = "Medium load test for GET /users/1 endpoint")
    public void testGetUserByIdMediumLoad() {
        logger.info("=== Starting Medium Load Test for GET /users/1 ===");
        
        // Medium load: 10 users, 15 second ramp-up, 5 loops each
        PerformanceResults results = performanceRunner.runGetLoadTest("/users/1", 10, 15, 5);
        
        assertNotNull(results, "Performance results should not be null");
        assertEquals(results.getThreadCount(), 10, "Thread count should match");
        assertEquals(results.getLoopCount(), 5, "Loop count should match");
        
        logger.info("Medium load test results:");
        logger.info(results.getSummary());
        
        // Validate performance criteria
        boolean slaPass = performanceRunner.validatePerformanceSLA(results, 
                3.0,     // Max 3% error rate
                3000.0,  // Max 3 second response time
                2.0      // Min 2 req/sec throughput
        );
        
        assertTrue(slaPass, "Medium load test should meet SLA requirements");
    }
    
    @Test(priority = 3, description = "POST endpoint load test with realistic data")
    public void testCreateUserPostLoad() {
        logger.info("=== Starting Load Test for POST /users ===");
        
        // Generate test data
        CreateUserRequest testUser = ApiTestDataProvider.generateRandomUser();
        
        // Load test: 8 users, 12 second ramp-up, 3 loops each
        PerformanceResults results = performanceRunner.runPostLoadTest("/users", testUser, 8, 12, 3);
        
        assertNotNull(results, "Performance results should not be null");
        assertEquals(results.getThreadCount(), 8, "Thread count should match");
        assertEquals(results.getLoopCount(), 3, "Loop count should match");
        
        logger.info("POST load test results:");
        logger.info(results.getSummary());
        
        // Validate performance criteria (more lenient for POST operations)
        boolean slaPass = performanceRunner.validatePerformanceSLA(results, 
                5.0,     // Max 5% error rate
                4000.0,  // Max 4 second response time
                1.5      // Min 1.5 req/sec throughput
        );
        
        assertTrue(slaPass, "POST load test should meet SLA requirements");
    }
    
    @Test(priority = 4, description = "Comprehensive API stress test")
    public void testApiStressTest() {
        logger.info("=== Starting Comprehensive API Stress Test ===");
        
        // Stress test: 15 users, 20 second ramp-up, 2 loops each
        PerformanceResults results = performanceRunner.runApiStressTest(15, 20, 2);
        
        assertNotNull(results, "Performance results should not be null");
        assertEquals(results.getThreadCount(), 15, "Thread count should match");
        assertEquals(results.getLoopCount(), 2, "Loop count should match");
        
        logger.info("Stress test results:");
        logger.info(results.getSummary());
        
        // Validate stress test criteria (more tolerant)
        boolean slaPass = performanceRunner.validatePerformanceSLA(results, 
                10.0,    // Max 10% error rate (higher tolerance for stress test)
                6000.0,  // Max 6 second response time
                1.0      // Min 1 req/sec throughput
        );
        
        assertTrue(slaPass, "Stress test should meet relaxed SLA requirements");
    }
    
    @Test(priority = 5, description = "Spike test to check sudden load handling")
    public void testSpikeTest() {
        logger.info("=== Starting Spike Test ===");
        
        // Spike test: normal 3 users, spike to 12 users for 2 loops
        PerformanceResults results = performanceRunner.runSpikeTest("/users", 3, 12, 2);
        
        assertNotNull(results, "Performance results should not be null");
        assertTrue(results.getThreadCount() >= 3, "Thread count should be at least normal load");
        
        logger.info("Spike test results:");
        logger.info(results.getSummary());
        
        // For spike test, we mainly check that the system didn't completely fail
        assertTrue(results.getTotalExecutionTime() > 0, "Test should have executed");
        assertNotNull(results.getTestPlanName(), "Test plan name should be set");
    }
    
    @Test(priority = 6, description = "Short endurance test", enabled = false)
    public void testEnduranceTest() {
        // Note: Disabled by default as it takes longer to run
        logger.info("=== Starting Short Endurance Test ===");
        
        // Short endurance: 5 users for 2 minutes
        PerformanceResults results = performanceRunner.runEnduranceTest("/users", 5, 2);
        
        assertNotNull(results, "Performance results should not be null");
        assertEquals(results.getThreadCount(), 5, "Thread count should match");
        
        logger.info("Endurance test results:");
        logger.info(results.getSummary());
        
        // Validate endurance criteria
        boolean slaPass = performanceRunner.validatePerformanceSLA(results, 
                5.0,     // Max 5% error rate
                4000.0,  // Max 4 second response time
                1.0      // Min 1 req/sec throughput
        );
        
        assertTrue(slaPass, "Endurance test should meet SLA requirements");
    }
    
    @Test(priority = 7, description = "Performance baseline measurement")
    public void testPerformanceBaseline() {
        logger.info("=== Establishing Performance Baseline ===");
        
        // Baseline: single user, minimal load
        PerformanceResults results = performanceRunner.runGetLoadTest("/users", 1, 1, 5);
        
        assertNotNull(results, "Performance results should not be null");
        assertEquals(results.getThreadCount(), 1, "Thread count should be 1");
        assertEquals(results.getLoopCount(), 5, "Loop count should be 5");
        
        logger.info("Performance baseline results:");
        logger.info(results.getSummary());
        
        // Store baseline metrics for comparison (in real scenario, you'd save to file/database)
        logger.info("=== BASELINE METRICS ===");
        logger.info("Execution Time: {} ms", results.getTotalExecutionTime());
        logger.info("Average Response Time: {:.2f} ms", results.getAverageResponseTime());
        logger.info("Throughput: {:.2f} req/sec", results.getThroughputPerSecond());
        logger.info("Error Rate: {:.2f}%", results.getErrorPercentage());
        
        // Basic validations for single user
        assertTrue(results.getTotalExecutionTime() > 0, "Should have execution time");
        assertTrue(results.getErrorPercentage() <= 2.0, "Single user should have minimal errors");
    }
} 