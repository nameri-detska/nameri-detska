package com.nameri.detska;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KidFacility {

    private UUID id;
    private String name;
    private KidFacilityType kidFacilityType;
    private KidFacilityOwnershipType kidFacilityOwnershipType;
    private String address;
    private double latitude;
    private double longitude;
}
