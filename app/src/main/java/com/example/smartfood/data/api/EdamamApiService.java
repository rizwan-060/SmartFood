package com.example.smartfood.data.api;

import com.example.smartfood.data.models.NutritionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EdamamApiService {

    // Endpoint: Get nutrition data for a specific ingredient/food
    // Example: https://api.edamam.com/api/nutrition-data?app_id=XXX&app_key=XXX&ingr=1%20large%20apple
    @GET("api/nutrition-data")
    Call<NutritionResponse> getNutritionData(
            @Query("app_id") String appId,
            @Query("app_key") String appKey,
            @Query("ingr") String ingredient // e.g., "100g chicken" or "1 large apple"
    );
}