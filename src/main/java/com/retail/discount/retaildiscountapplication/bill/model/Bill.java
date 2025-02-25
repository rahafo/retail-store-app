package com.retail.discount.retaildiscountapplication.bill.model;

import com.retail.discount.retaildiscountapplication.auth.model.User;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "bills")
@Builder
public class Bill {
    @Id
    private String id;
    @DBRef
    private User user;
    private List<BillItem> items;
    private double totalAmount;
    private double discountedAmount;
    private double netPayableAmount;
    private LocalDateTime createdAt;
}