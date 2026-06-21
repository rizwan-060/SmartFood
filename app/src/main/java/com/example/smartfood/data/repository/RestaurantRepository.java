package com.example.smartfood.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smartfood.data.api.ApiClient;
import com.example.smartfood.data.api.OSMApiService;
import com.example.smartfood.data.models.OSMPlace;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantRepository {

    private static final String TAG = "RestaurantRepo";
    private OSMApiService apiService;

    public RestaurantRepository() {
        apiService = ApiClient.getOSMClient().create(OSMApiService.class);
    }

    public LiveData<List<OSMPlace>> getNearbyRestaurants(String city) {
        MutableLiveData<List<OSMPlace>> restaurantData = new MutableLiveData<>();

        // Formatting the search specifically for OSM
        String searchQuery = "restaurants in " + city;

        // No API key required! Just tell it we want JSON format.
        apiService.searchRestaurants(searchQuery, "json", 15).enqueue(new Callback<List<OSMPlace>>() {
            @Override
            public void onResponse(Call<List<OSMPlace>> call, Response<List<OSMPlace>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    restaurantData.setValue(response.body());
                    Log.d(TAG, "Successfully fetched from OpenStreetMap!");
                } else {
                    restaurantData.setValue(null);
                    Log.e(TAG, "OSM API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<OSMPlace>> call, Throwable t) {
                restaurantData.setValue(null);
                Log.e(TAG, "Network Failure: " + t.getMessage());
            }
        });

        return restaurantData;
    }
}