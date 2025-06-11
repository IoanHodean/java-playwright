package com.automation.tests;

import org.testng.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.InputStream;
import java.util.Set;

import static org.testng.Assert.*;

/**
 * JSON Schema Validation Test Suite
 * 
 * Demonstrates:
 * 1. NetworkNT JSON schema validation  
 * 2. Custom validation with detailed error reporting
 * 3. Schema validation for API response structures
 * 4. Schema validation failure detection
 */
public class JsonSchemaValidationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaValidationTest.class);
    private ObjectMapper objectMapper;
    private JsonSchemaFactory schemaFactory;
    
    @BeforeClass
    public void setupClass() {
        logger.info("Setting up JSON Schema Validation tests");
        
        // Initialize JSON processing components
        objectMapper = new ObjectMapper();
        schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        
        System.out.println("=== JSON Schema Validation Test Suite ===");
    }
    
    @Test(description = "Validate valid user JSON against schema")
    public void testValidUserSchemaValidation() {
        logger.info("Testing valid user JSON schema validation");
        
        // Sample valid user JSON
        String validUserJson = """
            {
                "id": 1,
                "name": "John Doe",
                "email": "john.doe@example.com",
                "status": "active",
                "age": 30,
                "address": {
                    "city": "New York",
                    "country": "US",
                    "zipCode": "10001"
                },
                "roles": ["user"],
                "createdAt": "2023-01-01T10:00:00Z"
            }
            """;
            
        // Validate with NetworkNT
        validateJsonWithNetworkNT(validUserJson, "schemas/user-schema.json");
        
        System.out.println("✓ Valid user JSON schema validation passed");
    }
    
    @Test(description = "Test schema validation failure scenarios")
    public void testInvalidUserSchemaValidation() {
        logger.info("Testing schema validation failure detection");
        
        // Create invalid JSON for testing - missing required fields and wrong types
        String invalidJson = """
            {
                "id": "not-a-number",
                "name": "",
                "email": "invalid-email",
                "status": "unknown-status"
            }
            """;
            
        try {
            validateJsonWithNetworkNT(invalidJson, "schemas/user-schema.json");
            fail("Schema validation should have failed for invalid JSON");
        } catch (AssertionError e) {
            logger.info("Expected validation failure caught: {}", e.getMessage());
            assertTrue(e.getMessage().contains("JSON Schema validation failed"));
            System.out.println("✓ Schema validation correctly failed for invalid data");
        }
    }
    
    @Test(description = "Test missing required fields validation")
    public void testMissingRequiredFields() {
        logger.info("Testing missing required fields validation");
        
        // JSON missing required 'email' field
        String incompleteJson = """
            {
                "id": 1,
                "name": "John Doe",
                "status": "active"
            }
            """;
            
        try {
            validateJsonWithNetworkNT(incompleteJson, "schemas/user-schema.json");
            fail("Schema validation should have failed for missing required fields");
        } catch (AssertionError e) {
            logger.info("Expected validation failure for missing fields: {}", e.getMessage());
            assertTrue(e.getMessage().contains("email"));
            System.out.println("✓ Missing required fields correctly detected");
        }
    }
    
    @Test(description = "Test invalid enum value validation")
    public void testInvalidEnumValues() {
        logger.info("Testing invalid enum values validation");
        
        // JSON with invalid status enum value
        String invalidEnumJson = """
            {
                "id": 1,
                "name": "John Doe",
                "email": "john@example.com",
                "status": "invalid-status"
            }
            """;
            
        try {
            validateJsonWithNetworkNT(invalidEnumJson, "schemas/user-schema.json");
            fail("Schema validation should have failed for invalid enum value");
        } catch (AssertionError e) {
            logger.info("Expected validation failure for invalid enum: {}", e.getMessage());
            System.out.println("✓ Invalid enum value correctly detected");
        }
    }
    
    @Test(description = "Test nested object validation")
    public void testNestedObjectValidation() {
        logger.info("Testing nested object validation");
        
        // Valid user with nested address object
        String validNestedJson = """
            {
                "id": 1,
                "name": "Jane Smith",
                "email": "jane@example.com",
                "status": "active",
                "address": {
                    "street": "123 Main St",
                    "city": "Boston",
                    "zipCode": "02101",
                    "country": "US"
                },
                "roles": ["admin", "user"],
                "createdAt": "2023-06-01T14:30:00Z",
                "metadata": {
                    "department": "Engineering",
                    "level": "Senior"
                }
            }
            """;
            
        validateJsonWithNetworkNT(validNestedJson, "schemas/user-schema.json");
        System.out.println("✓ Nested object validation passed");
    }
    
    /**
     * Advanced JSON Schema validation using NetworkNT library
     * Provides detailed validation error messages
     */
    private void validateJsonWithNetworkNT(String jsonData, String schemaPath) {
        try {
            // Load schema from classpath
            InputStream schemaStream = getClass().getClassLoader()
                .getResourceAsStream(schemaPath);
            
            if (schemaStream == null) {
                throw new RuntimeException("Schema file not found: " + schemaPath);
            }
            
            // Create schema and validate
            JsonSchema schema = schemaFactory.getSchema(schemaStream);
            JsonNode jsonNode = objectMapper.readTree(jsonData);
            Set<ValidationMessage> validationMessages = schema.validate(jsonNode);
            
            if (!validationMessages.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("JSON Schema validation failed:\n");
                for (ValidationMessage message : validationMessages) {
                    errorMessage.append("- ").append(message.getMessage()).append("\n");
                    logger.error("Schema validation error: {}", message.getMessage());
                }
                throw new AssertionError(errorMessage.toString());
            }
            
            logger.info("JSON Schema validation successful for: {}", schemaPath);
            
        } catch (AssertionError e) {
            throw e; // Re-throw assertion errors as-is
        } catch (Exception e) {
            logger.error("Schema validation error", e);
            throw new AssertionError("Schema validation failed: " + e.getMessage(), e);
        }
    }
    
    @AfterClass
    public void teardownClass() {
        logger.info("JSON Schema validation tests completed");
        System.out.println("=== JSON Schema Validation Tests Complete ===");
    }
} 