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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.kie.server.api.model.taskassigning.PlanningItem;
import org.kie.server.api.model.taskassigning.PlanningTask;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.User;

import static org.kie.server.services.taskassigning.core.model.ModelConstants.IS_NOT_DUMMY;
import static org.kie.server.services.taskassigning.core.model.ModelConstants.IS_PLANNING_USER;
import static org.kie.server.services.taskassigning.planning.util.UserUtil.extractTasks;

public class PlanningBuilder {

    private TaskAssigningSolution solution;
    private int publishWindowSize;

    private PlanningBuilder() {
    }

    public static PlanningBuilder create() {
        return new PlanningBuilder();
    }

    public PlanningBuilder withSolution(TaskAssigningSolution solution) {
        this.solution = solution;
        return this;
    }

    public PlanningBuilder withPublishWindowSize(int publishWindowSize) {
        this.publishWindowSize = publishWindowSize;
        return this;
    }

    public List<PlanningItem> build() {
        return solution.getUserList().stream()
                .map(this::buildPlanningItems)
                .flatMap(Collection::stream)
                .filter(item -> item.getPlanningTask().isPublished()).collect(Collectors.toList());
    }

    private List<PlanningItem> buildPlanningItems(User user) {
        int index = 0;
        int publishedCount = 0;
        PlanningItem planningItem;
        final List<Task> userTasks = extractTasks(user, IS_NOT_DUMMY); //dummy tasks has nothing to with the jBPM runtime, don't process them
        final List<PlanningItem> userPlanningItems = new ArrayList<>();
        for (Task task : userTasks) {
            planningItem = PlanningItem.builder()
                    .containerId(task.getContainerId())
                    .taskId(task.getId())
                    .processInstanceId(task.getProcessInstanceId())
                    .planningTask(PlanningTask.builder()
                                          .taskId(task.getId())
                                          .published(task.isPinned())
                                          .assignedUser(user.getUser().getEntityId())
                                          .index(index++)
                                          .build())
                    .build();

            userPlanningItems.add(planningItem);
            publishedCount += planningItem.getPlanningTask().isPublished() ? 1 : 0;
        }

        Iterator<PlanningItem> userPlanningItemsIt = userPlanningItems.iterator();
        while (userPlanningItemsIt.hasNext() && (publishedCount < publishWindowSize || IS_PLANNING_USER.test(user.getEntityId()))) {
            planningItem = userPlanningItemsIt.next();
            if (!planningItem.getPlanningTask().isPublished()) {
                planningItem.getPlanningTask().setPublished(true);
                publishedCount++;
            }
        }
        return userPlanningItems;
    }
}
