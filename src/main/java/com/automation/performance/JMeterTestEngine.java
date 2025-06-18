package com.automation.performance;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * JMeter Test Engine for programmatic performance testing.
 * Provides a simplified interface to create and execute JMeter test plans.
 */
public class JMeterTestEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(JMeterTestEngine.class);
    private StandardJMeterEngine jmeterEngine;
    private ListedHashTree testPlanTree;
    private TestPlan testPlan;
    private ThreadGroup threadGroup;
    private LoopController loopController;
    private boolean initialized = false;
    
    /**
     * Initialize JMeter engine with default settings.
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            // Set system properties for JMeter
            System.setProperty("jmeter.home", System.getProperty("user.dir"));
            System.setProperty("java.awt.headless", "true");
            
            // Initialize JMeter home directory first
            JMeterUtils.setJMeterHome(System.getProperty("user.dir"));
            
            // Create minimal properties
            java.util.Properties jmeterProps = new java.util.Properties();
            
            // Add essential JMeter properties to avoid initialization issues
            jmeterProps.setProperty("jmeter.reportgenerator.overall_granularity", "1000");
            jmeterProps.setProperty("jmeter.save.saveservice.output_format", "csv");
            jmeterProps.setProperty("jmeter.save.saveservice.response_data.on_error", "false");
            jmeterProps.setProperty("jmeter.save.saveservice.successful", "true");
            jmeterProps.setProperty("jmeter.save.saveservice.thread_counts", "true");
            jmeterProps.setProperty("jmeter.save.saveservice.time", "true");
            jmeterProps.setProperty("jmeter.save.saveservice.latency", "true");
            jmeterProps.setProperty("jmeter.save.saveservice.connect_time", "true");
            jmeterProps.setProperty("jmeter.save.saveservice.response_code", "true");
            jmeterProps.setProperty("jmeter.save.saveservice.response_message", "true");
            jmeterProps.setProperty("jmeter.save.saveservice.thread_name", "true");
            jmeterProps.setProperty("jmeter.save.saveservice.label", "true");
            
            // Add locale properties to avoid locale initialization issues
            jmeterProps.setProperty("language", "en");
            jmeterProps.setProperty("country", "US");
            jmeterProps.setProperty("jmeter.locale", "en_US");
            
            // Create a temporary properties file and load it first
            java.io.File tempProps = java.io.File.createTempFile("jmeter", ".properties");
            tempProps.deleteOnExit();
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempProps)) {
                jmeterProps.store(fos, "JMeter properties for test automation");
            }
            
            // Load JMeter properties BEFORE initializing logging and locale
            JMeterUtils.loadJMeterProperties(tempProps.getAbsolutePath());
            
            // Now safely initialize logging and locale (properties are loaded)
            JMeterUtils.initLogging();
            JMeterUtils.initLocale();
            
            // Create JMeter engine
            jmeterEngine = new StandardJMeterEngine();
            
            initialized = true;
            logger.info("JMeter engine initialized successfully with default configuration");
            
        } catch (Exception e) {
            logger.error("Failed to initialize JMeter engine", e);
            throw new RuntimeException("JMeter initialization failed", e);
        }
    }
    
    /**
     * Create a new test plan with specified parameters.
     */
    public JMeterTestEngine createTestPlan(String testPlanName, int threadCount, int rampUpSeconds, int loops) {
        initialize();
        
        try {
            // Create Test Plan
            testPlan = new TestPlan(testPlanName);
            testPlan.setFunctionalMode(false);
            testPlan.setTearDownOnShutdown(true);
            testPlan.setUserDefinedVariables(testPlan.getArguments());
            
            // Create Loop Controller
            loopController = new LoopController();
            loopController.setLoops(loops);
            loopController.setFirst(true);
            loopController.initialize();
            
            // Create Thread Group
            threadGroup = new ThreadGroup();
            threadGroup.setName("Thread Group");
            threadGroup.setNumThreads(threadCount);
            threadGroup.setRampUp(rampUpSeconds);
            threadGroup.setSamplerController(loopController);
            
            // Create Test Plan Tree
            testPlanTree = new ListedHashTree();
            HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
            
            logger.info("Test plan '{}' created with {} threads, {} second ramp-up, {} loops", 
                       testPlanName, threadCount, rampUpSeconds, loops);
            
            return this;
            
        } catch (Exception e) {
            logger.error("Failed to create test plan", e);
            throw new RuntimeException("Test plan creation failed", e);
        }
    }
    
    /**
     * Add HTTP request sampler to the test plan.
     */
    public JMeterTestEngine addHttpRequest(String samplerName, String protocol, String serverName, 
                                          int port, String path, String method) {
        return addHttpRequest(samplerName, protocol, serverName, port, path, method, null, null);
    }
    
    /**
     * Add HTTP request sampler with headers and body.
     */
    public JMeterTestEngine addHttpRequest(String samplerName, String protocol, String serverName, 
                                          int port, String path, String method, 
                                          Map<String, String> headers, String requestBody) {
        try {
            // Create HTTP Sampler
            HTTPSampler httpSampler = new HTTPSampler();
            httpSampler.setName(samplerName);
            httpSampler.setProtocol(protocol);
            httpSampler.setDomain(serverName);
            httpSampler.setPort(port);
            httpSampler.setPath(path);
            httpSampler.setMethod(method);
            
            if (requestBody != null && !requestBody.trim().isEmpty()) {
                httpSampler.addNonEncodedArgument("", requestBody, "");
                httpSampler.setPostBodyRaw(true);
            }
            
            // Add to thread group
            HashTree threadGroupTree = testPlanTree.getTree(testPlan).getTree(threadGroup);
            HashTree httpSamplerTree = threadGroupTree.add(httpSampler);
            
            // Add headers if provided
            if (headers != null && !headers.isEmpty()) {
                HeaderManager headerManager = new HeaderManager();
                headerManager.setName("HTTP Header Manager");
                
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    headerManager.add(new Header(entry.getKey(), entry.getValue()));
                }
                
                httpSamplerTree.add(headerManager);
            }
            
            logger.info("Added HTTP request: {} {} {}:{}{}", method, samplerName, serverName, port, path);
            
            return this;
            
        } catch (Exception e) {
            logger.error("Failed to add HTTP request sampler", e);
            throw new RuntimeException("HTTP request addition failed", e);
        }
    }
    
    /**
     * Add result collector to save test results.
     */
    public JMeterTestEngine addResultCollector(String filename) {
        try {
            // Create Summariser
            Summariser summer = new Summariser("summary");
            
            // Create Result Collector
            ResultCollector resultCollector = new ResultCollector(summer);
            resultCollector.setFilename(filename);
            resultCollector.setName("View Results Tree");
            
            // Add to test plan
            testPlanTree.add(testPlan, resultCollector);
            
            logger.info("Added result collector with output file: {}", filename);
            
            return this;
            
        } catch (Exception e) {
            logger.error("Failed to add result collector", e);
            throw new RuntimeException("Result collector addition failed", e);
        }
    }
    
    /**
     * Execute the test plan and return performance results.
     */
    public PerformanceResults execute() {
        if (testPlanTree == null || testPlan == null) {
            throw new IllegalStateException("Test plan not created. Call createTestPlan() first.");
        }
        
        try {
            logger.info("Starting JMeter test execution...");
            long startTime = System.currentTimeMillis();
            
            // Configure and run test
            jmeterEngine.configure(testPlanTree);
            jmeterEngine.run();
            
            // Wait for test completion
            while (jmeterEngine.isActive()) {
                Thread.sleep(100);
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            logger.info("JMeter test execution completed in {} ms", duration);
            
            // Create and return results
            PerformanceResults results = new PerformanceResults();
            results.setTestPlanName(testPlan.getName());
            results.setTotalExecutionTime(duration);
            results.setThreadCount(threadGroup.getNumThreads());
            results.setRampUpTime(threadGroup.getRampUp());
            results.setLoopCount(loopController.getLoops());
            
            return results;
            
        } catch (Exception e) {
            logger.error("JMeter test execution failed", e);
            throw new RuntimeException("Test execution failed", e);
        }
    }
    
    /**
     * Create a simple API load test configuration.
     */
    public static JMeterTestEngine createApiLoadTest(String baseUrl, String endpoint, String method, 
                                                    int users, int rampUpSeconds, int loops) {
        JMeterTestEngine engine = new JMeterTestEngine();
        
        // Parse URL
        String protocol = baseUrl.startsWith("https") ? "https" : "http";
        String serverName = baseUrl.replace("https://", "").replace("http://", "");
        int port = protocol.equals("https") ? 443 : 80;
        
        // Create headers for JSON API
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        
        return engine
            .createTestPlan("API Load Test", users, rampUpSeconds, loops)
            .addHttpRequest("API Request", protocol, serverName, port, endpoint, method, headers, null)
            .addResultCollector("target/jmeter-results.jtl");
    }
    
    /**
     * Shutdown JMeter engine.
     */
    public void shutdown() {
        if (jmeterEngine != null) {
            jmeterEngine.stopTest(true);
            logger.info("JMeter engine shutdown completed");
        }
    }
} 