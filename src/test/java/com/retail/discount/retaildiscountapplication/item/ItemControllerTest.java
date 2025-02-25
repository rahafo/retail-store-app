package com.retail.discount.retaildiscountapplication.item;

import com.retail.discount.retaildiscountapplication.item.dto.ItemRequest;
import com.retail.discount.retaildiscountapplication.item.dto.ItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private ItemRequest itemRequest;
    private ItemResponse itemResponse;
    private Page<ItemResponse> itemPage;

    @BeforeEach
    void setUp() {
        itemRequest = new ItemRequest();
        itemRequest.setName("Test Item");
        itemRequest.setPrice(100.0);
        itemRequest.setIsGrocery(false);

        itemResponse = ItemResponse.builder()
                .id("item1")
                .name("Test Item")
                .price(100.0)
                .isGrocery(false)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        itemPage = new PageImpl<>(List.of(itemResponse), pageable, 1);
    }

    @Test
    void createItem_ShouldReturnCreatedItem() {
        // Arrange
        when(itemService.createItem(any(ItemRequest.class))).thenReturn(itemResponse);

        // Act
        ResponseEntity<ItemResponse> response = itemController.createItem(itemRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(itemResponse, response.getBody());
    }

    @Test
    void getAllItems_ShouldReturnPageOfItems() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(itemService.getAllItems(any(Pageable.class))).thenReturn(itemPage);

        // Act
        ResponseEntity<Page<ItemResponse>> response = itemController.getAllItems(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(itemPage, response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }
}