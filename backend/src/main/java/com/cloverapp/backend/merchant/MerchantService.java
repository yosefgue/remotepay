package com.cloverapp.backend.merchant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantService {

    private final MerchantClient merchantClient;
    private final MerchantRepository merchantRepository;

    public MerchantService(MerchantClient merchantClient, MerchantRepository merchantRepository) {
        this.merchantClient = merchantClient;
        this.merchantRepository = merchantRepository;
    }

    @Transactional
    public MerchantEntity saveMerchant(String merchantId, String merchantName) {
        MerchantEntity merchant = merchantRepository.findByMerchantId(merchantId)
                .orElseGet(MerchantEntity::new);

        merchant.setMerchantId(merchantId);
        merchant.setMerchantName(merchantName);
        return merchantRepository.save(merchant);
    }

    @Transactional
    public MerchantEntity syncMerchant(String merchantId) {
        CloverMerchantResponse response = merchantClient.fetchMerchant(merchantId);

        if (response == null || response.id() == null) {
            throw new IllegalStateException("Failed to retrieve merchant profile from Clover API for merchantId: " + merchantId);
        }

        if (!merchantId.equals(response.id())) {
            throw new IllegalStateException("Mismatched merchant ID returned from Clover API: expected "
                    + merchantId + ", but got " + response.id());
        }

        return saveMerchant(response.id(), response.name());
    }
}