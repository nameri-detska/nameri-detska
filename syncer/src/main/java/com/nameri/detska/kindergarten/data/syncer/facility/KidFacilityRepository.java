package com.nameri.detska.kindergarten.data.syncer.facility;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class KidFacilityRepository implements PanacheRepositoryBase<KidFacility, UUID> {

    void replaceAll(List<KidFacility> facilities) {
        deleteAll();
        persist(facilities);
        getEntityManager().flush();
    }
}
