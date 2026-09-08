package com.cloverapp.backend.customer;

import org.springframework.stereotype.Component;
import com.cloverapp.backend.common.CloverApiClient;

@Component
public class CustomerClient {

    private static final int DEFAULT_LIMIT = 100;
    private static final String EXPAND_FIELDS = "emailAddresses,phoneNumbers";

    private final CloverApiClient cloverApiClient;

    public CustomerClient(CloverApiClient cloverApiClient) {
        this.cloverApiClient = cloverApiClient;
    }

    public CustomerResponse getCustomers(String merchantId) {
        return getCustomers(merchantId, DEFAULT_LIMIT, 0);
    }

    public CustomerResponse getCustomers(String merchantId, int limit, int offset) {
        return cloverApiClient.get(
                merchantId,
                uriBuilder -> uriBuilder
                        .path("/v3/merchants/{mId}/customers")
                        .queryParam("expand", EXPAND_FIELDS)
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build(merchantId),
                CustomerResponse.class
        );
    }

    public CustomerResponse.CustomerDto createCustomer(String merchantId, CloverCustomerRequest request) {
        return cloverApiClient.post(
                merchantId,
                "/v3/merchants/" + merchantId + "/customers",
                request,
                CustomerResponse.CustomerDto.class
        );
    }

    public void addEmailAddress(String merchantId, String customerId, String emailAddress) {
        cloverApiClient.post(
                merchantId,
                "/v3/merchants/" + merchantId + "/customers/" + customerId + "/email_addresses",
                new CloverCustomerRequest.EmailRequest(emailAddress),
                Void.class
        );
    }

    public void addPhoneNumber(String merchantId, String customerId, String phoneNumber) {
        cloverApiClient.post(
                merchantId,
                "/v3/merchants/" + merchantId + "/customers/" + customerId + "/phone_numbers",
                new CloverCustomerRequest.PhoneRequest(phoneNumber),
                Void.class
        );
    }
}
