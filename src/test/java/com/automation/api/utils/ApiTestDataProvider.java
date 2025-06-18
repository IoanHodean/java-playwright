package com.automation.api.utils;
import com.automation.api.models.request.CreateUserRequest;
import org.testng.annotations.DataProvider;

/**
 * Simplified test data provider for API tests.
 * Focuses on essential test data for basic CRUD operations.
 */
public class ApiTestDataProvider {
    
    /**
     * Provides valid user IDs for testing.
     */
    @DataProvider(name = "validUserIds")
    public static Object[][] getValidUserIds() {
        return new Object[][] {
            {1}, {2}, {3}
        };
    }
    
    /**
     * Provides create user request data.
     */
    @DataProvider(name = "createUserData")
    public static Object[][] getCreateUserData() {
        return new Object[][] {
            {
                CreateUserRequest.builder()
                    .name("John Doe")
                    .username("johndoe")
                    .email("john.doe@example.com")
                    .phone("555-1234")
                    .website("johndoe.com")
                    .build()
            }
        };
    }
    
    /**
     * Static test data for easy access.
     */
    public static class TestData {
        public static final CreateUserRequest SAMPLE_USER = CreateUserRequest.builder()
            .name("Test User")
            .username("testuser")
            .email("test@example.com")
            .phone("555-0000")
            .website("test.com")
            .build();
    }
} 