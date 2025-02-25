package com.retail.discount.retaildiscountapplication.bill.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateBillRequest {
    @NotEmpty(message = "Bill must contain at least one item")
    private List<BillItemDto> items;
}
