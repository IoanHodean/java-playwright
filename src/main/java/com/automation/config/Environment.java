package com.automation.config;

/**
 * Environment enumeration for managing different test environments.
 * Provides environment-specific configuration and validation.
 */
public enum Environment {
    LOCAL("local", "Local Development Environment", "http://localhost:8080"),
    DEV("dev", "Development Environment", "https://jsonplaceholder.typicode.com"),
    STAGING("staging", "Staging Environment - Pre-Production", "https://staging-api.company.com"),
    PROD("prod", "Production Environment", "https://api.company.com");
    
    private final String name;
    private final String description;
    private final String defaultBaseUrl;
    
    Environment(String name, String description, String defaultBaseUrl) {
        this.name = name;
        this.description = description;
        this.defaultBaseUrl = defaultBaseUrl;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getDefaultBaseUrl() {
        return defaultBaseUrl;
    }
    
    /**
     * Get environment from system property or environment variable.
     * Default to DEV if not specified.
     */
    public static Environment getCurrentEnvironment() {
        String envName = System.getProperty("test.environment");
        if (envName == null) {
            envName = System.getenv("TEST_ENVIRONMENT");
        }
        if (envName == null) {
            envName = "dev"; // Default environment
        }
        
        return fromString(envName);
    }
    
    /**
     * Convert string to Environment enum.
     */
    public static Environment fromString(String envName) {
        if (envName == null) {
            return DEV;
        }
        
        for (Environment env : Environment.values()) {
            if (env.getName().equalsIgnoreCase(envName.trim())) {
                return env;
            }
        }
        
        throw new IllegalArgumentException("Unknown environment: " + envName + 
            ". Valid environments are: LOCAL, DEV, STAGING, PROD");
    }
    
    /**
     * Check if performance testing is allowed in this environment.
     */
    public boolean isPerformanceTestingAllowed() {
        return this != PROD; // Don't run performance tests in production
    }
    
    /**
     * Check if this is a production environment.
     */
    public boolean isProduction() {
        return this == PROD;
    }
    
    /**
     * Get environment configuration file path.
     */
    public String getConfigFilePath() {
        return "config/environments/" + name + ".properties";
    }
    
    @Override
    public String toString() {
        return name + " (" + description + ")";
    }
} 