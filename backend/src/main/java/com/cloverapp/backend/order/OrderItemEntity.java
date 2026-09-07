package com.cloverapp.backend.order;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "clover_item_id")
    private String cloverItemId;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private Long price;

    @Column(name = "quantity")
    private Integer quantity = 1;

    public OrderItemEntity() {}

    public OrderItemEntity(Long orderId, String cloverItemId, String name, Long price, Integer quantity) {
        this.orderId = orderId;
        this.cloverItemId = cloverItemId;
        this.name = name;
        this.price = price;
        this.quantity = quantity != null ? quantity : 1;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getCloverItemId() { return cloverItemId; }
    public void setCloverItemId(String cloverItemId) { this.cloverItemId = cloverItemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
