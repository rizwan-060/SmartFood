package com.example.smartfood.data.api;

import com.example.smartfood.data.models.Recipe;
import com.example.smartfood.data.models.RecipeResponse;
import com.example.smartfood.data.models.SpoonacularImageResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SpoonacularApiService {

    @GET("recipes/complexSearch")
    Call<RecipeResponse> searchRecipes(
            @Query("query") String query,
            @Query("addRecipeInformation") boolean addRecipeInformation,
            @Query("apiKey") String apiKey
    );

    @GET("recipes/random")
    Call<RecipeResponse> getRandomRecipes(
            @Query("number") int number,
            @Query("apiKey") String apiKey
    );

    @Multipart
    @POST("food/images/classify")
    Call<SpoonacularImageResponse> classifyFoodImage(
            @Query("apiKey") String apiKey,
            @Part MultipartBody.Part file
    );

    @GET("recipes/random")
    Call<RecipeResponse> getRandomRecipesWithTags(
            @Query("number") int number,
            @Query("tags") String tags,
            @Query("apiKey") String apiKey
    );

    @GET("recipes/{id}/information")
    Call<Recipe> getRecipeInformation(
            @Path("id") int id,
            @Query("includeNutrition") boolean includeNutrition,
            @Query("apiKey") String apiKey
    );
}