package com.example.smartfood.data.api;

import com.example.smartfood.data.models.OSMPlace;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OSMApiService {
    // Search endpoint expecting a List back
    @GET("search")
    Call<List<OSMPlace>> searchRestaurants(
            @Query("q") String query,
            @Query("format") String format,
            @Query("limit") int limit
    );
}