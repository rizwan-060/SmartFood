package com.example.smartfood.data.api;

import com.example.smartfood.data.models.UnsplashResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UnsplashApiService {

    @GET("search/photos")
    Call<UnsplashResponse> searchPhotos(
            @Query("query") String query,
            @Query("client_id") String clientId,
            @Query("per_page") int perPage
    );
}