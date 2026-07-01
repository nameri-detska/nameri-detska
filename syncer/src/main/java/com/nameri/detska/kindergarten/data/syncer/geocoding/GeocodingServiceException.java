package com.nameri.detska.kindergarten.data.syncer.geocoding;

public class GeocodingServiceException extends RuntimeException {

    public GeocodingServiceException(String message) {
        super(message);
    }

    public GeocodingServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
