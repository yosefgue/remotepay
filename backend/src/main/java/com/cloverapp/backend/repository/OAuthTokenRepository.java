package com.cloverapp.backend.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cloverapp.backend.entity.OAuthToken;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
    Optional<OAuthToken> findByMerchantId(String merchantId);
    boolean existsByMerchantId(String merchantId);
    void deleteByMerchantId(String merchantId);
}
