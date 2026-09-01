package com.cloverapp.backend.inventory;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getItems(HttpSession session) {
        String merchantId = (String) session.getAttribute("merchant_id");

        if (merchantId == null || merchantId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<ItemDto> items = itemService.getItems(merchantId);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncItems(HttpSession session) {
        String merchantId = (String) session.getAttribute("merchant_id");

        if (merchantId == null || merchantId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        itemService.syncItems(merchantId);
        return ResponseEntity.noContent().build();
    }
}
