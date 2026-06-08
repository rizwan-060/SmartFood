package com.example.smartfood.data.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Recipe {
    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String title;
    @SerializedName("image")
    private String image;

    // --- NEW DETAIL FIELDS ---
    @SerializedName("readyInMinutes")
    private int readyInMinutes;
    @SerializedName("servings")
    private int servings;
    @SerializedName("extendedIngredients")
    private List<Ingredient> extendedIngredients;
    @SerializedName("analyzedInstructions")
    private List<Instruction> analyzedInstructions;
    @SerializedName("nutrition")
    private Nutrition nutrition;

    // --- GETTERS ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getImage() { return image; }
    public int getReadyInMinutes() { return readyInMinutes; }
    public int getServings() { return servings; }
    public List<Ingredient> getExtendedIngredients() { return extendedIngredients; }
    public List<Instruction> getAnalyzedInstructions() { return analyzedInstructions; }
    public Nutrition getNutrition() { return nutrition; }

    // --- NESTED CLASSES FOR COMPLEX JSON ---
    public static class Ingredient {
        @SerializedName("original")
        private String original;
        public String getOriginal() { return original; }
    }

    public static class Instruction {
        @SerializedName("steps")
        private List<Step> steps;
        public List<Step> getSteps() { return steps; }
    }

    public static class Step {
        @SerializedName("number")
        private int number;
        @SerializedName("step")
        private String stepText;
        public int getNumber() { return number; }
        public String getStepText() { return stepText; }
    }

    public static class Nutrition {
        @SerializedName("nutrients")
        private List<Nutrient> nutrients;
        public List<Nutrient> getNutrients() { return nutrients; }
    }

    public static class Nutrient {
        @SerializedName("name")
        private String name;
        @SerializedName("amount")
        private double amount;
        @SerializedName("unit")
        private String unit;
        public String getName() { return name; }
        public double getAmount() { return amount; }
        public String getUnit() { return unit; }
    }
}