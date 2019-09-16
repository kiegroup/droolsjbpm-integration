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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

public class IdentityProviderUtils {

    private static IdentityProviderUtils utils = new IdentityProviderUtils();

    private static ThreadLocal<Object> currentSecurityIdentityLocal = new ThreadLocal<Object>();

    private Class securityDomain;
    private Class securityIdentity;
    private Method currentSecurityDomain;
    private Method currentSecurityIdentity;
    private Method currentPrincipal;
    private Method currentRoles;

    private boolean isWildflyElytronKeycloak;

    private IdentityProviderUtils() {
        try {
            Class.forName("org.keycloak.KeycloakPrincipal");
            securityDomain = Class.forName("org.wildfly.security.auth.server.SecurityDomain");
            securityIdentity = Class.forName("org.wildfly.security.auth.server.SecurityIdentity");
            currentSecurityDomain = securityDomain.getMethod("getCurrent", new Class[0]);
            currentSecurityIdentity = securityDomain.getMethod("getCurrentSecurityIdentity", new Class[0]);
            currentPrincipal = securityIdentity.getMethod("getPrincipal", new Class[0]);
            currentRoles = securityIdentity.getMethod("getRoles", new Class[0]);

            isWildflyElytronKeycloak = true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            isWildflyElytronKeycloak = false;
            e.printStackTrace();
        }
    }

    public static IdentityProviderUtils getUtils() {
        return utils;
    }

    public String getName() {

        if (isWildflyElytronKeycloak) {
            try {
                Object currentSecurityIdentityObject = currentSecurityIdentityLocal.get();
                if (currentSecurityIdentityObject == null) {
                    Object securityDomainObject = currentSecurityDomain.invoke(null, new Object[0]);
                    currentSecurityIdentityObject = currentSecurityIdentity.invoke(securityDomainObject, new Object[0]);
                    currentSecurityIdentityLocal.set(currentSecurityIdentityObject);
                }
                Principal principal = (Principal) currentPrincipal.invoke(currentSecurityIdentityObject, new Object[0]);
                return principal.getName();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
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

        if (isWildflyElytronKeycloak) {
            try {
                Object currentSecurityIdentityObject = currentSecurityIdentityLocal.get();
                if (currentSecurityIdentityObject == null) {
                    Object securityDomainObject = currentSecurityDomain.invoke(null, new Object[0]);
                    currentSecurityIdentityObject = currentSecurityIdentity.invoke(securityDomainObject, new Object[0]);
                    currentSecurityIdentityLocal.set(currentSecurityIdentityObject);
                }

                Iterable<String> roles = (Iterable<String>) currentRoles.invoke(currentSecurityIdentityObject, new Object[0]);
                return StreamSupport.stream(roles.spliterator(), false).collect(Collectors.toList());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
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
