package com.nameri.detska;

import io.quarkus.cache.CacheResult;
import lombok.RequiredArgsConstructor;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class KidFacilityService {

    private final KidFacilityRepository kidFacilityRepository;

    @CacheResult(cacheName = "facilities")
    public List<KidFacility> getAllFacilities() {
        return kidFacilityRepository.findAll();
    }
}
