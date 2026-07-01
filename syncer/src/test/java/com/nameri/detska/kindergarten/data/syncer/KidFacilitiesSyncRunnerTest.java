package com.nameri.detska.kindergarten.data.syncer;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.nameri.detska.kindergarten.data.syncer.sync.SyncService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;

class KidFacilitiesSyncRunnerTest {

    @Test
    void shouldReturnZeroOnSuccess() {
        SyncService syncService = Mockito.mock(SyncService.class);
        KidFacilitiesSyncRunner runner = new KidFacilitiesSyncRunner(syncService);

        int exitCode = runner.run();

        assertEquals(0, exitCode);
    }

    @Test
    void shouldReturnOneOnFailure() {
        SyncService syncService = Mockito.mock(SyncService.class);
        doThrow(new RuntimeException("sync failed")).when(syncService).run();

        KidFacilitiesSyncRunner runner = new KidFacilitiesSyncRunner(syncService);

        int exitCode = runner.run();

        assertEquals(1, exitCode);
    }
}
