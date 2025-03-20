package com.example.orderservice.common.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class FeignClientInterceptor implements RequestInterceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN_TYPE = "Bearer";
    // public static String tokenJwt = "";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            String token = ((JwtAuthenticationToken) authentication).getToken().getTokenValue();
            // tokenJwt = token;
            System.out.println("🔹 Feign Interceptor: Adding JWT to request: " + token);
            requestTemplate.header(AUTHORIZATION_HEADER, BEARER_TOKEN_TYPE + " " + token);
        }
    }
}
