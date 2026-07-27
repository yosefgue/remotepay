package com.cloverapp.backend.controller;

import java.net.URI;
import java.util.Map;

import com.cloverapp.backend.service.RetrieveToken;

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

    private final String appId;

    private final String redirectUri;

    private final String authorizeHost;

    private final RetrieveToken retrieveToken;

    public CloverAuthController(@Value("${clover.app-id}") String appId, @Value("${clover.redirect-uri}") String redirectUri, @Value("${clover.base-authorize-url}") String authorizeHost, RetrieveToken retrieveToken) {
        this.appId = appId;
        this.redirectUri = redirectUri;
        this.authorizeHost = authorizeHost;
        this.retrieveToken = retrieveToken;
    }

    @GetMapping("/connect")
    public ResponseEntity<Map<String, String>> connect(@RequestParam String state) {
        // build url that wil be sent to the frontend and frontend will redirect
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

        return ResponseEntity.ok(Map.of("url", authorizeUri.toString()));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam("merchant_id") String merchantId) {

        retrieveToken.fetchAndSaveTokens(code, merchantId);

        URI localRedirect = UriComponentsBuilder
                .fromUriString("http://localhost:5173/auth-success")
                .queryParam("merchantId", merchantId)
                .queryParam("state", state)
                .build()
                .toUri();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(localRedirect)
                .build();
    }
}