/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.taskassigning.core.model;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.bendablelong.BendableLongScore;
import org.optaplanner.persistence.xstream.api.score.buildin.bendable.BendableScoreXStreamConverter;

@PlanningSolution
@XStreamAlias("TaTaskAssignmentSolution")
public class TaskAssigningSolution extends AbstractPersistable {

    @ValueRangeProvider(id = "userRange")
    @ProblemFactCollectionProperty
    private List<User> userList;

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "taskRange")
    private List<Task> taskList;

    @XStreamConverter(BendableScoreXStreamConverter.class)
    @PlanningScore(bendableHardLevelsSize = 2, bendableSoftLevelsSize = 6)
    private BendableLongScore score;

    public TaskAssigningSolution() {
    }

    public TaskAssigningSolution(long id, List<User> userList, List<Task> taskList) {
        super(id);
        this.userList = userList;
        this.taskList = taskList;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public BendableLongScore getScore() {
        return score;
    }

    public void setScore(BendableLongScore score) {
        this.score = score;
    }
}
