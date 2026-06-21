package com.example.smartfood.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smartfood.data.api.ApiClient;
import com.example.smartfood.data.api.UnsplashApiService;
import com.example.smartfood.data.models.UnsplashResponse;
import com.example.smartfood.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UnsplashRepository {

    private static final String TAG = "UnsplashRepo";
    private UnsplashApiService apiService;

    // Constructor: Initialize the API Service
    public UnsplashRepository() {
        apiService = ApiClient.getUnsplashClient().create(UnsplashApiService.class);
    }

    /**
     * Fetches a single high-quality image URL based on a search term.
     * We return a LiveData<String> so your UI team gets just the clean URL!
     */
    public LiveData<String> getFoodImageUrl(String query) {
        // Container to hold the final Image URL
        MutableLiveData<String> imageUrl = new MutableLiveData<>();

        // Make the network call (requesting 1 image)
        apiService.searchPhotos(query, Constants.UNSPLASH_API_KEY, 1)
                .enqueue(new Callback<UnsplashResponse>() {

                    @Override
                    public void onResponse(Call<UnsplashResponse> call, Response<UnsplashResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UnsplashResponse unsplashResponse = response.body();

                            // Check if Unsplash actually found an image
                            if (unsplashResponse.getResults() != null && !unsplashResponse.getResults().isEmpty()) {
                                // Extract the URL and put it in our LiveData container
                                String url = unsplashResponse.getResults().get(0).getUrls().getRegular();
                                imageUrl.setValue(url);
                                Log.d(TAG, "Successfully fetched image URL for: " + query);
                            } else {
                                imageUrl.setValue(null);
                                Log.d(TAG, "No images found for query: " + query);
                            }
                        } else {
                            imageUrl.setValue(null);
                            Log.e(TAG, "Unsplash API Error Code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<UnsplashResponse> call, Throwable t) {
                        imageUrl.setValue(null);
                        Log.e(TAG, "Network Failure: " + t.getMessage());
                    }
                });

        return imageUrl;
    }
}