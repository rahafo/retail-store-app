package com.retail.discount.retaildiscountapplication.bill;

import com.retail.discount.retaildiscountapplication.auth.model.User;
import com.retail.discount.retaildiscountapplication.auth.model.UserType;
import com.retail.discount.retaildiscountapplication.bill.model.BillItem;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DiscountService {

    private static final double BILL_DISCOUNT_THRESHOLD = 100.0;
    private static final double BILL_DISCOUNT_AMOUNT = 5.0;

    public double calculateNetPayableAmount(List<BillItem> items, double totalAmount, User user) {
        var percentageDiscount = getPercentageDiscount(user);
        var nonGroceryAmount = items.stream()
                .filter(item -> !item.getItem().getIsGrocery())
                .mapToDouble(item -> item.getQuantity() * item.getItem().getPrice())
                .sum();

        var amountAfterPercentageDiscount = totalAmount - (nonGroceryAmount * percentageDiscount);

        return amountAfterPercentageDiscount - calculateBillDiscount(amountAfterPercentageDiscount); //It wasn't clear in the doc if this should be applied after or before the percentage discount, but I assumed it should be after to make the discount amount less
    }

    private double getPercentageDiscount(User user) {
        if (user.getUserType() == UserType.EMPLOYEE) {
            return 0.3;
        }
        if (user.getUserType() == UserType.AFFILIATE) {
            return 0.1;
        }
        if (isLongTermCustomer(user)) {
            return 0.05;
        }
        return 0.0;
    }

    private boolean isLongTermCustomer(User user) {
        return ChronoUnit.YEARS.between(user.getRegistrationDate(), LocalDateTime.now()) >= 2;
    }

    private double calculateBillDiscount(double totalAmount) {
        return Math.floor(totalAmount / BILL_DISCOUNT_THRESHOLD) * BILL_DISCOUNT_AMOUNT;
    }
}