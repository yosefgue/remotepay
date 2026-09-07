package com.cloverapp.backend.order;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/draft")
    public ResponseEntity<OrderDetailResponse> saveDraft(
            HttpSession session,
            @RequestBody OrderRequest request
    ) {
        String merchantId = getMerchantId(session);
        if (merchantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        OrderDetailResponse order = orderService.saveDraft(merchantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PutMapping("/{id}/draft")
    public ResponseEntity<OrderDetailResponse> updateDraft(
            HttpSession session,
            @PathVariable Long id,
            @RequestBody OrderRequest request
    ) {
        String merchantId = getMerchantId(session);
        if (merchantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        OrderDetailResponse order = orderService.updateDraft(merchantId, id, request);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> getOrder(
            HttpSession session,
            @PathVariable Long id
    ) {
        String merchantId = getMerchantId(session);
        if (merchantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        OrderDetailResponse order = orderService.getOrder(merchantId, id);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getOrders(HttpSession session) {
        String merchantId = getMerchantId(session);
        if (merchantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<OrderSummaryResponse> orders = orderService.getOrders(merchantId);
        return ResponseEntity.ok(orders);
    }

    private String getMerchantId(HttpSession session) {
        String merchantId = (String) session.getAttribute("merchant_id");
        return (merchantId == null || merchantId.isBlank()) ? null : merchantId;
    }
}
