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

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;

import org.kie.server.api.security.SecurityAdapter;

import static java.util.Collections.emptyList;

public class JACCIdentityProvider
        extends BaseIdentityProvider {

    public static boolean available() {
        return getSubjectFromContainer() != null;
    }

    @Override
    public String getName() {
        if (!contextUsers.isEmpty()) {
            return contextUsers.peek();
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
        if (!contextUsers.isEmpty()) {
            return emptyList();
        }

        List<String> roles = new ArrayList<String>();

        Subject subject = getSubjectFromContainer();
        if (subject != null) {
            Set<Principal> principals = subject.getPrincipals();

            if (principals != null) {

                roles = new ArrayList<String>();
                for (Principal principal : principals) {
                    if (principal instanceof Group) {
                        Enumeration<? extends Principal> groups = ((Group) principal).members();

                        while (groups.hasMoreElements()) {
                            Principal groupPrincipal = groups.nextElement();
                            roles.add(groupPrincipal.getName());
                        }
                        break;
                    }
                }
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
                    if (principal instanceof Group) {
                        Enumeration<? extends Principal> groups = ((Group) principal).members();
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
            return (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
        } catch (Exception e) {
            return null;
        }
    }

    protected boolean supportedPrincipal(Principal principal) {
        if (!(principal instanceof Group) && !principal.getClass().getName().endsWith("BasicAuthorizationPrincipal")) {
            return true;
        }

        return false;
    }
}
