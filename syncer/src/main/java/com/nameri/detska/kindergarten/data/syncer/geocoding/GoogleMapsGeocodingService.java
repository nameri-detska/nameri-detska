package com.nameri.detska.kindergarten.data.syncer.geocoding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingConstants.BG_TO_LATIN;
import static com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingConstants.FIELD_ADDRESS_COMPONENTS;
import static com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingConstants.FIELD_GEOMETRY;
import static com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingConstants.FIELD_LATITUDE;
import static com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingConstants.FIELD_LOCATION;
import static com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingConstants.FIELD_LONGITUDE;
import static com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingConstants.FIELD_LONG_NAME;
import static com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingConstants.FIELD_RESULTS;
import static com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingConstants.FIELD_SHORT_NAME;
import static com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingConstants.FIELD_STATUS;
import static com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingConstants.FIELD_TYPES;
import static com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingConstants.STATUS_OK;
import static com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingConstants.STATUS_ZERO_RESULTS;
import static com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingConstants.TYPE_LOCALITY;

/**
 * Currently, there are some places that cannot be geocoded properly, and we need to override manually. I tried using also Places API, but still it was kinda bad. My free
 * quota is already reached and the pricing is kinda heavy (2.83 per 1000 requests - one run of our scenario takes 402..), so I cannot continue testing right now - there
 * could be room for improvement here.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class GoogleMapsGeocodingService implements GeocodingService {

    private static final String HEADER_ACCEPT = "Accept";
    private static final String MEDIA_TYPE_JSON = "application/json";

    @ConfigProperty(name = "google.maps.geocoding.url-template")
    String geocodingUrlTemplate;

    private final ObjectMapper objectMapper;
    private final HttpClient client;

    @Override
    public GeocodingResult geocode(String address) {
        try {
            String url = geocodingUrlTemplate.replace("{address}", URLEncoder.encode(address, StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_ACCEPT, MEDIA_TYPE_JSON)
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new GeocodingServiceException(
                    "Google Maps returned HTTP " + response.statusCode() + " for " + address);
            }

            JsonNode root = objectMapper.readTree(response.body());
            String status = root.get(FIELD_STATUS).asText();

            if (STATUS_OK.equals(status)) {
                JsonNode results = root.get(FIELD_RESULTS);
                JsonNode best = pickResult(results, extractLocality(address));
                JsonNode location = best.get(FIELD_GEOMETRY).get(FIELD_LOCATION);
                return GeocodingResult.found(location.get(FIELD_LATITUDE).asDouble(), location.get(FIELD_LONGITUDE).asDouble());
            }

            if (STATUS_ZERO_RESULTS.equals(status)) {
                return GeocodingResult.NOT_FOUND;
            }

            throw new GeocodingServiceException("Google Maps returned status " + status + " for " + address);
        } catch (GeocodingServiceException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GeocodingServiceException("Geocoding interrupted for " + address, e);
        } catch (IOException e) {
            throw new GeocodingServiceException("Network error geocoding " + address, e);
        } catch (Exception e) {
            throw new GeocodingServiceException("Unexpected error geocoding " + address, e);
        }
    }

    private JsonNode pickResult(JsonNode results, String expectedLocality) {
        if (expectedLocality == null || expectedLocality.isBlank() || results.isEmpty()) {
            return results.get(0);
        }

        String latin = transliterate(expectedLocality);

        for (JsonNode result : results) {
            if (resultContainsLocality(result, expectedLocality)
                || resultContainsLocality(result, latin)) {
                return result;
            }
        }

        for (JsonNode result : results) {
            String locality = extractResultLocality(result);
            if (locality != null && !"Sofia".equalsIgnoreCase(locality)) {
                return result;
            }
        }

        return results.get(0);
    }

    private boolean resultContainsLocality(JsonNode result, String locality) {
        JsonNode components = result.get(FIELD_ADDRESS_COMPONENTS);
        if (components == null || !components.isArray()) {
            return false;
        }

        String lower = locality.toLowerCase();
        for (JsonNode component : components) {
            String longName = component.path(FIELD_LONG_NAME).asText(null);
            String shortName = component.path(FIELD_SHORT_NAME).asText(null);
            if (longName != null && longName.toLowerCase().contains(lower)) {
                return true;
            }
            if (shortName != null && shortName.toLowerCase().contains(lower)) {
                return true;
            }
        }
        return false;
    }

    private static String extractResultLocality(JsonNode result) {
        JsonNode components = result.get(FIELD_ADDRESS_COMPONENTS);
        if (components == null || !components.isArray()) {
            return null;
        }

        for (JsonNode component : components) {
            JsonNode types = component.get(FIELD_TYPES);
            if (types == null) {
                continue;
            }
            for (JsonNode type : types) {
                if (TYPE_LOCALITY.equals(type.asText())) {
                    String name = component.path(FIELD_SHORT_NAME).asText(null);
                    if (name != null && !name.isEmpty()) {
                        return name;
                    }
                    name = component.path(FIELD_LONG_NAME).asText(null);
                    if (name != null && !name.isEmpty()) {
                        return name;
                    }
                }
            }
        }
        return null;
    }

    private static String transliterate(String bg) {
        StringBuilder sb = new StringBuilder();
        for (char c : bg.toCharArray()) {
            sb.append(BG_TO_LATIN.getOrDefault(c, String.valueOf(c)));
        }
        return sb.toString();
    }

    private static String extractLocality(String address) {
        if (address.startsWith("гр. ")) {
            return extractAfterPrefix(address, 4);
        }
        if (address.startsWith("с. ")) {
            return extractAfterPrefix(address, 3);
        }
        return null;
    }

    private static String extractAfterPrefix(String address, int prefixLen) {
        int comma = address.indexOf(',');
        if (comma > prefixLen) {
            return address.substring(prefixLen, comma).trim();
        }
        return address.substring(prefixLen).trim();
    }
}
