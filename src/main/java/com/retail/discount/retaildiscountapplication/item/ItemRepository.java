package com.retail.discount.retaildiscountapplication.item;

import com.retail.discount.retaildiscountapplication.item.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends MongoRepository<Item, String> {
}