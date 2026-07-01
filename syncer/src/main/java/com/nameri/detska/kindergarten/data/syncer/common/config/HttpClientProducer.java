package com.nameri.detska.kindergarten.data.syncer.common.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.net.http.HttpClient;

@ApplicationScoped
public class HttpClientProducer {

    @Produces
    @ApplicationScoped
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }
}
