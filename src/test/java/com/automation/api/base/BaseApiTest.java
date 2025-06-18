package com.automation.api.base;

import com.automation.api.config.ApiConfig;
import com.automation.api.config.RestAssuredConfig;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

import java.lang.reflect.Method;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Base class for API tests.
 * Simplified setup with essential functionality for API testing.
 */
public abstract class BaseApiTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseApiTest.class);
    protected ApiConfig apiConfig;
    protected RequestSpecification requestSpec;
    protected ResponseSpecification responseSpec;
    
    @BeforeClass
    public void setupApiFramework() {
        logger.info("Setting up API test framework");
        
        try {
            // Initialize API configuration
            apiConfig = ApiConfig.getInstance();
            
            // Setup RestAssured global configuration
            RestAssuredConfig.setupGlobalConfig();
            
            // Set specifications
            requestSpec = RestAssuredConfig.getRequestSpec();
            responseSpec = RestAssuredConfig.getResponseSpec();
            
            logger.info("API test framework setup completed successfully");
            
        } catch (Exception e) {
            logger.error("Failed to setup API test framework", e);
            throw new RuntimeException("API test framework setup failed", e);
        }
    }
    
    @BeforeMethod
    public void setupTest(Method method) {
        String testName = method.getName();
        logger.info("Starting API test: {}", testName);
    }
    
    @AfterMethod
    public void cleanupTest(Method method) {
        String testName = method.getName();
        logger.info("Completed API test: {}", testName);
    }
    
    /**
     * Validate response against JSON schema.
     */
    protected void validateSchema(ValidatableResponse response, String schemaPath) {
        if (apiConfig.isSchemaValidationEnabled()) {
            try {
                String fullSchemaPath = "schemas/api/" + schemaPath;
                logger.info("Validating response against schema: {}", schemaPath);
                
                response.body(matchesJsonSchemaInClasspath(fullSchemaPath));
                
                logger.info("Schema validation passed for: {}", schemaPath);
                
            } catch (Exception e) {
                logger.error("Schema validation failed for: {}", schemaPath, e);
                throw new AssertionError("Schema validation failed for: " + schemaPath, e);
            }
        }
    }
    
    /**
     * Log request details for debugging.
     */
    protected void logRequestDetails(String httpMethod, String endpoint, Object requestBody) {
        logger.info("API Request - Method: {}, Endpoint: {}", httpMethod, endpoint);
        if (requestBody != null) {
            logger.debug("Request Body: {}", requestBody);
        }
    }
    
    /**
     * Log response details for debugging.
     */
    protected void logResponseDetails(ValidatableResponse response) {
        try {
            int statusCode = response.extract().statusCode();
            long responseTime = response.extract().time();
            
            logger.info("API Response - Status: {}, Time: {}ms", statusCode, responseTime);
            
        } catch (Exception e) {
            logger.warn("Could not log response details", e);
        }
    }
} 