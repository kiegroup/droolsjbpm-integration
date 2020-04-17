/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.taskassigning.planning;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kie.server.services.taskassigning.core.model.AbstractPersistable;
import org.kie.server.services.taskassigning.core.model.OrganizationalEntity;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskOrUser;
import org.kie.server.services.taskassigning.core.model.User;
import org.kie.server.services.taskassigning.user.system.api.Group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestUtil {

    private TestUtil() {
    }

    public static User mockUser(long userId, List<Task> tasks) {
        return initializeUser(new User(userId, "User_" + userId), tasks);
    }

    public static User initializeUser(User user, List<Task> tasks) {
        TaskOrUser previous = user;
        for (Task task : tasks) {
            previous.setNextTask(task);
            task.setPreviousTaskOrUser(previous);
            task.setUser(user);
            previous = task;
        }
        return user;
    }

    public static org.kie.server.services.taskassigning.user.system.api.Group mockExternalGroup(String groupId) {
        return () -> groupId;
    }

    public static org.kie.server.services.taskassigning.user.system.api.User mockExternalUser(String userId,
                                                                                              Set<Group> groups) {
        return new org.kie.server.services.taskassigning.user.system.api.User() {

            @Override
            public String getId() {
                return userId;
            }

            @Override
            public Set<org.kie.server.services.taskassigning.user.system.api.Group> getGroups() {
                return groups;
            }

            @Override
            public Map<String, Object> getAttributes() {
                return Collections.emptyMap();
            }
        };
    }

    public static org.kie.server.services.taskassigning.user.system.api.User mockExternalUser(String userId) {
        return mockExternalUser(userId, Collections.emptySet());
    }

    public static <T extends AbstractPersistable & OrganizationalEntity> void assertContains(String entityId, Collection<T> entities) {
        assertNotNull("entityId: " + entityId + " is missing in collection: " + entities,
                      entities.stream()
                              .filter(entity -> entityId.equals(entity.getEntityId()))
                              .filter(entity -> entityId.hashCode() == entity.getId())
                              .findFirst().orElse(null));
    }

    public static <T extends AbstractPersistable & OrganizationalEntity> void assertContains(String entityId, int times, Collection<T> entities) {
        long currentTimes = entities.stream().filter(entity -> entityId.equals(entity.getEntityId())).count();
        assertEquals("entityId: " + entityId + " is not present the expected times.", times, currentTimes);
    }

    public static <T extends AbstractPersistable & OrganizationalEntity> void assertNotContains(String entityId, Collection<T> entities) {
        assertNull("entityId: " + entityId + " must not be present in collection: " + entities,
                   entities.stream()
                           .filter(entity -> entityId.equals(entity.getEntityId()))
                           .filter(entity -> entityId.hashCode() == entity.getId())
                           .findFirst().orElse(null));
    }
}
