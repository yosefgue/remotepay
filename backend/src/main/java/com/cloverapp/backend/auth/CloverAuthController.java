package com.cloverapp.backend.auth;

import java.net.URI;
import java.util.UUID;

import com.cloverapp.backend.merchant.MerchantService;
import jakarta.servlet.http.HttpServletRequest;
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

    private final String appId;
    private final String redirectUri;
    private final String authorizeHost;
    private final AuthOnboardingService authOnboardingService;

    public CloverAuthController(@Value("${clover.app-id}") String appId, @Value("${clover.redirect-uri}") String redirectUri, @Value("${clover.base-authorize-url}") String authorizeHost, AuthOnboardingService authOnboardingService) {
        this.appId = appId;
        this.redirectUri = redirectUri;
        this.authorizeHost = authorizeHost;
        this.authOnboardingService = authOnboardingService;
    }

    @GetMapping("/connect")
    public ResponseEntity<Void> connect(HttpServletRequest request) {
        String state = UUID.randomUUID().toString();

        // remove any old sessions
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        //create new session
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("state", state);

        // build url that wil be sent to the frontend and frontend will redirect
        URI authorizeUri = UriComponentsBuilder.fromUriString(authorizeHost)
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
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String savedState = (String) session.getAttribute("state");

        if (savedState == null || !savedState.equals(state)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        session.invalidate();

        authOnboardingService.onboardMerchantWithTokens(code, merchantId);

        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("merchant_id", merchantId);

        URI localRedirect = UriComponentsBuilder
                .fromUriString("https://racoon-turtle-avenging.ngrok-free.dev/dashboard")
                .build()
                .toUri();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(localRedirect)
                .build();
    }
}