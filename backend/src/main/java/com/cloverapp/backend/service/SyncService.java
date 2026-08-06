package com.cloverapp.backend.service;

import com.cloverapp.backend.dto.CustomerResponse;
import com.cloverapp.backend.dto.ItemResponse;
import com.cloverapp.backend.dto.MerchantDTO;
import com.cloverapp.backend.entity.OAuthToken;
import com.cloverapp.backend.repository.OAuthTokenRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SyncService {
    private final OAuthTokenRepository oAuthTokenRepository;
    private final RestClient restClient;

    public SyncService(OAuthTokenRepository oAuthTokenRepository, RetrieveToken retrieveToken) {
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.restClient = RestClient.create();
    }

    public void getItems(String merchantId) {
        OAuthToken oAuthToken = oAuthTokenRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new RuntimeException("OAuth token not found"));
        String accessToken = oAuthToken.getAccessToken();
        ItemResponse response = restClient.get()
                .uri("https://apisandbox.dev.clover.com/v3/merchants/{mId}/items", merchantId)
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ItemResponse.class);
    }

    public void getMerchant(String merchantId) {
        OAuthToken oAuthToken = oAuthTokenRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new RuntimeException("OAuth token not found"));
        String accessToken = oAuthToken.getAccessToken();
        MerchantDTO response = restClient.get()
                .uri("https://apisandbox.dev.clover.com/v3/merchants/{mId}", merchantId)
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(MerchantDTO.class);
    }

    public void getCustomers(String merchantId) {
        OAuthToken oAuthToken = oAuthTokenRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new RuntimeException("OAuth token not found"));
        String accessToken = oAuthToken.getAccessToken();
        CustomerResponse response = restClient.get()
                .uri("https://apisandbox.dev.clover.com/v3/merchants/{mId}/customers?expand=phoneNumbers,emailAddresses", merchantId)
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CustomerResponse.class);
    }
}
