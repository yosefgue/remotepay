package com.cloverapp.backend.auth;

import com.cloverapp.backend.merchant.CloverMerchantResponse;
import com.cloverapp.backend.merchant.MerchantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class AuthOnboardingService {

    private final CloverTokenClient tokenClient;
    private final MerchantService merchantService;
    private final CloverTokenService tokenService;

    public AuthOnboardingService(CloverTokenClient tokenClient,
                              MerchantService merchantService,
                              CloverTokenService tokenService) {
        this.tokenClient = tokenClient;
        this.merchantService = merchantService;
        this.tokenService = tokenService;
    }

    @Transactional
    public void onboardMerchantWithTokens(String code, String merchantId) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(merchantId, "merchantId must not be null");

        CloverTokenResponse tokenResponse = tokenClient.fetchTokenClover(code);
        CloverMerchantResponse merchantResponse = tokenClient.fetchAuthMerchant(
                merchantId,
                tokenResponse.access_token()
        );

        if (!merchantId.equals(merchantResponse.id())) {
            throw new IllegalStateException("Mismatched merchant ID: query param was "
                    + merchantId + " but Clover API returned " + merchantResponse.id());
        }

        merchantService.saveMerchant(merchantResponse.id(), merchantResponse.name());
        tokenService.saveTokens(merchantResponse.id(), tokenResponse);
    }
}