package com.cloverapp.backend.merchant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<MerchantEntity, Long> {

    Optional<MerchantEntity> findByMerchantId(String merchantId);

    boolean existsByMerchantId(String merchantId);

    void deleteByMerchantId(String merchantId);
}