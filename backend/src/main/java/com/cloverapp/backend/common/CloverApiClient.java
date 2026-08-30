package com.cloverapp.backend.common;

import com.cloverapp.backend.auth.CloverTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;

@Component
public class CloverApiClient {

    private final RestClient restClient;
    private final CloverTokenService cloverTokenService;

    public CloverApiClient(
            CloverTokenService cloverTokenService,
            @Value("${clover.base-api-url}") String cloverApiBaseUrl
    ) {
        this.cloverTokenService = cloverTokenService;
        this.restClient = RestClient.builder()
                .baseUrl(cloverApiBaseUrl)
                .build();
    }

    public <T> T get(String merchantId, String path, Class<T> responseType) {
        String accessToken = cloverTokenService.getValidAccessToken(merchantId);

        return restClient.get()
                .uri(path) // baseUrl is already attached
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(responseType);
    }

    public <T> T get(String merchantId, Function<UriBuilder, URI> uriFunction, Class<T> responseType) {
        String accessToken = cloverTokenService.getValidAccessToken(merchantId);

        return restClient.get()
                .uri(uriFunction) // uriBuilder automatically prepends baseUrl
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(responseType);
    }
}