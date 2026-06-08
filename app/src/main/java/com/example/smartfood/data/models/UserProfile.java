package com.example.smartfood.data.models;

public class UserProfile {
    private String userId;
    private String name;
    private String dietaryPreference;
    public UserProfile(){}
    public UserProfile(String userId, String name, String dietaryPreference) {
        this.userId = userId;
        this.name = name;
        this.dietaryPreference = dietaryPreference;
    }
    public String getUserId() {
        return userId;
    }
    public String getName() {
        return name;
    }
    public String getDietaryPreference() {
        return dietaryPreference;
    }
}
