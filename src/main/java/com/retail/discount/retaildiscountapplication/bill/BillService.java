package com.retail.discount.retaildiscountapplication.bill;

import com.retail.discount.retaildiscountapplication.auth.UserRepository;
import com.retail.discount.retaildiscountapplication.bill.dto.BillItemDto;
import com.retail.discount.retaildiscountapplication.bill.dto.BillResponse;
import com.retail.discount.retaildiscountapplication.bill.dto.CreateBillRequest;
import com.retail.discount.retaildiscountapplication.bill.model.Bill;
import com.retail.discount.retaildiscountapplication.bill.model.BillItem;
import com.retail.discount.retaildiscountapplication.exception.ApiException;
import com.retail.discount.retaildiscountapplication.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.retail.discount.retaildiscountapplication.auth.model.UserType.EMPLOYEE;
import static com.retail.discount.retaildiscountapplication.bill.dto.BillResponse.mapToBillResponse;

@Service
@RequiredArgsConstructor
public class BillService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BillRepository billRepository;
    private final DiscountService discountService;

    @Transactional
    public BillResponse createBill(CreateBillRequest request) {
        var userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.BAD_REQUEST));

        var items = request.getItems().stream()
                .map(this::validateAndCreateBillItem)
                .toList();

        var totalAmount = calculateTotalAmount(items);
        var netPayableAmount = discountService.calculateNetPayableAmount(items, totalAmount, user);

        var bill = Bill.builder()
                .user(user)
                .items(items)
                .totalAmount(totalAmount)
                .discountedAmount(totalAmount - netPayableAmount)
                .netPayableAmount(netPayableAmount)
                .createdAt(LocalDateTime.now())
                .build();

        var savedBill = billRepository.save(bill);
        return mapToBillResponse(savedBill);
    }

    BillItem validateAndCreateBillItem(BillItemDto request) {
        var item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ApiException("Item not found", HttpStatus.BAD_REQUEST));

        return BillItem.builder()
                .item(item)
                .quantity(request.getQuantity())
                .build();
    }

    double calculateTotalAmount(List<BillItem> items) {
        return items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getItem().getPrice())
                .sum();
    }

    public BillResponse getBill(String id) {
        var bill = billRepository.findById(id)
                .orElseThrow(() -> new ApiException("Bill not found", HttpStatus.BAD_REQUEST));
        var userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        var currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        var isOwner = bill.getUser().getId().equals(currentUser.getId());
        var isEmployee = currentUser.getUserType() == EMPLOYEE;

        if (!isOwner && !isEmployee) {
            throw new ApiException("Not authorized to access this bill", HttpStatus.FORBIDDEN);
        }

        return mapToBillResponse(bill);
    }
}