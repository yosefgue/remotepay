package com.cloverapp.backend.order;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderClient orderClient;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderClient orderClient
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderClient = orderClient;
    }

    @Transactional
    public OrderDetailResponse saveDraft(String merchantId, OrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setMerchantId(merchantId);
        order.setCustomerId(request.customerId());
        order.setTitle(request.title());

        long subtotal = calculateSubtotal(request.items());
        order.setSubtotalAmount(subtotal);
        order.setTotalAmount(subtotal);

        OrderEntity savedOrder = orderRepository.save(order);

        List<OrderItemEntity> itemEntities = toItemEntities(savedOrder.getId(), request.items());
        List<OrderItemEntity> savedItems = orderItemRepository.saveAll(itemEntities);

        return OrderDetailResponse.of(savedOrder, savedItems);
    }

    @Transactional
    public OrderDetailResponse updateDraft(String merchantId, Long orderId, OrderRequest request) {
        OrderEntity order = orderRepository.findByMerchantIdAndId(merchantId, orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!"DRAFT".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only draft orders can be edited");
        }

        order.setCustomerId(request.customerId());
        order.setTitle(request.title());

        long subtotal = calculateSubtotal(request.items());
        order.setSubtotalAmount(subtotal);
        order.setTotalAmount(subtotal);

        OrderEntity updatedOrder = orderRepository.save(order);

        orderItemRepository.deleteByOrderId(orderId);
        List<OrderItemEntity> newItemEntities = toItemEntities(orderId, request.items());
        List<OrderItemEntity> savedItems = orderItemRepository.saveAll(newItemEntities);

        return OrderDetailResponse.of(updatedOrder, savedItems);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(String merchantId, Long orderId) {
        OrderEntity order = orderRepository.findByMerchantIdAndId(merchantId, orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        List<OrderItemEntity> items = orderItemRepository.findByOrderId(orderId);
        return OrderDetailResponse.of(order, items);
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getOrders(String merchantId) {
        return orderRepository.findByMerchantId(merchantId)
                .stream()
                .map(OrderSummaryResponse::fromEntity)
                .toList();
    }

    private long calculateSubtotal(List<OrderRequest.ItemRequest> items) {
        if (items == null || items.isEmpty()) {
            return 0L;
        }

        return items.stream()
                .mapToLong(item -> {
                    long price = item.price() != null ? item.price() : 0L;
                    int qty = item.quantity() != null && item.quantity() > 0 ? item.quantity() : 1;
                    return price * qty;
                })
                .sum();
    }

    private List<OrderItemEntity> toItemEntities(Long orderId, List<OrderRequest.ItemRequest> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        List<OrderItemEntity> entities = new ArrayList<>();
        for (OrderRequest.ItemRequest item : items) {
            int qty = item.quantity() != null && item.quantity() > 0 ? item.quantity() : 1;
            entities.add(new OrderItemEntity(
                    orderId,
                    item.itemId(),
                    item.name(),
                    item.price() != null ? item.price() : 0L,
                    qty
            ));
        }
        return entities;
    }
}
