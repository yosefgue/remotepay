package com.cloverapp.backend.order;

import com.cloverapp.backend.customer.CustomerResponse;

import java.time.Instant;
import java.util.List;

public record OrderDetailResponse(
        Long id,
        String title,
        CustomerResponse customer,
        String status,
        String currency,
        Long subtotalAmount,
        Long taxAmount,
        Long totalAmount,
        String linkToken,
        Instant createdAt,
        List<OrderItemResponse> items
) {
    public record OrderItemResponse(
            Long id,
            String itemId,
            String name,
            Long price,
            Integer quantity
    ) {
        public static OrderItemResponse fromEntity(OrderItemEntity entity) {
            return new OrderItemResponse(
                    entity.getId(),
                    entity.getCloverItemId(),
                    entity.getName(),
                    entity.getPrice(),
                    entity.getQuantity()
            );
        }
    }

    public static OrderDetailResponse of(OrderEntity order, List<OrderItemEntity> items, CustomerResponse customer) {
        List<OrderItemResponse> itemResponses = items != null
                ? items.stream().map(OrderItemResponse::fromEntity).toList()
                : List.of();

        return new OrderDetailResponse(
                order.getId(),
                order.getTitle(),
                customer,
                order.getStatus(),
                order.getCurrency(),
                order.getSubtotalAmount(),
                order.getTaxAmount(),
                order.getTotalAmount(),
                order.getLinkToken(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}
