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
    public List<CustomerResponse> getCustomers(String merchantId) {
        return customerRepository.findByMerchantId(merchantId)
                .stream()
                .map(CustomerResponse::fromEntity)
                .toList();
    }

    @Transactional
    public CustomerResponse createCustomer(String merchantId, CustomerRequest request) {
        List<CloverCustomerRequest.EmailRequest> emails = (request.email() != null && !request.email().isBlank())
                ? List.of(new CloverCustomerRequest.EmailRequest(request.email().trim()))
                : null;

        List<CloverCustomerRequest.PhoneRequest> phones = (request.phoneNumber() != null && !request.phoneNumber().isBlank())
                ? List.of(new CloverCustomerRequest.PhoneRequest(request.phoneNumber().trim()))
                : null;

        CloverCustomerRequest cloverReq = new CloverCustomerRequest(
                request.firstName(),
                request.lastName(),
                emails,
                phones
        );

        CloverCustomerResponse.CustomerDto created = customerClient.createCustomer(merchantId, cloverReq);

        CustomerEntity entity = new CustomerEntity(
                created.id(),
                merchantId,
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phoneNumber()
        );
        CustomerEntity saved = customerRepository.save(entity);

        return CustomerResponse.fromEntity(saved);
    }

    @Transactional
    public void syncCustomers(String merchantId) {

        Map<String, CustomerEntity> existingCustomersMap = customerRepository.findByMerchantId(merchantId)
                .stream()
                .filter(c -> c.getCustomerId() != null && !c.getCustomerId().isBlank())
                .collect(Collectors.toMap(CustomerEntity::getCustomerId, Function.identity(), (a, b) -> a));

        List<CustomerEntity> toSave = new ArrayList<>();
        int offset = 0;

        while (true) {
            CloverCustomerResponse response = customerClient.getCustomers(merchantId, PAGE_LIMIT, offset);

            if (response == null || response.elements() == null || response.elements().isEmpty()) {
                break;
            }

            for (CloverCustomerResponse.CustomerDto dto : response.elements()) {
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

    @Transactional(readOnly = true)
    public Map<String, String> getCustomerNamesMap(String merchantId) {
        return customerRepository.findByMerchantId(merchantId)
                .stream()
                .filter(c -> c.getCustomerId() != null && !c.getCustomerId().isBlank())
                .collect(Collectors.toMap(
                        CustomerEntity::getCustomerId,
                        c -> c.getFullName() != null ? c.getFullName() : "",
                        (a, b) -> a
                ));
    }
}
