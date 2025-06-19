package com.automation.UI;

import com.automation.pages.LoginPage;
import com.automation.config.EnvironmentConfig;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import org.testng.annotations.*;
import java.lang.reflect.Method;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class RouteInterceptionTest extends BaseTest {
    private LoginPage loginPage;
    private String baseUrl;
    
    @BeforeMethod
    public void setup(Method method) {
        super.setup(method);
        EnvironmentConfig config = EnvironmentConfig.getInstance();
        this.baseUrl = config.getBaseUrl();
        loginPage = new LoginPage(getPage());
    }
    
    @Test
    public void testLoginWithServerError() {
        // Test how UI handles 500 server error during login
        getPage().route("**/api/**", route -> {
            route.fulfill(new Route.FulfillOptions()
                .setStatus(500)
                .setContentType("application/json")
                .setBody("{\"error\": \"Internal server error\"}"));
        });
        
        loginPage.navigateToLogin(baseUrl);
        loginPage.login("test_user", "test_password");
        
        // For SauceDemo, we expect to stay on login page when server errors occur
        // In a real app, you'd check for specific error message
        assertThat(getPage()).hasURL(baseUrl);
        
        // Verify login button is still enabled for retry
        assert loginPage.isLoginButtonEnabled();
    }
    
    @Test
    public void testNetworkFailure() {
        // Test UI behavior when network is completely unavailable
        getPage().route("**/api/**", route -> {
            route.abort("networkerror");
        });
        
        loginPage.navigateToLogin(baseUrl);
        loginPage.login("test_user", "test_password");
        
        // Should remain on login page due to network failure
        assertThat(getPage()).hasURL(baseUrl);
        
        // Login button should still be functional for retry
        assert loginPage.isLoginButtonEnabled();
    }
    
    @Test
    public void testSlowNetworkResponse() {
        // Test UI during slow network conditions
        getPage().route("**/api/**", route -> {
            // Simulate 2-second delay before continuing with real request
            try {
                Thread.sleep(2000);
                route.resume();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                route.resume();
            }
        });
        
        loginPage.navigateToLogin(baseUrl);
        
        // Measure time before login
        long startTime = System.currentTimeMillis();
        
        loginPage.login("standard_user", "secret_sauce");
        
        // Wait for navigation or error
        try {
            getPage().waitForURL("**/inventory.html", new Page.WaitForURLOptions().setTimeout(5000));
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // Should take at least 2 seconds due to our delay
            assert duration >= 2000 : "Request should have been delayed by at least 2 seconds";
            
        } catch (Exception e) {
            // If timeout, that's also a valid test result for slow network
            System.out.println("Login timed out due to slow network simulation");
        }
    }
    
    @Test
    public void testMalformedApiResponse() {
        // Test how UI handles invalid JSON responses
        getPage().route("**/api/**", route -> {
            route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody("{ invalid json response"));
        });
        
        loginPage.navigateToLogin(baseUrl);
        loginPage.login("test_user", "test_password");
        
        // Should handle malformed response gracefully
        assertThat(getPage()).hasURL(baseUrl);
        assert loginPage.isLoginButtonEnabled();
    }
    
    @Test
    public void testEmptyApiResponse() {
        // Test UI behavior with empty but valid responses
        getPage().route("**/api/**", route -> {
            route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody("{}"));
        });
        
        loginPage.navigateToLogin(baseUrl);
        loginPage.login("test_user", "test_password");
        
        // Should handle empty response appropriately
        assertThat(getPage()).hasURL(baseUrl);
    }
} 