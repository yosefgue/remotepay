package com.cloverapp.backend.common;

import com.cloverapp.backend.auth.OAuthTokenRepository;
import com.cloverapp.backend.auth.CloverTokenService;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CloverApiClient {
    public final OAuthTokenRepository oAuthTokenRepository;
    public final RestClient restClient;

    public CloverApiClient(OAuthTokenRepository oAuthTokenRepository, CloverTokenService retrieveTokenService) {
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.restClient = RestClient.create();
    }
}
