/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.impl.util;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;

import org.wildfly.security.auth.server.SecurityDomain;

public class IdentityProviderUtils {

    private static final String USE_KEYCLOAK_PROPERTY = "org.jbpm.workbench.kie_server.keycloak";

    private static boolean KIE_SERVER_KEYCLOAK = Boolean.parseBoolean(System.getProperty(USE_KEYCLOAK_PROPERTY, "false"));

    private static IdentityProviderUtils utils = new IdentityProviderUtils();

    private IdentityProviderUtils(){}

    public static IdentityProviderUtils getUtils(){
        return utils;
    }

    public String getName() {

        if (KIE_SERVER_KEYCLOAK){
            return SecurityDomain.getCurrent().getCurrentSecurityIdentity().getPrincipal().getName();
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
        return null;
    }

    public List<String> getRoles() {

        if (KIE_SERVER_KEYCLOAK){
            return StreamSupport.stream(SecurityDomain.getCurrent().getCurrentSecurityIdentity().getRoles().spliterator(), false).collect(Collectors.toList());
        }

        List<String> roles = new ArrayList<String>();
        Subject subject = getSubjectFromContainer();
        if (subject != null) {
            Set<Principal> principals = subject.getPrincipals();

            if (principals != null) {

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

        return roles;
    }

    protected Subject getSubjectFromContainer() {
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
