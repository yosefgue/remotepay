package com.cloverapp.backend.common;

import com.cloverapp.backend.auth.OAuthTokenRepository;
import com.cloverapp.backend.auth.RetrieveTokenService;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CloverApiClient {
    private final OAuthTokenRepository oAuthTokenRepository;
    private final RestClient restClient;

    public CloverApiClient(OAuthTokenRepository oAuthTokenRepository, RetrieveTokenService retrieveTokenService) {
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.restClient = RestClient.create();
    }
}
