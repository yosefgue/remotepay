package com.cloverapp.backend.customer;

public record CustomerRequest(
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {}
