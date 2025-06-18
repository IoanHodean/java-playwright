package com.automation.UI;

import com.automation.pages.LoginPage;
import com.automation.config.EnvironmentConfig;
import org.testng.annotations.*;
import java.lang.reflect.Method;

public class LoginTest extends BaseTest {
    private LoginPage loginPage;
    private String baseUrl;
    private String username;
    private String password;
    
    @BeforeMethod
    public void setup(Method method) {
        super.setup(method);
        EnvironmentConfig config = EnvironmentConfig.getInstance();
        
        // Store values once
        this.baseUrl = config.getBaseUrl();
        this.username = config.getUsername();
        this.password = config.getPassword();
        
        loginPage = new LoginPage(getPage());
    }
    
    @Test
    public void testSuccessfulLogin() {
        // Navigate to login page
        loginPage.navigateToLogin(baseUrl);
        
        // Perform login with valid credentials
        loginPage.login(username, password);
        
        // Verify successful login - SauceDemo redirects to inventory page
        assert getPage().url().contains("inventory.html");
    }
    
    @Test
    public void testFailedLogin() {
        // Navigate to login page
        loginPage.navigateToLogin(baseUrl);
        
        // Perform login with invalid credentials
        loginPage.login("invalid_user", "wrong_password");
        
        // Verify error message
        String errorMessage = loginPage.getErrorMessage();
        assert errorMessage.contains("Username and password do not match");
    }
} 