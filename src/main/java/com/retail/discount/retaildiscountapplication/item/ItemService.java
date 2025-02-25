package com.retail.discount.retaildiscountapplication.item;

import com.retail.discount.retaildiscountapplication.item.dto.ItemRequest;
import com.retail.discount.retaildiscountapplication.item.dto.ItemResponse;
import com.retail.discount.retaildiscountapplication.item.model.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.retail.discount.retaildiscountapplication.item.dto.ItemResponse.mapToItemResponse;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemResponse createItem(ItemRequest request) {
        var item = Item.builder()
                .name(request.getName())
                .price(request.getPrice())
                .isGrocery(request.getIsGrocery())
                .build();

        return mapToItemResponse(itemRepository.save(item));
    }


    public Page<ItemResponse> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable)
                .map(ItemResponse::mapToItemResponse);
    }
}