package com.nameri.detska.kindergarten.data.syncer.ingestion;

public class IngestionServiceException extends RuntimeException {

    public IngestionServiceException(String message) {
        super(message);
    }

    public IngestionServiceException(Throwable cause) {
        super(cause);
    }
}
