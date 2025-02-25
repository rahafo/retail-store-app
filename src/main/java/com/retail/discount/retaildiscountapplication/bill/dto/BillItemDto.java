package com.retail.discount.retaildiscountapplication.bill.dto;

import com.retail.discount.retaildiscountapplication.bill.model.BillItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BillItemDto {
    @NotNull(message = "Item ID is required")
    private String itemId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    public static List<BillItemDto> mapToBillItemDto(List<BillItem> items) {
        return items.stream().map(item -> BillItemDto.builder().itemId(item.getItem().getId()).quantity(item.getQuantity()).build()).toList();
    }
}