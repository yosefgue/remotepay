package com.cloverapp.backend.customer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private static final int PAGE_LIMIT = 100;

    private final CustomerClient customerClient;
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerClient customerClient, CustomerRepository customerRepository) {
        this.customerClient = customerClient;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerEntity> getCustomers(String merchantId) {
        return customerRepository.findByMerchantId(merchantId);
    }

    @Transactional
    public void syncCustomers(String merchantId) {

        Map<String, CustomerEntity> existingCustomersMap = customerRepository.findByMerchantId(merchantId)
                .stream()
                .collect(Collectors.toMap(CustomerEntity::getCustomerId, Function.identity()));

        List<CustomerEntity> toSave = new ArrayList<>();
        int offset = 0;

        while (true) {
            CustomerResponse response = customerClient.getCustomers(merchantId, PAGE_LIMIT, offset);

            if (response == null || response.elements() == null || response.elements().isEmpty()) {
                break;
            }

            for (CustomerResponse.CustomerDto dto : response.elements()) {
                CustomerEntity entity = existingCustomersMap.getOrDefault(
                        dto.id(),
                        new CustomerEntity(dto.id(), merchantId, null, null, null, null)
                );

                entity.setFirstName(dto.firstName());
                entity.setLastName(dto.lastName());
                entity.setEmail(dto.getPrimaryEmail());
                entity.setPhoneNumber(dto.getPrimaryPhoneNumber());

                toSave.add(entity);
            }

            if (response.elements().size() < PAGE_LIMIT) {
                break;
            }

            offset += PAGE_LIMIT;
        }
        if (!toSave.isEmpty()) {
            customerRepository.saveAll(toSave);
        }
    }
}