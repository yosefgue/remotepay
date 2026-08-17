package com.cloverapp.backend.merchant;

import com.cloverapp.backend.auth.OAuthTokenEntity;
import com.cloverapp.backend.common.CloverApiClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class MerchantSync {
    private final CloverApiClient cloverApiClient;

    public MerchantSync(CloverApiClient cloverApiClient) {
        this.cloverApiClient = cloverApiClient;
    }

    public void getMerchant(String merchantId) {
        OAuthTokenEntity oAuthTokenEntity = cloverApiClient.oAuthTokenRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new RuntimeException("OAuth token not found"));
        String accessToken = oAuthTokenEntity.getAccessToken();
        CloverMerchantResponse response = cloverApiClient.restClient.get()
                .uri("https://apisandbox.dev.clover.com/v3/merchants/{mId}", merchantId)
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CloverMerchantResponse.class);
    }
}
