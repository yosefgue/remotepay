package com.cloverapp.backend.auth;

public class DBTokenService {
    private final OAuthTokenRepository repository;
    public DBTokenService(OAuthTokenRepository repository) {
        this.repository = repository;
    };
    public String getToken(String merchantId) {
        OAuthTokenEntity token = repository.findByMerchantId(merchantId).orElseThrow(() -> new RuntimeException("Merchant token not found"));
        return token.getAccessToken();
    }
}