package com.cloverapp.backend.customer;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CloverCustomerRequest(
        String firstName,
        String lastName
) {
    public record EmailRequest(String emailAddress) {}
    public record PhoneRequest(String phoneNumber) {}
}
