package com.cloverapp.backend.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
public class CloverTokenService {

    private static final Logger log = LoggerFactory.getLogger(CloverTokenService.class);

    private final OAuthTokenRepository tokenRepository;
    private final CloverTokenClient cloverTokenClient;

    public CloverTokenService(OAuthTokenRepository tokenRepository, CloverTokenClient cloverTokenClient) {
        this.tokenRepository = tokenRepository;
        this.cloverTokenClient = cloverTokenClient;
    }

    @Transactional
    public void saveTokens(String merchantId, CloverTokenResponse response) {
        Objects.requireNonNull(merchantId, "merchantId must not be null");
        Objects.requireNonNull(response, "response must not be null");

        OAuthTokenEntity tokenEntity = tokenRepository.findByMerchantId(merchantId)
                .orElseGet(OAuthTokenEntity::new);

        tokenEntity.setMerchantId(merchantId);
        tokenEntity.setAccessToken(response.access_token());
        tokenEntity.setRefreshToken(response.refresh_token());
        tokenEntity.setAccessTokenExpiresAt(Instant.ofEpochSecond(response.access_token_expiration()));
        tokenEntity.setRefreshTokenExpiresAt(Instant.ofEpochSecond(response.refresh_token_expiration()));

        tokenRepository.save(tokenEntity);
    }

    public boolean hasValidRefreshToken(String merchantId) {
        Objects.requireNonNull(merchantId, "merchantId must not be null");

        OAuthTokenEntity token = tokenRepository.findByMerchantId(merchantId).orElse(null);

        if (token == null || token.getRefreshTokenExpiresAt() == null) {
            return false;
        }

        return Instant.now().isBefore(token.getRefreshTokenExpiresAt().minusSeconds(300));
    }

    @Transactional
    public String getValidAccessToken(String merchantId) {
        Objects.requireNonNull(merchantId, "merchantId must not be null");
        OAuthTokenEntity token = tokenRepository.findByMerchantId(merchantId).orElse(null);

        if (token == null) {
            return null;
        }

        // token expired or expiring in < 60s -> refresh it
        if (!Instant.now().isBefore(token.getAccessTokenExpiresAt().minusSeconds(60))) {
            try {
                String refreshToken = token.getRefreshToken();
                CloverTokenResponse response = cloverTokenClient.refreshCloverToken(refreshToken);
                saveTokens(merchantId, response);
                return response.access_token();
            } catch (Exception e) {
                log.warn("Failed to refresh Clover token for merchant {}: {}", merchantId, e.getMessage());
                return null;
            }
        }
        return token.getAccessToken();
    }
}