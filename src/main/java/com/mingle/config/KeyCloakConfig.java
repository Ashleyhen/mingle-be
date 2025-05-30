package com.mingle.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

@ApplicationScoped
public class KeyCloakConfig {

    @ConfigProperty(name="mingle.keycloak.url")
    String url;

    @ConfigProperty(name="mingle.keycloak.realm")
    String realm;

    @ConfigProperty(name="mingle.keycloak.client.id")
    String clientId;

    @ConfigProperty(name="mingle.keycloak.client.secret")
    String secret;

    @Produces
    public Keycloak initKeyCloak(){
        return KeycloakBuilder.builder()
                .serverUrl(url)
                .realm(realm) // Admin realm
                .clientId(clientId)
                .clientSecret(secret)
                .grantType("client_credentials")
                .build();


    }
}
