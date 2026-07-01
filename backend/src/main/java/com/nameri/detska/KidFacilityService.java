package com.nameri.detska;

import lombok.RequiredArgsConstructor;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

import io.quarkus.cache.CacheResult;

@ApplicationScoped
@RequiredArgsConstructor
public class KidFacilityService {

    private final KidFacilityRepository kidFacilityRepository;

    @CacheResult(cacheName = "facilities")
    public List<KidFacility> getAllFacilities() {
        return kidFacilityRepository.findAll();
    }
}
