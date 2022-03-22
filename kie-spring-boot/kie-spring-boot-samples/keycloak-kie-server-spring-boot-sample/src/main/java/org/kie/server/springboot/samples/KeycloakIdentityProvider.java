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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.kie.internal.identity.IdentityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;


public class KeycloakIdentityProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(KeycloakIdentityProvider.class);

    @Bean
    public IdentityProvider identityProvider(ApplicationContext context) {
        Environment env = context.getEnvironment();
        Keycloak keycloakAdminClient = KeycloakBuilder.builder()
                .serverUrl(env.getProperty("keycloak.auth-server-url"))
                .realm(env.getProperty("keycloak.realm"))
                .clientId(env.getProperty("keycloak.resource"))
                .username(env.getProperty("keycloak-identity.admin.user"))
                .password(env.getProperty("keycloak-identity.admin.pwd"))
                .build();

        return new IdentityProvider() {
                
            @Override
            public List<String> getRolesFor(String userId) {
                if(getName().equals(userId)) {
                    return getRoles();
                } else {
                    // presumed this is cached.
                    try {
                        UsersResource usersResource = keycloakAdminClient.realm(env.getProperty("keycloak.realm")).users();
                        List<UserRepresentation> users = usersResource.search(userId);
                        if (users.isEmpty()) {
                          return Collections.emptyList();
                        }
                        UserRepresentation user = users.get(0);
                        return usersResource.get(user.getId()).roles().realmLevel().listAll().stream().map(RoleRepresentation::getName).collect(Collectors.toList());
                    } catch (Exception e) {
                        logger.debug("getRolesFor({}) caused an error while querying keycloack", userId, e);
                        return Collections.emptyList();
                    }
                }
            }

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
