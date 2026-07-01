package com.nameri.detska.kindergarten.data.syncer.geocoding;

import jakarta.inject.Inject;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(GoogleMapsGeocodingServiceTest.TestConfig.class)
class GoogleMapsGeocodingServiceTest {

    public static class TestConfig implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("quarkus.datasource.devservices.enabled", "false");
        }
    }

    @InjectMock
    HttpClient httpClient;

    @Inject
    GoogleMapsGeocodingService geocodingService;

    @Test
    @SuppressWarnings("unchecked")
    void shouldGeocodeSuccessfully() throws Exception {
        String responseBody = """
            {
              "results": [{
                "geometry": {
                  "location": { "lat": 42.697708, "lng": 23.321868 }
                }
              }],
              "status": "OK"
            }
            """;

        HttpResponse<String> mockResponse = mockResponse(200, responseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        GeocodingResult result = geocodingService.geocode("гр. София, ул. Примерна №1");

        assertTrue(result.found());
        assertEquals(42.697708, result.latitude(), 0.0001);
        assertEquals(23.321868, result.longitude(), 0.0001);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnNotFoundForZeroResults() throws Exception {
        String responseBody = """
            { "results": [], "status": "ZERO_RESULTS" }
            """;

        HttpResponse<String> mockResponse = mockResponse(200, responseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        GeocodingResult result = geocodingService.geocode("гр. София, Неизвестна улица");

        assertFalse(result.found());
    }

    @Test
    void shouldThrowOnHttpError() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new java.io.IOException("Network error"));

        try {
            geocodingService.geocode("гр. София");
        } catch (GeocodingServiceException e) {
            assertEquals("Network error geocoding гр. София", e.getMessage());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldThrowOnRequestDenied() throws Exception {
        String responseBody = """
            { "results": [], "status": "REQUEST_DENIED" }
            """;

        HttpResponse<String> mockResponse = mockResponse(200, responseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        try {
            geocodingService.geocode("гр. София");
        } catch (GeocodingServiceException e) {
            assertTrue(e.getMessage().contains("REQUEST_DENIED"));
        }
    }

    @SuppressWarnings("unchecked")
    private static HttpResponse<String> mockResponse(int statusCode, String body) {
        HttpResponse<String> mock = (HttpResponse<String>) Mockito.mock(HttpResponse.class);
        when(mock.statusCode()).thenReturn(statusCode);
        when(mock.body()).thenReturn(body);
        return mock;
    }
}
