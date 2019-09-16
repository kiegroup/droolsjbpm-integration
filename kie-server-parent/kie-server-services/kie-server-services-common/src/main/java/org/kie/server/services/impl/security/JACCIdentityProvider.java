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
import java.util.ServiceLoader;

import org.kie.internal.identity.IdentityProvider;
import org.kie.server.api.security.SecurityAdapter;
import org.kie.server.services.impl.util.IdentityProviderUtils;

public class JACCIdentityProvider implements IdentityProvider {

    private static final ServiceLoader<SecurityAdapter> securityAdapters = ServiceLoader.load(SecurityAdapter.class);

    private List<SecurityAdapter> adapters = new ArrayList<>();

    private static final String USE_KEYCLOAK_PROPERTY = "org.jbpm.workbench.kie_server.keycloak";

    private static boolean KIE_SERVER_KEYCLOAK = Boolean.parseBoolean(System.getProperty(USE_KEYCLOAK_PROPERTY, "false"));

    public JACCIdentityProvider() {
        for (SecurityAdapter adapter : securityAdapters) {
            adapters.add(adapter);
        }
    }

    @Override
    public String getName() {

        String name = IdentityProviderUtils.getUtils().getName();
        if (name.isEmpty() || name == null) {
            return getNameFromAdapter();
        }
        return name;
    }

    @Override
    public List<String> getRoles() {

        List<String> roles = new ArrayList<String>();

        roles.addAll(IdentityProviderUtils.getUtils().getRoles());
        roles.addAll(getRolesFromAdapter());

        return roles;
    }

    @Override
    public boolean hasRole(String s) {
        return false;
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
