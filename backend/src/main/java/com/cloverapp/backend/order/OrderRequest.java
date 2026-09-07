package com.cloverapp.backend.order;

import java.util.List;

public record OrderRequest(
        String title,
        String customerId,
        List<ItemRequest> items
) {
    public record ItemRequest(
            String itemId,
            String name,
            Long price,
            Integer quantity
    ) {}
}
