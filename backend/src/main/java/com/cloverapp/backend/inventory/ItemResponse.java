package com.cloverapp.backend.inventory;

import java.util.List;

public record ItemResponse(
        List<ItemDto> elements
) {
    public record ItemDto(
            String id,
            String name,
            Long price,
            Integer stockCount,
            Boolean available,
            Boolean deleted,
            Long modifiedTime
    ) {}
}