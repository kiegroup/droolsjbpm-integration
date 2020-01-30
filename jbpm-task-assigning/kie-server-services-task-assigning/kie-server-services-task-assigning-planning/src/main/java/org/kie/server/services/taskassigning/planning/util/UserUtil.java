/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.taskassigning.planning.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.kie.server.api.model.taskassigning.UserType;
import org.kie.server.services.taskassigning.core.model.Group;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.User;

public class UserUtil {

    private UserUtil() {
    }

    public static User fromExternalUser(org.kie.server.services.taskassigning.user.system.api.User externalUser) {
        final User user = new User(externalUser.getId().hashCode(), externalUser.getId());
        final Set<Group> groups = new HashSet<>();
        user.setGroups(groups);
        if (externalUser.getGroups() != null) {
            externalUser.getGroups().forEach(externalGroup -> groups.add(new Group(externalGroup.getId().hashCode(), externalGroup.getId())));
        }
        return user;
    }

    public static boolean isUser(String userType) {
        return UserType.User.equals(userType);
    }

    public static List<Task> extractTasks(User user, Predicate<Task> predicate) {
        List<Task> result = new ArrayList<>();
        Task nextTask = user != null ? user.getNextTask() : null;
        while (nextTask != null) {
            if (predicate.test(nextTask)) {
                result.add(nextTask);
            }
            nextTask = nextTask.getNextTask();
        }
        return result;
    }
}
