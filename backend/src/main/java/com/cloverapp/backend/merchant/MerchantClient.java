package com.cloverapp.backend.merchant;

import com.cloverapp.backend.common.CloverApiClient;
import org.springframework.stereotype.Component;

@Component
public class MerchantClient {
    private final CloverApiClient cloverApiClient;

    public MerchantClient(CloverApiClient cloverApiClient) {
        this.cloverApiClient = cloverApiClient;
    }

    public CloverMerchantResponse fetchMerchant(String merchantId) {
        return cloverApiClient.get(
                merchantId,
                "v3/merchants/" + merchantId,  CloverMerchantResponse.class);
    }
}
