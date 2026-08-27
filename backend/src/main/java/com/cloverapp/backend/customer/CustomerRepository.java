package com.cloverapp.backend.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    // find all customers related to one merchant
    List<CustomerEntity> findByMerchantId(String merchantId);
    // find one specific customer related to one merchant
    Optional<CustomerEntity> findByMerchantIdAndCustomerId(String merchantId, String customerId);

    boolean existsByMerchantIdAndCustomerId(String merchantId, String customerId);
    void deleteByMerchantId(String merchantId);
}