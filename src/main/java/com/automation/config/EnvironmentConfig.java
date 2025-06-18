package com.automation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for different environments (dev, staging, prod).
 * Loads environment-specific properties and provides access to configuration values.
 */
public class EnvironmentConfig {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfig.class);
    private static EnvironmentConfig instance;
    private final Properties properties;
    
    private EnvironmentConfig() {
        properties = new Properties();
        loadEnvironmentProperties();
    }
    
    public static synchronized EnvironmentConfig getInstance() {
        if (instance == null) {
            instance = new EnvironmentConfig();
        }
        return instance;
    }
    
    private void loadEnvironmentProperties() {
        String environment = System.getProperty("env", "dev");
        String propertiesFile = "config/" + environment + ".properties";
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (input == null) {
                logger.warn("Properties file not found: {}, using defaults", propertiesFile);
                loadDefaultProperties();
                return;
            }
            properties.load(input);
            logger.info("Loaded environment configuration for: {}", environment);
        } catch (IOException e) {
            logger.error("Error loading properties file: {}", propertiesFile, e);
            loadDefaultProperties();
        }
    }
    
    private void loadDefaultProperties() {
        properties.setProperty("base.url", "https://www.saucedemo.com");
        properties.setProperty("username", "standard_user");
        properties.setProperty("password", "secret_sauce");
    }
    
    public String getBaseUrl() { return properties.getProperty("base.url"); }
    public String getUsername() { return properties.getProperty("username"); }
    public String getPassword() { return properties.getProperty("password"); }
}
