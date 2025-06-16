package pages;

import com.microsoft.playwright.Page;

public class LoginPage extends BasePage {
    // Selectors
    private static final String USERNAME_FIELD = "#user-name";
    private static final String PASSWORD_FIELD = "#password";
    private static final String LOGIN_BUTTON = "#login-button";
    private static final String ERROR_MESSAGE = "[data-test='error']";
    
    public LoginPage(Page page) {
        super(page);
    }
    
    public void navigateToLogin(String baseUrl) {
        navigateToPage(baseUrl);
    }
    
    public void login(String username, String password) {
        page.fill(USERNAME_FIELD, username);
        page.fill(PASSWORD_FIELD, password);
        page.click(LOGIN_BUTTON);
    }
    
    public String getErrorMessage() {
        return page.locator(ERROR_MESSAGE).textContent();
    }
    
    public boolean isLoginButtonEnabled() {
        return page.locator(LOGIN_BUTTON).isEnabled();
    }
        
} 