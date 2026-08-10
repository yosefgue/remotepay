package com.cloverapp.backend.auth;

public record CloverTokenResponse(
        String access_token,
        long access_token_expiration,
        String refresh_token,
        long refresh_token_expiration
    ) {
}
