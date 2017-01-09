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
import java.util.ServiceLoader;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;

import org.kie.internal.identity.IdentityProvider;
import org.kie.server.api.security.SecurityAdapter;

public class JACCIdentityProvider implements IdentityProvider {

    private static final ServiceLoader<SecurityAdapter> securityAdapters = ServiceLoader.load(SecurityAdapter.class);

    private List<SecurityAdapter> adapters = new ArrayList<>();

    public JACCIdentityProvider() {
        for (SecurityAdapter adapter : securityAdapters) {
            adapters.add(adapter);
        }
    }

    @Override
    public String getName() {
        Subject subject = getSubjectFromContainer();

        if (subject != null) {
            Set<Principal> principals = subject.getPrincipals();

            if (principals != null) {
                for (Principal principal : principals) {
                    if (!(principal instanceof Group)) {
                        return principal.getName();
                    }
                }
            }
        }
        return getNameFromAdapter();
    }

    @Override
    public List<String> getRoles() {
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
                            Principal groupPrincipal = (Principal) groups.nextElement();
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
        return false;
    }

    protected Subject getSubjectFromContainer() {
        try {
            return (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
        } catch (Exception e) {
            return null;
        }
    }

    protected String getNameFromAdapter() {
        for (SecurityAdapter adapter : adapters) {
            String name = adapter.getUser();
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }

        return "unknown";
    }

    protected List<String> getRolesFromAdapter() {
        List<String> roles = new ArrayList<String>();

        for (SecurityAdapter adapter : adapters) {
            List<String> adapterRoles = adapter.getRoles();
            if (adapterRoles != null && !adapterRoles.isEmpty()) {
                roles.addAll(adapterRoles);
            }
        }

        return roles;
    }
}
