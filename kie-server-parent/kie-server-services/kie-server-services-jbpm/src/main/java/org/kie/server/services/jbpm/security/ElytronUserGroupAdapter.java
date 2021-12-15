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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jbpm.services.task.identity.adapter.UserGroupAdapter;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.auth.server.SecurityIdentity;

public class ElytronUserGroupAdapter implements UserGroupAdapter {

    @Override
    public List<String> getGroupsForUser(String userId) {

        Optional<SecurityIdentity> identityOptional = getCurrentSecurityIdentity();

        if (identityOptional.isPresent()) {
            SecurityIdentity identity = identityOptional.get();
            if (identity.getPrincipal().getName().equals(userId)) {
                return StreamSupport.stream(identity.getRoles().spliterator(), false)
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    private Optional<SecurityIdentity> getCurrentSecurityIdentity() {
        SecurityDomain securityDomain = SecurityDomain.getCurrent();
        if (securityDomain == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(securityDomain.getCurrentSecurityIdentity());
        }
    }
}
