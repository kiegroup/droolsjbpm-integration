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

package org.kie.server.services.jbpm.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jbpm.services.task.identity.adapter.UserGroupAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.security.auth.server.RealmIdentity;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.auth.server.SecurityIdentity;
import org.wildfly.security.authz.AuthorizationFailureException;

public class ElytronUserGroupAdapter implements UserGroupAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ElytronUserGroupAdapter.class);

    @Override
    public List<String> getGroupsForUser(String userId) {

        String userName = getUserName();
        logger.debug("Identifier Elytron as {}", userId);
        if (userName == null) {
            return new ArrayList<>();
        }

        if (userId.equals(userName)) {
            logger.debug("User identified as {} but auth as {}", userId, userName);
            return toPrincipalRoles(userId);
        } else {
            try {
                if (runAsPrincipalExists(userId)) {
                    logger.debug("Executing run as {}", userId);
                   return toRunAsPrincipalRoles(userId, true);
                } else {
                   return new ArrayList<>();
                }
            } catch (AuthorizationFailureException ex) {
                return toRunAsPrincipalRoles(userId, false);
            } catch (RealmUnavailableException | SecurityException e) {
                return new ArrayList<>();
            }
        }
    }

    public List<String> toPrincipalRoles(String userId) {
        return toRoles(getCurrentSecurityIdentity().get());
    }

    public List<String> toRunAsPrincipalRoles(String userId, boolean authenticate) {
        return toRoles(getCurrentSecurityIdentity().get().createRunAsIdentity(userId, authenticate));
    }

    public String getUserName() {
        Optional<SecurityIdentity> identityOptional = getCurrentSecurityIdentity();
        return identityOptional.isPresent() ? identityOptional.get().getPrincipal().getName() : null;
    }

    public List<String> toRoles(SecurityIdentity securityIdentity) {
        if (securityIdentity == null) {
            return new ArrayList<>();
        }

        List<String> roles = StreamSupport.stream(securityIdentity.getRoles().spliterator(), false)
                .collect(Collectors.toCollection(ArrayList::new));
        return roles;
    }

    public boolean runAsPrincipalExists(String runAsPrincipal) throws RealmUnavailableException {
        SecurityDomain securityDomain = SecurityDomain.getCurrent();
        RealmIdentity realmIdentity = null;
        try {
            realmIdentity = securityDomain.getIdentity(runAsPrincipal);
            return realmIdentity.exists();
        } finally {
            if (realmIdentity != null) {
                realmIdentity.dispose();
            }
        }
    }

    public Optional<SecurityIdentity> getCurrentSecurityIdentity() {
        SecurityDomain securityDomain = SecurityDomain.getCurrent();
        if (securityDomain == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(securityDomain.getCurrentSecurityIdentity());
        }
    }
}
