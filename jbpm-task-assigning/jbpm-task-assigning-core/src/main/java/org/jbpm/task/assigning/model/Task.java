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

package org.jbpm.task.assigning.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.jbpm.task.assigning.model.solver.StartAndEndTimeUpdatingVariableListener;
import org.jbpm.task.assigning.model.solver.TaskDifficultyComparator;
import org.jbpm.task.assigning.model.solver.TaskHelper;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.variable.AnchorShadowVariable;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;

/**
 * Task is the only planning entity that will be changed during the problem solving, and we have only one
 * PlanningVariable.
 * <p>
 * In this particular problem we want to do the assignments in a way that the list of tasks
 * for a given user is ordered. (see e.g. that Higher priority tasks must be resolved first is a soft constraint)
 * <p>
 * User1: A <- B <- C <- D
 * <p>
 * User2: E <- F
 * <p>
 * The initial task of each sequence points to the user that will own all the tasks in the list, so when a solution is
 * created we'll have something like this.
 * <p>
 * User1 <- A <- B <- C <- D  (In this example, User1 is the anchor)
 * <p>
 * This explains why property "previousTaskOrUser" can be assigned with User or a Task.
 * <p>
 * BUT the solver builds the solutions in a way that only the first item of the "chain" points to
 * a user. This is how a CHAINED configuration works. And the way the solver knows which must be the fact class
 * that must be used for setting the previousTaskOrUser property is by considering the order in the PlanningVariable
 * configuration.
 * @PlanningVariable(valueRangeProviderRefs = {"userRange", "taskRange"}, graphType = PlanningVariableGraphType.CHAINED)
 * <p>
 * Here we basically declared that we want to build a CHAINED graph but also since the "userRange" is the first
 * valueRangeProviderRef we are declaring that the User will be used as anchor.
 * So the solver will always start by using an User as the head of the list and then it'll consider adding Tasks
 * to the linked structure.
 * <p>
 * Additionally for the calculation of the scores, etc, given a Task of a particular solution we'll want to know quickly
 * which is the User that was assigned to this task.
 * <p>
 * If we have the following chain
 * <p>
 * Employee1 (the anchor) <- A <- B <- C <- D
 * <p>
 * and we take e.g. D, then to get the assigned Employee1 the list must be iterated, but this probably not the best idea.
 * <p>
 * Solution, add a shadow variable and let the solver populate this variable when the solution is constructed.
 * With the declaration below a shadow variable is defined for keeping a reference to the anchor. This shadow variable
 * is populated and kept consistent by the solver.
 * <p>
 * Shadow variable:
 * Let all Tasks have a reference to the anchor of the chain, the assigned user.
 * @AnchorShadowVariable(sourceVariableName = "previousTaskOrUser")
 * private User user;
 * <p>
 * CustomShadowVariable startTime:
 * Additionally is a convenient shadow variable is declared for having the startTime of a task already calculated.
 * @CustomShadowVariable(variableListenerClass = StartTimeUpdatingVariableListener.class,
 * // Arguable, to adhere to API specs (although this works), nextTask and user should also be a source,
 * // because this shadow must be triggered after nextTask and user (but there is no need to be triggered by those)
 * sources = {@PlanningVariableReference(variableName = "previousTaskOrUser")})
 * private Integer startTime;
 * <p>
 * So the variableListenerClass is invoked when the source variable is changed/assigned.
 */
@PlanningEntity(difficultyComparatorClass = TaskDifficultyComparator.class)
@XStreamAlias("TaTask")
public class Task extends TaskOrUser {

    public static final String PREVIOUS_TASK_OR_USER = "previousTaskOrUser";
    public static final String USER_RANGE = "userRange";
    public static final String TASK_RANGE = "taskRange";
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";

    private long processInstanceId;
    private String processId;
    private String containerId;
    private String name;
    private int priority;
    private Map<String, Object> inputData;

    @PlanningPin
    private boolean pinned;
    private boolean published;

    private Set<OrganizationalEntity> potentialOwners = new HashSet<>();
    private Set<TypedLabel> typedLabels = new HashSet<>();

    /**
     * Planning variable: changes during planning, between score calculations.
     */
    @PlanningVariable(valueRangeProviderRefs = {USER_RANGE, TASK_RANGE},
            graphType = PlanningVariableGraphType.CHAINED)
    private TaskOrUser previousTaskOrUser;

    /**
     * Shadow variable, let all Tasks have a reference to the anchor of the chain, the assigned user.
     */
    @AnchorShadowVariable(sourceVariableName = PREVIOUS_TASK_OR_USER)
    private User user;

    /**
     * When the previousTask changes we need to update the startTime for current task and also the startTime
     * for all the tasks that comes after.  previousTask -> currentTask -> C -> D -> E since each task can only start
     * after his previous one has finished. As part of the update the endTime for the modified tasks will also be updated.
     */
    @CustomShadowVariable(variableListenerClass = StartAndEndTimeUpdatingVariableListener.class,
            // Arguable, to adhere to API specs (although this works), nextTask and user should also be a source,
            // because this shadow must be triggered after nextTask and user (but there is no need to be triggered by those)
            sources = {@PlanningVariableReference(variableName = PREVIOUS_TASK_OR_USER)})
    private Integer startTime; // In minutes

    /**
     * Assume a duration of 1 minute for all tasks
     */
    private int duration = 1;

    /**
     * This declaration basically indicates that the endTime is actually calculated as part of the startTime shadow
     * variable calculation.
     */
    @CustomShadowVariable(variableListenerRef = @PlanningVariableReference(variableName = START_TIME))
    private Integer endTime; // In minutes

    public Task() {
    }

    public Task(long id, String name, int priority) {
        super(id);
        this.name = name;
        this.priority = priority;
        pinned = false;
    }

    public Task(long id,
                long processInstanceId,
                String processId,
                String containerId,
                String name,
                int priority,
                Map<String, Object> inputData) {
        super(id);
        this.processInstanceId = processInstanceId;
        this.processId = processId;
        this.containerId = containerId;
        this.name = name;
        this.priority = priority;
        this.inputData = inputData;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Map<String, Object> getInputData() {
        return inputData;
    }

    public void setInputData(Map<String, Object> inputData) {
        this.inputData = inputData;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    /**
     * @return the set of OrganizationalEntities that are enabled for executing this task.
     */
    public Set<OrganizationalEntity> getPotentialOwners() {
        return potentialOwners;
    }

    public void setPotentialOwners(Set<OrganizationalEntity> potentialOwners) {
        this.potentialOwners = potentialOwners;
    }

    /**
     * @return the set of labels for this task.
     */
    public Set<TypedLabel> getTypedLabels() {
        return typedLabels;
    }

    public void setTypedLabels(Set<TypedLabel> typedLabels) {
        this.typedLabels = typedLabels;
    }

    public TaskOrUser getPreviousTaskOrUser() {
        return previousTaskOrUser;
    }

    public void setPreviousTaskOrUser(TaskOrUser previousTaskOrUser) {
        this.previousTaskOrUser = previousTaskOrUser;
    }

    @Override
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    @Override
    public Integer getEndTime() {
        return endTime;
    }

    public void setEndTime(Integer endTime) {
        this.endTime = endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", priority=" + priority +
                ", pinned=" + pinned +
                ", potentialOwners=" + potentialOwners +
                ", typedLabels=" + typedLabels +
                ", previousTaskOrUser=" + previousTaskOrUser +
                ", user=" + user +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * Indicates if the currently assigned user can execute this task. If this is the case, the user is a potential
     * owner of the task.
     * @return true the assigned user can execute this task, false in any other case.
     */
    public boolean acceptsAssignedUser() {
        if (User.PLANNING_USER.getEntityId().equals(getUser().getEntityId())) {
            //planning user belongs to all the groups by definition.
            return true;
        }
        return TaskHelper.isPotentialOwner(this, getUser());
    }
}
