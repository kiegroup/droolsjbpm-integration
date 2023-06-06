/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.impl.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.kie.server.api.security.SecurityAdapter;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.auth.server.SecurityIdentity;

import static java.util.Collections.emptyList;

public class ElytronIdentityProvider
        extends BaseIdentityProvider {

    public static boolean available() {
        try {
            // ensure SecurityDomain is on classpath, if not directly return false
            Class.forName("org.wildfly.security.auth.server.SecurityDomain");
            return SecurityDomain.getCurrent() != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public String getName() {
        if (!contextUsers.isEmpty()) {
            return contextUsers.peek();
        }
        final Optional<SecurityIdentity> identity = getCurrentSecurityIdentity();
        if (identity.isPresent()) {
            if (identity != null && identity.get().getPrincipal() != null && !Objects.equals("anonymous", identity.get().getPrincipal().getName())) {
                return identity.get().getPrincipal().getName();
            }
        }

        return getNameFromAdapter();
    }

    @Override
    public List<String> getRoles() {
        if (!contextUsers.isEmpty()) {
            return emptyList();
        }

        final List<String> roles = new ArrayList<String>();

        roles.addAll(getRolesFromSecurityIdentity());
        roles.addAll(getRolesFromAdapter());

        return roles;
    }

    @Override
    public boolean hasRole(String s) {
        if (getRolesFromSecurityIdentity().contains(s)) {
            return true;
        }
        for (SecurityAdapter adapter : adapters) {
            List<String> adapterRoles = adapter.getRoles();
            if (adapterRoles != null && adapterRoles.contains(s)) {
                return true;
            }
        }

        return false;
    }

    private List<String> getRolesFromSecurityIdentity() {
        final List<String> result = new ArrayList<String>();
        final Optional<SecurityIdentity> identity = getCurrentSecurityIdentity();
        if (identity.isPresent()) {
            for (String role : identity.get().getRoles()) {
                result.add(role);
            }
        }

        return result;
    }

    private Optional<SecurityIdentity> getCurrentSecurityIdentity() {
        SecurityDomain securityDomain = SecurityDomain.getCurrent();
        if (securityDomain == null) {
            return Optional.empty();
        } else {
            return Optional.of(securityDomain.getCurrentSecurityIdentity());
        }
    }
}
