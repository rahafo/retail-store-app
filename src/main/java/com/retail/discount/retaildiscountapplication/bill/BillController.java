package com.retail.discount.retaildiscountapplication.bill;

import com.retail.discount.retaildiscountapplication.bill.dto.BillResponse;
import com.retail.discount.retaildiscountapplication.bill.dto.CreateBillRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    public ResponseEntity<BillResponse> createBill(@Valid @RequestBody CreateBillRequest request) {
        return ResponseEntity.ok(billService.createBill(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillResponse> getBill(@PathVariable String id) {
        return ResponseEntity.ok(billService.getBill(id));
    }
}