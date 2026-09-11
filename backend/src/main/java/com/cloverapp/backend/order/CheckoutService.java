package com.cloverapp.backend.order;

import com.cloverapp.backend.customer.CustomerResponse;
import com.cloverapp.backend.customer.CustomerService;
import com.cloverapp.backend.merchant.MerchantService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class CheckoutService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerService customerService;
    private final MerchantService merchantService;

    public CheckoutService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerService customerService,
            MerchantService merchantService
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerService = customerService;
        this.merchantService = merchantService;
    }

    @Transactional
    public OrderDetailResponse getCheckoutDetails(String token) {
        OrderEntity order = orderRepository.findByLinkToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if ("DRAFT".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }

        if (!"PAID".equals(order.getStatus())) {
            if ("EXPIRED".equals(order.getStatus()) ||
                    (order.getExpiresAt() != null && Instant.now().isAfter(order.getExpiresAt()))) {
                order.setStatus("EXPIRED");
                orderRepository.save(order);
                throw new ResponseStatusException(HttpStatus.GONE, "This payment link has expired");
            }
        }

        List<OrderItemEntity> items = orderItemRepository.findByOrderId(order.getId());
        CustomerResponse customer = customerService.getCustomer(order.getMerchantId(), order.getCustomerId());
        String merchantName = merchantService.getMerchantName(order.getMerchantId());

        return OrderDetailResponse.of(order, items, customer, merchantName);
    }
}
