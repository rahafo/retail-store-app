package com.retail.discount.retaildiscountapplication.bill.dto;

import com.retail.discount.retaildiscountapplication.bill.model.Bill;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static com.retail.discount.retaildiscountapplication.bill.dto.BillItemDto.mapToBillItemDto;

@Data
@Builder
public class BillResponse {
    private String id;
    private String userId;
    private List<BillItemDto> items;
    private double totalAmount;
    private double discountedAmount;
    private double netPayableAmount;
    private LocalDateTime createdAt;

    public static BillResponse mapToBillResponse(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .userId(bill.getUser().getId())
                .totalAmount(bill.getTotalAmount())
                .netPayableAmount(bill.getNetPayableAmount())
                .discountedAmount(bill.getDiscountedAmount())
                .items(mapToBillItemDto(bill.getItems()))
                .createdAt(bill.getCreatedAt())
                .build();
    }
}