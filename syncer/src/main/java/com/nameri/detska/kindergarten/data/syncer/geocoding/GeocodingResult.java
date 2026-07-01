package com.nameri.detska.kindergarten.data.syncer.geocoding;

public record GeocodingResult(double latitude, double longitude, boolean found) {

    static final GeocodingResult NOT_FOUND = new GeocodingResult(0.0, 0.0, false);

    public static GeocodingResult found(double latitude, double longitude) {
        return new GeocodingResult(latitude, longitude, true);
    }
}
