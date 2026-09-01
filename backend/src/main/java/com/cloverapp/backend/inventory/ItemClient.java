package com.cloverapp.backend.inventory;

import com.cloverapp.backend.common.CloverApiClient;
import org.springframework.stereotype.Component;

@Component
public class ItemClient {

    private static final int DEFAULT_LIMIT = 100;
    private static final String EXPAND_FIELDS = "itemStock";

    private final CloverApiClient cloverApiClient;

    public ItemClient(CloverApiClient cloverApiClient) {
        this.cloverApiClient = cloverApiClient;
    }

    public CloverItemResponse getItems(String merchantId) {
        return getItems(merchantId, DEFAULT_LIMIT, 0);
    }

    public CloverItemResponse getItems(String merchantId, int limit, int offset) {
        return cloverApiClient.get(
                merchantId,
                uriBuilder -> uriBuilder
                        .path("/v3/merchants/{mId}/items")
                        .queryParam("expand", EXPAND_FIELDS)
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build(merchantId),
                CloverItemResponse.class
        );
    }
}