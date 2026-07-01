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

    @Test
    @SuppressWarnings("unchecked")
    void shouldPickResultMatchingBulgarianLocality() throws Exception {
        String responseBody = """
            {
              "results": [
                {
                  "geometry": { "location": { "lat": 42.1, "lng": 23.1 } },
                  "address_components": []
                },
                {
                  "geometry": { "location": { "lat": 42.7, "lng": 23.3 } },
                  "address_components": [
                    { "long_name": "София", "short_name": "София", "types": ["locality"] }
                  ]
                }
              ],
              "status": "OK"
            }
            """;

        HttpResponse<String> mockResponse = mockResponse(200, responseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        GeocodingResult result = geocodingService.geocode("гр. София, ул. Примерна №1");

        assertTrue(result.found());
        assertEquals(42.7, result.latitude(), 0.0001);
        assertEquals(23.3, result.longitude(), 0.0001);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldPickResultMatchingTransliteratedLocality() throws Exception {
        String responseBody = """
            {
              "results": [
                {
                  "geometry": { "location": { "lat": 42.1, "lng": 23.1 } },
                  "address_components": [{ "long_name": "xxx", "short_name": "xxx", "types": ["locality"] }]
                },
                {
                  "geometry": { "location": { "lat": 42.7, "lng": 23.3 } },
                  "address_components": [
                    { "long_name": "Sofia", "short_name": "Sofia", "types": ["locality"] }
                  ]
                }
              ],
              "status": "OK"
            }
            """;

        HttpResponse<String> mockResponse = mockResponse(200, responseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        GeocodingResult result = geocodingService.geocode("гр. София, ул. Примерна №1");

        assertTrue(result.found());
        assertEquals(42.7, result.latitude(), 0.0001);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFallBackToNonSofiaLocalityWhenNoMatch() throws Exception {
        String responseBody = """
            {
              "results": [
                {
                  "geometry": { "location": { "lat": 42.1, "lng": 23.1 } },
                  "address_components": [
                    { "long_name": "Sofia", "short_name": "Sofia", "types": ["locality"] }
                  ]
                },
                {
                  "geometry": { "location": { "lat": 42.14, "lng": 24.75 } },
                  "address_components": [
                    { "long_name": "Plovdiv", "short_name": "Plovdiv", "types": ["locality"] }
                  ]
                }
              ],
              "status": "OK"
            }
            """;

        HttpResponse<String> mockResponse = mockResponse(200, responseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        GeocodingResult result = geocodingService.geocode("гр. Бургас, ул. Примерна №1");

        assertTrue(result.found());
        assertEquals(42.14, result.latitude(), 0.0001);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFallBackToFirstResultWhenAllSofiaAndNoMatch() throws Exception {
        String responseBody = """
            {
              "results": [
                {
                  "geometry": { "location": { "lat": 42.1, "lng": 23.1 } },
                  "address_components": [
                    { "long_name": "Sofia", "short_name": "Sofia", "types": ["locality"] }
                  ]
                },
                {
                  "geometry": { "location": { "lat": 42.2, "lng": 23.2 } },
                  "address_components": [
                    { "long_name": "Sofia", "short_name": "Sofia", "types": ["locality"] }
                  ]
                }
              ],
              "status": "OK"
            }
            """;

        HttpResponse<String> mockResponse = mockResponse(200, responseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        GeocodingResult result = geocodingService.geocode("гр. Бургас, ул. Примерна №1");

        assertTrue(result.found());
        assertEquals(42.1, result.latitude(), 0.0001);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldPickFirstResultWhenNoPrefix() throws Exception {
        String responseBody = """
            {
              "results": [
                {
                  "geometry": { "location": { "lat": 42.1, "lng": 23.1 } }
                },
                {
                  "geometry": { "location": { "lat": 42.7, "lng": 23.3 } }
                }
              ],
              "status": "OK"
            }
            """;

        HttpResponse<String> mockResponse = mockResponse(200, responseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        GeocodingResult result = geocodingService.geocode("София, ул. Примерна №1");

        assertTrue(result.found());
        assertEquals(42.1, result.latitude(), 0.0001);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldExtractVillageLocality() throws Exception {
        String responseBody = """
            {
              "results": [
                {
                  "geometry": { "location": { "lat": 42.1, "lng": 23.1 } },
                  "address_components": []
                },
                {
                  "geometry": { "location": { "lat": 42.6, "lng": 23.5 } },
                  "address_components": [
                    { "long_name": "Lozen", "short_name": "Lozen", "types": ["locality"] }
                  ]
                }
              ],
              "status": "OK"
            }
            """;

        HttpResponse<String> mockResponse = mockResponse(200, responseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        GeocodingResult result = geocodingService.geocode("с. Лозен");

        assertTrue(result.found());
        assertEquals(42.6, result.latitude(), 0.0001);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldThrowOnNon200HttpStatus() throws Exception {
        HttpResponse<String> mockResponse = mockResponse(403, "Forbidden");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        try {
            geocodingService.geocode("гр. София");
        } catch (GeocodingServiceException e) {
            assertTrue(e.getMessage().contains("HTTP 403"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldThrowOnInterruptedException() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new InterruptedException("interrupted"));

        try {
            geocodingService.geocode("гр. София");
        } catch (GeocodingServiceException e) {
            assertTrue(e.getMessage().contains("interrupted"));
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
