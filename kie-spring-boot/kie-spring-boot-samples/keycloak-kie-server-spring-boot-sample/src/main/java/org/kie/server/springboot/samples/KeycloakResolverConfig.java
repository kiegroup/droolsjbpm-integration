package org.kie.server.springboot.samples;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakResolverConfig {

    @Bean
    public KeycloakConfigResolver myKeycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }
}