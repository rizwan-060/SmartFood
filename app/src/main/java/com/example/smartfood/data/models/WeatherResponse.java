package com.example.smartfood.data.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {

    @SerializedName("main")
    private MainData main;

    @SerializedName("weather")
    private List<WeatherCondition> weather;

    public MainData getMain() { return main; }
    public List<WeatherCondition> getWeather() { return weather; }

    public static class MainData {
        @SerializedName("temp")
        private double temp;

        public double getTemp() { return temp; }
    }
    
    public static class WeatherCondition {
        @SerializedName("main")
        private String mainCondition;

        @SerializedName("description")
        private String description;

        public String getMainCondition() { return mainCondition; }
        public String getDescription() { return description; }
    }
}