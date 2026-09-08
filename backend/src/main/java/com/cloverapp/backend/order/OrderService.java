package com.cloverapp.backend.order;

import com.cloverapp.backend.customer.CustomerEntity;
import com.cloverapp.backend.customer.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final OrderClient orderClient;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerRepository customerRepository,
            OrderClient orderClient
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.orderClient = orderClient;
    }

    @Transactional
    public OrderDetailResponse saveDraft(String merchantId, OrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setMerchantId(merchantId);
        order.setCustomerId(resolveCustomerId(merchantId, request));
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

        order.setCustomerId(resolveCustomerId(merchantId, request));
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
        List<OrderEntity> orders = orderRepository.findByMerchantId(merchantId);
        if (orders.isEmpty()) {
            return List.of();
        }

        Map<String, String> customerNames = customerRepository.findByMerchantId(merchantId)
                .stream()
                .filter(c -> c.getCustomerId() != null && !c.getCustomerId().isBlank())
                .collect(Collectors.toMap(
                        CustomerEntity::getCustomerId,
                        c -> c.getFullName() != null ? c.getFullName() : "",
                        (a, b) -> a
                ));

        return orders.stream()
                .map(order -> {
                    String name = order.getCustomerId() != null
                            ? customerNames.get(order.getCustomerId())
                            : null;
                    return OrderSummaryResponse.of(order, name);
                })
                .toList();
    }

    private String resolveCustomerId(String merchantId, OrderRequest request) {
        if (request.customerId() != null && !request.customerId().isBlank()) {
            return request.customerId();
        }

        if (request.customer() != null && hasAnyCustomerInfo(request.customer())) {
            OrderRequest.CustomerInfo info = request.customer();

            CustomerEntity customer = new CustomerEntity(
                    null,
                    merchantId,
                    info.firstName(),
                    info.lastName(),
                    info.email(),
                    info.phoneNumber()
            );
            customerRepository.save(customer);

            return null;
        }

        return null;
    }

    private boolean hasAnyCustomerInfo(OrderRequest.CustomerInfo info) {
        return (info.firstName() != null && !info.firstName().isBlank())
                || (info.lastName() != null && !info.lastName().isBlank())
                || (info.email() != null && !info.email().isBlank())
                || (info.phoneNumber() != null && !info.phoneNumber().isBlank());
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
