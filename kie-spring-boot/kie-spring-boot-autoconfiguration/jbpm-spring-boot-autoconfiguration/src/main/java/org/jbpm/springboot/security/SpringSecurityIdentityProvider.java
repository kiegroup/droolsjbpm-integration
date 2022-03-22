/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.springboot.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.kie.internal.identity.IdentityProvider;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;

import static java.util.Collections.emptyList;

public class SpringSecurityIdentityProvider implements IdentityProvider {

    private Stack<String> contextUsers;
    private ApplicationContext context;
    
    public SpringSecurityIdentityProvider(ApplicationContext context) {
        this.context = context;
        contextUsers = new Stack<>();
    }
    @Override
    public void setContextIdentity(String userId) {
        contextUsers.push(userId);
    }

    @Override
    public void removeContextIdentity() {
        contextUsers.pop();
    }

    public String getName() {
        if(!contextUsers.isEmpty()) {
            return contextUsers.peek();
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return UNKNOWN_USER_IDENTITY;
    }

    @Override
    public List<String> getRolesFor(String userId) {
        if (getName().equals(userId)) {
            return getRoles();
        } else {
            try {
                UserDetailsManager manager = context.getBean(UserDetailsManager.class);
                UserDetails userDetails = manager.loadUserByUsername(userId);
                return toRoles(userDetails.getAuthorities());
            } catch (NoSuchBeanDefinitionException | UsernameNotFoundException e) {
                return emptyList();
            }
        }
    }

    public List<String> getRoles() {
        if(!contextUsers.isEmpty()) {
            return getRolesFor(contextUsers.peek());
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return toRoles(auth.getAuthorities());
        }

        return emptyList();
    }

    private List<String> toRoles(Collection<? extends GrantedAuthority> authorities) {
        List<String> roles = new ArrayList<String>();

        for (GrantedAuthority ga : authorities) {
            String roleName = ga.getAuthority();
            if (roleName.startsWith("ROLE_")) {
                roleName = roleName.replaceFirst("ROLE_", "");
            }
            roles.add(roleName);
        }

        return roles;
    }

    public boolean hasRole(String role) {
        return false;
    }
}