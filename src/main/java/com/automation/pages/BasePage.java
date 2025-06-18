package com.automation.pages;

import com.microsoft.playwright.Page;
import com.automation.config.PlaywrightConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasePage {
    protected final Page page;
    protected final PlaywrightConfig playwrightConfig;
    private static final Logger logger = LoggerFactory.getLogger(BasePage.class);
    
    public BasePage(Page page) {
        this.page = page;
        this.playwrightConfig = PlaywrightConfig.getInstance();
    }
    
    // Common navigation method
    protected void navigateToPage(String url) {
        logger.info("Navigating to: {}", url);
        page.navigate(url);
    }
    
    // Common screenshot method
    protected void takeScreenshot(String name) {
        playwrightConfig.takeScreenshot(page, name);
    }
} 