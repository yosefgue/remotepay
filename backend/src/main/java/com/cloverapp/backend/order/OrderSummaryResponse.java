package com.cloverapp.backend.order;

import java.time.Instant;

public record OrderSummaryResponse(
        Long id,
        String customerName,
        String status,
        Long totalAmount,
        String linkToken,
        Instant createdAt
) {
    public static OrderSummaryResponse of(OrderEntity order, String customerName) {
        return new OrderSummaryResponse(
                order.getId(),
                customerName,
                order.getStatus(),
                order.getTotalAmount(),
                order.getLinkToken(),
                order.getCreatedAt()
        );
    }
}
