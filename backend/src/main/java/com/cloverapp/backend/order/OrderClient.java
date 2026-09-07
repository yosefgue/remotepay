package com.cloverapp.backend.order;

import com.cloverapp.backend.common.CloverApiClient;
import org.springframework.stereotype.Component;

@Component
public class OrderClient {

    private final CloverApiClient cloverApiClient;

    public OrderClient(CloverApiClient cloverApiClient) {
        this.cloverApiClient = cloverApiClient;
    }

    public CloverOrderResponse createAtomicOrder(String merchantId, CloverOrderRequest request) {
        return cloverApiClient.post(
                merchantId,
                "/v3/merchants/" + merchantId + "/atomic_order/orders",
                request,
                CloverOrderResponse.class
        );
    }
}
