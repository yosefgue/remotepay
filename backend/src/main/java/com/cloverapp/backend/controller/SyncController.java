package com.cloverapp.backend.controller;

import com.cloverapp.backend.service.SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clover/sync")
public class SyncController {
    private final SyncService syncService;
    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/item")
    public ResponseEntity<String> syncItems(@CookieValue("clover_session") String merchantId) {
        syncService.getItems(merchantId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/merchant")
    public ResponseEntity<String> syncMerchant(@CookieValue("clover_session") String merchantId) {
        syncService.getMerchant(merchantId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/customer")
    public ResponseEntity<String> syncCustomers(@CookieValue("clover_session") String merchantId) {
        syncService.getCustomers(merchantId);
        return ResponseEntity.ok().build();
    }
}
