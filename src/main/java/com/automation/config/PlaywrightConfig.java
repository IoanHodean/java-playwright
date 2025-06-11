package com.automation.config;

import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

/**
 * Playwright Configuration Manager
 * 
 * Singleton class that manages all Playwright-related configurations
 * including browser settings, timeouts, recording options, and environment-specific settings.
 * 
 * Features:
 * - Singleton thread-safe implementation
 * - Environment-specific configuration loading
 * - Browser factory with customizable options
 * - Screenshot, video, and trace recording management
 * - Comprehensive timeout and wait configurations
 */
public class PlaywrightConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(PlaywrightConfig.class);
    private static volatile PlaywrightConfig instance;
    private static final Object lock = new Object();
    
    private Properties config;
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    
    // Configuration keys
    private static final String CONFIG_FILE = "config/playwright.properties";
    
    /**
     * Private constructor for singleton pattern
     */
    private PlaywrightConfig() {
        loadConfiguration();
        initializePlaywright();
    }
    
    /**
     * Get singleton instance with thread-safe double-checked locking
     */
    public static PlaywrightConfig getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new PlaywrightConfig();
                }
            }
        }
        return instance;
    }
    
    /**
     * Load configuration from properties file
     */
    private void loadConfiguration() {
        config = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                logger.warn("Configuration file not found: {}, using defaults", CONFIG_FILE);
                loadDefaultConfiguration();
                return;
            }
            config.load(input);
            logger.info("Playwright configuration loaded successfully from: {}", CONFIG_FILE);
        } catch (IOException e) {
            logger.error("Error loading configuration file: {}", CONFIG_FILE, e);
            loadDefaultConfiguration();
        }
    }
    
    /**
     * Load default configuration if properties file is not found
     */
    private void loadDefaultConfiguration() {
        config.setProperty("browser.type", "chromium");
        config.setProperty("browser.headless", "false");
        config.setProperty("viewport.width", "1920");
        config.setProperty("viewport.height", "1080");
        config.setProperty("timeout.default", "30000");
        config.setProperty("base.url", "https://example.com");
        logger.info("Default Playwright configuration loaded");
    }
    
    /**
     * Initialize Playwright with configuration
     */
    private void initializePlaywright() {
        try {
            playwright = Playwright.create();
            logger.info("Playwright initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Playwright", e);
            throw new RuntimeException("Playwright initialization failed", e);
        }
    }
    
    /**
     * Create browser instance with configured options
     */
    public Browser createBrowser() {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
        
        // Basic browser options
        launchOptions.setHeadless(getBooleanProperty("browser.headless", false));
        launchOptions.setSlowMo(getIntProperty("browser.slowMotion", 0));
        
        // Browser arguments
        String browserArgs = getProperty("browser.args", "");
        if (!browserArgs.isEmpty()) {
            launchOptions.setArgs(Arrays.asList(browserArgs.split(",")));
        }
        
        // Browser type selection
        String browserType = getProperty("browser.type", "chromium").toLowerCase();
        
        try {
            switch (browserType) {
                case "firefox":
                    browser = playwright.firefox().launch(launchOptions);
                    break;
                case "webkit":
                    browser = playwright.webkit().launch(launchOptions);
                    break;
                case "chrome":
                    launchOptions.setChannel("chrome");
                    browser = playwright.chromium().launch(launchOptions);
                    break;
                case "msedge":
                    launchOptions.setChannel("msedge");
                    browser = playwright.chromium().launch(launchOptions);
                    break;
                case "chromium":
                default:
                    browser = playwright.chromium().launch(launchOptions);
                    break;
            }
            
            logger.info("Browser created successfully: {} (headless: {})", 
                browserType, launchOptions.headless);
            return browser;
            
        } catch (Exception e) {
            logger.error("Failed to create browser: {}", browserType, e);
            throw new RuntimeException("Browser creation failed", e);
        }
    }
    
    /**
     * Create browser context with configured options
     */
    public BrowserContext createContext(Browser browser) {
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
        
        // Viewport configuration
        contextOptions.setViewportSize(
            getIntProperty("viewport.width", 1920),
            getIntProperty("viewport.height", 1080)
        );
        
        // Security options
        contextOptions.setIgnoreHTTPSErrors(getBooleanProperty("browser.ignoreHTTPSErrors", true));
        contextOptions.setAcceptDownloads(getBooleanProperty("browser.acceptDownloads", true));
        
        // Recording options
        setupRecordingOptions(contextOptions);
        
        // Locale and timezone
        contextOptions.setLocale(getProperty("locale", "en-US"));
        // Skip timezone setting for now to avoid compatibility issues
        
        try {
            context = browser.newContext(contextOptions);
            
            // Set default timeouts
            context.setDefaultTimeout(getIntProperty("timeout.default", 30000));
            context.setDefaultNavigationTimeout(getIntProperty("timeout.navigation", 60000));
            
            logger.info("Browser context created successfully with viewport: {}x{}", 
                getIntProperty("viewport.width", 1920),
                getIntProperty("viewport.height", 1080));
            
            return context;
            
        } catch (Exception e) {
            logger.error("Failed to create browser context", e);
            throw new RuntimeException("Browser context creation failed", e);
        }
    }
    
    /**
     * Setup recording options for context
     */
    private void setupRecordingOptions(Browser.NewContextOptions contextOptions) {
        // Video recording
        String videoRecord = getProperty("video.record", "off");
        if (!"off".equals(videoRecord)) {
            contextOptions.setRecordVideoDir(Paths.get(getProperty("video.path", "target/videos")));
            contextOptions.setRecordVideoSize(
                getIntProperty("video.size.width", 1024),
                getIntProperty("video.size.height", 768)
            );
        }
        
        // Trace recording
        String traceRecord = getProperty("trace.record", "off");
        if (!"off".equals(traceRecord)) {
            // Trace recording will be started/stopped in test methods
            logger.info("Trace recording configured: {}", traceRecord);
        }
    }
    
    /**
     * Create new page with configured options
     */
    public Page createPage() {
        if (context == null) {
            throw new IllegalStateException("Browser context not initialized. Call createContext() first.");
        }
        
        Page page = context.newPage();
        
        // Configure page timeouts
        page.setDefaultTimeout(getIntProperty("timeout.action", 5000));
        page.setDefaultNavigationTimeout(getIntProperty("timeout.navigation", 60000));
        
        logger.info("New page created successfully");
        return page;
    }
    
    /**
     * Navigate to base URL or specific path
     */
    public void navigateToBaseUrl(Page page, String path) {
        String baseUrl = getBaseUrl();
        String fullUrl = path.isEmpty() ? baseUrl : baseUrl + (path.startsWith("/") ? path : "/" + path);
        
        try {
            page.navigate(fullUrl);
            logger.info("Navigated to: {}", fullUrl);
        } catch (Exception e) {
            logger.error("Navigation failed to: {}", fullUrl, e);
            throw new RuntimeException("Navigation failed", e);
        }
    }
    
    /**
     * Get base URL based on environment
     */
    public String getBaseUrl() {
        String environment = getProperty("environment", "dev");
        String urlKey = "app." + environment + ".url";
        String url = getProperty(urlKey, getProperty("base.url", "https://example.com"));
        logger.debug("Base URL for environment '{}': {}", environment, url);
        return url;
    }
    
    /**
     * Take screenshot with configured options
     */
    public void takeScreenshot(Page page, String fileName) {
        try {
            String screenshotPath = getProperty("screenshot.path", "target/screenshots");
            boolean fullPage = getBooleanProperty("screenshot.fullpage", true);
            
            Page.ScreenshotOptions options = new Page.ScreenshotOptions();
            options.setPath(Paths.get(screenshotPath, fileName + ".png"));
            options.setFullPage(fullPage);
            
            page.screenshot(options);
            logger.info("Screenshot saved: {}/{}.png", screenshotPath, fileName);
            
        } catch (Exception e) {
            logger.error("Failed to take screenshot: {}", fileName, e);
        }
    }
    
    /**
     * Start tracing if configured
     */
    public void startTrace(BrowserContext context, String traceName) {
        String traceRecord = getProperty("trace.record", "off");
        if (!"off".equals(traceRecord)) {
            try {
                Tracing.StartOptions options = new Tracing.StartOptions();
                options.setScreenshots(getBooleanProperty("trace.screenshots", true));
                options.setSnapshots(getBooleanProperty("trace.snapshots", true));
                
                context.tracing().start(options);
                logger.info("Trace recording started: {}", traceName);
                
            } catch (Exception e) {
                logger.error("Failed to start trace recording: {}", traceName, e);
            }
        }
    }
    
    /**
     * Stop tracing and save
     */
    public void stopTrace(BrowserContext context, String traceName) {
        String traceRecord = getProperty("trace.record", "off");
        if (!"off".equals(traceRecord)) {
            try {
                String tracePath = getProperty("trace.path", "target/traces");
                context.tracing().stop(new Tracing.StopOptions()
                    .setPath(Paths.get(tracePath, traceName + ".zip")));
                
                logger.info("Trace recording saved: {}/{}.zip", tracePath, traceName);
                
            } catch (Exception e) {
                logger.error("Failed to stop trace recording: {}", traceName, e);
            }
        }
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        try {
            if (context != null) {
                context.close();
                logger.info("Browser context closed");
            }
            if (browser != null) {
                browser.close();
                logger.info("Browser closed");
            }
            if (playwright != null) {
                playwright.close();
                logger.info("Playwright closed");
            }
        } catch (Exception e) {
            logger.error("Error during cleanup", e);
        }
    }
    
    // Utility methods for property access
    public String getProperty(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }
    
    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(config.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer property {}, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }
    
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return Boolean.parseBoolean(config.getProperty(key, String.valueOf(defaultValue)));
    }
    
    // Getters for configured values
    public String getBrowserType() { return getProperty("browser.type", "chromium"); }
    public boolean isHeadless() { return getBooleanProperty("browser.headless", false); }
    public int getDefaultTimeout() { return getIntProperty("timeout.default", 30000); }
    public int getParallelWorkers() { return getIntProperty("parallel.workers", 4); }
    public int getRetryCount() { return getIntProperty("retry.count", 2); }
    public String getEnvironment() { return getProperty("environment", "dev"); }
} 