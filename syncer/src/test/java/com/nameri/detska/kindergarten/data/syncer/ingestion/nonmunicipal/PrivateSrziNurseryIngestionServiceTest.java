package com.nameri.detska.kindergarten.data.syncer.ingestion.nonmunicipal;

import jakarta.inject.Inject;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityOwnershipType;
import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityType;
import com.nameri.detska.kindergarten.data.syncer.ingestion.IngestionServiceException;
import com.nameri.detska.kindergarten.data.syncer.ingestion.KidFacilityDto;
import com.nameri.detska.kindergarten.data.syncer.ingestion.nonmunicipal.pdf.AIPrivateNurseryPdfParser;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(PrivateSrziNurseryIngestionServiceTest.TestConfig.class)
class PrivateSrziNurseryIngestionServiceTest {

    public static class TestConfig implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("quarkus.datasource.devservices.enabled", "false");
        }
    }

    @InjectMock
    HttpClient httpClient;

    @InjectMock
    AIPrivateNurseryPdfParser pdfParser;

    @Inject
    PrivateSrziNurseryIngestionService ingestionService;

    @Test
    @SuppressWarnings("unchecked")
    void shouldDownloadAndParsePdf() throws Exception {
        byte[] pdfBytes = "fake pdf content".getBytes();
        HttpResponse<byte[]> mockResponse = mockByteResponse(200, pdfBytes);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        KidFacilityDto dto = KidFacilityDto.builder()
            .name("ДГ Тест").address("гр. София")
            .kidFacilityType(KidFacilityType.NURSERY)
            .kidFacilityOwnershipType(KidFacilityOwnershipType.PRIVATE_SRZI)
            .build();
        when(pdfParser.parse(pdfBytes)).thenReturn(List.of(dto));

        List<KidFacilityDto> result = ingestionService.ingest();

        assertEquals(1, result.size());
        assertEquals("ДГ Тест", result.get(0).name());
        verify(pdfParser).parse(pdfBytes);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnEmptyListForNullPdfBytes() throws Exception {
        HttpResponse<byte[]> mockResponse = mockByteResponse(200, null);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        List<KidFacilityDto> result = ingestionService.ingest();

        assertTrue(result.isEmpty());
        verify(pdfParser, never()).parse(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnEmptyListForEmptyPdfBytes() throws Exception {
        HttpResponse<byte[]> mockResponse = mockByteResponse(200, new byte[0]);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        List<KidFacilityDto> result = ingestionService.ingest();

        assertTrue(result.isEmpty());
        verify(pdfParser, never()).parse(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldThrowOnNon200StatusCode() throws Exception {
        HttpResponse<byte[]> mockResponse = mockByteResponse(404, new byte[0]);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        assertThrows(IngestionServiceException.class, () -> ingestionService.ingest());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldThrowOnIOException() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new IOException("Network error"));

        assertThrows(IngestionServiceException.class, () -> ingestionService.ingest());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldThrowOnInterruptedException() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new InterruptedException("interrupted"));

        assertThrows(IngestionServiceException.class, () -> ingestionService.ingest());
    }

    @SuppressWarnings("unchecked")
    private static HttpResponse<byte[]> mockByteResponse(int statusCode, byte[] body) {
        HttpResponse<byte[]> mock = (HttpResponse<byte[]>) Mockito.mock(HttpResponse.class);
        when(mock.statusCode()).thenReturn(statusCode);
        when(mock.body()).thenReturn(body);
        return mock;
    }
}
