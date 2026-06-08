package com.example.smartfood.data.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecipeResponse {

    @SerializedName("results")
    private List<Recipe> results;
    @SerializedName("recipes")
    private List<Recipe> recipes;
    public List<Recipe> getResults() {
        return results;
    }
    public List<Recipe> getRecipes() {
        return recipes;
    }
}