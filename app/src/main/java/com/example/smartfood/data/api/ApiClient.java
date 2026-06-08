package com.example.smartfood.data.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit spoonacularRetrofit = null;
    private static Retrofit edamamRetrofit = null;
    private static Retrofit weatherRetrofit = null;
    private static Retrofit unsplashRetrofit = null;
    private static Retrofit osmRetrofit = null;

    // Base URLs
    public static final String SPOONACULAR_BASE_URL = "https://api.spoonacular.com/";
    public static final String EDAMAM_BASE_URL = "https://api.edamam.com/";
    public static final String WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String UNSPLASH_BASE_URL = "https://api.unsplash.com/";
    public static final String OSM_BASE_URL = "https://nominatim.openstreetmap.org/";

    private static OkHttpClient getClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder().addInterceptor(interceptor).build();
    }

    // ---------------------------------------------------
    // SPOONACULAR CLIENT
    // ---------------------------------------------------
    public static Retrofit getSpoonacularClient() {
        if (spoonacularRetrofit == null) {
            spoonacularRetrofit = new Retrofit.Builder()
                    .baseUrl(SPOONACULAR_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getClient())
                    .build();
        }
        return spoonacularRetrofit;
    }

    // ---------------------------------------------------
    // EDAMAM CLIENT
    // ---------------------------------------------------
    public static Retrofit getEdamamClient() {
        if (edamamRetrofit == null) {
            edamamRetrofit = new Retrofit.Builder()
                    .baseUrl(EDAMAM_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getClient())
                    .build();
        }
        return edamamRetrofit;
    }

    // ---------------------------------------------------
    // WEATHER CLIENT (We will use this later)
    // ---------------------------------------------------
    public static Retrofit getWeatherClient() {
        if (weatherRetrofit == null) {
            weatherRetrofit = new Retrofit.Builder()
                    .baseUrl(WEATHER_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getClient())
                    .build();
        }
        return weatherRetrofit;
    }
    public static Retrofit getUnsplashClient() {
        if (unsplashRetrofit == null) {
            unsplashRetrofit = new Retrofit.Builder()
                    .baseUrl(UNSPLASH_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return unsplashRetrofit;
    }

    public static Retrofit getOSMClient() {
        if (osmRetrofit == null) {
            okhttp3.logging.HttpLoggingInterceptor logging = new okhttp3.logging.HttpLoggingInterceptor();
            logging.setLevel(okhttp3.logging.HttpLoggingInterceptor.Level.BODY);

            // OpenStreetMap requires a custom User-Agent header (No API Key needed!)
            okhttp3.Interceptor headerInterceptor = chain -> {
                okhttp3.Request original = chain.request();
                okhttp3.Request request = original.newBuilder()
                        .header("User-Agent", "SmartFoodApp/1.0")
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            };

            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                    .addInterceptor(headerInterceptor)
                    .addInterceptor(logging)
                    .build();

            osmRetrofit = new Retrofit.Builder()
                    .baseUrl(OSM_BASE_URL)
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return osmRetrofit;
    }


}