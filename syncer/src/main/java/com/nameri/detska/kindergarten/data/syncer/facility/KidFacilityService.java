package com.nameri.detska.kindergarten.data.syncer.facility;

import lombok.RequiredArgsConstructor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class KidFacilityService {

    private final KidFacilityRepository kidFacilityRepository;

    @Transactional
    public void replaceAll(List<KidFacility> facilities) {
        kidFacilityRepository.replaceAll(facilities);
    }
}
