package com.qurve.global.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.DefaultMapOAuth2AccessTokenResponseConverter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;

/** Handles provider errors returned as HTTP 200 without logging the response body. */
@Slf4j
final class NaverTokenResponseConverter implements Converter<Map<String, Object>, OAuth2AccessTokenResponse> {

    private static final Set<String> SAFE_ERROR_CODES = Set.of(
            "invalid_request", "invalid_client", "invalid_grant", "unauthorized_client",
            "unsupported_grant_type", "unsupported_response_type", "invalid_scope",
            "access_denied", "server_error", "temporarily_unavailable");

    private final DefaultMapOAuth2AccessTokenResponseConverter delegate =
            new DefaultMapOAuth2AccessTokenResponseConverter();

    @Override
    public OAuth2AccessTokenResponse convert(Map<String, Object> response) {
        Object token = response.get("access_token");
        if (response.containsKey("error") || !(token instanceof String value && StringUtils.hasText(value))) {
            Object rawCode = response.get("error");
            String code = rawCode instanceof String value && SAFE_ERROR_CODES.contains(value)
                    ? value : "invalid_token_response";
            // Never log descriptions, arbitrary provider values, tokens, or the full response.
            log.warn("Naver token response rejected: error={}, error_field_present={}, error_description_present={}",
                    code, response.containsKey("error"), response.containsKey("error_description"));
            throw new OAuth2AuthorizationException(new OAuth2Error(code,
                    "Naver token response rejected; see sanitized server diagnostic", null));
        }
        return delegate.convert(response);
    }
}
