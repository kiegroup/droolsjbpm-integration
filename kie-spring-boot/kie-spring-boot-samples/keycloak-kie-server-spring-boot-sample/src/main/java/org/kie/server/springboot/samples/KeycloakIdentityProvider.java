/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.springboot.samples;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.KeycloakPrincipal;
import org.kie.internal.identity.IdentityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;

public class KeycloakIdentityProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(KeycloakIdentityProvider.class);

    @Bean
    public IdentityProvider identityProvider() {
        
        return new IdentityProvider() {

            @Override
            public boolean hasRole(String role) {
                List<String> keycloakRoles = getRoles();
                return (keycloakRoles != null && keycloakRoles.contains(role));
            }
            
            @Override
            public List<String> getRoles() {
                List<String> roles = new ArrayList<>();
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                
                if (auth != null && auth.isAuthenticated() && auth instanceof KeycloakAuthenticationToken) {
                    roles = ((KeycloakAuthenticationToken) auth).getAuthorities()
                                                                .stream()
                                                                .map(GrantedAuthority::getAuthority)
                                                                .collect(Collectors.toList());
                } 
                logger.debug("getRoles: {}", roles);
                return roles;
            }

            
            @Override
            public String getName() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String name = "";
                if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof KeycloakPrincipal) {
                    name = ((KeycloakPrincipal<?>) auth.getPrincipal()).getName();
                }
                logger.debug("getName: {}", name);
                return name;
            }
        };
    }

}
