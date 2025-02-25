package com.retail.discount.retaildiscountapplication.item;

import com.retail.discount.retaildiscountapplication.item.dto.ItemRequest;
import com.retail.discount.retaildiscountapplication.item.dto.ItemResponse;
import com.retail.discount.retaildiscountapplication.item.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private ItemRequest itemRequest;
    private Item item;

    @BeforeEach
    void setUp() {
        itemRequest = new ItemRequest();
        itemRequest.setName("Test Item");
        itemRequest.setPrice(10.0);
        itemRequest.setIsGrocery(true);

        item = Item.builder()
                .id("1")
                .name("Test Item")
                .price(10.0)
                .isGrocery(true)
                .build();
    }

    @Test
    void createItem_ShouldCreateAndReturnItem() {
        // Arrange
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        // Act
        ItemResponse response = itemService.createItem(itemRequest);

        // Assert
        assertNotNull(response);
        assertEquals(item.getId(), response.getId());
        assertEquals(item.getName(), response.getName());
        assertEquals(item.getPrice(), response.getPrice());
        assertEquals(item.getIsGrocery(), response.isGrocery());

        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());

        Item savedItem = itemCaptor.getValue();
        assertEquals(itemRequest.getName(), savedItem.getName());
        assertEquals(itemRequest.getPrice(), savedItem.getPrice());
        assertEquals(itemRequest.getIsGrocery(), savedItem.getIsGrocery());
    }

    @Test
    void getAllItems_ShouldReturnPageOfItems() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> itemPage = new PageImpl<>(List.of(item), pageable, 1);
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(itemPage);

        // Act
        Page<ItemResponse> response = itemService.getAllItems(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(item.getId(), response.getContent().get(0).getId());
        assertEquals(item.getName(), response.getContent().get(0).getName());
        assertEquals(item.getPrice(), response.getContent().get(0).getPrice());
        assertEquals(item.getIsGrocery(), response.getContent().get(0).isGrocery());
    }
}