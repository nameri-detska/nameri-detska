package com.nameri.detska.kindergarten.data.syncer.geocoding;

/**
 * Service for geocoding an address.
 */
public interface GeocodingService {

    /**
     * Geocode an address.
     *
     * @param address the address to geocode
     * @return the corresponding GeocodingResult(lat, lgn)
     */
    GeocodingResult geocode(String address);
}
