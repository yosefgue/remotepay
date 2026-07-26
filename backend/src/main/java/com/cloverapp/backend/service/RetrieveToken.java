package com.cloverapp.backend.service;

import com.cloverapp.backend.entity.OAuthToken;
import com.cloverapp.backend.repository.OAuthTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

@Service
public class RetrieveToken {
    @Value("${clover.app-id}")
    private String appId;
    @Value("${clover.app-secret}")
    private String appSecret;
    @Value("${clover.base-authorize-url}")
    private String authorizeHost;

    private final OAuthTokenRepository tokenRepository;
    private final RestClient restClient;

    public RetrieveToken(OAuthTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
        this.restClient = RestClient.create();
    }

    public void fetchAndSaveTokens(String code, String merchantId) {
        String tokenUrl = "https://" + authorizeHost + "/oauth/v2/token";

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
            throw new RuntimeException("Failed to exchange auth code for tokens with Clover");
        }
        // new token entity, also checks if merchant is new or already exists
        OAuthToken tokenEntity = tokenRepository.findByMerchantId(merchantId)
                .orElseGet(OAuthToken::new);

        Instant now = Instant.now();

        tokenEntity.setMerchantId(merchantId);
        tokenEntity.setAccessToken(response.access_token());
        tokenEntity.setRefreshToken(response.refresh_token());
        tokenEntity.setAccessTokenExpiresAt(now.plusSeconds(response.access_token_expiration()));
        tokenEntity.setRefreshTokenExpiresAt(now.plusSeconds(response.refresh_token_expiration()));
        // save tokens in db
        tokenRepository.save(tokenEntity);
    }

    // dto record that holds tokens
    private record CloverTokenResponse(
            String access_token,
            long access_token_expiration,
            String refresh_token,
            long refresh_token_expiration
    ) {}
}
