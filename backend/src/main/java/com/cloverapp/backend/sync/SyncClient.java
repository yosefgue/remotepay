package com.cloverapp.backend.sync;

import com.cloverapp.backend.customer.CustomerResponse;
import com.cloverapp.backend.inventory.ItemResponse;
import com.cloverapp.backend.merchant.CloverMerchantResponse;
import com.cloverapp.backend.auth.OAuthTokenEntity;
import com.cloverapp.backend.auth.OAuthTokenRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Component
public class SyncClient {
    private final OAuthTokenRepository oAuthTokenRepository;
    private final RestClient restClient;

    public SyncClient(OAuthTokenRepository oAuthTokenRepository) {
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.restClient = RestClient.create();
    }

    public void getItems(String merchantId) {
        OAuthTokenEntity oAuthTokenEntity = oAuthTokenRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new RuntimeException("OAuth token not found"));
        String accessToken = oAuthTokenEntity.getAccessToken();
        ItemResponse response = restClient.get()
                .uri("https://apisandbox.dev.clover.com/v3/merchants/{mId}/items", merchantId)
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ItemResponse.class);
    }

    public void getMerchant(String merchantId) {
        OAuthTokenEntity oAuthTokenEntity = oAuthTokenRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new RuntimeException("OAuth token not found"));
        String accessToken = oAuthTokenEntity.getAccessToken();
        CloverMerchantResponse response = restClient.get()
                .uri("https://apisandbox.dev.clover.com/v3/merchants/{mId}", merchantId)
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CloverMerchantResponse.class);
    }

    public void getCustomers(String merchantId) {
        OAuthTokenEntity oAuthTokenEntity = oAuthTokenRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new RuntimeException("OAuth token not found"));
        String accessToken = oAuthTokenEntity.getAccessToken();
        CustomerResponse response = restClient.get()
                .uri("https://apisandbox.dev.clover.com/v3/merchants/{mId}/customers?expand=phoneNumbers,emailAddresses", merchantId)
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CustomerResponse.class);
    }
}
