package com.cloverapp.backend.auth;

import com.cloverapp.backend.merchant.CloverMerchantResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
    private final String apiHost;
    private final RestClient restClient;

    public CloverTokenClient(
            @Value("${clover.app-id}") String appId,
            @Value("${clover.app-secret}") String appSecret,
            @Value("${clover.base-authorize-url}") String authorizeHost,
            @Value("${clover.base-api-url}") String apiHost) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.authorizeHost = authorizeHost;
        this.apiHost = apiHost;
        this.restClient = RestClient.create();
    }

    public CloverTokenResponse fetchTokenClover(String code) {
        Objects.requireNonNull(code, "code must not be null");

        String tokenUrl = authorizeHost + "/oauth/v2/token";

        Map<String, String> requestPayload = Map.of(
                "client_id", appId,
                "client_secret", appSecret,
                "code", code
        );

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

    public CloverMerchantResponse fetchAuthMerchant(String merchantId, String accessToken) {
        Objects.requireNonNull(merchantId, "merchantId must not be null");
        Objects.requireNonNull(accessToken, "accessToken must not be null");

        return restClient.get()
                .uri(apiHost + "/v3/merchants/{mId}", merchantId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CloverMerchantResponse.class);
    }
}