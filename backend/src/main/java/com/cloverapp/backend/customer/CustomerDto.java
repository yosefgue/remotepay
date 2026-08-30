package com.cloverapp.backend.customer;

public record CustomerDto(
        String id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
    public static CustomerDto fromEntity(CustomerEntity entity) {
        return new CustomerDto(
                entity.getCustomerId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPhoneNumber()
        );
    }
}