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

package org.kie.remote.services.jms.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jbpm.services.task.identity.JAASUserGroupCallbackImpl;
import org.jbpm.services.task.identity.adapter.UserGroupAdapter;
import org.kie.api.task.TaskService;
import org.kie.remote.services.jms.RequestMessageBean;

/**
 * This UserGroupAdapter is meant to be stored in the {@link JAASUserGroupCallbackImpl}'s
 * {@link ThreadLocal} field for external {@link UserGroupAdapter} implementations. 
 * </p>
 * It's necessary because the {@link RequestMessageBean} itself must run as a specific or
 * anonymous user (per EJB/MDB specs). 
 * </p>
 * In order to make sure that a {@link TaskService} command does run with the right user/group
 * info, an instance of this adapter is created with the correct values and injected. 
 */
public class JmsUserGroupAdapter implements UserGroupAdapter {

    private final String userId;
    private final List<String> localGroups;
    
    public JmsUserGroupAdapter(String userId, String... groups) {
        this.userId = userId;
        this.localGroups = Arrays.asList(groups);
    }
    
    @Override
    public List<String> getGroupsForUser(String userId) {
        if( this.userId.equals(userId) ) { 
            return localGroups;
        }
        return Collections.EMPTY_LIST;
    }

}
