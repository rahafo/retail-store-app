package com.retail.discount.retaildiscountapplication.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ItemRequest {
    @NotBlank(message = "Item name is required")
    private String name;

    @Positive(message = "Price must be greater than 0")
    private double price;

    @NotNull(message = "isGrocery flag is required")
    private Boolean isGrocery;
}