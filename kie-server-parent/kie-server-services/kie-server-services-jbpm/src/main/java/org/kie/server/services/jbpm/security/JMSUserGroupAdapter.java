/*
 * Copyright 2015 JBoss Inc
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
import org.kie.server.services.impl.security.adapters.JMSSecurityAdapter;

public class JMSUserGroupAdapter implements UserGroupAdapter {

    private JMSSecurityAdapter jmsSecurityAdapter = new JMSSecurityAdapter();

    @Override
    public List<String> getGroupsForUser(String userId) {
        if (userId.equals(jmsSecurityAdapter.getUser())) {
            return jmsSecurityAdapter.getRoles();
        }

        return Collections.emptyList();
    }
}
