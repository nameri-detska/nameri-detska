package com.nameri.detska;

import lombok.RequiredArgsConstructor;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class KidFacilityResource {

    private final KidFacilityService kidFacilityService;

    @GET
    @Path("facilities")
    public List<KidFacility> getAllFacilities() {
        return kidFacilityService.getAllFacilities();
    }
}
