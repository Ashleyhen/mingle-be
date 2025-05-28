package com.mingle.security;

import io.grpc.*;
import io.quarkus.grpc.GlobalInterceptor;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
@GlobalInterceptor
@ApplicationScoped
public class AuthInterceptor implements ServerInterceptor {

    @Inject
    CurrentIdentityAssociation identityAssociation;
    @Inject
    JsonWebToken jwt;

    @Inject
    JWTParser jwtParser;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        identityAssociation.getDeferredIdentity()
                .subscribe().with(identity -> {
                    String token = extractBearerToken(headers);
                    parseJwtAsync(token)
                            .subscribe().with(jwt -> {
                                String email = jwt.getClaim("email");
                                log.info("Authenticated User Email: {}", email);
                            });
                });
        identityAssociation.getDeferredIdentity()
                .subscribe().with(identity -> {
                    if (identity.isAnonymous()) {
                        log.warn("Unauthenticated gRPC request");
                    } else {
                        String email = identity.getAttribute("email");
                        String name = identity.getPrincipal().getName();
                        log.info("Authenticated User: {} ({})", name, email);
                    }
                });

        return next.startCall(call, headers);
    }
    public String extractBearerToken(Metadata headers) {
        Metadata.Key<String> AUTHORIZATION_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        String authHeader = headers.get(AUTHORIZATION_KEY);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Extract the token
        }
        return null; // Return null if header is missing or malformed
    }
    public Uni<JsonWebToken> parseJwtAsync(String token) {
        return Uni.createFrom().item(() -> {
                    try {
                        return jwtParser.parse(token);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool()); // Offload execution
    }
}