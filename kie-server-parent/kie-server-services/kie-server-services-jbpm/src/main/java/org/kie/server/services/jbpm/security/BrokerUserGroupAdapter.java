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

package org.kie.server.services.jbpm.security;

import java.util.Collections;
import java.util.List;

import org.jbpm.services.task.identity.adapter.UserGroupAdapter;
import org.kie.server.services.impl.security.adapters.BrokerSecurityAdapter;

public class BrokerUserGroupAdapter implements UserGroupAdapter {

    private BrokerSecurityAdapter jmsSecurityAdapter = new BrokerSecurityAdapter();

    @Override
    public List<String> getGroupsForUser(String userId) {
        if (userId.equals(jmsSecurityAdapter.getUser())) {
            return jmsSecurityAdapter.getRoles();
        }

        return Collections.emptyList();
    }
}
