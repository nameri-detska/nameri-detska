package com.nameri.detska.kindergarten.data.syncer.ingestion.municipal;

import jakarta.inject.Inject;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityType;
import com.nameri.detska.kindergarten.data.syncer.ingestion.IngestionServiceException;
import com.nameri.detska.kindergarten.data.syncer.ingestion.KidFacilityDto;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(MunicipalIngestionServiceTest.TestConfig.class)
class MunicipalIngestionServiceTest {

    public static class TestConfig implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                "municipal.kid.facilities.api.url", "https://test.local/api",
                "quarkus.datasource.devservices.enabled", "false"
            );
        }
    }

    @InjectMock
    HttpClient httpClient;

    @Inject
    MunicipalIngestionService ingestionService;

    @Test
    void shouldIngestKindergartens() throws Exception {
        String responseBody = """
            {
              "items": {
                "kinderGardens": [{
                  "nameStr": "ДГ \\"Слънце\\"",
                  "address": "гр. София, ул. Примерна №1",
                  "name": { "publicType": "ДГ" }
                }]
              }
            }
            """;

        mockResponse(200, responseBody);

        List<KidFacilityDto> results = ingestionService.ingest();

        assertEquals(1, results.size());
        KidFacilityDto dto = results.get(0);
        assertEquals("ДГ \"Слънце\"", dto.name());
        assertEquals("гр. София, ул. Примерна №1", dto.address());
        assertEquals(KidFacilityType.KINDERGARTEN, dto.kidFacilityType());
    }

    @Test
    void shouldDetectNurseryType() throws Exception {
        String responseBody = """
            {
              "items": {
                "kinderGardens": [{
                  "nameStr": "СДЯ \\"Звездичка\\"",
                  "address": "гр. София, ул. Детска №2",
                  "name": { "publicType": "СДЯ" }
                }]
              }
            }
            """;

        mockResponse(200, responseBody);

        List<KidFacilityDto> results = ingestionService.ingest();

        assertEquals(1, results.size());
        assertEquals(KidFacilityType.NURSERY, results.get(0).kidFacilityType());
    }

    @Test
    void shouldDetectKindergartenWithNurseryGroups() throws Exception {
        String responseBody = """
            {
              "items": {
                "kinderGardens": [{
                  "nameStr": "ДГ \\"Пролет\\"",
                  "address": "гр. София, ул. Цветна №3",
                  "name": { "publicType": "Детска градина с яслени групи" }
                }]
              }
            }
            """;

        mockResponse(200, responseBody);

        List<KidFacilityDto> results = ingestionService.ingest();

        assertEquals(1, results.size());
        assertEquals(KidFacilityType.KINDERGARTEN_WITH_NURSERY, results.get(0).kidFacilityType());
    }

    @Test
    void shouldFilterInvalidEntries() throws Exception {
        String responseBody = """
            {
              "items": {
                "kinderGardens": [
                  {
                    "nameStr": "",
                    "address": "гр. София, ул. Невалидна",
                    "name": { "publicType": "ДГ" }
                  },
                  {
                    "nameStr": "ДГ \\"Валидна\\"",
                    "address": "гр. София, ул. Валидна №1",
                    "name": { "publicType": "ДГ" }
                  },
                  {
                    "nameStr": "ДГ \\"Без адрес\\"",
                    "address": "   ",
                    "name": { "publicType": "ДГ" }
                  }
                ]
              }
            }
            """;

        mockResponse(200, responseBody);

        List<KidFacilityDto> results = ingestionService.ingest();

        assertEquals(1, results.size());
        assertEquals("ДГ \"Валидна\"", results.get(0).name());
    }

    @Test
    void shouldThrowOnUnknownType() throws Exception {
        String responseBody = """
            {
              "items": {
                "kinderGardens": [{
                  "nameStr": "Непознат тип",
                  "address": "гр. София",
                  "name": { "publicType": "Нещо непознато" }
                }]
              }
            }
            """;

        mockResponse(200, responseBody);

        assertThrows(IngestionServiceException.class, () -> ingestionService.ingest());
    }

    @Test
    void shouldThrowOnHttpError() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new java.io.IOException("Connection refused"));

        assertThrows(IngestionServiceException.class, () -> ingestionService.ingest());
    }

    @Test
    void shouldThrowOnNon200Response() throws Exception {
        HttpResponse<String> mockResponse = mockResp(500, "");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        IngestionServiceException ex = assertThrows(
            IngestionServiceException.class, () -> ingestionService.ingest());
        assertTrue(ex.getMessage().contains("500"));
    }

    @Test
    void shouldThrowOnEmptyResponse() throws Exception {
        String responseBody = """
            { "items": null }
            """;

        mockResponse(200, responseBody);

        assertThrows(IngestionServiceException.class, () -> ingestionService.ingest());
    }

    @Test
    void shouldIngestMultipleFacilities() throws Exception {
        String responseBody = """
            {
              "items": {
                "kinderGardens": [
                  {
                    "nameStr": "ДГ \\"Едно\\"",
                    "address": "гр. София, ул. Първа",
                    "name": { "publicType": "ДГ" }
                  },
                  {
                    "nameStr": "СДЯ \\"Две\\"",
                    "address": "гр. София, ул. Втора",
                    "name": { "publicType": "СДЯ" }
                  },
                  {
                    "nameStr": "ДГ \\"Три\\"",
                    "address": "гр. София, ул. Трета",
                    "name": { "publicType": "ДГ с яслени групи" }
                  }
                ]
              }
            }
            """;

        mockResponse(200, responseBody);

        List<KidFacilityDto> results = ingestionService.ingest();

        assertEquals(3, results.size());
        assertEquals(KidFacilityType.KINDERGARTEN, results.get(0).kidFacilityType());
        assertEquals(KidFacilityType.NURSERY, results.get(1).kidFacilityType());
        assertEquals(KidFacilityType.KINDERGARTEN_WITH_NURSERY, results.get(2).kidFacilityType());
    }

    private void mockResponse(int statusCode, String body) throws Exception {
        HttpResponse<String> mockResponse = mockResp(statusCode, body);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);
    }

    private HttpResponse<String> mockResp(int statusCode, String body) {
        HttpResponse<String> mock = (HttpResponse<String>) Mockito.mock(HttpResponse.class);
        when(mock.statusCode()).thenReturn(statusCode);
        when(mock.body()).thenReturn(body);
        return mock;
    }
}
