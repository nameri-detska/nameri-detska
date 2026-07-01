package com.nameri.detska.kindergarten.data.syncer.ingestion;

import lombok.Builder;

import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityOwnershipType;
import com.nameri.detska.kindergarten.data.syncer.facility.KidFacilityType;

@Builder
public record KidFacilityDto(String name, KidFacilityType kidFacilityType, KidFacilityOwnershipType kidFacilityOwnershipType, String address) {

}
