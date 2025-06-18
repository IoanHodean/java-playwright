package com.automation.api.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request model for creating users in JSONPlaceholder API.
 * Used for POST /users requests.
 */
public class CreateUserRequest {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("website")
    private String website;
    
    // Default constructor
    public CreateUserRequest() {}
    
    // Constructor with essential fields
    public CreateUserRequest(String name, String username, String email) {
        this.name = name;
        this.username = username;
        this.email = email;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    // Builder pattern
    public static CreateUserRequestBuilder builder() {
        return new CreateUserRequestBuilder();
    }
    
    public static class CreateUserRequestBuilder {
        private CreateUserRequest request = new CreateUserRequest();
        
        public CreateUserRequestBuilder name(String name) {
            request.setName(name);
            return this;
        }
        
        public CreateUserRequestBuilder username(String username) {
            request.setUsername(username);
            return this;
        }
        
        public CreateUserRequestBuilder email(String email) {
            request.setEmail(email);
            return this;
        }
        
        public CreateUserRequestBuilder phone(String phone) {
            request.setPhone(phone);
            return this;
        }
        
        public CreateUserRequestBuilder website(String website) {
            request.setWebsite(website);
            return this;
        }
        
        public CreateUserRequest build() {
            return request;
        }
    }
    
    @Override
    public String toString() {
        return "CreateUserRequest{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", website='" + website + '\'' +
                '}';
    }
} 