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
    public ResponseEntity<List<CustomerDto>> getCustomers(HttpSession session) {
        String merchantId = (String) session.getAttribute("merchant_id");

        if (merchantId == null || merchantId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<CustomerDto> customers = customerService.getCustomers(merchantId);
        return ResponseEntity.ok(customers);
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncCustomers(HttpSession session) {
        String merchantId = (String) session.getAttribute("merchant_id");

        if (merchantId == null || merchantId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        customerService.syncCustomers(merchantId);
        return ResponseEntity.noContent().build();
    }
}