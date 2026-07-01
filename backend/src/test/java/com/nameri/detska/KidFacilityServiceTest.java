package com.nameri.detska;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class KidFacilityServiceTest {

    @Test
    void shouldDelegateToRepository() {
        List<KidFacility> expected = List.of(
            KidFacility.builder().name("ДГ №1").build()
        );

        KidFacilityRepository repository = new KidFacilityRepository(null) {

            @Override
            public List<KidFacility> findAll() {
                return expected;
            }
        };

        KidFacilityService service = new KidFacilityService(repository);
        List<KidFacility> result = service.getAllFacilities();

        assertSame(expected, result);
    }

    @Test
    void shouldHandleEmptyList() {
        KidFacilityRepository repository = new KidFacilityRepository(null) {

            @Override
            public List<KidFacility> findAll() {
                return List.of();
            }
        };

        KidFacilityService service = new KidFacilityService(repository);
        List<KidFacility> result = service.getAllFacilities();

        assertEquals(0, result.size());
    }
}
