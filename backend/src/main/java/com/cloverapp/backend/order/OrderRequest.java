package com.cloverapp.backend.order;

import java.util.List;

public record OrderRequest(
        String title,
        String customerId,
        CustomerInfo customer,
        List<ItemRequest> items
) {
    public record CustomerInfo(
            String firstName,
            String lastName,
            String email,
            String phoneNumber
    ) {}

    public record ItemRequest(
            String itemId,
            String name,
            Long price,
            Integer quantity
    ) {}
}
