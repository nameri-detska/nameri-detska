package com.nameri.detska.kindergarten.data.syncer.ingestion.municipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

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
public class MunicipalIngestionService implements KidFacilityIngestionService {

    private static final String NURSERY_IDENTIFIER = "сдя";
    private static final String KINDERGARTEN_WITH_NURSERY_IDENTIFIER = "яслени";
    private static final String KINDERGARTEN_IDENTIFIER = "дг";

    @ConfigProperty(name = "municipal.kid.facilities.api.url")
    String apiUrl;

    private final ObjectMapper objectMapper;
    private final HttpClient client;

    @Override
    public List<KidFacilityDto> ingest() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IngestionServiceException("Municipal API returned HTTP " + response.statusCode());
            }

            ApiResponse apiResponse = objectMapper.readValue(response.body(), ApiResponse.class);
            if (apiResponse == null || apiResponse.items() == null || apiResponse.items().kinderGardens() == null) {
                throw new IngestionServiceException("Municipal API returned empty response");
            }

            return apiResponse.items().kinderGardens().stream()
                .filter(this::isValid)
                .map(this::toDto)
                .toList();
        } catch (IOException e) {
            throw new IngestionServiceException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IngestionServiceException(e);
        }
    }

    private boolean isValid(KinderGarden kinderGarden) {
        return kinderGarden.nameStr() != null
            && !kinderGarden.nameStr().isBlank()
            && kinderGarden.address() != null
            && !kinderGarden.address().isBlank();
    }

    private KidFacilityDto toDto(KinderGarden kinderGarden) {
        return KidFacilityDto.builder()
            .name(kinderGarden.nameStr().trim())
            .address(kinderGarden.address().trim())
            .kidFacilityOwnershipType(KidFacilityOwnershipType.MUNICIPAL)
            .kidFacilityType(infertKidFacilityType(kinderGarden))
            .build();
    }

    private KidFacilityType infertKidFacilityType(KinderGarden kinderGarden) {
        String type = Optional.ofNullable(kinderGarden.name())
            .map(Name::publicType)
            .map(String::toLowerCase)
            .orElseThrow(() -> new IngestionServiceException("Kid facility type is missing"));

        if (type.contains(NURSERY_IDENTIFIER)) {
            return KidFacilityType.NURSERY;
        }

        if (type.contains(KINDERGARTEN_WITH_NURSERY_IDENTIFIER)) {
            return KidFacilityType.KINDERGARTEN_WITH_NURSERY;
        }

        if (type.contains(KINDERGARTEN_IDENTIFIER)) {
            return KidFacilityType.KINDERGARTEN;
        }

        throw new IngestionServiceException("Unknown kid facility type: " + type);
    }

    private record ApiResponse(Items items) {

    }

    private record Items(List<KinderGarden> kinderGardens) {

    }

    private record KinderGarden(String nameStr, String address, Name name) {

    }

    private record Name(String publicType) {

    }
}
