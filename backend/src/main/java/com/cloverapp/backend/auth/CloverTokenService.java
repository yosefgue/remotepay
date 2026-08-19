package com.cloverapp.backend.auth;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
public class CloverTokenService {

    private final OAuthTokenRepository tokenRepository;
    private final CloverTokenClient cloverTokenClient;

    public CloverTokenService(OAuthTokenRepository tokenRepository,  CloverTokenClient cloverTokenClient) {
        this.tokenRepository = tokenRepository;
        this.cloverTokenClient = cloverTokenClient;
    }

    private void saveTokenToDb(String merchantId, CloverTokenResponse response) {
        Objects.requireNonNull(merchantId, "merchantId must not be null");
        Objects.requireNonNull(response, "response must not be null");

        OAuthTokenEntity tokenEntity = tokenRepository.findByMerchantId(merchantId)
                .orElseGet(OAuthTokenEntity::new);

        Instant now = Instant.now();

        tokenEntity.setMerchantId(merchantId);
        tokenEntity.setAccessToken(response.access_token());
        tokenEntity.setRefreshToken(response.refresh_token());
        tokenEntity.setAccessTokenExpiresAt(now.plusSeconds(response.access_token_expiration()));
        tokenEntity.setRefreshTokenExpiresAt(now.plusSeconds(response.refresh_token_expiration()));
        // save tokens in db
        tokenRepository.save(tokenEntity);
    }

    public void fetchAndSaveTokens(String code, String merchantId) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(merchantId, "merchantId must not be null");

        CloverTokenResponse response = cloverTokenClient.fetchTokenClover(code);
        saveTokenToDb(merchantId, response);
    }

    public boolean hasValidRefreshToken(String merchantId) {
        Objects.requireNonNull(merchantId, "merchantId must not be null");

        OAuthTokenEntity token = tokenRepository.findByMerchantId(merchantId).orElse(null);

        if (token == null || token.getRefreshTokenExpiresAt() == null) {
            return false;
        }

        return Instant.now().isBefore(token.getRefreshTokenExpiresAt().minusSeconds(300));
    }
}
