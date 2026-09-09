package com.cloverapp.backend.customer;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getCustomers(HttpSession session) {
        String merchantId = getMerchantId(session);
        if (merchantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<CustomerResponse> customers = customerService.getCustomers(merchantId);
        return ResponseEntity.ok(customers);
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            HttpSession session,
            @RequestBody CustomerRequest request
    ) {
        String merchantId = getMerchantId(session);
        if (merchantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CustomerResponse created = customerService.createCustomer(merchantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncCustomers(HttpSession session) {
        String merchantId = getMerchantId(session);
        if (merchantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        customerService.syncCustomers(merchantId);
        return ResponseEntity.noContent().build();
    }

    private String getMerchantId(HttpSession session) {
        String merchantId = (String) session.getAttribute("merchant_id");
        return (merchantId == null || merchantId.isBlank()) ? null : merchantId;
    }
}
