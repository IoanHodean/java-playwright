package com.automation.tests;

import org.testng.Assert;
import org.testng.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample test class to demonstrate TestNG reporting capabilities
 */
public class SampleTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    
    @BeforeClass
    public void setupClass() {
        logger.info("Setting up test class");
        System.out.println("=== Starting Sample Test Class ===");
    }
    
    @Test(description = "Sample test that passes")
    public void testThatPasses() {
        logger.info("Running test that should pass");
        Assert.assertTrue(true, "This test should always pass");
        System.out.println("✓ Test passed successfully");
    }
    
    @Test(description = "Sample test that fails", enabled = false)
    public void testThatFails() {
        logger.error("Running test that will fail");
        Assert.fail("This test is designed to fail for demonstration");
    }
    
    @Test(description = "Sample test with data", dataProvider = "testData")
    public void testWithData(String input, int expectedLength) {
        logger.info("Testing with input: {}", input);
        Assert.assertEquals(input.length(), expectedLength, "Length mismatch");
    }
    
    @DataProvider(name = "testData")
    public Object[][] provideTestData() {
        return new Object[][] {
            {"Hello", 5},
            {"TestNG", 6},
            {"Automation", 10}
        };
    }
    
    @AfterClass
    public void teardownClass() {
        logger.info("Cleaning up test class");
        System.out.println("=== Finished Sample Test Class ===");
    }
} 