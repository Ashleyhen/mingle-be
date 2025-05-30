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

    @ConfigProperty(name="mingle.keycloak.clientId")
    String clientId;

    @ConfigProperty(name="mingle.keycloak.username")
    String username;

    @ConfigProperty(name="mingle.keycloak.password")
    String password;

    @Produces
    public Keycloak initKeyCloak(){
        return KeycloakBuilder.builder()
                .serverUrl(url)
                .realm("master") // Admin realm
                .clientId("mingle-keycloak-admin")
                .clientSecret("bWluZ2xlLXNlY3JldA==")
                .grantType("client_credentials")
                .username("mingle-be")
                .password("mingle-be")
                .build();


    }
}
