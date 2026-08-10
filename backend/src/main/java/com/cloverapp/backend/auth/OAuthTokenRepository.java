package com.cloverapp.backend.auth;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthTokenRepository extends JpaRepository<OAuthTokenEntity, Long> {
    Optional<OAuthTokenEntity> findByMerchantId(String merchantId);
    boolean existsByMerchantId(String merchantId);
    void deleteByMerchantId(String merchantId);
}
