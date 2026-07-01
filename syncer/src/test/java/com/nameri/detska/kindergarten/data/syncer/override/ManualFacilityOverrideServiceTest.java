package com.nameri.detska.kindergarten.data.syncer.override;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nameri.detska.kindergarten.data.syncer.facility.KidFacility;
import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityRepository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManualFacilityOverrideServiceTest {

    private KidFacilityRepository repository;
    private ManualFacilityOverrideService overrideService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        repository = mock(KidFacilityRepository.class);
        overrideService = new ManualFacilityOverrideService(repository);

        resetMaps();
    }

    @SuppressWarnings("unchecked")
    private void resetMaps() throws Exception {
        Field coordsField = ManualFacilityOverrideService.class.getDeclaredField("coordinatesOverrides");
        coordsField.setAccessible(true);
        coordsField.set(overrideService, new LinkedHashMap<>());

        Field addrField = ManualFacilityOverrideService.class.getDeclaredField("addressesOverrides");
        addrField.setAccessible(true);
        addrField.set(overrideService, new LinkedHashMap<>());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldOverrideCoordinates() throws Exception {
        Field coordsField = ManualFacilityOverrideService.class.getDeclaredField("coordinatesOverrides");
        coordsField.setAccessible(true);
        Map<String, double[]> coords = (Map<String, double[]>) coordsField.get(overrideService);
        coords.put("гр. София, ул. А", new double[]{42.7, 23.3});

        KidFacility facility = KidFacility.builder()
            .address("гр. София, ул. А").latitude(0.0).longitude(0.0).build();
        PanacheQuery<KidFacility> query = mock(PanacheQuery.class);
        when(query.firstResultOptional()).thenReturn(Optional.of(facility));
        when(repository.find("address", "гр. София, ул. А")).thenReturn(query);

        overrideService.applyOverrides();

        assertEquals(42.7, facility.latitude(), 0.0001);
        assertEquals(23.3, facility.longitude(), 0.0001);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldOverrideAddress() throws Exception {
        Field addrField = ManualFacilityOverrideService.class.getDeclaredField("addressesOverrides");
        addrField.setAccessible(true);
        Map<String, String> addrs = (Map<String, String>) addrField.get(overrideService);
        addrs.put("гр. София, ул. А", "гр. София, ул. Б");

        KidFacility facility = KidFacility.builder().address("гр. София, ул. А").build();
        PanacheQuery<KidFacility> query = mock(PanacheQuery.class);
        when(query.firstResultOptional()).thenReturn(Optional.of(facility));
        when(repository.find("address", "гр. София, ул. А")).thenReturn(query);

        overrideService.applyOverrides();

        assertEquals("гр. София, ул. Б", facility.address());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSkipOverrideWhenFacilityNotFound() throws Exception {
        Field coordsField = ManualFacilityOverrideService.class.getDeclaredField("coordinatesOverrides");
        coordsField.setAccessible(true);
        Map<String, double[]> coords = (Map<String, double[]>) coordsField.get(overrideService);
        coords.put("гр. София, ул. X", new double[]{42.7, 23.3});

        PanacheQuery<KidFacility> query = mock(PanacheQuery.class);
        when(query.firstResultOptional()).thenReturn(Optional.empty());
        when(repository.find("address", "гр. София, ул. X")).thenReturn(query);

        overrideService.applyOverrides();
    }

    @Test
    void shouldNotCallRepositoryWhenNoOverrides() {
        overrideService.applyOverrides();

        verify(repository, never()).find(anyString(), (Object[]) any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldApplyBothCoordinateAndAddressOverrides() throws Exception {
        Field coordsField = ManualFacilityOverrideService.class.getDeclaredField("coordinatesOverrides");
        coordsField.setAccessible(true);
        Map<String, double[]> coords = (Map<String, double[]>) coordsField.get(overrideService);
        coords.put("гр. София, ул. А", new double[]{42.7, 23.3});

        Field addrField = ManualFacilityOverrideService.class.getDeclaredField("addressesOverrides");
        addrField.setAccessible(true);
        Map<String, String> addrs = (Map<String, String>) addrField.get(overrideService);
        addrs.put("гр. Бургас, ул. Б", "гр. Бургас, ул. В");

        KidFacility facility1 = KidFacility.builder()
            .address("гр. София, ул. А").latitude(0.0).longitude(0.0).build();
        PanacheQuery<KidFacility> query1 = mock(PanacheQuery.class);
        when(query1.firstResultOptional()).thenReturn(Optional.of(facility1));
        when(repository.find("address", "гр. София, ул. А")).thenReturn(query1);

        KidFacility facility2 = KidFacility.builder().address("гр. Бургас, ул. Б").build();
        PanacheQuery<KidFacility> query2 = mock(PanacheQuery.class);
        when(query2.firstResultOptional()).thenReturn(Optional.of(facility2));
        when(repository.find("address", "гр. Бургас, ул. Б")).thenReturn(query2);

        overrideService.applyOverrides();

        assertEquals(42.7, facility1.latitude(), 0.0001);
        assertEquals(23.3, facility1.longitude(), 0.0001);
        assertEquals("гр. Бургас, ул. В", facility2.address());
    }
}
