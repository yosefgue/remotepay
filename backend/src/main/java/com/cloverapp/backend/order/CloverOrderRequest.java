package com.cloverapp.backend.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CloverOrderRequest(
        OrderCart orderCart
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrderCart(
            List<LineItem> lineItems
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record LineItem(
            ItemRef item,
            String name,
            Long price
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ItemRef(
            String id
    ) {}

    public static CloverOrderRequest fromOrderItems(List<OrderItemEntity> items) {
        List<LineItem> lineItems = new ArrayList<>();

        for (OrderItemEntity item : items) {
            int qty = item.getQuantity() != null ? item.getQuantity() : 1;

            for (int i = 0; i < qty; i++) {
                if (item.getCloverItemId() != null && !item.getCloverItemId().isBlank()) {
                    lineItems.add(new LineItem(new ItemRef(item.getCloverItemId()), null, null));
                } else {
                    lineItems.add(new LineItem(null, item.getName(), item.getPrice()));
                }
            }
        }

        return new CloverOrderRequest(new OrderCart(lineItems));
    }
}
