package com.qurve.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class PlatformAuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String CLIENT_ATTRIBUTE = PlatformAuthorizationRequestRepository.class.getName() + ".CLIENT";

    private final HttpSessionOAuth2AuthorizationRequestRepository delegate = new HttpSessionOAuth2AuthorizationRequestRepository();

    private final String appRedirectUri;
    private final String webRedirectUri;

    public PlatformAuthorizationRequestRepository(@Value("${app.oauth2.success-redirect-uri}") String appRedirectUri, @Value("${app.oauth2.web-success-redirect-uri}") String webRedirectUri) {
        this.appRedirectUri = appRedirectUri;
        this.webRedirectUri = webRedirectUri;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return delegate.loadAuthorizationRequest(request);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            delegate.saveAuthorizationRequest(null, request, response);
            return;
        }

        String client = isTestWebRequest(request) ? "web" : "app";

        OAuth2AuthorizationRequest savedRequest = OAuth2AuthorizationRequest.from(authorizationRequest)
                        .attributes(attributes -> attributes.put(CLIENT_ATTRIBUTE, client))
                        .build();

        delegate.saveAuthorizationRequest(savedRequest, request, response);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = delegate.removeAuthorizationRequest(request, response);

        if (authorizationRequest != null) {
            request.setAttribute(CLIENT_ATTRIBUTE, authorizationRequest.getAttribute(CLIENT_ATTRIBUTE));
        }

        return authorizationRequest;
    }

    public String getSuccessRedirectUri(HttpServletRequest request) {
        return "web".equals(request.getAttribute(CLIENT_ATTRIBUTE)) ? webRedirectUri : appRedirectUri;
    }

    private boolean isTestWebRequest(HttpServletRequest request) {
        String referer = request.getHeader("Referer");

        if (referer == null || referer.isBlank()) {
            return false;
        }

        try {
            java.net.URI uri = java.net.URI.create(referer);

            return "https".equalsIgnoreCase(uri.getScheme())
                    && "qurve-app.netlify.app".equalsIgnoreCase(uri.getHost())
                    && (uri.getPort() == -1 || uri.getPort() == 443);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}