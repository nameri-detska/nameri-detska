package com.nameri.detska.kindergarten.data.syncer.sync;

import jakarta.enterprise.inject.Instance;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityOwnershipType;
import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityService;
import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityType;
import com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingResult;
import com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingService;
import com.nameri.detska.kindergarten.data.syncer.ingestion.KidFacilityDto;
import com.nameri.detska.kindergarten.data.syncer.ingestion.KidFacilityIngestionService;
import com.nameri.detska.kindergarten.data.syncer.override.ManualFacilityOverrideService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SyncServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldOrchestrateIngestGeocodeReplace() {
        KidFacilityDto dto1 = KidFacilityDto.builder()
            .name("KG 1").address("гр. София, ул. А")
            .kidFacilityType(KidFacilityType.KINDERGARTEN)
            .kidFacilityOwnershipType(KidFacilityOwnershipType.MUNICIPAL)
            .build();

        KidFacilityIngestionService ingestor = Mockito.mock(KidFacilityIngestionService.class);
        when(ingestor.ingest()).thenReturn(List.of(dto1));

        GeocodingService geocodingService = Mockito.mock(GeocodingService.class);
        when(geocodingService.geocode("гр. София, ул. А"))
            .thenReturn(GeocodingResult.found(42.7, 23.3));

        KidFacilityService kidFacilityService = Mockito.mock(KidFacilityService.class);

        ManualFacilityOverrideService overrideService = Mockito.mock(ManualFacilityOverrideService.class);

        Instance<KidFacilityIngestionService> ingestors = Mockito.mock(Instance.class);
        when(ingestors.iterator()).thenReturn(List.of(ingestor).iterator());

        SyncService syncService = new SyncService(ingestors, geocodingService, kidFacilityService, overrideService);
        syncService.run();

        verify(geocodingService).geocode("гр. София, ул. А");
        verify(kidFacilityService).replaceAll(Mockito.anyList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldHandleEmptyIngestion() {
        KidFacilityIngestionService ingestor = Mockito.mock(KidFacilityIngestionService.class);
        when(ingestor.ingest()).thenReturn(List.of());

        GeocodingService geocodingService = Mockito.mock(GeocodingService.class);
        KidFacilityService kidFacilityService = Mockito.mock(KidFacilityService.class);
        ManualFacilityOverrideService overrideService = Mockito.mock(ManualFacilityOverrideService.class);

        Instance<KidFacilityIngestionService> ingestors = Mockito.mock(Instance.class);
        when(ingestors.iterator()).thenReturn(List.of(ingestor).iterator());

        SyncService syncService = new SyncService(ingestors, geocodingService, kidFacilityService, overrideService);
        syncService.run();

        verify(kidFacilityService).replaceAll(List.of());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldAddFacilityWithZeroCoordsOnGeocodingError() {
        KidFacilityDto dto = KidFacilityDto.builder()
            .name("KG 1").address("гр. София, ул. А")
            .kidFacilityType(KidFacilityType.KINDERGARTEN)
            .kidFacilityOwnershipType(KidFacilityOwnershipType.MUNICIPAL)
            .build();

        KidFacilityIngestionService ingestor = Mockito.mock(KidFacilityIngestionService.class);
        when(ingestor.ingest()).thenReturn(List.of(dto));

        GeocodingService geocodingService = Mockito.mock(GeocodingService.class);
        when(geocodingService.geocode("гр. София, ул. А"))
            .thenThrow(new com.nameri.detska.kindergarten.data.syncer.geocoding.GeocodingServiceException("error"));

        KidFacilityService kidFacilityService = Mockito.mock(KidFacilityService.class);
        ManualFacilityOverrideService overrideService = Mockito.mock(ManualFacilityOverrideService.class);

        Instance<KidFacilityIngestionService> ingestors = Mockito.mock(Instance.class);
        when(ingestors.iterator()).thenReturn(List.of(ingestor).iterator());

        SyncService syncService = new SyncService(ingestors, geocodingService, kidFacilityService, overrideService);
        syncService.run();

        verify(kidFacilityService).replaceAll(Mockito.argThat(facilities -> facilities.size() == 1
            && facilities.get(0).latitude() == 0.0
            && facilities.get(0).longitude() == 0.0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCleanDoAddressPrefix() {
        KidFacilityDto dto = KidFacilityDto.builder()
            .name("KG 1").address("до София, ул. А")
            .kidFacilityType(KidFacilityType.KINDERGARTEN)
            .kidFacilityOwnershipType(KidFacilityOwnershipType.MUNICIPAL)
            .build();

        KidFacilityIngestionService ingestor = Mockito.mock(KidFacilityIngestionService.class);
        when(ingestor.ingest()).thenReturn(List.of(dto));

        GeocodingService geocodingService = Mockito.mock(GeocodingService.class);
        when(geocodingService.geocode("София, ул. А"))
            .thenReturn(GeocodingResult.found(42.7, 23.3));

        KidFacilityService kidFacilityService = Mockito.mock(KidFacilityService.class);
        ManualFacilityOverrideService overrideService = Mockito.mock(ManualFacilityOverrideService.class);

        Instance<KidFacilityIngestionService> ingestors = Mockito.mock(Instance.class);
        when(ingestors.iterator()).thenReturn(List.of(ingestor).iterator());

        SyncService syncService = new SyncService(ingestors, geocodingService, kidFacilityService, overrideService);
        syncService.run();

        verify(geocodingService).geocode("София, ул. А");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldIterateMultipleIngestors() {
        KidFacilityDto dto1 = KidFacilityDto.builder()
            .name("KG 1").address("гр. София, ул. А")
            .kidFacilityType(KidFacilityType.KINDERGARTEN)
            .kidFacilityOwnershipType(KidFacilityOwnershipType.MUNICIPAL)
            .build();
        KidFacilityDto dto2 = KidFacilityDto.builder()
            .name("KG 2").address("гр. София, ул. Б")
            .kidFacilityType(KidFacilityType.NURSERY)
            .kidFacilityOwnershipType(KidFacilityOwnershipType.PRIVATE_MON)
            .build();

        KidFacilityIngestionService ingestor1 = Mockito.mock(KidFacilityIngestionService.class);
        when(ingestor1.ingest()).thenReturn(List.of(dto1));
        KidFacilityIngestionService ingestor2 = Mockito.mock(KidFacilityIngestionService.class);
        when(ingestor2.ingest()).thenReturn(List.of(dto2));

        GeocodingService geocodingService = Mockito.mock(GeocodingService.class);
        when(geocodingService.geocode("гр. София, ул. А"))
            .thenReturn(GeocodingResult.found(42.7, 23.3));
        when(geocodingService.geocode("гр. София, ул. Б"))
            .thenReturn(GeocodingResult.found(42.8, 23.4));

        KidFacilityService kidFacilityService = Mockito.mock(KidFacilityService.class);
        ManualFacilityOverrideService overrideService = Mockito.mock(ManualFacilityOverrideService.class);

        Instance<KidFacilityIngestionService> ingestors = Mockito.mock(Instance.class);
        when(ingestors.iterator()).thenReturn(List.of(ingestor1, ingestor2).iterator());

        SyncService syncService = new SyncService(ingestors, geocodingService, kidFacilityService, overrideService);
        syncService.run();

        verify(kidFacilityService).replaceAll(Mockito.argThat(facilities -> facilities.size() == 2));
        verify(overrideService).applyOverrides();
    }
}
