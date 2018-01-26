/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import java.util.List;

import org.kie.api.task.UserGroupCallback;
import org.kie.internal.identity.IdentityProvider;

/**
 * This implementation mimics {@link JAASUserGroupCallbackImpl} that is used on JEE application servers.
 * This one is instead based on spring security and provides exact same features.
 *
 */
public class SpringSecurityUserGroupCallback implements UserGroupCallback {

    private IdentityProvider identityProvider;   
    
    public SpringSecurityUserGroupCallback(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    @Override
    public boolean existsUser(String userId) {
        return true;
    }

    @Override
    public boolean existsGroup(String groupId) {
        return true;
    }

    @Override
    public List<String> getGroupsForUser(String userId) {
        return identityProvider.getRoles();
    }

}
