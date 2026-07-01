package com.nameri.detska.kindergarten.data.syncer.ingestion.nonmunicipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.nameri.detska.kindergarten.data.syncer.ingestion.IngestionServiceException;
import com.nameri.detska.kindergarten.data.syncer.ingestion.KidFacilityDto;
import com.nameri.detska.kindergarten.data.syncer.ingestion.KidFacilityIngestionService;
import com.nameri.detska.kindergarten.data.syncer.ingestion.nonmunicipal.pdf.AIPrivateNurseryPdfParser;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class PrivateSrziNurseryIngestionService implements KidFacilityIngestionService {

    private static final int HTTP_STATUS_OK = 200;

    @ConfigProperty(name = "private.nurseries.srzi.pdf.url")
    String pdfUrl;

    private final HttpClient client;
    private final AIPrivateNurseryPdfParser pdfParser;

    @Override
    public List<KidFacilityDto> ingest() {
        byte[] pdfBytes = downloadPdf();

        if (pdfBytes == null || pdfBytes.length == 0) {
            return List.of();
        }

        return pdfParser.parse(pdfBytes);
    }

    private byte[] downloadPdf() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(pdfUrl))
                .GET()
                .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != HTTP_STATUS_OK) {
                throw new IngestionServiceException("SRZI PDF download returned HTTP " + response.statusCode());
            }

            return response.body();
        } catch (IOException e) {
            throw new IngestionServiceException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IngestionServiceException(e);
        }
    }
}
