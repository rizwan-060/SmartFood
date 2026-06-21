package com.example.smartfood.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smartfood.data.models.GroceryItem;

import java.util.ArrayList;
import java.util.List;

public class GroceryRepository {

    // Simulating a database of average Pakistani grocery prices
    private List<GroceryItem> getMockDatabase() {
        List<GroceryItem> db = new ArrayList<>();

        // Chicken
        db.add(new GroceryItem("Chicken", 650.0, "1 kg", "Metro"));
        db.add(new GroceryItem("Chicken", 620.0, "1 kg", "Imtiaz"));

        // Milk
        db.add(new GroceryItem("Milk", 220.0, "1 Liter", "Local Dairy"));
        db.add(new GroceryItem("Milk", 280.0, "1 Liter", "Carrefour (Nestle)"));

        // Vegetables
        db.add(new GroceryItem("Onion", 150.0, "1 kg", "Sunday Market"));
        db.add(new GroceryItem("Onion", 180.0, "1 kg", "Metro"));

        db.add(new GroceryItem("Tomato", 120.0, "1 kg", "Sunday Market"));
        db.add(new GroceryItem("Tomato", 140.0, "1 kg", "Imtiaz"));

        // Pantry
        db.add(new GroceryItem("Flour (Ata)", 1400.0, "10 kg", "Imtiaz"));
        db.add(new GroceryItem("Cooking Oil", 550.0, "1 Liter", "Carrefour"));
        db.add(new GroceryItem("Eggs", 300.0, "1 Dozen", "Metro"));

        return db;
    }

    public LiveData<List<GroceryItem>> comparePrices(String ingredientQuery) {
        MutableLiveData<List<GroceryItem>> searchResults = new MutableLiveData<>();
        List<GroceryItem> matchedItems = new ArrayList<>();

        // Simulate a search query delay to act like a real network call
        new Thread(() -> {
            try {
                Thread.sleep(500); // Half-second fake loading time
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Search our mock database (case-insensitive)
            for (GroceryItem item : getMockDatabase()) {
                if (item.getIngredientName().toLowerCase().contains(ingredientQuery.toLowerCase())) {
                    matchedItems.add(item);
                }
            }

            // Post results back to the main thread
            searchResults.postValue(matchedItems);
        }).start();

        return searchResults;
    }
}