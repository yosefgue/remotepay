package com.cloverapp.backend.inventory;

import jakarta.persistence.*;

@Entity
@Table(name = "items")
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id")
    private String itemId;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private Long price;

    @Column(name = "available")
    private Boolean available = true;

    @Column(name = "stock_quantity")
    private Double stockQuantity;

    @Column(name = "clover_modified_time")
    private Long cloverModifiedTime;

    public ItemEntity() {}

    public static ItemEntity fromClover(String merchantId, CloverItemResponse.ItemDto dto) {
        ItemEntity entity = new ItemEntity();
        entity.itemId = dto.id();
        entity.merchantId = merchantId;
        entity.name = dto.name();
        entity.price = dto.price();
        entity.available = dto.available() == null || dto.available();
        entity.stockQuantity = dto.getQuantity();
        entity.cloverModifiedTime = dto.modifiedTime();
        return entity;
    }

    public void updateFromClover(CloverItemResponse.ItemDto dto) {
        this.name = dto.name();
        this.price = dto.price();
        this.available = dto.available() == null || dto.available();
        this.stockQuantity = dto.getQuantity();
        this.cloverModifiedTime = dto.modifiedTime();
    }

    public Long getId() { return id; }
    public String getItemId() { return itemId; }
    public String getMerchantId() { return merchantId; }
    public String getName() { return name; }
    public Long getPrice() { return price; }
    public Boolean getAvailable() { return available; }
    public Double getStockQuantity() { return stockQuantity; }
    public Long getCloverModifiedTime() { return cloverModifiedTime; }

    public void setName(String name) { this.name = name; }
    public void setPrice(Long price) { this.price = price; }
    public void setAvailable(Boolean available) { this.available = available; }
    public void setStockQuantity(Double stockQuantity) { this.stockQuantity = stockQuantity; }
    public void setCloverModifiedTime(Long cloverModifiedTime) { this.cloverModifiedTime = cloverModifiedTime; }
}