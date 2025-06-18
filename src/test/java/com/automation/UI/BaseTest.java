package com.automation.UI;

import com.microsoft.playwright.*;
import com.automation.config.PlaywrightConfig;
import org.testng.ITestResult;
import org.testng.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public abstract class BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected PlaywrightConfig config;
    protected Page page;
    
    @BeforeMethod
    public void setup(Method method) {
        String testName = method.getName();
        logger.info("Starting test: {}", testName);
        
        try {
            config = PlaywrightConfig.getInstance();
            page = config.createPage();
            logger.info("Test setup completed for: {}", testName);
        } catch (Exception e) {
            logger.error("Failed to setup test: {}", testName, e);
            throw new RuntimeException("Test setup failed", e);
        }
    }
    
    @AfterMethod
    public void cleanup(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        try {
            // Take screenshot on failure (only if page is still active)
            if (!result.isSuccess() && page != null && !page.isClosed()) {
                try {
                    String screenshotName = testName + "_failure";
                    config.takeScreenshot(page, screenshotName);
                    logger.info("Screenshot captured for failed test: {}", testName);
                } catch (Exception screenshotError) {
                    logger.warn("Could not capture screenshot for failed test: {}", testName, screenshotError);
                }
            }
            
            // Clean up resources
            if (config != null) {
                try {
                    config.cleanup();
                } catch (Exception cleanupError) {
                    logger.warn("Error during config cleanup for test: {}", testName, cleanupError);
                }
            }
            
            logger.info("Test cleanup completed for: {}", testName);
            
        } catch (Exception e) {
            logger.error("Error during cleanup for test: {}", testName, e);
        }
    }
    
    protected Page getPage() {
        return page;
    }
} 