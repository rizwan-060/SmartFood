package com.example.smartfood.data.models;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class NutritionResponse {
    @SerializedName("calories")
    private int calories;
    @SerializedName("totalWeight")
    private double totalWeight;

    @SerializedName("totalNutrients")
    private Map<String,NutrientInfo> totalNutrients;

    public int getCalories() {
        return calories;
    }
    public double getTotalWeight() {
        return totalWeight;
    }

    public Map<String,NutrientInfo> getTotalNutrients() {
        return totalNutrients;
    }



}
