package com.automation.api.config;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.config;

/**
 * RestAssured configuration for JSONPlaceholder API testing.
 * Simplified setup for essential API test functionality.
 */
public class RestAssuredConfig {
    private static final Logger logger = LoggerFactory.getLogger(RestAssuredConfig.class);
    private static final ApiConfig apiConfig = ApiConfig.getInstance();
    private static boolean isConfigured = false;
    
    /**
     * Setup global RestAssured configuration.
     */
    public static synchronized void setupGlobalConfig() {
        if (isConfigured) {
            return;
        }
        
        try {
            // Global timeout configuration
            RestAssured.config = config()
                .httpClient(HttpClientConfig.httpClientConfig()
                    .setParam("http.connection.timeout", apiConfig.getConnectionTimeout())
                    .setParam("http.socket.timeout", apiConfig.getResponseTimeout()));
            
            // Global logging configuration
            RestAssured.config = RestAssured.config()
                .logConfig(LogConfig.logConfig()
                    .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
                    .enablePrettyPrinting(true));
            
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
            
            isConfigured = true;
            logger.info("RestAssured global configuration completed successfully");
            
        } catch (Exception e) {
            logger.error("Failed to setup RestAssured global configuration", e);
            throw new RuntimeException("RestAssured configuration failed", e);
        }
    }
    
    /**
     * Create request specification for JSONPlaceholder API.
     */
    public static RequestSpecification getRequestSpec() {
        setupGlobalConfig();
        
        return new RequestSpecBuilder()
            .setBaseUri(apiConfig.getBaseUrl())
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .log(LogDetail.ALL)
            .build();
    }
    
    /**
     * Create basic response specification.
     */
    public static ResponseSpecification getResponseSpec() {
        return new ResponseSpecBuilder()
            .log(LogDetail.ALL)
            .build();
    }
} 