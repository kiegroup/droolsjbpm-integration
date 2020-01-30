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

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.kie.server.api.model.taskassigning.PlanningItem;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.User;
import org.kie.server.services.taskassigning.core.model.solver.realtime.AddTaskProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.AssignTaskProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.ReleaseTaskProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.RemoveTaskProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.TaskPropertyChangeProblemFactChange;
import org.kie.server.services.taskassigning.planning.util.IndexedElement;
import org.slf4j.Logger;

public class TraceHelper {

    private static final Function<String, String> BREAK = (s) -> System.lineSeparator() + s;
    private static final String TASK_WITH_NAME_FORMAT = " -> ({}, {})";

    private TraceHelper() {
    }

    static void traceProgrammedChanges(Logger logger, List<RemoveTaskProblemFactChange> removedTasksChanges,
                                       List<ReleaseTaskProblemFactChange> releasedTasksChanges,
                                       Map<String, List<IndexedElement<AssignTaskProblemFactChange>>> changesByUserId,
                                       List<TaskPropertyChangeProblemFactChange> propertyChanges,
                                       List<AddTaskProblemFactChange> newTaskChanges) {

        logger.trace(BREAK.apply("*** Removed tasks ***"));
        logger.trace("Total tasks removed from solution is {}", removedTasksChanges.size());
        removedTasksChanges.forEach(change -> logger.trace(TASK_WITH_NAME_FORMAT, change.getTask().getId(), change.getTask().getName()));
        logger.trace("*** End of Removed tasks ***");

        logger.trace(BREAK.apply("*** Released tasks ***"));
        logger.trace("Total tasks released from solution is {}", releasedTasksChanges.size());
        releasedTasksChanges.forEach(change -> logger.trace(TASK_WITH_NAME_FORMAT, change.getTask().getId(), change.getTask().getName()));
        logger.trace("*** End of Released tasks ***");

        logger.trace(BREAK.apply("*** Changes per user ***"));
        logger.trace("Total users with programmed changes is {}", changesByUserId.size());
        changesByUserId.forEach((key, userChanges) -> {
            if (userChanges != null) {
                userChanges.forEach(change -> {
                    logger.trace(BREAK.apply("  AssignTaskToUserChanges for user: {}"), key);

                    logger.trace(BREAK.apply("   -> taskId: {}, pinned: {}, index: {}, status: {}"),
                                 change.getElement().getTask().getId(),
                                 change.isPinned(),
                                 change.getIndex(),
                                 change.getElement().getTask().getStatus());
                    logger.trace("  End of AssignTaskToUserChanges for user: {}", key);
                });
            }
        });
        logger.trace("*** End of Changes per user ***");

        logger.trace(BREAK.apply("*** Property changes ***"));
        logger.trace("Total tasks with property changes is {}", propertyChanges.size());

        propertyChanges.forEach(change -> {
            String changeDesc = "";
            if (change.getPriority() != null) {
                changeDesc = " setPriority = " + change.getPriority();
            }
            if (change.getStatus() != null) {
                changeDesc = " setStatus = " + change.getStatus();
            }
            logger.trace(TASK_WITH_NAME_FORMAT + " {}", change.getTask().getId(), change.getTask().getName(), changeDesc);
        });
        logger.trace("*** End of Property changes ***");

        logger.trace(BREAK.apply("*** New tasks ***"));
        logger.trace("Total new tasks added to solution is {}", newTaskChanges.size());
        newTaskChanges.forEach(change -> logger.trace(TASK_WITH_NAME_FORMAT, change.getTask().getId(), change.getTask().getName()));
        logger.trace("*** End of New tasks ***");
    }

    static void traceSolution(Logger logger, TaskAssigningSolution solution) {
        logger.trace(BREAK.apply("*** Start of solution trace, with users = {} and tasks = {} ***"), solution.getUserList().size(), solution.getTaskList().size());
        for (User user : solution.getUserList()) {
            Task nextTask = user.getNextTask();
            while (nextTask != null) {
                logger.trace("{} -> {}, pinned: {}, priority: {}, status: {}",
                             user.getEntityId(),
                             nextTask.getId(),
                             nextTask.isPinned(),
                             nextTask.getPriority(),
                             nextTask.getStatus());

                nextTask = nextTask.getNextTask();
            }
        }
        logger.trace("*** End of solution trace ***");
    }

    static void tracePublishedTasks(Logger logger, List<PlanningItem> publishedTasks) {
        logger.trace(BREAK.apply("*** Start of published tasks trace with {} published tasks ***"), publishedTasks.size());
        publishedTasks.forEach(item -> logger.trace("{} -> {}, index: {}, published: {}",
                                                    item.getPlanningTask().getAssignedUser(),
                                                    item.getTaskId(),
                                                    item.getPlanningTask().getIndex(),
                                                    item.getPlanningTask().isPublished()));
        logger.trace("*** End of published trace ***");
    }
}
