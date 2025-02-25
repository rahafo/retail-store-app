package com.retail.discount.retaildiscountapplication.bill;

import com.retail.discount.retaildiscountapplication.auth.model.User;
import com.retail.discount.retaildiscountapplication.auth.model.UserType;
import com.retail.discount.retaildiscountapplication.bill.model.BillItem;
import com.retail.discount.retaildiscountapplication.item.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @InjectMocks
    private DiscountService discountService;

    private User employeeUser;
    private User affiliateUser;
    private User longTermUser;
    private User regularUser;
    private List<BillItem> groceryItems;
    private List<BillItem> nonGroceryItems;
    private List<BillItem> mixedItems;

    @BeforeEach
    void setUp() {
        // Create different user types
        employeeUser = User.builder()
                .userType(UserType.EMPLOYEE)
                .registrationDate(LocalDateTime.now())
                .build();

        affiliateUser = User.builder()
                .userType(UserType.AFFILIATE)
                .registrationDate(LocalDateTime.now())
                .build();

        longTermUser = User.builder()
                .userType(UserType.REGULAR)
                .registrationDate(LocalDateTime.now().minusYears(3))
                .build();

        regularUser = User.builder()
                .userType(UserType.REGULAR)
                .registrationDate(LocalDateTime.now())
                .build();

        // Create grocery items (no percentage discount)
        Item groceryItem = Item.builder().id("1").name("Bread").price(5.0).isGrocery(true).build();
        BillItem groceryBillItem = BillItem.builder().item(groceryItem).quantity(2).build();
        groceryItems = List.of(groceryBillItem);

        // Create non-grocery items (percentage discount applies)
        Item nonGroceryItem = Item.builder().id("2").name("Electronics").price(50.0).isGrocery(false).build();
        BillItem nonGroceryBillItem = BillItem.builder().item(nonGroceryItem).quantity(2).build();
        nonGroceryItems = List.of(nonGroceryBillItem);

        // Mixed items
        mixedItems = List.of(groceryBillItem, nonGroceryBillItem);
    }

    @Test
    void calculateNetPayableAmount_EmployeeWithGroceryItems_ShouldApplyOnlyBillDiscount() {
        // Total: 2 * $5 = $10 (all grocery)
        // Employee discount (30%): $0 (doesn't apply to groceries)
        // Bill discount: $0 (below $100)
        // Expected: $10

        // Act
        double result = discountService.calculateNetPayableAmount(groceryItems, 10.0, employeeUser);

        // Assert
        assertEquals(10.0, result, 0.01);
    }

    @Test
    void calculateNetPayableAmount_EmployeeWithNonGroceryItems_ShouldApplyBothDiscounts() {
        // Total: 2 * $50 = $100 (all non-grocery)
        // Employee discount (30%): $100 * 30% = $30
        // Amount after percentage discount: $100 - $30 = $70
        // Bill discount: Math.floor($70 / $100) * $5 = $0
        // Expected: $70

        // Act
        double result = discountService.calculateNetPayableAmount(nonGroceryItems, 100.0, employeeUser);

        // Assert
        assertEquals(70.0, result, 0.01);
    }

    @Test
    void calculateNetPayableAmount_EmployeeWithMixedItems_ShouldApplyDiscountOnlyToNonGrocery() {
        // Total: (2 * $5) + (2 * $50) = $110 ($10 grocery, $100 non-grocery)
        // Employee discount (30%): $100 * 30% = $30
        // Amount after percentage discount: $110 - $30 = $80
        // Bill discount: Math.floor($80 / $100) * $5 = $0
        // Expected: $80

        // Act
        double result = discountService.calculateNetPayableAmount(mixedItems, 110.0, employeeUser);

        // Assert
        assertEquals(80.0, result, 0.01);
    }

    @Test
    void calculateNetPayableAmount_AffiliateWithMixedItems_ShouldApplyCorrectDiscount() {
        // Total: (2 * $5) + (2 * $50) = $110 ($10 grocery, $100 non-grocery)
        // Affiliate discount (10%): $100 * 10% = $10
        // Amount after percentage discount: $110 - $10 = $100
        // Bill discount: Math.floor($100 / $100) * $5 = $5
        // Expected: $95

        // Act
        double result = discountService.calculateNetPayableAmount(mixedItems, 110.0, affiliateUser);

        // Assert
        assertEquals(95.0, result, 0.01);
    }

    @Test
    void calculateNetPayableAmount_LongTermCustomerWithMixedItems_ShouldApplyCorrectDiscount() {
        // Total: (2 * $5) + (2 * $50) = $110 ($10 grocery, $100 non-grocery)
        // Long-term customer discount (5%): $100 * 5% = $5
        // Amount after percentage discount: $110 - $5 = $105
        // Bill discount: Math.floor($105 / $100) * $5 = $5
        // Expected: $100

        // Act
        double result = discountService.calculateNetPayableAmount(mixedItems, 110.0, longTermUser);

        // Assert
        assertEquals(100.0, result, 0.01);
    }

    @Test
    void calculateNetPayableAmount_RegularCustomerWithLargeBill_ShouldApplyOnlyBillDiscount() {
        // Create a large bill ($990) to test bill discount calculation
        Item expensiveItem = Item.builder().id("3").name("Luxury Item").price(990.0).isGrocery(false).build();
        BillItem expensiveBillItem = BillItem.builder().item(expensiveItem).quantity(1).build();
        List<BillItem> largeOrderItems = List.of(expensiveBillItem);

        // Total: $990 (all non-grocery)
        // Regular customer discount: $0 (not eligible)
        // Amount after percentage discount: $990
        // Bill discount: Math.floor($990 / $100) * $5 = $45
        // Expected: $945

        // Act
        double result = discountService.calculateNetPayableAmount(largeOrderItems, 990.0, regularUser);

        // Assert
        assertEquals(945.0, result, 0.01);
    }

    @Test
    void calculateNetPayableAmount_WithLargeGroceryOrder_ShouldApplyBillDiscount() {
        // Create a large grocery bill ($500)
        Item bulkGroceryItem = Item.builder().id("4").name("Bulk Food").price(100.0).isGrocery(true).build();
        BillItem bulkGroceryBillItem = BillItem.builder().item(bulkGroceryItem).quantity(5).build();
        List<BillItem> largeGroceryItems = List.of(bulkGroceryBillItem);

        // Total: $500 (all grocery)
        // Employee discount: $0 (doesn't apply to groceries)
        // Amount after percentage discount: $500
        // Bill discount: Math.floor($500 / $100) * $5 = $25
        // Expected: $475

        // Act
        double result = discountService.calculateNetPayableAmount(largeGroceryItems, 500.0, employeeUser);

        // Assert
        assertEquals(475.0, result, 0.01);
    }
}