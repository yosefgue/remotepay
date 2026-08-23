package com.cloverapp.backend.common;

import com.cloverapp.backend.auth.OAuthTokenRepository;
import com.cloverapp.backend.auth.CloverTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CloverApiClient {
    public final RestClient restClient;
    public final CloverTokenService cloverTokenService;
    private final String cloverApiBaseUrl;

    public CloverApiClient(CloverTokenService cloverTokenService, @Value("${clover.base-api-url}") String cloverApiBaseUrl) {
        this.restClient = RestClient.create();
        this.cloverTokenService = cloverTokenService;
        this.cloverApiBaseUrl = cloverApiBaseUrl;
    }

    public <T> T get(String merchantId, String path, Class<T> responseType) {
        String accessToken = cloverTokenService.getValidAccessToken(merchantId);

        return restClient.get()
                .uri(cloverApiBaseUrl + path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(responseType);
    }

}
