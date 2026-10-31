package com.yessinCoding.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RateLimitingFilterHolder {
    private final RateLimitingFilter rateLimitingFilter;

    public RateLimitingFilter getFilter() {
        return rateLimitingFilter;
    }
}
