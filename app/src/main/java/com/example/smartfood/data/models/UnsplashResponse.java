package com.example.smartfood.data.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UnsplashResponse {

    @SerializedName("results")
    private List<UnsplashPhoto> results;

    public List<UnsplashPhoto> getResults() {
        return results;
    }

    public static class UnsplashPhoto {

        @SerializedName("urls")
        private PhotoUrls urls;

        public PhotoUrls getUrls() {
            return urls;
        }
    }

    public static class PhotoUrls{

        @SerializedName("regular")
        private String regular;

        public String getRegular() {
            return regular;
        }
    }
}
