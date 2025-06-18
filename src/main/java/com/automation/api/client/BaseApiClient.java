package com.automation.api.client;

import com.automation.api.config.ApiConfig;
import com.automation.api.config.RestAssuredConfig;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Base API client for common CRUD operations.
 * Simplified for essential API testing functionality.
 */
public class BaseApiClient {
    private static final Logger logger = LoggerFactory.getLogger(BaseApiClient.class);
    protected final ApiConfig apiConfig;
    protected RequestSpecification requestSpec;
    protected ResponseSpecification responseSpec;
    
    public BaseApiClient() {
        this.apiConfig = ApiConfig.getInstance();
        this.requestSpec = RestAssuredConfig.getRequestSpec();
        this.responseSpec = RestAssuredConfig.getResponseSpec();
    }
    
    /**
     * Perform GET request to specified endpoint.
     */
    public ValidatableResponse performGet(String endpoint) {
        return performGet(endpoint, null, null);
    }
    
    /**
     * Perform GET request with path parameters.
     */
    public ValidatableResponse performGet(String endpoint, Map<String, Object> pathParams) {
        return performGet(endpoint, pathParams, null);
    }
    
    /**
     * Perform GET request with path and query parameters.
     */
    public ValidatableResponse performGet(String endpoint, Map<String, Object> pathParams, 
                                        Map<String, Object> queryParams) {
        logger.info("Performing GET request to: {}", endpoint);
        
        try {
            RequestSpecification request = given(requestSpec);
            
            if (pathParams != null && !pathParams.isEmpty()) {
                request.pathParams(pathParams);
            }
            
            if (queryParams != null && !queryParams.isEmpty()) {
                request.queryParams(queryParams);
            }
            
            ValidatableResponse response = request
                .when()
                    .get(endpoint)
                .then()
                    .spec(responseSpec);
            
            logger.info("GET request completed for: {}", endpoint);
            return response;
            
        } catch (Exception e) {
            logger.error("GET request failed for: {}", endpoint, e);
            throw new RuntimeException("GET request failed for endpoint: " + endpoint, e);
        }
    }
    
    /**
     * Perform POST request with request body.
     */
    public ValidatableResponse performPost(String endpoint, Object requestBody) {
        return performPost(endpoint, requestBody, null);
    }
    
    /**
     * Perform POST request with request body and path parameters.
     */
    public ValidatableResponse performPost(String endpoint, Object requestBody, 
                                         Map<String, Object> pathParams) {
        logger.info("Performing POST request to: {}", endpoint);
        
        try {
            RequestSpecification request = given(requestSpec)
                .body(requestBody);
            
            if (pathParams != null && !pathParams.isEmpty()) {
                request.pathParams(pathParams);
            }
            
            ValidatableResponse response = request
                .when()
                    .post(endpoint)
                .then()
                    .spec(responseSpec);
            
            logger.info("POST request completed for: {}", endpoint);
            return response;
            
        } catch (Exception e) {
            logger.error("POST request failed for: {}", endpoint, e);
            throw new RuntimeException("POST request failed for endpoint: " + endpoint, e);
        }
    }
    
    /**
     * Perform PUT request with request body.
     */
    public ValidatableResponse performPut(String endpoint, Object requestBody) {
        return performPut(endpoint, requestBody, null);
    }
    
    /**
     * Perform PUT request with request body and path parameters.
     */
    public ValidatableResponse performPut(String endpoint, Object requestBody, 
                                        Map<String, Object> pathParams) {
        logger.info("Performing PUT request to: {}", endpoint);
        
        try {
            RequestSpecification request = given(requestSpec)
                .body(requestBody);
            
            if (pathParams != null && !pathParams.isEmpty()) {
                request.pathParams(pathParams);
            }
            
            ValidatableResponse response = request
                .when()
                    .put(endpoint)
                .then()
                    .spec(responseSpec);
            
            logger.info("PUT request completed for: {}", endpoint);
            return response;
            
        } catch (Exception e) {
            logger.error("PUT request failed for: {}", endpoint, e);
            throw new RuntimeException("PUT request failed for endpoint: " + endpoint, e);
        }
    }
    
    /**
     * Perform DELETE request.
     */
    public ValidatableResponse performDelete(String endpoint) {
        return performDelete(endpoint, null);
    }
    
    /**
     * Perform DELETE request with path parameters.
     */
    public ValidatableResponse performDelete(String endpoint, Map<String, Object> pathParams) {
        logger.info("Performing DELETE request to: {}", endpoint);
        
        try {
            RequestSpecification request = given(requestSpec);
            
            if (pathParams != null && !pathParams.isEmpty()) {
                request.pathParams(pathParams);
            }
            
            ValidatableResponse response = request
                .when()
                    .delete(endpoint)
                .then()
                    .spec(responseSpec);
            
            logger.info("DELETE request completed for: {}", endpoint);
            return response;
            
        } catch (Exception e) {
            logger.error("DELETE request failed for: {}", endpoint, e);
            throw new RuntimeException("DELETE request failed for endpoint: " + endpoint, e);
        }
    }
} 