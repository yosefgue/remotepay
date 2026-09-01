package com.cloverapp.backend.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    // find all items related to one merchant
    List<ItemEntity> findByMerchantId(String merchantId);

    // find a specific item for a merchant
    Optional<ItemEntity> findByMerchantIdAndItemId(String merchantId, String itemId);

    // check if an item exists for a merchant
    boolean existsByMerchantIdAndItemId(String merchantId, String itemId);

    // delete all items for a merchant
    void deleteByMerchantId(String merchantId);

    // delete a specific item for a merchant
    void deleteByMerchantIdAndItemId(String merchantId, String itemId);
}
