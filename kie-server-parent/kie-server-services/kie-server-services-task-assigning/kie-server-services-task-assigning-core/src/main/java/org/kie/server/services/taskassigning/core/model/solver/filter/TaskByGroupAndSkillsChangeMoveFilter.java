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

package org.kie.server.services.taskassigning.core.model.solver.filter;

import org.kie.server.services.taskassigning.core.model.DefaultLabels;
import org.kie.server.services.taskassigning.core.model.ModelConstants;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.TaskOrUser;
import org.kie.server.services.taskassigning.core.model.User;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static org.kie.server.services.taskassigning.core.model.solver.TaskHelper.hasAllLabels;
import static org.kie.server.services.taskassigning.core.model.solver.TaskHelper.isPotentialOwner;

/**
 * SelectionFilter implementation for determining if a move of a TaskA to UserB can be realized. The move is accepted
 * if UserB is a potential owner for the task and has all the required skills for the task if any, or if it's the
 * planning user.
 */
public class TaskByGroupAndSkillsChangeMoveFilter
        implements SelectionFilter<TaskAssigningSolution, ChangeMove<TaskAssigningSolution>> {

    @Override
    public boolean accept(ScoreDirector<TaskAssigningSolution> scoreDirector, ChangeMove<TaskAssigningSolution> changeMove) {
        final Task taskToMove = (Task) changeMove.getEntity();
        final TaskOrUser taskOrUser = (TaskOrUser) changeMove.getToPlanningValue();
        final User user = taskOrUser.getUser();

        return user != null && user.isEnabled() &&
                (ModelConstants.IS_PLANNING_USER.test(user.getEntityId()) ||
                        (isPotentialOwner(taskToMove, user) && hasAllLabels(taskToMove, user, DefaultLabels.SKILLS.name())));
    }
}
