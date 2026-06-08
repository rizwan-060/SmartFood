package com.example.smartfood.data.models;

import com.google.gson.annotations.SerializedName;

public class NutrientInfo {
    @SerializedName("label")

    private String label;
    @SerializedName("quantity")
    private double quantity;
    @SerializedName("unit")
    private String unit;

    public String getLabel(){
        return label;
    }
    public double getQuantity(){
        return quantity;
    }
    public String getUnit(){
        return unit;
    }
}
