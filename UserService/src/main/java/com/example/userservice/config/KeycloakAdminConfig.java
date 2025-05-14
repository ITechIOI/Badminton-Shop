package com.example.userservice.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// KeycloakAdminConfig.java
@Configuration
public class KeycloakAdminConfig {
    @Bean
    public Keycloak keycloakAdmin(
            @Value("${keycloak.admin.server-url}") String serverUrl,
            @Value("${keycloak.admin.realm}")       String realmMaster,
            @Value("${keycloak.admin.client-id}")   String clientId,
            @Value("${keycloak.admin.username}")    String username,
            @Value("${keycloak.admin.password}")    String password
    ) {
        System.out.println("KeycloakAdminConfig.keycloakAdmin");
        System.out.println("serverUrl = " + serverUrl);
        System.out.println("realmMaster = " + realmMaster);
        System.out.println("clientId = " + clientId);
        System.out.println("username = " + username);
        System.out.println("password = " + password);

        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realmMaster)
                .clientId(clientId)
                .username(username)
                .password(password)
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }

    @Bean
    public RealmResource realmResource(Keycloak keycloak, @Value("${keycloak.realm}") String realm) {
        return keycloak.realm(realm);
    }
}