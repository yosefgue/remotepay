package com.cloverapp.backend.customer;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CloverCustomerRequest(
        String firstName,
        String lastName,
        List<EmailRequest> emailAddresses,
        List<PhoneRequest> phoneNumbers
) {
    public record EmailRequest(String emailAddress) {}
    public record PhoneRequest(String phoneNumber) {}
}
