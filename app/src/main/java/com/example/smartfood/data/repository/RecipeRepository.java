package com.example.smartfood.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smartfood.data.api.ApiClient;
import com.example.smartfood.data.api.SpoonacularApiService;
import com.example.smartfood.data.models.Recipe;
import com.example.smartfood.data.models.RecipeResponse;
import com.example.smartfood.data.models.SpoonacularImageResponse;
import com.example.smartfood.utils.Constants;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeRepository {

    private static final String TAG = "RecipeRepository";
    private SpoonacularApiService apiService;

    // Constructor: Initialize the API Service when the Repository is created
    public RecipeRepository() {
        apiService = ApiClient.getSpoonacularClient().create(SpoonacularApiService.class);
    }

    /**
     * Searches for recipes.
     * We return LiveData so the UI team can "observe" it. When the background network
     * call finishes, the LiveData automatically updates the UI.
     */
    public LiveData<List<Recipe>> searchRecipes(String query) {
        // Create an empty container to hold our recipes
        MutableLiveData<List<Recipe>> recipeData = new MutableLiveData<>();

        // Make the background API call
        apiService.searchRecipes(query, true, Constants.SPOONACULAR_API_KEY)
                .enqueue(new Callback<RecipeResponse>() {

                    @Override
                    public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Success! Put the list of recipes into our LiveData container
                            recipeData.setValue(response.body().getResults());
                        } else {
                            // API Error (e.g., out of points, bad request)
                            recipeData.setValue(null);
                            Log.e(TAG, "API Error Code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeResponse> call, Throwable t) {
                        // Network Error (e.g., no wifi)
                        recipeData.setValue(null);
                        Log.e(TAG, "Network Failure: " + t.getMessage());
                    }
                });

        // Return the container immediately (it will be populated when the network call finishes)
        return recipeData;
    }

    /**
     * Fetches random recipes for the Home Dashboard.
     */
    public LiveData<List<Recipe>> getRandomRecipes(int number) {
        MutableLiveData<List<Recipe>> randomRecipeData = new MutableLiveData<>();

        apiService.getRandomRecipes(number, Constants.SPOONACULAR_API_KEY)
                .enqueue(new Callback<RecipeResponse>() {
                    @Override
                    public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            randomRecipeData.setValue(response.body().getRecipes());
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeResponse> call, Throwable t) {
                        randomRecipeData.setValue(null);
                        Log.e(TAG, "Network Failure: " + t.getMessage());
                    }
                });

        return randomRecipeData;
    }

    public LiveData<SpoonacularImageResponse> classifyFoodImage(File imageFile) {
        MutableLiveData<SpoonacularImageResponse> classificationData = new MutableLiveData<>();

        // Convert the File into a MultipartBody.Part for Retrofit
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        apiService.classifyFoodImage(Constants.SPOONACULAR_API_KEY, body)
                .enqueue(new Callback<SpoonacularImageResponse>() {
                    @Override
                    public void onResponse(Call<SpoonacularImageResponse> call, Response<SpoonacularImageResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            classificationData.setValue(response.body());
                        } else {
                            classificationData.setValue(null);
                            Log.e(TAG, "Image API Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<SpoonacularImageResponse> call, Throwable t) {
                        classificationData.setValue(null);
                        Log.e(TAG, "Image Network Failure: " + t.getMessage());
                    }
                });

        return classificationData;
    }

    // NEW: Fetch random recipes based on Firebase diet tags
    public LiveData<List<Recipe>> getRandomRecipesWithTags(int number, String tags) {
        MutableLiveData<List<Recipe>> data = new MutableLiveData<>();

        apiService.getRandomRecipesWithTags(number, tags, Constants.SPOONACULAR_API_KEY)
                .enqueue(new Callback<RecipeResponse>() {
                    @Override
                    public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            data.setValue(response.body().getRecipes());
                        } else {
                            data.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeResponse> call, Throwable t) {
                        data.setValue(null);
                    }
                });
        return data;
    }

    // NEW: Fetch full recipe details by ID
    public LiveData<Recipe> getRecipeDetails(int recipeId) {
        MutableLiveData<Recipe> data = new MutableLiveData<>();

        apiService.getRecipeInformation(recipeId, true, Constants.SPOONACULAR_API_KEY)
                .enqueue(new Callback<Recipe>() {
                    @Override
                    public void onResponse(Call<Recipe> call, Response<Recipe> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            data.setValue(response.body());
                        } else {
                            data.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<Recipe> call, Throwable t) {
                        data.setValue(null);
                    }
                });
        return data;
    }
}