package com.retail.discount.retaildiscountapplication.bill.model;

import com.retail.discount.retaildiscountapplication.item.model.Item;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Data
@Builder
public class BillItem {
    @DBRef
    private Item item;
    private int quantity;
}