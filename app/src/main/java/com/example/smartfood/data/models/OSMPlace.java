package com.example.smartfood.data.models;

import com.google.gson.annotations.SerializedName;

public class OSMPlace {
    // OpenStreetMap returns the full name and address combined in "display_name"
    @SerializedName("display_name")
    private String displayName;

    @SerializedName("lat")
    private String lat;

    @SerializedName("lon")
    private String lon;

    public String getDisplayName() { return displayName; }
    public String getLat() { return lat; }
    public String getLon() { return lon; }
}