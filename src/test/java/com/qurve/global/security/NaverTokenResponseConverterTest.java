package com.qurve.global.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@ExtendWith(OutputCaptureExtension.class)
class NaverTokenResponseConverterTest {

    @Test
    void reportsProviderErrorWithoutLoggingResponseSecrets(CapturedOutput output) {
        var converter = new NaverTokenResponseConverter();
        var response = Map.<String, Object>of(
                "error", "invalid_client",
                "error_description", "private-client-secret",
                "refresh_token", "private-refresh-token");

        var failure = catchThrowableOfType(OAuth2AuthorizationException.class,
                () -> converter.convert(response));

        assertThat(failure.getError().getErrorCode()).isEqualTo("invalid_client");
        assertThat(output.getAll()).contains("Naver token response rejected", "invalid_client")
                .doesNotContain("private-client-secret", "private-refresh-token");
        assertThat(failure).hasMessageNotContaining("private-client-secret");
    }

    @Test
    void preservesSuccessfulTokensAndExpiryWithoutLoggingThem(CapturedOutput output) {
        var response = new NaverTokenResponseConverter().convert(Map.of(
                "access_token", "private-access-token", "refresh_token", "private-refresh-token",
                "token_type", "bearer", "expires_in", "3600"));

        assertThat(response.getAccessToken().getTokenValue()).isEqualTo("private-access-token");
        assertThat(response.getRefreshToken().getTokenValue()).isEqualTo("private-refresh-token");
        assertThat(response.getAccessToken().getExpiresAt()).isEqualTo(
                response.getAccessToken().getIssuedAt().plusSeconds(3600));
        assertThat(output.getAll()).doesNotContain("private-access-token", "private-refresh-token");
    }

    @Test
    void redactsUnknownErrorCodesAndRejectsErrorEvenWhenTokenIsPresent(CapturedOutput output) {
        var failure = catchThrowableOfType(OAuth2AuthorizationException.class,
                () -> new NaverTokenResponseConverter().convert(Map.of(
                        "error", "secret-disguised-as-error", "access_token", "private-access-token")));

        assertThat(failure.getError().getErrorCode()).isEqualTo("invalid_token_response");
        assertThat(output.getAll()).contains("error_field_present=true")
                .doesNotContain("secret-disguised-as-error", "private-access-token");
        assertThat(failure).hasMessageNotContaining("secret-disguised-as-error");
    }

    @Test
    void distinguishesMissingTokenWithoutProviderError(CapturedOutput output) {
        var failure = catchThrowableOfType(OAuth2AuthorizationException.class,
                () -> new NaverTokenResponseConverter().convert(Map.of("access_token", " ")));

        assertThat(failure.getError().getErrorCode()).isEqualTo("invalid_token_response");
        assertThat(output.getAll()).contains("error_field_present=false");
    }
}
