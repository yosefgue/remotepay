package com.cloverapp.backend.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByMerchantId(String merchantId);

    Optional<OrderEntity> findByMerchantIdAndId(String merchantId, Long id);

    Optional<OrderEntity> findByLinkToken(String linkToken);

    Optional<OrderEntity> findByCloverOrderId(String cloverOrderId);

    Optional<OrderEntity> findByMerchantIdAndCloverOrderId(String merchantId, String cloverOrderId);
}
