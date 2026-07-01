package com.nameri.detska.kindergarten.data.syncer.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

import java.util.ArrayList;
import java.util.List;

import com.nameri.detska.kindergarten.data.syncer.facility.KidFacility;
import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityService;
import com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingResult;
import com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingService;
import com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingServiceException;
import com.nameri.detska.kindergarten.data.syncer.ingestion.KidFacilityDto;
import com.nameri.detska.kindergarten.data.syncer.ingestion.KidFacilityIngestionService;
import com.nameri.detska.kindergarten.data.syncer.override.ManualFacilityOverrideService;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SyncService {

    private final Instance<KidFacilityIngestionService> ingestors;
    private final GeocodingService geocodingService;
    private final KidFacilityService kidFacilityService;
    private final ManualFacilityOverrideService overrideService;

    public void run() {
        List<KidFacilityDto> kidFacilityDtos = ingest();
        List<KidFacility> kidFacilities = geocode(kidFacilityDtos);
        kidFacilityService.replaceAll(kidFacilities);
        overrideService.applyOverrides();
        log.info("Replaced database with {} facilities", kidFacilities.size());
    }

    private List<KidFacilityDto> ingest() {
        List<KidFacilityDto> kidFacilityDtos = new ArrayList<>();

        for (KidFacilityIngestionService ingestor : ingestors) {
            List<KidFacilityDto> facilities = ingestor.ingest();
            log.info("Fetched {} facilities from {}", facilities.size(), ingestor.getClass().getSimpleName());
            kidFacilityDtos.addAll(facilities);
        }

        log.info("Total: {} ingested facilities", kidFacilityDtos.size());
        return kidFacilityDtos;
    }

    private static String cleanAddress(String address) {
        return address
            .replaceAll("\\bдо\\b\\s*", "")
            .trim();
    }

    private List<KidFacility> geocode(List<KidFacilityDto> kidFacilityDtos) {
        List<KidFacility> facilities = new ArrayList<>(kidFacilityDtos.size());

        for (KidFacilityDto dto : kidFacilityDtos) {
            double latitude = 0.0;
            double longitude = 0.0;

            try {
                GeocodingResult result = geocodingService.geocode(cleanAddress(dto.address()));
                if (result.found()) {
                    latitude = result.latitude();
                    longitude = result.longitude();
                }
            } catch (GeocodingServiceException e) {
                log.warn("Skipping geocoding of {} due to geocoding error", dto.address(), e);
            }

            facilities.add(KidFacility.builder()
                .name(dto.name())
                .kidFacilityType(dto.kidFacilityType())
                .kidFacilityOwnershipType(dto.kidFacilityOwnershipType())
                .address(dto.address())
                .latitude(latitude)
                .longitude(longitude)
                .build());
        }

        log.info("Geocoded {} facilities using {}", facilities.size(), geocodingService.getClass().getSimpleName());
        return facilities;
    }
}
