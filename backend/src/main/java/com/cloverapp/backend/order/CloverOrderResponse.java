package com.cloverapp.backend.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CloverOrderResponse(
        OrderCart orderCart,
        Long subtotal,
        Long totalTaxAmount,
        Long total
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderCart(
            String id,
            String currency,
            String state
    ) {}

    public String getId() {
        return orderCart != null ? orderCart.id() : null;
    }
}
