package com.example.smartfood.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smartfood.data.api.ApiClient;
import com.example.smartfood.data.api.EdamamApiService;
import com.example.smartfood.data.models.NutritionResponse;
import com.example.smartfood.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NutritionRepository {
    private static final String TAG = "NutritionRepo";
    private EdamamApiService apiService;

    public NutritionRepository(){
        apiService= ApiClient.getEdamamClient().create(EdamamApiService.class);
    }

    public LiveData<NutritionResponse> getNutritionData(String ingredient) {
        // Create an empty container to hold the nutrition data
        MutableLiveData<NutritionResponse> nutritionData = new MutableLiveData<>();

        // Make the background API call to Edamam
        apiService.getNutritionData(Constants.EDAMAM_APP_ID, Constants.EDAMAM_APP_KEY, ingredient)
                .enqueue(new Callback<NutritionResponse>() {

                    @Override
                    public void onResponse(Call<NutritionResponse> call, Response<NutritionResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Success! Put the nutrition data into our LiveData container
                            nutritionData.setValue(response.body());
                            Log.d(TAG, "Successfully fetched nutrition for: " + ingredient);
                        } else {
                            // API Error (e.g., 401 Unauthorized, bad request)
                            nutritionData.setValue(null);
                            Log.e(TAG, "API Error Code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<NutritionResponse> call, Throwable t) {
                        // Network Error (e.g., no internet connection)
                        nutritionData.setValue(null);
                        Log.e(TAG, "Network Failure: " + t.getMessage());
                    }
                });

        // Return the container immediately
        return nutritionData;
    }
}
