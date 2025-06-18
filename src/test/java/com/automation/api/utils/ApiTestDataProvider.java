package com.automation.api.utils;

import com.github.javafaker.Faker;
import com.automation.api.models.request.CreateUserRequest;
import org.testng.annotations.DataProvider;

/**
 * Test data provider for API tests using JavaFaker to generate realistic test data.
 * Provides dynamic, randomized data for comprehensive testing scenarios.
 * 
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Use with TestNG DataProvider
 * @Test(dataProvider = "createUserData", dataProviderClass = ApiTestDataProvider.class)
 * public void testCreateUser(CreateUserRequest user) {
 *     // Test with realistic fake data
 *     System.out.println("Testing with: " + user.getName());
 * }
 * 
 * // Generate random users programmatically
 * CreateUserRequest randomUser = ApiTestDataProvider.generateRandomUser();
 * 
 * // Use special test data
 * CreateUserRequest longNameUser = ApiTestDataProvider.TestData.getUserWithLongName();
 * CreateUserRequest[] userBatch = ApiTestDataProvider.TestData.generateUserBatch(10);
 * }</pre>
 * 
 * <p><b>Available DataProviders:</b></p>
 * <ul>
 *   <li>{@code validUserIds} - Provides valid user IDs (1-5)</li>
 *   <li>{@code randomUserIds} - Provides random user IDs (1-100) using Faker</li>
 *   <li>{@code createUserData} - Provides realistic user objects with Faker data</li>
 *   <li>{@code invalidUserData} - Provides invalid user objects for negative testing</li>
 * </ul>
 * 
 * @author Test Automation Framework
 * @version 1.0
 * @see com.github.javafaker.Faker
 */
public class ApiTestDataProvider {
    
    private static final Faker faker = new Faker();
    
    /**
     * Provides valid user IDs for testing.
     * Returns user IDs from 1 to 5.
     * 
     * @return Object[][] containing valid user IDs
     */
    @DataProvider(name = "validUserIds")
    public static Object[][] getValidUserIds() {
        return new Object[][] {
            {1}, {2}, {3}, {4}, {5}
        };
    }
    
    /**
     * Provides random user IDs for edge case testing.
     * Uses Faker to generate random numbers between 1 and 100.
     * Each test run will have different random IDs.
     * 
     * @return Object[][] containing 3 random user IDs
     */
    @DataProvider(name = "randomUserIds")
    public static Object[][] getRandomUserIds() {
        return new Object[][] {
            {faker.number().numberBetween(1, 100)},
            {faker.number().numberBetween(1, 100)},
            {faker.number().numberBetween(1, 100)}
        };
    }
    
    /**
     * Provides create user request data with realistic fake data.
     * Each user object contains realistic names, emails, phone numbers, etc.
     * Data is dynamically generated for each test run.
     * 
     * @return Object[][] containing 3 CreateUserRequest objects with fake data
     */
    @DataProvider(name = "createUserData")
    public static Object[][] getCreateUserData() {
        return new Object[][] {
            {generateRandomUser()},
            {generateRandomUser()},
            {generateRandomUser()}
        };
    }
    
    /**
     * Provides invalid user data for negative testing.
     * Contains users with validation issues like empty names, invalid emails, etc.
     * Perfect for testing API validation and error handling.
     * 
     * @return Object[][] containing 3 CreateUserRequest objects with validation issues
     */
    @DataProvider(name = "invalidUserData")
    public static Object[][] getInvalidUserData() {
        return new Object[][] {
            // User with empty name
            {
                CreateUserRequest.builder()
                    .name("")
                    .username(faker.name().username())
                    .email(faker.internet().emailAddress())
                    .phone(faker.phoneNumber().phoneNumber())
                    .website(faker.internet().domainName())
                    .build()
            },
            // User with invalid email
            {
                CreateUserRequest.builder()
                    .name(faker.name().fullName())
                    .username(faker.name().username())
                    .email("invalid-email")
                    .phone(faker.phoneNumber().phoneNumber())
                    .website(faker.internet().domainName())
                    .build()
            },
            // User with missing username
            {
                CreateUserRequest.builder()
                    .name(faker.name().fullName())
                    .username("")
                    .email(faker.internet().emailAddress())
                    .phone(faker.phoneNumber().phoneNumber())
                    .website(faker.internet().domainName())
                    .build()
            }
        };
    }
    
    /**
     * Generates a random user with realistic fake data.
     * Each call returns a completely different user with realistic information.
     * 
     * <p><b>Generated fields:</b></p>
     * <ul>
     *   <li>Name: Full names like "John Smith", "Mary Johnson"</li>
     *   <li>Username: Realistic usernames like "john.smith", "mary_johnson"</li>
     *   <li>Email: Valid email formats like "john@example.com"</li>
     *   <li>Phone: Various phone number formats</li>
     *   <li>Website: Domain names like "example.com", "test.org"</li>
     * </ul>
     * 
     * @return CreateUserRequest with randomly generated realistic data
     */
    public static CreateUserRequest generateRandomUser() {
        return CreateUserRequest.builder()
            .name(faker.name().fullName())
            .username(faker.name().username())
            .email(faker.internet().emailAddress())
            .phone(faker.phoneNumber().phoneNumber())
            .website(faker.internet().domainName())
            .build();
    }
    
    /**
     * Static test data for consistent testing scenarios.
     * Contains both predefined data and special data generation methods.
     */
    public static class TestData {
        
        /**
         * Predefined sample user for consistent testing.
         * Use when you need predictable test data.
         */
        public static final CreateUserRequest SAMPLE_USER = CreateUserRequest.builder()
            .name("John Doe")
            .username("johndoe")
            .email("john.doe@example.com")
            .phone("555-1234")
            .website("johndoe.com")
            .build();
            
        /**
         * Generates a user with an extremely long name for boundary testing.
         * Name length will be between 100-200 characters.
         * Useful for testing field length limits and UI handling.
         * 
         * @return CreateUserRequest with very long name
         */
        public static CreateUserRequest getUserWithLongName() {
            return CreateUserRequest.builder()
                .name(faker.lorem().characters(100, 200)) // Very long name
                .username(faker.name().username())
                .email(faker.internet().emailAddress())
                .phone(faker.phoneNumber().phoneNumber())
                .website(faker.internet().domainName())
                .build();
        }
        
        /**
         * Generates a user with special characters in name and username.
         * Tests handling of special characters like @#$% in user data.
         * Useful for security testing and input validation.
         * 
         * @return CreateUserRequest with special characters
         */
        public static CreateUserRequest getUserWithSpecialChars() {
            return CreateUserRequest.builder()
                .name(faker.name().fullName() + " @#$%")
                .username(faker.name().username() + "_123")
                .email(faker.internet().emailAddress())
                .phone(faker.phoneNumber().phoneNumber())
                .website(faker.internet().domainName())
                .build();
        }
        
        /**
         * Generates a batch of users for bulk testing scenarios.
         * All users will have different realistic data generated by Faker.
         * 
         * <p><b>Usage:</b></p>
         * <pre>{@code
         * CreateUserRequest[] users = ApiTestDataProvider.TestData.generateUserBatch(100);
         * for (CreateUserRequest user : users) {
         *     // Test bulk operations
         *     apiClient.createUser(user);
         * }
         * }</pre>
         * 
         * @param count Number of users to generate (must be positive)
         * @return Array of CreateUserRequest objects with unique fake data
         * @throws IllegalArgumentException if count is less than 1
         */
        public static CreateUserRequest[] generateUserBatch(int count) {
            if (count < 1) {
                throw new IllegalArgumentException("Count must be at least 1");
            }
            
            CreateUserRequest[] users = new CreateUserRequest[count];
            for (int i = 0; i < count; i++) {
                users[i] = generateRandomUser();
            }
            return users;
        }
    }
} 