package com.example.smartfood.data.models;

import com.google.gson.annotations.SerializedName;

public class SpoonacularImageResponse {

    @SerializedName("category")
    private String category;

    @SerializedName("probability")
    private double probability;

    public String getCategory() {
        return category;
    }

    public double getProbability() {
        return probability;
    }
}