package com.automation.base;

import com.automation.config.PlaywrightConfig;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.testng.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Base Test Class for Playwright Test Automation
 * 
 * Provides common functionality for all test classes including:
 * - Playwright browser and context lifecycle management
 * - Screenshot and video recording on test failures
 * - Trace recording for debugging
 * - Environment-specific configuration
 * - Integration with TestNG and Allure reporting
 * - Thread-safe singleton pattern implementation
 */
public abstract class BaseTest {
    
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    
    // Thread-local storage for parallel execution
    private static final ThreadLocal<PlaywrightConfig> playwrightConfig = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browser = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> context = new ThreadLocal<>();
    private static final ThreadLocal<Page> page = new ThreadLocal<>();
    
    /**
     * Suite-level setup - runs once per test suite
     */
    @BeforeSuite(alwaysRun = true)
    public void setupSuite() {
        logger.info("========================================");
        logger.info("Starting Test Automation Suite");
        logger.info("========================================");
        
        // Log environment information
        PlaywrightConfig config = PlaywrightConfig.getInstance();
        logger.info("Environment: {}", config.getEnvironment());
        logger.info("Browser: {}", config.getBrowserType());
        logger.info("Headless mode: {}", config.isHeadless());
        logger.info("Base URL: {}", config.getBaseUrl());
        logger.info("Parallel workers: {}", config.getParallelWorkers());
        
        // Suite information logged above
    }
    
    /**
     * Class-level setup - runs once per test class
     */
    @BeforeClass(alwaysRun = true)
    public void setupClass() {
        logger.debug("Setting up test class: {}", this.getClass().getSimpleName());
        
        // Initialize Playwright configuration for this thread
        playwrightConfig.set(PlaywrightConfig.getInstance());
    }
    
    /**
     * Method-level setup - runs before each test method
     */
    @BeforeMethod(alwaysRun = true)
    public void setupMethod(Method method) {
        String testName = method.getName();
        logger.info("Starting test: {}", testName);
        
        try {
            // Create browser, context, and page for this test
            createBrowserSession();
            
            // Start trace recording if configured
            getPlaywrightConfig().startTrace(getContext(), 
                this.getClass().getSimpleName() + "_" + testName);
            
            logger.debug("Browser session created successfully for test: {}", testName);
            
        } catch (Exception e) {
            logger.error("Failed to setup browser session for test: {}", testName, e);
            throw new RuntimeException("Test setup failed", e);
        }
    }
    
    /**
     * Method-level teardown - runs after each test method
     */
    @AfterMethod(alwaysRun = true)
    public void teardownMethod(Method method) {
        String testName = method.getName();
        logger.debug("Cleaning up test: {}", testName);
        
        try {
            // Stop trace recording
            getPlaywrightConfig().stopTrace(getContext(), 
                this.getClass().getSimpleName() + "_" + testName);
            
            // Close browser session
            closeBrowserSession();
            
            logger.debug("Test cleanup completed: {}", testName);
            
        } catch (Exception e) {
            logger.error("Error during test cleanup: {}", testName, e);
        }
    }
    
    /**
     * Class-level teardown - runs once per test class
     */
    @AfterClass(alwaysRun = true)
    public void teardownClass() {
        logger.debug("Tearing down test class: {}", this.getClass().getSimpleName());
        
        // Clean up any remaining resources
        cleanup();
    }
    
    /**
     * Suite-level teardown - runs once per test suite
     */
    @AfterSuite(alwaysRun = true)
    public void teardownSuite() {
        logger.info("========================================");
        logger.info("Test Automation Suite Completed");
        logger.info("========================================");
    }
    
    /**
     * Create browser session (browser, context, page)
     */
    protected void createBrowserSession() {
        PlaywrightConfig config = getPlaywrightConfig();
        
        // Create browser
        Browser browserInstance = config.createBrowser();
        browser.set(browserInstance);
        
        // Create context
        BrowserContext contextInstance = config.createContext(browserInstance);
        context.set(contextInstance);
        
        // Create page
        Page pageInstance = config.createPage();
        page.set(pageInstance);
        
        logger.debug("Browser session created: Browser={}, Context={}, Page={}", 
            browserInstance != null, contextInstance != null, pageInstance != null);
    }
    
    /**
     * Close browser session
     */
    protected void closeBrowserSession() {
        try {
            Page currentPage = page.get();
            if (currentPage != null) {
                currentPage.close();
                page.remove();
            }
            
            BrowserContext currentContext = context.get();
            if (currentContext != null) {
                currentContext.close();
                context.remove();
            }
            
            Browser currentBrowser = browser.get();
            if (currentBrowser != null) {
                currentBrowser.close();
                browser.remove();
            }
            
        } catch (Exception e) {
            logger.error("Error closing browser session", e);
        }
    }
    
    /**
     * Handle test failure - take screenshot and attach to Allure
     */
    @AfterMethod(alwaysRun = true)
    public void handleTestFailure(Method method) {
        // This method is called after teardownMethod, so we need to check if page exists
        Page currentPage = page.get();
        if (currentPage != null) {
            try {
                String testName = this.getClass().getSimpleName() + "_" + method.getName();
                
                // Take screenshot
                getPlaywrightConfig().takeScreenshot(currentPage, testName + "_failure");
                
                logger.info("Screenshot captured for failed test: {}", testName);
                
            } catch (Exception e) {
                logger.error("Failed to capture failure screenshot for: {}", method.getName(), e);
            }
        }
    }
    
    /**
     * Navigate to base URL
     */
    protected void navigateToBaseUrl() {
        navigateToBaseUrl("");
    }
    
    /**
     * Navigate to base URL with specific path
     */
    protected void navigateToBaseUrl(String path) {
        getPlaywrightConfig().navigateToBaseUrl(getPage(), path);
    }
    
    /**
     * Take screenshot manually during test
     */
    protected void takeScreenshot(String fileName) {
        getPlaywrightConfig().takeScreenshot(getPage(), fileName);
    }
    
    /**
     * Wait for element to be visible
     */
    protected void waitForElement(String selector) {
        getPage().waitForSelector(selector, new Page.WaitForSelectorOptions()
            .setTimeout(getPlaywrightConfig().getDefaultTimeout()));
    }
    
    /**
     * Wait for page load
     */
    protected void waitForPageLoad() {
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
    }
    
    /**
     * Cleanup all resources
     */
    protected void cleanup() {
        try {
            closeBrowserSession();
            
            PlaywrightConfig config = playwrightConfig.get();
            if (config != null) {
                config.cleanup();
                playwrightConfig.remove();
            }
            
        } catch (Exception e) {
            logger.error("Error during cleanup", e);
        }
    }
    
    // Getter methods for accessing Playwright objects
    protected PlaywrightConfig getPlaywrightConfig() {
        PlaywrightConfig config = playwrightConfig.get();
        if (config == null) {
            config = PlaywrightConfig.getInstance();
            playwrightConfig.set(config);
        }
        return config;
    }
    
    protected Browser getBrowser() {
        Browser browserInstance = browser.get();
        if (browserInstance == null) {
            throw new IllegalStateException("Browser not initialized. Ensure setupMethod() was called.");
        }
        return browserInstance;
    }
    
    protected BrowserContext getContext() {
        BrowserContext contextInstance = context.get();
        if (contextInstance == null) {
            throw new IllegalStateException("Browser context not initialized. Ensure setupMethod() was called.");
        }
        return contextInstance;
    }
    
    protected Page getPage() {
        Page pageInstance = page.get();
        if (pageInstance == null) {
            throw new IllegalStateException("Page not initialized. Ensure setupMethod() was called.");
        }
        return pageInstance;
    }
    
    // Utility methods for common actions
    protected void click(String selector) {
        getPage().click(selector);
        logger.debug("Clicked element: {}", selector);
    }
    
    protected void type(String selector, String text) {
        getPage().fill(selector, text);
        logger.debug("Typed '{}' into element: {}", text, selector);
    }
    
    protected String getText(String selector) {
        String text = getPage().textContent(selector);
        logger.debug("Got text '{}' from element: {}", text, selector);
        return text;
    }
    
    protected boolean isVisible(String selector) {
        boolean visible = getPage().isVisible(selector);
        logger.debug("Element '{}' visible: {}", selector, visible);
        return visible;
    }
    
    protected void scrollToElement(String selector) {
        getPage().locator(selector).scrollIntoViewIfNeeded();
        logger.debug("Scrolled to element: {}", selector);
    }
} 