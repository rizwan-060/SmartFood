package com.example.smartfood.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smartfood.data.api.ApiClient;
import com.example.smartfood.data.api.WeatherApiService;
import com.example.smartfood.data.models.WeatherResponse;
import com.example.smartfood.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {

    private static final String TAG = "WeatherRepo";
    private WeatherApiService apiService;

    // Constructor: Initialize the Weather API Service
    public WeatherRepository() {
        apiService = ApiClient.getWeatherClient().create(WeatherApiService.class);
    }

    /**
     * Fetches the current weather for a given city.
     * Returns LiveData so the UI can update automatically when the weather changes.
     */
    public LiveData<WeatherResponse> getWeather(String cityName) {
        // Create an empty container to hold the weather data
        MutableLiveData<WeatherResponse> weatherData = new MutableLiveData<>();

        // Make the background API call to OpenWeatherMap
        apiService.getWeatherByCity(cityName, Constants.WEATHER_API_KEY, "metric")
                .enqueue(new Callback<WeatherResponse>() {

                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Success! Put the weather data into our LiveData container
                            weatherData.setValue(response.body());
                            Log.d(TAG, "Successfully fetched weather for: " + cityName);
                        } else {
                            // API Error (e.g., 401 Unauthorized, 404 City Not Found)
                            weatherData.setValue(null);
                            Log.e(TAG, "Weather API Error Code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        // Network Error (e.g., no internet connection)
                        weatherData.setValue(null);
                        Log.e(TAG, "Network Failure: " + t.getMessage());
                    }
                });

        // Return the container immediately
        return weatherData;
    }
}