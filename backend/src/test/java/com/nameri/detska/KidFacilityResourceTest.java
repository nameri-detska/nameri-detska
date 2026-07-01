package com.nameri.detska;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KidFacilityResourceTest {

    @Test
    void shouldReturnFacilitiesFromService() {
        List<KidFacility> facilities = List.of(
            KidFacility.builder()
                .name("ДГ №1")
                .kidFacilityType(KidFacilityType.KINDERGARTEN)
                .kidFacilityOwnershipType(KidFacilityOwnershipType.MUNICIPAL)
                .address("гр. София, ул. А")
                .latitude(42.7)
                .longitude(23.3)
                .build()
        );

        KidFacilityService service = new KidFacilityService(null) {

            @Override
            public List<KidFacility> getAllFacilities() {
                return facilities;
            }
        };

        KidFacilityResource resource = new KidFacilityResource(service);
        List<KidFacility> result = resource.getAllFacilities();

        assertEquals(1, result.size());
        assertEquals("ДГ №1", result.get(0).getName());
    }
}
