package com.nameri.detska.kindergarten.data.syncer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.nameri.detska.kindergarten.data.syncer.sync.SyncService;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@Slf4j
@QuarkusMain
@RequiredArgsConstructor
public class KidFacilitiesSyncRunner implements QuarkusApplication {

    private final SyncService syncService;

    @Override
    public int run(String... args) {
        try {
            syncService.run();
            return 0;
        } catch (Exception e) {
            log.error("Sync failed", e);
            return 1;
        }
    }
}
