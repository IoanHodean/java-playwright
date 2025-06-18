package com.automation.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * API Configuration manager for JSONPlaceholder API testing.
 * Simplified singleton pattern for essential API settings.
 */
public class ApiConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApiConfig.class);
    private static ApiConfig instance;
    private final Properties properties;
    
    private ApiConfig() {
        properties = new Properties();
        loadApiProperties();
    }
    
    public static synchronized ApiConfig getInstance() {
        if (instance == null) {
            instance = new ApiConfig();
        }
        return instance;
    }
    
    private void loadApiProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config/api.properties")) {
            if (input == null) {
                logger.warn("API properties file not found, using defaults");
                loadDefaultProperties();
                return;
            }
            properties.load(input);
            logger.info("API configuration loaded successfully");
        } catch (IOException e) {
            logger.error("Error loading API properties file", e);
            loadDefaultProperties();
        }
    }
    
    private void loadDefaultProperties() {
        properties.setProperty("api.jsonplaceholder.base.url", "https://jsonplaceholder.typicode.com");
        properties.setProperty("api.connection.timeout", "10000");
        properties.setProperty("api.response.timeout", "30000");
        properties.setProperty("api.schema.validation.enabled", "true");
        logger.info("Default API configuration loaded");
    }
    
    // Essential configuration getters
    public String getBaseUrl() {
        return getProperty("api.jsonplaceholder.base.url", "https://jsonplaceholder.typicode.com");
    }
    
    public int getConnectionTimeout() {
        return getIntProperty("api.connection.timeout", 10000);
    }
    
    public int getResponseTimeout() {
        return getIntProperty("api.response.timeout", 30000);
    }
    
    public boolean isSchemaValidationEnabled() {
        return getBooleanProperty("api.schema.validation.enabled", true);
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