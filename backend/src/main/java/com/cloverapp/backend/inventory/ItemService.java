package com.cloverapp.backend.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private static final int PAGE_LIMIT = 100;

    private final ItemClient itemClient;
    private final ItemRepository itemRepository;

    public ItemService(ItemClient itemClient, ItemRepository itemRepository) {
        this.itemClient = itemClient;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<ItemDto> getItems(String merchantId) {
        return itemRepository.findByMerchantId(merchantId)
                .stream()
                .map(ItemDto::fromEntity)
                .toList();
    }

    @Transactional
    public void syncItems(String merchantId) {
        Map<String, ItemEntity> existingItemsMap = itemRepository.findByMerchantId(merchantId)
                .stream()
                .collect(Collectors.toMap(ItemEntity::getItemId, Function.identity()));

        List<ItemEntity> toSave = new ArrayList<>();
        int offset = 0;

        while (true) {
            CloverItemResponse response = itemClient.getItems(merchantId, PAGE_LIMIT, offset);

            if (response == null || response.elements() == null || response.elements().isEmpty()) {
                break;
            }

            for (CloverItemResponse.ItemDto dto : response.elements()) {
                ItemEntity entity = existingItemsMap.get(dto.id());
                if (entity != null) {
                    entity.updateFromClover(dto);
                    toSave.add(entity);
                } else {
                    ItemEntity newEntity = ItemEntity.fromClover(merchantId, dto);
                    toSave.add(newEntity);
                    existingItemsMap.put(dto.id(), newEntity);
                }
            }

            if (response.elements().size() < PAGE_LIMIT) {
                break;
            }

            offset += PAGE_LIMIT;
        }

        if (!toSave.isEmpty()) {
            itemRepository.saveAll(toSave);
        }
    }
}
