package com.cloverapp.backend.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/clover")
public class CloverAuthController {

    private static final String CLOVER_OAUTH_STATE_SESSION_KEY = "cloverOAuthState";

    @Value("${clover.app-id}")
    private String appId;

    @Value("${clover.redirect-uri}")
    private String redirectUri;

    @Value("${clover.base-authorize-url}")
    private String authorizeHost;

    @GetMapping("/connect")
    public ResponseEntity<Void> connect(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute(CLOVER_OAUTH_STATE_SESSION_KEY, state);

        URI authorizeUri = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(authorizeHost)
                .path("/oauth/v2/authorize")
                .queryParam("client_id", appId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .build()
                .encode()
                .toUri();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(authorizeUri)
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam("merchant_id") String merchantId,
            HttpSession session) {
        String savedState = (String) session.getAttribute(CLOVER_OAUTH_STATE_SESSION_KEY);
        session.removeAttribute(CLOVER_OAUTH_STATE_SESSION_KEY);
        if (savedState == null || !savedState.equals(state)) {
            return ResponseEntity.badRequest().build();
        }
        // NOT FINISHED
    }
}
