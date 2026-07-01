package com.nameri.detska.kindergarten.data.syncer.ingestion;

import java.util.List;

/**
 * Service for ingesting kid facilities.
 */
public interface KidFacilityIngestionService {

    /**
     * Ingests kid facilities.
     *
     * @return the list of ingested kid facilities
     */
    List<KidFacilityDto> ingest();
}
