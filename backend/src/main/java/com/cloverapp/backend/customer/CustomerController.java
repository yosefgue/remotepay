package com.cloverapp.backend.customer;

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
    public ResponseEntity<List<CustomerEntity>> getCustomers(@RequestParam String merchantId) {
        List<CustomerEntity> customers = customerService.getCustomers(merchantId);
        return ResponseEntity.ok(customers);
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncCustomers(@RequestParam String merchantId) {
        customerService.syncCustomers(merchantId);
        return ResponseEntity.noContent().build();
    }
}