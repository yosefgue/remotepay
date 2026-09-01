package com.cloverapp.backend.inventory;

public record ItemDto(
        String id,
        String name,
        Long price,
        Boolean available,
        Double stockQuantity
) {
    public static ItemDto fromEntity(ItemEntity entity) {
        return new ItemDto(
                entity.getItemId(),
                entity.getName(),
                entity.getPrice(),
                entity.getAvailable(),
                entity.getStockQuantity()
        );
    }
}
