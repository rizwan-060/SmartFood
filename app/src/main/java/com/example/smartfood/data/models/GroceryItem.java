package com.example.smartfood.data.models;

public class GroceryItem {
    private String ingredientName;
    private double price;
    private String unit;
    private String storeName;

    public GroceryItem(String ingredientName, double price, String unit, String storeName) {
        this.ingredientName = ingredientName;
        this.price = price;
        this.unit = unit;
        this.storeName = storeName;
    }

    public String getIngredientName() { return ingredientName; }
    public double getPrice() { return price; }
    public String getUnit() { return unit; }
    public String getStoreName() { return storeName; }
}