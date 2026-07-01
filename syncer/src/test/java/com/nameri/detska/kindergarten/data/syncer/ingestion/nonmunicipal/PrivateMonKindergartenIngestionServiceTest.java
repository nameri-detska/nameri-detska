package com.nameri.detska.kindergarten.data.syncer.ingestion.nonmunicipal;

import jakarta.inject.Inject;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(PrivateMonKindergartenIngestionServiceTest.TestConfig.class)
class PrivateMonKindergartenIngestionServiceTest {

    public static class TestConfig implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                "quarkus.datasource.devservices.enabled", "false",
                "private.mon.kindergartens.register.url", "https://test.local/register",
                "private.mon.kindergartens.detail.url", "https://test.local/detail"
            );
        }
    }

    @InjectMock
    HttpClient httpClient;

    @Inject
    PrivateMonKindergartenIngestionService ingestionService;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(httpClient);
    }

    @Test
    void shouldIngestKindergartensFromApi() throws Exception {
        mockSuccessiveResponses(
            registerResponse("""
                {
                  "data": {
                    "publicInstitutions": [
                      { "instid": "2200016", "name": "ЧАСТНА ДЕТСКА ГРАДИНА \\"СЛЪНЧЕВ АНГЕЛ\\"", "procID": "9643" }
                    ]
                  }
                }
                """),
            detailResponse("""
                {
                  "status": 1,
                  "data": [{
                    "name": "ЧАСТНА ДЕТСКА ГРАДИНА \\"СЛЪНЧЕВ АНГЕЛ\\"",
                    "settlementAddress": "район Витоша, ул. Шумако №29",
                    "primaryAddress": [{
                      "primaryAddrAddress": "район Витоша, ж.к. Симеоново, ул. Шумако №29"
                    }]
                  }]
                }
                """)
        );

        List<KidFacilityDto> results = ingestionService.ingest();

        assertEquals(1, results.size());
        KidFacilityDto dto = results.get(0);
        assertEquals("ЧАСТНА ДЕТСКА ГРАДИНА \"СЛЪНЧЕВ АНГЕЛ\"", dto.name());
        assertEquals("район Витоша, ж.к. Симеоново, ул. Шумако №29", dto.address());
        assertEquals(KidFacilityType.KINDERGARTEN, dto.kidFacilityType());
    }

    @Test
    void shouldFallbackToSettlementAddress() throws Exception {
        mockSuccessiveResponses(
            registerResponse("""
                {
                  "data": {
                    "publicInstitutions": [
                      { "instid": "2200000", "name": "ДЕТСКА ГРАДИНА №131", "procID": "9600" }
                    ]
                  }
                }
                """),
            detailResponse("""
                {
                  "status": 1,
                  "data": [{
                    "name": "ДЕТСКА ГРАДИНА №131",
                    "settlementAddress": "ул. Флора №17",
                    "primaryAddress": []
                  }]
                }
                """)
        );

        List<KidFacilityDto> results = ingestionService.ingest();

        assertEquals(1, results.size());
        assertEquals("ул. Флора №17", results.get(0).address());
    }

    @Test
    void shouldIngestMultipleFacilities() throws Exception {
        mockSuccessiveResponses(
            registerResponse("""
                {
                  "data": {
                    "publicInstitutions": [
                      { "instid": "2200010", "name": "ЧАСТНА ДЕТСКА ГРАДИНА ТАТКОВА ГРАДИНА", "procID": "9632" },
                      { "instid": "2200016", "name": "ЧАСТНА ДЕТСКА ГРАДИНА СЛЪНЧЕВ АНГЕЛ", "procID": "9643" }
                    ]
                  }
                }
                """),
            detailResponse("""
                {
                  "status": 1,
                  "data": [{
                    "name": "ЧАСТНА ДЕТСКА ГРАДИНА ТАТКОВА ГРАДИНА",
                    "settlementAddress": "ул. Първа №1",
                    "primaryAddress": [{ "primaryAddrAddress": "ул. Първа №1" }]
                  }]
                }
                """),
            detailResponse("""
                {
                  "status": 1,
                  "data": [{
                    "name": "ЧАСТНА ДЕТСКА ГРАДИНА СЛЪНЧЕВ АНГЕЛ",
                    "settlementAddress": "ул. Втора №2",
                    "primaryAddress": [{ "primaryAddrAddress": "ул. Втора №2" }]
                  }]
                }
                """)
        );

        List<KidFacilityDto> results = ingestionService.ingest();

        assertEquals(2, results.size());
        assertEquals("ЧАСТНА ДЕТСКА ГРАДИНА ТАТКОВА ГРАДИНА", results.get(0).name());
        assertEquals("ЧАСТНА ДЕТСКА ГРАДИНА СЛЪНЧЕВ АНГЕЛ", results.get(1).name());
    }

    @Test
    void shouldSkipInstitutionWhenDetailFails() throws Exception {
        mockSuccessiveResponses(
            registerResponse("""
                {
                  "data": {
                    "publicInstitutions": [
                      { "instid": "2200010", "name": "Валидна градина", "procID": "9632" },
                      { "instid": "2200016", "name": "Невалидна градина", "procID": "9643" }
                    ]
                  }
                }
                """),
            detailResponse("""
                {
                  "status": 1,
                  "data": [{
                    "name": "Валидна градина",
                    "settlementAddress": "ул. Първа",
                    "primaryAddress": [{ "primaryAddrAddress": "ул. Първа" }]
                  }]
                }
                """),
            mockResponse(500, "")
        );

        List<KidFacilityDto> results = ingestionService.ingest();

        assertEquals(1, results.size());
        assertEquals("Валидна градина", results.get(0).name());
    }

    @Test
    void shouldReturnEmptyListForEmptyRegisterResponse() throws Exception {
        mockSuccessiveResponses(
            registerResponse("""
                { "data": { "publicInstitutions": [] } }
                """)
        );

        List<KidFacilityDto> results = ingestionService.ingest();

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldThrowOnRegisterApiNon200() throws Exception {
        HttpResponse<String> errorResponse = mockResponse(500, "");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(errorResponse);

        assertThrows(IngestionServiceException.class, () -> ingestionService.ingest());
    }

    @Test
    void shouldThrowOnRegisterApiIOException() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new java.io.IOException("Connection refused"));

        assertThrows(IngestionServiceException.class, () -> ingestionService.ingest());
    }

    @Test
    void shouldReturnEmptyForNullRegisterData() throws Exception {
        mockSuccessiveResponses(
            registerResponse("""
                { "data": null }
                """)
        );

        assertThrows(IngestionServiceException.class, () -> ingestionService.ingest());
    }

    private void mockSuccessiveResponses(HttpResponse<String>... responses) throws Exception {
        var stub = when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)));
        for (HttpResponse<String> response : responses) {
            stub = stub.thenReturn(response);
        }
    }

    private HttpResponse<String> registerResponse(String body) {
        return mockResponse(200, body);
    }

    private HttpResponse<String> detailResponse(String body) {
        return mockResponse(200, body);
    }

    private HttpResponse<String> mockResponse(int statusCode, String body) {
        HttpResponse<String> mock = (HttpResponse<String>) Mockito.mock(HttpResponse.class);
        doReturn(statusCode).when(mock).statusCode();
        doReturn(body).when(mock).body();
        return mock;
    }
}
