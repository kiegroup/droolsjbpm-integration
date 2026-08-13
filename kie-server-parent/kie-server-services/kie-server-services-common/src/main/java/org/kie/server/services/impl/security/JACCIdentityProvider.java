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

import org.kie.server.api.security.SecurityAdapter;

import javax.security.auth.Subject;
import jakarta.security.jacc.PolicyContext;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.kie.server.services.impl.security.SecurityMigrationUtils.getGroupsEquivalentReflective;
import static org.kie.server.services.impl.security.SecurityMigrationUtils.getRolesFromPrincipals;
import static org.kie.server.services.impl.security.SecurityMigrationUtils.isGroupPrincipal;

public class JACCIdentityProvider
        extends BaseIdentityProvider {

    public static final String SECURITY_AUTH_SUBJECT_CONTAINER = "javax.security.auth.Subject.container";

    @Override
    public String getName() {
        if (contextUsers.get() != null) {
            return contextUsers.get();
        }

        Subject subject = getSubjectFromContainer();

        if (subject != null) {
            Set<Principal> principals = subject.getPrincipals();

            if (principals != null) {
                for (Principal principal : principals) {
                    if (supportedPrincipal(principal)) {
                        return principal.getName();
                    }
                }
            }
        }
        return getNameFromAdapter();
    }

    @Override
    public List<String> getRoles() {
        if (contextUsers.get() != null) {
            return emptyList();
        }

        List<String> roles = new ArrayList<String>();

        Subject subject = getSubjectFromContainer();
        if (subject != null) {
            Set<Principal> principals = subject.getPrincipals();

            if (principals != null) {
                roles = getRolesFromPrincipals(principals);
            }
        }

        roles.addAll(getRolesFromAdapter());

        return roles;
    }

    @Override
    public boolean hasRole(String s) {
        Subject subject = getSubjectFromContainer();
        if (subject != null) {
            Set<Principal> principals = subject.getPrincipals();
            if (principals != null) {
                for (Principal principal : principals) {
                    if (isGroupPrincipal(principal)) {
                        Optional<Enumeration<? extends Principal>> optionalGroups = getGroupsEquivalentReflective(principal);
                        if (optionalGroups.isPresent()) {
                            Enumeration<? extends Principal> groups = optionalGroups.get();
                            while (groups.hasMoreElements()) {
                                Principal groupPrincipal = groups.nextElement();
                                if (groupPrincipal.getName().equals(s)) {
                                    return true;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        for (SecurityAdapter adapter : adapters) {
            List<String> adapterRoles = adapter.getRoles();
            if (adapterRoles != null && adapterRoles.contains(s)) {
                return true;
            }
        }

        return false;
    }

    protected static Subject getSubjectFromContainer() {
        try {
            return (Subject) PolicyContext.getContext(SECURITY_AUTH_SUBJECT_CONTAINER);
        } catch (Exception e) {
            return null;
        }
    }

    protected boolean supportedPrincipal(Principal principal) {
        return !isGroupPrincipal(principal) && !principal.getClass().getName().endsWith("BasicAuthorizationPrincipal");
    }


}
