package config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentConfig {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfig.class);
    private static EnvironmentConfig instance;
    private final Dotenv dotenv;
    
    private EnvironmentConfig() {
        dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
        logger.info("Environment configuration loaded");
        
        // Debug: Print loaded values
        logger.info("BASE_URL: {}", dotenv.get("BASE_URL"));
        logger.info("APP_USERNAME: {}", dotenv.get("APP_USERNAME"));
        logger.info("APP_PASSWORD: {}", dotenv.get("APP_PASSWORD") != null ? "***" : "null");
    }
    
    public static synchronized EnvironmentConfig getInstance() {
        if (instance == null) {
            instance = new EnvironmentConfig();
        }
        return instance;
    }
    
    public String getBaseUrl() {
        return dotenv.get("BASE_URL");
    }
    
    public String getUsername() {
        return dotenv.get("APP_USERNAME");
    }
    
    public String getPassword() {
        return dotenv.get("APP_PASSWORD");
    }
}
