package com.enoca.flowpilot.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public Long getCurrentEmployeeId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException(
                    "Authenticated user not found"
            );
        }

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new IllegalStateException(
                    "Unsupported authentication type"
            );
        }

        Object employeeId =
                jwtAuthentication
                        .getTokenAttributes()
                        .get("employeeId");

        if (employeeId == null) {
            throw new IllegalStateException(
                    "employeeId claim not found in JWT"
            );
        }

        if (employeeId instanceof Number number) {
            return number.longValue();
        }

        throw new IllegalStateException(
                "Invalid employeeId claim in JWT"
        );
    }
}