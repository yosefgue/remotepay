package com.cloverapp.backend.order;

import java.time.Instant;

public record OrderSummaryResponse(
        Long id,
        String title,
        String status,
        Long totalAmount,
        String linkToken,
        Instant createdAt
) {
    public static OrderSummaryResponse fromEntity(OrderEntity entity) {
        return new OrderSummaryResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getTotalAmount(),
                entity.getLinkToken(),
                entity.getCreatedAt()
        );
    }
}
