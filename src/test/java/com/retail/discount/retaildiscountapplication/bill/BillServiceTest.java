package com.retail.discount.retaildiscountapplication.bill;

import com.retail.discount.retaildiscountapplication.auth.UserRepository;
import com.retail.discount.retaildiscountapplication.auth.model.User;
import com.retail.discount.retaildiscountapplication.auth.model.UserType;
import com.retail.discount.retaildiscountapplication.bill.dto.BillItemDto;
import com.retail.discount.retaildiscountapplication.bill.dto.BillResponse;
import com.retail.discount.retaildiscountapplication.bill.dto.CreateBillRequest;
import com.retail.discount.retaildiscountapplication.bill.model.Bill;
import com.retail.discount.retaildiscountapplication.bill.model.BillItem;
import com.retail.discount.retaildiscountapplication.exception.ApiException;
import com.retail.discount.retaildiscountapplication.item.ItemRepository;
import com.retail.discount.retaildiscountapplication.item.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BillRepository billRepository;

    @Mock
    private DiscountService discountService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BillService billService;

    private User user;
    private User employeeUser;
    private Item item;
    private CreateBillRequest createBillRequest;
    private Bill bill;
    private BillItem billItem;

    @BeforeEach
    void setUp() {
        // Setup SecurityContextHolder mock
        SecurityContextHolder.setContext(securityContext);

        // Setup test data
        user = User.builder()
                .id("user1")
                .name("Test User")
                .email("test@example.com")
                .userType(UserType.REGULAR)
                .registrationDate(LocalDateTime.now())
                .build();

        employeeUser = User.builder()
                .id("emp1")
                .name("Employee User")
                .email("employee@example.com")
                .userType(UserType.EMPLOYEE)
                .registrationDate(LocalDateTime.now())
                .build();

        item = Item.builder()
                .id("item1")
                .name("Test Item")
                .price(100.0)
                .isGrocery(false)
                .build();

        billItem = BillItem.builder()
                .item(item)
                .quantity(2)
                .build();

        BillItemDto billItemDto = BillItemDto.builder()
                .itemId("item1")
                .quantity(2)
                .build();

        createBillRequest = new CreateBillRequest();
        createBillRequest.setItems(List.of(billItemDto));

        bill = Bill.builder()
                .id("bill1")
                .user(user)
                .items(List.of(billItem))
                .totalAmount(200.0)
                .discountedAmount(20.0)
                .netPayableAmount(180.0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createBill_ShouldCreateAndSaveBill() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyString())).thenReturn(Optional.of(item));
        when(discountService.calculateNetPayableAmount(anyList(), anyDouble(), any(User.class))).thenReturn(180.0);
        when(billRepository.save(any(Bill.class))).thenReturn(bill);

        // Act
        BillResponse response = billService.createBill(createBillRequest);

        // Assert
        assertNotNull(response);
        assertEquals(bill.getId(), response.getId());
        assertEquals(bill.getTotalAmount(), response.getTotalAmount());
        assertEquals(bill.getDiscountedAmount(), response.getDiscountedAmount());
        assertEquals(bill.getNetPayableAmount(), response.getNetPayableAmount());

        ArgumentCaptor<Bill> billCaptor = ArgumentCaptor.forClass(Bill.class);
        verify(billRepository).save(billCaptor.capture());

        Bill savedBill = billCaptor.getValue();
        assertEquals(user, savedBill.getUser());
        assertEquals(200.0, savedBill.getTotalAmount());
        assertEquals(20.0, savedBill.getDiscountedAmount());
        assertEquals(180.0, savedBill.getNetPayableAmount());
        assertNotNull(savedBill.getCreatedAt());
    }

    @Test
    void createBill_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            billService.createBill(createBillRequest);
        });

        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(billRepository, never()).save(any());
    }

    @Test
    void getBill_AsOwner_ShouldReturnBill() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(billRepository.findById(anyString())).thenReturn(Optional.of(bill));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // Act
        BillResponse response = billService.getBill("bill1");

        // Assert
        assertNotNull(response);
        assertEquals(bill.getId(), response.getId());
        assertEquals(bill.getTotalAmount(), response.getTotalAmount());
        assertEquals(bill.getNetPayableAmount(), response.getNetPayableAmount());
    }

    @Test
    void getBill_AsEmployee_ShouldReturnBill() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        Bill otherUserBill = Bill.builder()
                .id("bill2")
                .user(User.builder().id("user2").build())
                .items(List.of(billItem))
                .totalAmount(200.0)
                .discountedAmount(20.0)
                .netPayableAmount(180.0)
                .build();

        when(billRepository.findById(anyString())).thenReturn(Optional.of(otherUserBill));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(employeeUser));

        // Act
        BillResponse response = billService.getBill("bill2");

        // Assert
        assertNotNull(response);
        assertEquals(otherUserBill.getId(), response.getId());
    }

    @Test
    void getBill_AsUnauthorizedUser_ShouldThrowException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        User otherUser = User.builder()
                .id("user3")
                .name("Other User")
                .email("test@example.com")
                .userType(UserType.REGULAR)
                .build();

        Bill otherUserBill = Bill.builder()
                .id("bill3")
                .user(User.builder().id("user2").build())
                .items(List.of(billItem))
                .build();

        when(billRepository.findById(anyString())).thenReturn(Optional.of(otherUserBill));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(otherUser));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            billService.getBill("bill3");
        });

        assertEquals("Not authorized to access this bill", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void getBill_WithNonExistentBill_ShouldThrowException() {
        // Arrange
        when(billRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            billService.getBill("nonExistentBill");
        });

        assertEquals("Bill not found", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void getBill_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(billRepository.findById(anyString())).thenReturn(Optional.of(bill));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            billService.getBill("bill1");
        });

        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void validateAndCreateBillItem_ShouldReturnBillItem() {
        // Arrange
        BillItemDto billItemDto = BillItemDto.builder()
                .itemId("item1")
                .quantity(2)
                .build();

        when(itemRepository.findById(anyString())).thenReturn(Optional.of(item));

        // Act
        BillItem result = billService.validateAndCreateBillItem(billItemDto);

        // Assert
        assertNotNull(result);
        assertEquals(item, result.getItem());
        assertEquals(2, result.getQuantity());
    }

    @Test
    void calculateTotalAmount_ShouldReturnCorrectTotal() {
        // Arrange
        Item item1 = Item.builder().price(10.0).build();
        Item item2 = Item.builder().price(20.0).build();

        BillItem billItem1 = BillItem.builder().item(item1).quantity(2).build();
        BillItem billItem2 = BillItem.builder().item(item2).quantity(3).build();

        List<BillItem> items = List.of(billItem1, billItem2);

        // Expected: (2 * 10) + (3 * 20) = 20 + 60 = 80

        // Act
        double totalAmount = billService.calculateTotalAmount(items);

        // Assert
        assertEquals(80.0, totalAmount, 0.001);
    }

    @Test
    void createBill_WithEmptyItemsList_ShouldCreateBillWithZeroAmount() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        CreateBillRequest emptyRequest = new CreateBillRequest();
        emptyRequest.setItems(List.of());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(discountService.calculateNetPayableAmount(anyList(), eq(0.0), any(User.class))).thenReturn(0.0);

        Bill emptyBill = Bill.builder()
                .id("emptyBill")
                .user(user)
                .items(List.of())
                .totalAmount(0.0)
                .discountedAmount(0.0)
                .netPayableAmount(0.0)
                .createdAt(LocalDateTime.now())
                .build();

        when(billRepository.save(any(Bill.class))).thenReturn(emptyBill);

        // Act
        BillResponse response = billService.createBill(emptyRequest);

        // Assert
        assertNotNull(response);
        assertEquals(0.0, response.getTotalAmount());
        assertEquals(0.0, response.getNetPayableAmount());
    }
}