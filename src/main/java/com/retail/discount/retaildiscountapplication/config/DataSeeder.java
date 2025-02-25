package com.retail.discount.retaildiscountapplication.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.discount.retaildiscountapplication.item.ItemRepository;
import com.retail.discount.retaildiscountapplication.item.model.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ItemRepository itemRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DataSeeder(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (itemRepository.count() == 0) {
            InputStream inputStream = new ClassPathResource("data.json").getInputStream();
            List<Item> items = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
            itemRepository.saveAll(items);
            log.info("✅ Sample Items inserted from JSON file!");
        }
    }
}
