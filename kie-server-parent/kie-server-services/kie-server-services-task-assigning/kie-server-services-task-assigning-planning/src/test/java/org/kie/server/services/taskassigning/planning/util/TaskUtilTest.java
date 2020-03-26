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

package org.kie.server.services.taskassigning.planning.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.kie.server.api.model.taskassigning.OrganizationalEntity;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.api.model.taskassigning.UserType;
import org.kie.server.services.taskassigning.core.model.Group;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.kie.server.services.taskassigning.core.model.DefaultLabels.AFFINITIES;
import static org.kie.server.services.taskassigning.core.model.DefaultLabels.SKILLS;

public class TaskUtilTest {

    private static final String SKILLS_ATTRIBUTE = "skills";
    private static final String AFFINITIES_ATTRIBUTE = "affinities";
    private static final long TASK_ID = 0L;
    private static final long PROCESS_INSTANCE_ID = 1L;
    private static final String PROCESS_ID = "PROCESS_ID";
    private static final String CONTAINER_ID = "CONTAINER_ID";
    private static final String NAME = "NAME";
    private static final int PRIORITY = 2;
    private static final String STATUS = "Ready";
    private static final Map<String, Object> INPUT_DATA = new HashMap<>();
    private static final OrganizationalEntity OE_1 = OrganizationalEntity.builder().type(UserType.USER).name("OE1").build();
    private static final OrganizationalEntity OE_2 = OrganizationalEntity.builder().type(UserType.GROUP).name("OE2").build();
    private static final String SKILL1 = "skill1";
    private static final String SKILL2 = "skill2";
    private static final String SKILL3 = "skill3";
    private static final String AFFINITY1 = "affinity1";
    private static final String AFFINITY2 = "affinity2";

    @Test
    public void fromTaskData() {
        fromTaskData(INPUT_DATA, Collections.emptySet(), Collections.emptySet());
    }

    @Test
    public void fromTaskDataWithSkillsAndAffinities() {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put(SKILLS_ATTRIBUTE, String.format("%s,  %s,%s", SKILL2, SKILL1, SKILL3));
        inputData.put(AFFINITIES_ATTRIBUTE, String.format("  %s,  %s", AFFINITY2, AFFINITY1));
        fromTaskData(inputData, new HashSet<>(Arrays.asList(SKILL3, SKILL2, SKILL1)), new HashSet<>(Arrays.asList(AFFINITY1, AFFINITY2)));
    }

    private void fromTaskData(Map<String, Object> inputData, Set<Object> expectedSkills, Set<Object> expectedAffinities) {
        Set<OrganizationalEntity> potentialOwners = new HashSet<>();
        potentialOwners.add(OE_1);
        potentialOwners.add(OE_2);

        TaskData taskData = TaskData.builder()
                .taskId(TASK_ID)
                .processInstanceId(PROCESS_INSTANCE_ID)
                .processId(PROCESS_ID)
                .containerId(CONTAINER_ID)
                .name(NAME)
                .priority(PRIORITY)
                .status(STATUS)
                .inputData(inputData)
                .potentialOwners(potentialOwners)
                .build();

        Task task = TaskUtil.fromTaskData(taskData);
        assertEquals(TASK_ID, task.getId(), 0);
        assertEquals(PROCESS_INSTANCE_ID, task.getProcessInstanceId(), 0);
        assertEquals(PROCESS_ID, task.getProcessId());
        assertEquals(CONTAINER_ID, task.getContainerId());
        assertEquals(NAME, task.getName());
        assertEquals(PRIORITY, task.getPriority(), 0);
        assertEquals(STATUS, task.getStatus());
        assertEquals(inputData, task.getInputData());
        assertEquals(potentialOwners.size(), task.getPotentialOwners().size(), 2);
        User user = task.getPotentialOwners().stream()
                .filter(u -> OE_1.getName().equals(u.getEntityId()) && u.isUser())
                .map(u -> (User) u)
                .findFirst().orElse(null);
        assertNotNull(user);
        Group group = task.getPotentialOwners().stream()
                .filter(g -> OE_2.getName().equals(g.getEntityId()) && !g.isUser())
                .map(g -> (Group) g)
                .findFirst().orElse(null);
        assertNotNull(group);
        assertEquals(expectedSkills, task.getLabelValues(SKILLS.name()));
        assertEquals(expectedAffinities, task.getLabelValues(AFFINITIES.name()));
    }
}
