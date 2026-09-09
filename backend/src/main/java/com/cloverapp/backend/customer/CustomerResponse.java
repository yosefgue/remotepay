package com.cloverapp.backend.customer;

public record CustomerResponse(
        String customerId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
    public static CustomerResponse fromEntity(CustomerEntity entity) {
        return new CustomerResponse(
                entity.getCustomerId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPhoneNumber()
        );
    }
}