package com.automation.api.tests;

import com.automation.api.base.BaseApiTest;
import com.automation.api.client.ApiEndpoints;
import com.automation.api.client.BaseApiClient;
import com.automation.api.models.request.CreateUserRequest;
import com.automation.api.models.response.UserResponse;
import com.automation.api.utils.ApiTestDataProvider;
import io.restassured.response.ValidatableResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * API tests for JSONPlaceholder User endpoints.
 * Demonstrates essential CRUD operations with clean, focused test cases.
 */
public class UserApiTest extends BaseApiTest {
    private static final Logger logger = LoggerFactory.getLogger(UserApiTest.class);
    private BaseApiClient apiClient;
    
    @BeforeMethod
    public void setupUserApiTest() {
        apiClient = new BaseApiClient();
    }
    
    @Test(priority = 1)
    public void testGetAllUsers() {
        // Given
        String endpoint = ApiEndpoints.USERS;
        logRequestDetails("GET", endpoint, null);
        
        // When
        ValidatableResponse response = apiClient.performGet(endpoint);
        
        // Then
        logResponseDetails(response);
        response.statusCode(200);
        
        // Extract and validate response
        List<UserResponse> users = response.extract().jsonPath().getList("", UserResponse.class);
        assertNotNull(users, "Users list should not be null");
        assertFalse(users.isEmpty(), "Users list should not be empty");
        assertTrue(users.size() >= 10, "Should return at least 10 users");
        
        // Validate schema
        validateSchema(response, "users-array-schema.json");
        
        logger.info("Test completed: Retrieved {} users successfully", users.size());
    }
    
    @Test(priority = 2, dataProvider = "validUserIds", dataProviderClass = ApiTestDataProvider.class)
    public void testGetUserById(int userId) {
        // Given
        String endpoint = ApiEndpoints.USER_BY_ID;
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("userId", userId);
        
        logRequestDetails("GET", endpoint, pathParams);
        
        // When
        ValidatableResponse response = apiClient.performGet(endpoint, pathParams);
        
        // Then
        logResponseDetails(response);
        response.statusCode(200);
        
        // Extract and validate response
        UserResponse user = response.extract().as(UserResponse.class);
        assertNotNull(user, "User should not be null");
        assertEquals(user.getId(), userId, "User ID should match requested ID");
        assertNotNull(user.getName(), "User name should not be null");
        assertNotNull(user.getUsername(), "Username should not be null");
        assertNotNull(user.getEmail(), "Email should not be null");
        assertTrue(user.getEmail().contains("@"), "Email should be valid format");
        
        // Validate schema
        validateSchema(response, "user-schema.json");
        
        logger.info("Test completed: Retrieved user {} successfully", user.getName());
    }
    
    @Test(priority = 3, dataProvider = "createUserData", dataProviderClass = ApiTestDataProvider.class)
    public void testCreateUser(CreateUserRequest userRequest) {
        // Given
        String endpoint = ApiEndpoints.USERS;
        logRequestDetails("POST", endpoint, userRequest);
        
        // When
        ValidatableResponse response = apiClient.performPost(endpoint, userRequest);
        
        // Then
        logResponseDetails(response);
        response.statusCode(201);
        
        // Extract and validate response
        UserResponse createdUser = response.extract().as(UserResponse.class);
        assertNotNull(createdUser, "Created user should not be null");
        assertTrue(createdUser.getId() > 0, "User ID should be positive");
        assertEquals(createdUser.getName(), userRequest.getName(), "Name should match request");
        assertEquals(createdUser.getUsername(), userRequest.getUsername(), "Username should match request");
        assertEquals(createdUser.getEmail(), userRequest.getEmail(), "Email should match request");
        
        logger.info("Test completed: Created user {} with ID {}", createdUser.getName(), createdUser.getId());
    }
    
    @Test(priority = 4)
    public void testUpdateUser() {
        // Given
        int userId = 1;
        CreateUserRequest updateRequest = ApiTestDataProvider.TestData.SAMPLE_USER;
        
        String endpoint = ApiEndpoints.USER_BY_ID;
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("userId", userId);
        
        logRequestDetails("PUT", endpoint, updateRequest);
        
        // When
        ValidatableResponse response = apiClient.performPut(endpoint, updateRequest, pathParams);
        
        // Then
        logResponseDetails(response);
        response.statusCode(200);
        
        // Extract and validate response
        UserResponse updatedUser = response.extract().as(UserResponse.class);
        assertNotNull(updatedUser, "Updated user should not be null");
        assertEquals(updatedUser.getId(), userId, "User ID should remain unchanged");
        assertEquals(updatedUser.getName(), updateRequest.getName(), "Name should be updated");
        assertEquals(updatedUser.getUsername(), updateRequest.getUsername(), "Username should be updated");
        assertEquals(updatedUser.getEmail(), updateRequest.getEmail(), "Email should be updated");
        
        logger.info("Test completed: Updated user ID {} successfully", userId);
    }
} 