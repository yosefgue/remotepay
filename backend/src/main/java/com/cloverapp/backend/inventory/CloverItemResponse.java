package com.cloverapp.backend.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

public record CloverItemResponse(
        List<ItemDto> elements
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItemDto(
            String id,
            String name,
            Long price,
            Boolean available,
            Long modifiedTime,
            ItemStockDto itemStock
    ) {
        public Double getQuantity() {
            return itemStock != null ? itemStock.quantity() : null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItemStockDto(
            Double quantity
    ) {}
}