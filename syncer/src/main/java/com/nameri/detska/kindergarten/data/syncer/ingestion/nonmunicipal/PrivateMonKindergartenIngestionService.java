package com.nameri.detska.kindergarten.data.syncer.ingestion.nonmunicipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityOwnershipType;
import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityType;
import com.nameri.detska.kindergarten.data.syncer.ingestion.IngestionServiceException;
import com.nameri.detska.kindergarten.data.syncer.ingestion.KidFacilityDto;
import com.nameri.detska.kindergarten.data.syncer.ingestion.KidFacilityIngestionService;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class PrivateMonKindergartenIngestionService implements KidFacilityIngestionService {

    private static final int HTTP_STATUS_OK = 200;

    @ConfigProperty(name = "private.mon.kindergartens.register.url")
    String registerUrl;

    @ConfigProperty(name = "private.mon.kindergartens.detail.url")
    String detailUrl;

    private final HttpClient client;
    private final ObjectMapper objectMapper;

    @Override
    public List<KidFacilityDto> ingest() {
        List<PublicInstitution> institutions = fetchInstitutions();
        if (institutions.isEmpty()) {
            return List.of();
        }

        List<KidFacilityDto> facilities = new ArrayList<>();
        for (PublicInstitution institution : institutions) {
            InstitutionDetail detail = fetchDetail(institution);
            if (detail != null && isValid(detail)) {
                facilities.add(toDto(detail));
            }
        }

        return facilities;
    }

    private List<PublicInstitution> fetchInstitutions() {
        try {
            String body = """
                {
                    "region": [22, 23],
                    "instType": [2],
                    "financialSchoolType": [3],
                    "isRIActive": 1
                }
                """;

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(registerUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HTTP_STATUS_OK) {
                throw new IngestionServiceException("MON register API returned HTTP " + response.statusCode());
            }

            RegisterApiResponse apiResponse = objectMapper.readValue(response.body(), RegisterApiResponse.class);
            if (apiResponse == null || apiResponse.data() == null || apiResponse.data().publicInstitutions() == null) {
                throw new IngestionServiceException("MON register API returned empty response");
            }

            return apiResponse.data().publicInstitutions();
        } catch (IOException e) {
            throw new IngestionServiceException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IngestionServiceException(e);
        }
    }

    private InstitutionDetail fetchDetail(PublicInstitution institution) {
        try {
            String body = String.format(
                """
                    {
                        "instid": "%s",
                        "procID": "%s"
                    }
                    """, institution.instid(), institution.procID()
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(detailUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HTTP_STATUS_OK) {
                log.warn("MON detail API returned HTTP {} for institution {}", response.statusCode(), institution.instid());
                return null;
            }

            InstitutionDetailResponse detailResponse = objectMapper.readValue(response.body(), InstitutionDetailResponse.class);
            if (detailResponse == null || detailResponse.data() == null || detailResponse.data().isEmpty()) {
                log.warn("MON detail API returned empty data for institution {}", institution.instid());
                return null;
            }

            return detailResponse.data().getFirst();
        } catch (IOException e) {
            log.error("Failed to fetch detail for institution {}", institution.instid(), e);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private boolean isValid(InstitutionDetail detail) {
        return detail.name() != null
            && !detail.name().isBlank()
            && address(detail) != null
            && !address(detail).isBlank();
    }

    private KidFacilityDto toDto(InstitutionDetail detail) {
        return KidFacilityDto.builder()
            .name(detail.name().trim())
            .address(address(detail).trim())
            .kidFacilityType(KidFacilityType.KINDERGARTEN)
            .kidFacilityOwnershipType(KidFacilityOwnershipType.PRIVATE_MON)
            .build();
    }

    private String address(InstitutionDetail detail) {
        if (detail.primaryAddress() != null && !detail.primaryAddress().isEmpty()) {
            PrimaryAddress primary = detail.primaryAddress().getFirst();
            if (primary.primaryAddrAddress() != null && !primary.primaryAddrAddress().isBlank()) {
                return primary.primaryAddrAddress();
            }
        }
        return detail.settlementAddress();
    }

    private record RegisterApiResponse(RegisterData data) {

    }

    private record RegisterData(List<PublicInstitution> publicInstitutions) {

    }

    private record PublicInstitution(String instid, String name, String procID) {

    }

    private record InstitutionDetailResponse(List<InstitutionDetail> data) {

    }

    private record InstitutionDetail(String name, String settlementAddress, List<PrimaryAddress> primaryAddress) {

    }

    private record PrimaryAddress(String primaryAddrAddress) {

    }
}
