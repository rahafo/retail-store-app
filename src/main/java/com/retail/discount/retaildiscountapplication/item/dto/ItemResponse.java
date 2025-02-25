package com.retail.discount.retaildiscountapplication.item.dto;

import com.retail.discount.retaildiscountapplication.item.model.Item;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemResponse {
    private String id;
    private String name;
    private double price;
    private boolean isGrocery;

    public static ItemResponse mapToItemResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .isGrocery(item.getIsGrocery())
                .name(item.getName())
                .price(item.getPrice())
                .build();
    }
}