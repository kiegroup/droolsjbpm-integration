/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.integrationtests.jbpm.util;

import java.util.ArrayList;
import java.util.List;

import org.kie.api.task.UserGroupCallback;

public class FixedUserGroupCallbackImpl implements UserGroupCallback {

    @Override
    public boolean existsUser(String s) {
        return true;
    }

    @Override
    public boolean existsGroup(String s) {
        return true;
    }

    @Override
    public List<String> getGroupsForUser(String s) {
        ArrayList<String> groups = new ArrayList<String>();

        // User john is assigned in group engineering
        if (s.equals("john")) {
            groups.add("engineering");
        }

        return groups;
    }
}

