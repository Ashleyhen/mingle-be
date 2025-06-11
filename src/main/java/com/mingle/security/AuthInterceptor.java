package com.mingle.security;

import io.grpc.*;
import io.quarkus.grpc.GlobalInterceptor;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.*;
import jakarta.json.JsonObject;


@Slf4j
@GlobalInterceptor
@ApplicationScoped
public class AuthInterceptor implements ServerInterceptor {

    @Inject
    CurrentIdentityAssociation identityAssociation;

    @Inject
    JWTParser jwtParser;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Extract and process token synchronously
        extractBearerToken(headers).ifPresent(token -> {
            try {
                JsonWebToken jwt = jwtParser.parse(token);

                // Create SecurityIdentity from JWT claims
                QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder();
                builder.setPrincipal(jwt);

                // Add roles if present in JWT
                if (jwt.containsClaim("groups")) {
                    Object groups = jwt.getClaim("groups");
                    if (groups instanceof Collection) {
                        ((Collection<?>) groups).forEach(role ->
                                builder.addRole(role.toString()));
                    }
                }

                // Add email claim if needed
                if (jwt.containsClaim("email")) {
                    String email = jwt.getClaim("email");
                    builder.addAttribute("email", email);
                    log.info("Authenticated User Email: {}", email);
                }
                if(jwt.containsClaim("realm_access")){
                    JsonObject jsonObject =jwt.getClaim("realm_access");
                    jsonObject.get("roles").asJsonArray()
                            .forEach(t->builder.addRole(t.toString()));
                }
                // Set the security identity
                builder.addAttribute("sub",jwt.getSubject());

                identityAssociation.setIdentity(builder.build());

            } catch (ParseException e) {
                log.error("Failed to parse JWT token", e);
            }
        });

        return next.startCall(call, headers);
    }

    public Optional<String> extractBearerToken(Metadata headers) {
        Metadata.Key<String> AUTHORIZATION_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        String authHeader = headers.get(AUTHORIZATION_KEY);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Optional.of(authHeader.substring(7));
        }
        return Optional.empty();
    }
}