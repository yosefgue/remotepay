package com.cloverapp.backend.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/checkout")
public class PublicCheckoutController {
    private final CheckoutService checkoutService;

    public PublicCheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable String token) {
        OrderDetailResponse order = checkoutService.getCheckoutDetails(token);
        return ResponseEntity.ok(order);
    }
}
