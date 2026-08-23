package com.cloverapp.backend.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Objects;

@Component
public class CloverTokenClient {
    private final String appId;
    private final String appSecret;
    private final String authorizeHost;
    private final RestClient restClient;

    public CloverTokenClient(
            @Value("${clover.app-id}") String appId,
            @Value("${clover.app-secret}") String appSecret,
            @Value("${clover.base-authorize-url}") String authorizeHost) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.authorizeHost = authorizeHost;
        this.restClient = RestClient.create();
    }

    public CloverTokenResponse fetchTokenClover(String code) {
        Objects.requireNonNull(code, "code must not be null");

        String tokenUrl = authorizeHost + "/oauth/v2/token";

        // prepare payload as java object (dictionary, or map in this case)
        Map<String, String> requestPayload = Map.of(
                "client_id", appId,
                "client_secret", appSecret,
                "code", code
        );

        // http post clover api for tokens
        CloverTokenResponse response = restClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestPayload)
                .retrieve()
                .body(CloverTokenResponse.class);

        if (response == null) {
            throw new CloverTokenExchangeException("Failed to exchange auth code for tokens with Clover");
        }

        return response;
    }

    public CloverTokenResponse refreshCloverToken(String refreshToken) {
        Objects.requireNonNull(refreshToken, "refreshToken must not be null");

        String tokenUrl = authorizeHost + "/oauth/v2/refresh";

        Map<String, String> requestPayload = Map.of(
                "client_id", appId,
                "refresh_token", refreshToken
        );

        CloverTokenResponse response = restClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestPayload)
                .retrieve()
                .body(CloverTokenResponse.class);

        if (response == null) {
            throw new CloverTokenExchangeException("Failed to exchange refresh token for tokens with Clover");
        }

        return response;
    }

}
