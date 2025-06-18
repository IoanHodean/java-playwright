package com.automation.api.config;

import com.automation.config.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Enhanced API Configuration manager with multi-environment support.
 * Provides environment-specific configuration and feature flags.
 */
public class ApiConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApiConfig.class);
    private static ApiConfig instance;
    private final Properties properties;
    private final Environment currentEnvironment;
    
    private ApiConfig() {
        this.currentEnvironment = Environment.getCurrentEnvironment();
        this.properties = new Properties();
        loadApiProperties();
        logEnvironmentInfo();
    }
    
    public static synchronized ApiConfig getInstance() {
        if (instance == null) {
            instance = new ApiConfig();
        }
        return instance;
    }
    
    private void loadApiProperties() {
        // First load default api.properties
        loadDefaultApiProperties();
        
        // Then load environment-specific properties
        loadEnvironmentProperties();
        
        // Finally, apply any system property overrides
        applySystemPropertyOverrides();
    }
    
    private void loadDefaultApiProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config/api.properties")) {
            if (input != null) {
                properties.load(input);
                logger.debug("Default API properties loaded");
            }
        } catch (IOException e) {
            logger.warn("Could not load default API properties", e);
        }
    }
    
    private void loadEnvironmentProperties() {
        String envConfigPath = currentEnvironment.getConfigFilePath();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(envConfigPath)) {
            if (input == null) {
                logger.warn("Environment properties file not found: {}, using defaults", envConfigPath);
                loadDefaultProperties();
                return;
            }
            
            Properties envProperties = new Properties();
            envProperties.load(input);
            
            // Merge environment properties (they override defaults)
            properties.putAll(envProperties);
            
            logger.info("Environment configuration loaded successfully for: {}", currentEnvironment);
        } catch (IOException e) {
            logger.error("Error loading environment properties file: {}", envConfigPath, e);
            loadDefaultProperties();
        }
    }
    
    private void applySystemPropertyOverrides() {
        // Allow system properties to override any configuration
        System.getProperties().stringPropertyNames().forEach(key -> {
            if (key.startsWith("api.") || key.startsWith("db.") || key.startsWith("performance.")) {
                String value = System.getProperty(key);
                properties.setProperty(key, value);
                logger.debug("Applied system property override: {} = {}", key, value);
            }
        });
    }
    
    private void loadDefaultProperties() {
        properties.setProperty("api.base.url", currentEnvironment.getDefaultBaseUrl());
        properties.setProperty("api.connection.timeout", "10000");
        properties.setProperty("api.response.timeout", "30000");
        properties.setProperty("api.schema.validation.enabled", "true");
        properties.setProperty("performance.testing.enabled", "true");
        properties.setProperty("database.validation.enabled", "false");
        properties.setProperty("security.testing.enabled", "false");
        logger.info("Default API configuration loaded for environment: {}", currentEnvironment);
    }
    
    private void logEnvironmentInfo() {
        logger.info("=== ENVIRONMENT CONFIGURATION ===");
        logger.info("Environment: {}", currentEnvironment);
        logger.info("Base URL: {}", getBaseUrl());
        logger.info("Connection Timeout: {}ms", getConnectionTimeout());
        logger.info("Response Timeout: {}ms", getResponseTimeout());
        logger.info("Schema Validation: {}", isSchemaValidationEnabled());
        logger.info("Performance Testing: {}", isPerformanceTestingEnabled());
        logger.info("Database Validation: {}", isDatabaseValidationEnabled());
        logger.info("Security Testing: {}", isSecurityTestingEnabled());
        logger.info("=====================================");
    }
    
    // Environment getters
    public Environment getCurrentEnvironment() {
        return currentEnvironment;
    }
    
    public String getEnvironmentName() {
        return currentEnvironment.getName();
    }
    
    public String getEnvironmentDescription() {
        return currentEnvironment.getDescription();
    }
    
    // Essential configuration getters
    public String getBaseUrl() {
        return getProperty("api.base.url", currentEnvironment.getDefaultBaseUrl());
    }
    
    public int getConnectionTimeout() {
        return getIntProperty("api.connection.timeout", 10000);
    }
    
    public int getResponseTimeout() {
        return getIntProperty("api.response.timeout", 30000);
    }
    
    public int getRetryCount() {
        return getIntProperty("api.retry.count", 3);
    }
    
    // Feature flags
    public boolean isSchemaValidationEnabled() {
        return getBooleanProperty("api.schema.validation.enabled", true);
    }
    
    public boolean isPerformanceTestingEnabled() {
        return getBooleanProperty("performance.testing.enabled", true) && 
               currentEnvironment.isPerformanceTestingAllowed();
    }
    
    public boolean isDatabaseValidationEnabled() {
        return getBooleanProperty("database.validation.enabled", false);
    }
    
    public boolean isSecurityTestingEnabled() {
        return getBooleanProperty("security.testing.enabled", false);
    }
    
    // Performance testing configuration
    public int getPerformanceThreadCount() {
        return getIntProperty("performance.thread.count", 5);
    }
    
    public int getPerformanceRampUp() {
        return getIntProperty("performance.ramp.up", 10);
    }
    
    public int getPerformanceDuration() {
        return getIntProperty("performance.duration", 60);
    }
    
    // Database configuration
    public String getDatabaseHost() {
        return getProperty("db.host", "localhost");
    }
    
    public int getDatabasePort() {
        return getIntProperty("db.port", 5432);
    }
    
    public String getDatabaseName() {
        return getProperty("db.name", "test_automation");
    }
    
    public String getDatabaseUsername() {
        return getProperty("db.username", "testuser");
    }
    
    public String getDatabasePassword() {
        return getProperty("db.password", "testpass");
    }
    
    // Logging configuration
    public String getLoggingLevel() {
        return getProperty("logging.level", "INFO");
    }
    
    public boolean isApiRequestLoggingEnabled() {
        return getBooleanProperty("logging.api.requests", true);
    }
    
    public boolean isApiResponseLoggingEnabled() {
        return getBooleanProperty("logging.api.responses", false);
    }
    
    // Helper methods
    private String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value != null) {
            return value;
        }
        return properties.getProperty(key, defaultValue);
    }
    
    private int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for property: {}, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }
    
    private boolean getBooleanProperty(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getProperty(key, String.valueOf(defaultValue)));
    }
} 