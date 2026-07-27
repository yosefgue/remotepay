package com.cloverapp.backend.controller;

import com.cloverapp.backend.repository.OAuthTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthCheckController {

    private final OAuthTokenRepository tokenRepository;

    @Value("${app.cookie-secure:false}")
    private boolean isSecureCookie;

    public AuthCheckController(OAuthTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * 1. CREATE SESSION (Missing Route)
     * Called by React's AuthSuccess.tsx after state verification.
     * Sets the HttpOnly 'clover_session' cookie on localhost / production domain.
     */
    @PostMapping("/session")
    public ResponseEntity<Map<String, Object>> createSession(@RequestBody Map<String, String> payload) {
        String merchantId = payload.get("merchantId");

        // Validate that merchant tokens exist in database
        if (merchantId == null || !tokenRepository.existsByMerchantId(merchantId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false, "message", "Invalid or unknown merchantId"));
        }

        // Generate HttpOnly session cookie (7-day validity)
        ResponseCookie sessionCookie = ResponseCookie.from("clover_session", merchantId)
                .httpOnly(true)
                .secure(isSecureCookie) // false in dev (http://localhost), true in prod (https)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
                .body(Map.of(
                        "authenticated", true,
                        "merchantId", merchantId
                ));
    }

    /**
     * 2. CHECK CURRENT SESSION
     * Called by React's ProtectedRoute or AuthContext on initial page load / refresh.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentSession(
            @CookieValue(name = "clover_session", required = false) String merchantId) {

        if (merchantId == null || !tokenRepository.existsByMerchantId(merchantId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false));
        }

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "merchantId", merchantId
        ));
    }

    /**
     * 3. LOGOUT / DESTROY SESSION
     * Clears the 'clover_session' cookie from the client browser.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie deleteCookie = ResponseCookie.from("clover_session", "")
                .httpOnly(true)
                .secure(isSecureCookie)
                .path("/")
                .maxAge(0) // Immediately expire cookie
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }
}