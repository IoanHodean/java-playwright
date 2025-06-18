package com.automation.config;

import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Handles browser setup and config
 * 
 * Keeps track of browser settings, timeouts, and other Playwright stuff.
 * Using singleton pattern to avoid multiple instances causing issues.
 */
public class PlaywrightConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(PlaywrightConfig.class);
    private static PlaywrightConfig instance;
    private final Playwright playwright;
    private final Browser browser;
    private BrowserContext context;
    
    private PlaywrightConfig() {
        playwright = Playwright.create();
        browser = createBrowser();
        logger.info("Playwright configuration initialized");
    }
    
    public static synchronized PlaywrightConfig getInstance() {
        if (instance == null) {
            instance = new PlaywrightConfig();
        }
        return instance;
    }
    
    private Browser createBrowser() {
        BrowserType browserType = playwright.chromium();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
            .setHeadless(false)
            .setSlowMo(100); // For debugging
        
        return browserType.launch(launchOptions);
    }
    
    public Page createPage() {
        if (context == null) {
            context = createContext();
        }
        
        Page page = context.newPage();
        page.setDefaultTimeout(30000);
        page.setDefaultNavigationTimeout(60000);
        
        return page;
    }
    
    private BrowserContext createContext() {
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
            .setViewportSize(1920, 1080)
            .setIgnoreHTTPSErrors(true)
            .setLocale("en-US");
        
        // Video recording
        contextOptions.setRecordVideoDir(Paths.get("target/videos"));
        contextOptions.setRecordVideoSize(1024, 768);
        
        return browser.newContext(contextOptions);
    }
    
    public void startTrace(String traceName) {
        if (context != null) {
            try {
                Tracing.StartOptions options = new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true);
                
                context.tracing().start(options);
                logger.info("Trace recording started: {}", traceName);
            } catch (Exception e) {
                logger.error("Failed to start trace recording: {}", traceName, e);
            }
        }
    }
    
    public void stopTrace(String traceName) {
        if (context != null) {
            try {
                String tracePath = "target/traces";
                context.tracing().stop(new Tracing.StopOptions()
                    .setPath(Paths.get(tracePath, traceName + ".zip")));
                
                logger.info("Trace recording saved: {}/{}.zip", tracePath, traceName);
            } catch (Exception e) {
                logger.error("Failed to stop trace recording: {}", traceName, e);
            }
        }
    }
    
    public void takeScreenshot(Page page, String fileName) {
        try {
            String screenshotPath = "target/screenshots";
            Page.ScreenshotOptions options = new Page.ScreenshotOptions()
                .setPath(Paths.get(screenshotPath, fileName + ".png"))
                .setFullPage(true);
            
            page.screenshot(options);
            logger.info("Screenshot saved: {}/{}.png", screenshotPath, fileName);
        } catch (Exception e) {
            logger.error("Failed to take screenshot: {}", fileName, e);
        }
    }
    
    public void cleanup() {
        try {
            if (context != null) {
                context.close();
                context = null;
                logger.info("Browser context closed");
            }
        } catch (Exception e) {
            logger.error("Error during cleanup", e);
        }
    }
    
    /**
     * Load settings from properties file
     * Falls back to defaults if file not found
     */
    private void loadConfiguration() {
        Properties config = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config/playwright.properties")) {
            if (input == null) {
                logger.warn("Configuration file not found: {}, using defaults", "config/playwright.properties");
                loadDefaultConfiguration();
                return;
            }
            config.load(input);
            logger.info("Playwright configuration loaded successfully from: {}", "config/playwright.properties");
        } catch (IOException e) {
            logger.error("Error loading configuration file: {}", "config/playwright.properties", e);
            loadDefaultConfiguration();
        }
    }
    
    /**
     * Set up default values if config file is missing
     */
    private void loadDefaultConfiguration() {
        // Implementation of loadDefaultConfiguration method
    }
    
    /**
     * Create a new browser with our settings
     * Supports Chrome, Firefox, and WebKit
     */
    public Browser createBrowser(BrowserType.LaunchOptions launchOptions) {
        // Implementation of createBrowser method
        return null; // Placeholder return, actual implementation needed
    }
    
    /**
     * Set up a new browser context with our viewport and other settings
     */
    public BrowserContext createContext(Browser browser) {
        // Implementation of createContext method
        return null; // Placeholder return, actual implementation needed
    }
    
    /**
     * Setup recording options for context
     */
    private void setupRecordingOptions(Browser.NewContextOptions contextOptions) {
        // Implementation of setupRecordingOptions method
    }
    
    /**
     * Go to a URL - handles both relative and absolute paths
     */
    public void navigateToBaseUrl(Page page, String path) {
        // Implementation of navigateToBaseUrl method
    }
    
    /**
     * Get the base URL for the current environment
     * Falls back to example.com if not set
     */
    public String getBaseUrl() {
        // Implementation of getBaseUrl method
        return null; // Placeholder return, actual implementation needed
    }
    
    // Helper methods for getting config values
    public String getProperty(String key, String defaultValue) {
        // Implementation of getProperty method
        return null; // Placeholder return, actual implementation needed
    }
    
    public int getIntProperty(String key, int defaultValue) {
        // Implementation of getIntProperty method
        return 0; // Placeholder return, actual implementation needed
    }
    
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        // Implementation of getBooleanProperty method
        return false; // Placeholder return, actual implementation needed
    }
    
    // Getters for configured values
    public String getBrowserType() { return getProperty("browser.type", "chromium"); }
    public boolean isHeadless() { return getBooleanProperty("browser.headless", false); }
    public int getDefaultTimeout() { return getIntProperty("timeout.default", 30000); }
    public int getParallelWorkers() { return getIntProperty("parallel.workers", 4); }
    public int getRetryCount() { return getIntProperty("retry.count", 2); }
    public String getEnvironment() { return getProperty("environment", "dev"); }
} 