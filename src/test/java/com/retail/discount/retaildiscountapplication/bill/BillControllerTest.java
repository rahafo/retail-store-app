package com.retail.discount.retaildiscountapplication.bill;

import com.retail.discount.retaildiscountapplication.bill.dto.BillItemDto;
import com.retail.discount.retaildiscountapplication.bill.dto.BillResponse;
import com.retail.discount.retaildiscountapplication.bill.dto.CreateBillRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillControllerTest {

    @Mock
    private BillService billService;

    @InjectMocks
    private BillController billController;

    private CreateBillRequest createBillRequest;
    private BillResponse billResponse;

    @BeforeEach
    void setUp() {
        var billItemDto = BillItemDto.builder()
                .itemId("item1")
        .quantity(2).build();

        createBillRequest = new CreateBillRequest();
        createBillRequest.setItems(List.of(billItemDto));

        billResponse = BillResponse.builder()
                .id("bill1")
                .totalAmount(200.0)
                .discountedAmount(20.0)
                .netPayableAmount(180.0)
                .createdAt(LocalDateTime.now())
                .items(List.of())
                .build();
    }

    @Test
    void createBill_ShouldReturnCreatedBill() {
        // Arrange
        when(billService.createBill(any(CreateBillRequest.class))).thenReturn(billResponse);

        // Act
        ResponseEntity<BillResponse> response = billController.createBill(createBillRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(billResponse, response.getBody());
    }

    @Test
    void getBill_ShouldReturnBill() {
        // Arrange
        when(billService.getBill(anyString())).thenReturn(billResponse);

        // Act
        ResponseEntity<BillResponse> response = billController.getBill("bill1");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(billResponse, response.getBody());
    }
}