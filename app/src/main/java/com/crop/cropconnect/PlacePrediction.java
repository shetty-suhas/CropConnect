package com.crop.cropconnect;

public class PlacePrediction {
    String placeId;
    String primaryText;
    String secondaryText;

    public PlacePrediction(String placeId, String primaryText, String secondaryText) {
        this.placeId = placeId;
        this.primaryText = primaryText;
        this.secondaryText = secondaryText;
    }
}