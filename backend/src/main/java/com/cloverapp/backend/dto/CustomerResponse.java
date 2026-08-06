package com.cloverapp.backend.dto;

import java.util.List;

public record CustomerResponse(
        List<CustomerDto> elements
) {

    public record CustomerDto(
            String id,
            String firstName,
            String lastName,
            EmailAddressesWrapper emailAddresses,
            PhoneNumbersWrapper phoneNumbers
    ) {
        public String getPrimaryEmail() {
            if (emailAddresses == null || emailAddresses.elements() == null || emailAddresses.elements().isEmpty()) {
                return null;
            }
            return emailAddresses.elements().stream()
                    .filter(e -> Boolean.TRUE.equals(e.primaryEmail()))
                    .map(EmailAddressDto::emailAddress)
                    .findFirst()
                    .orElseGet(() -> emailAddresses.elements().getFirst().emailAddress());
        }

        public String getPrimaryPhoneNumber() {
            if (phoneNumbers == null || phoneNumbers.elements() == null || phoneNumbers.elements().isEmpty()) {
                return null;
            }
            return phoneNumbers.elements().getFirst().phoneNumber();
        }
    }

    public record EmailAddressesWrapper(
            List<EmailAddressDto> elements
    ) {}

    public record EmailAddressDto(
            String id,
            String emailAddress,
            Boolean primaryEmail
    ) {}

    public record PhoneNumbersWrapper(
            List<PhoneNumberDto> elements
    ) {}

    public record PhoneNumberDto(
            String id,
            String phoneNumber
    ) {}
}