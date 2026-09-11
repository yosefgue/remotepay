package com.cloverapp.backend.order;

import com.cloverapp.backend.customer.*;
import com.cloverapp.backend.merchant.MerchantService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerService customerService;
    private final MerchantService merchantService;
    private final OrderClient orderClient;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerService customerService,
            MerchantService merchantService,
            OrderClient orderClient
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerService = customerService;
        this.merchantService = merchantService;
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

        return saveOrderAndItems(order, request.items());
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

        orderItemRepository.deleteByOrderId(orderId);
        return saveOrderAndItems(order, request.items());
    }

    @Transactional
    public OrderDetailResponse createCloverOrder(String merchantId, OrderRequest request) {
        String customerId = resolveCustomerId(merchantId, request);

        CloverOrderRequest cloverReq = CloverOrderRequest.fromItemRequests(request.items());
        CloverOrderResponse cloverResponse = orderClient.createAtomicOrder(merchantId, cloverReq);

        OrderEntity order = new OrderEntity();
        order.setMerchantId(merchantId);
        order.setCustomerId(customerId);
        order.setCloverOrderId(cloverResponse.getId());
        order.setTitle(request.title());
        order.setStatus("OPEN");
        order.setLinkToken(UUID.randomUUID().toString().replace("-", ""));
        order.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));

        long subtotal = cloverResponse.subtotal() != null ? cloverResponse.subtotal() : calculateSubtotal(request.items());
        long tax = cloverResponse.totalTaxAmount() != null ? cloverResponse.totalTaxAmount() : 0L;
        long total = cloverResponse.total() != null ? cloverResponse.total() : subtotal;

        order.setSubtotalAmount(subtotal);
        order.setTaxAmount(tax);
        order.setTotalAmount(total);

        return saveOrderAndItems(order, request.items());
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(String merchantId, Long orderId) {
        OrderEntity order = orderRepository.findByMerchantIdAndId(merchantId, orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        List<OrderItemEntity> items = orderItemRepository.findByOrderId(orderId);
        CustomerResponse customer = customerService.getCustomer(merchantId, order.getCustomerId());
        String merchantName = merchantService.getMerchantName(merchantId);

        return OrderDetailResponse.of(order, items, customer, merchantName);
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getOrders(String merchantId) {
        List<OrderEntity> orders = orderRepository.findByMerchantId(merchantId);
        if (orders.isEmpty()) {
            return List.of();
        }

        Map<String, String> customerNames = customerService.getCustomerNamesMap(merchantId);

        return orders.stream()
                .map(order -> {
                    String name = order.getCustomerId() != null
                            ? customerNames.get(order.getCustomerId())
                            : null;
                    return OrderSummaryResponse.of(order, name);
                })
                .toList();
    }

    @Transactional
    public void deleteDraftOrder(String merchantId, Long id) {
        OrderEntity order = orderRepository.findByMerchantIdAndId(merchantId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!"DRAFT".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only draft orders can be deleted");
        }

        orderRepository.delete(order);
    }

    private OrderDetailResponse saveOrderAndItems(OrderEntity order, List<OrderRequest.ItemRequest> items) {
        OrderEntity savedOrder = orderRepository.save(order);

        List<OrderItemEntity> itemEntities = toItemEntities(savedOrder.getId(), items);
        List<OrderItemEntity> savedItems = orderItemRepository.saveAll(itemEntities);

        CustomerResponse customer = customerService.getCustomer(order.getMerchantId(), order.getCustomerId());
        return OrderDetailResponse.of(savedOrder, savedItems, customer);
    }

    private String resolveCustomerId(String merchantId, OrderRequest request) {
        if (request.customerId() != null && !request.customerId().isBlank()) {
            return request.customerId();
        }

        if (request.customer() != null && hasAnyCustomerInfo(request.customer())) {
            CustomerRequest cloverRequest = new CustomerRequest(
                    request.customer().firstName(),
                    request.customer().lastName(),
                    request.customer().email(),
                    request.customer().phoneNumber()
            );

            CustomerResponse createdCustomer = customerService.createCustomer(merchantId, cloverRequest);
            return createdCustomer.customerId();
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
