///*
// * Copyright 2019 Red Hat, Inc. and/or its affiliates.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.kie.server.services.impl.security.adapters;
//
//import java.security.Principal;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Collectors;
//import java.util.stream.StreamSupport;
////import java.util.Set;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.kie.server.api.security.SecurityAdapter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
////import org.keycloak.KeycloakPrincipal;
//import org.wildfly.security.auth.server.SecurityDomain;
//
//public class KeycloakSecurityAdapter implements SecurityAdapter {
//
//    private static final Logger logger = LoggerFactory.getLogger(KeycloakSecurityAdapter.class);
//
////    private boolean active;
//
//    public KeycloakSecurityAdapter() {
////        try {
////            Class.forName("org.keycloak.KeycloakPrincipal");
////            Class.forName("org.keycloak.KeycloakSecurityContext");
////            active = true;
////        } catch (Exception e) {
////            logger.debug("KeyCloak not on classpath due to {}", e.toString());
////            active = false;
////        }
//    }
//
//    @Override
//    public String getUser(Object... params) {
//        Principal principal = SecurityDomain.getCurrent().getCurrentSecurityIdentity().getPrincipal();
//
////        if (active) {
////            HttpServletRequest request = KeycloakSecurityFilter.getRequest();
////            if (request != null && request.getUserPrincipal() != null && request.getUserPrincipal() instanceof KeycloakPrincipal) {
////                return request.getUserPrincipal().getName();
////            }
////        }
//        return principal.getName();
//    }
//
//    @Override
//    public List<String> getRoles(Object... params) {
//        return StreamSupport.stream(SecurityDomain.getCurrent().getCurrentSecurityIdentity().getRoles().spliterator(), false).collect(Collectors.toList());
////        if (true) {
////            HttpServletRequest request = KeycloakSecurityFilter.getRequest();
////            if (request != null && request.getUserPrincipal() != null && request.getUserPrincipal() instanceof KeycloakPrincipal) {
////                KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) request.getUserPrincipal();
////                return new ArrayList(keycloakPrincipal.getKeycloakSecurityContext().getToken().getRealmAccess().getRoles());
////            }
////        }
////        return null;
//    }
//}
