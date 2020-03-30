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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.task.model.Status;
import org.kie.server.api.model.taskassigning.PlanningTask;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.services.taskassigning.core.model.DefaultLabels;
import org.kie.server.services.taskassigning.core.model.Group;
import org.kie.server.services.taskassigning.core.model.ModelConstants;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.User;
import org.kie.server.services.taskassigning.core.model.solver.realtime.AddTaskProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.AddUserProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.AssignTaskProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.DisableUserProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.ReleaseTaskProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.RemoveTaskProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.RemoveUserProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.TaskPropertyChangeProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.UserPropertyChangeProblemFactChange;
import org.kie.server.services.taskassigning.user.system.api.UserSystemService;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.ProblemFactChange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.kie.api.task.model.Status.Completed;
import static org.kie.api.task.model.Status.Exited;
import static org.kie.api.task.model.Status.Failed;
import static org.kie.api.task.model.Status.InProgress;
import static org.kie.api.task.model.Status.Obsolete;
import static org.kie.api.task.model.Status.Ready;
import static org.kie.api.task.model.Status.Reserved;
import static org.kie.api.task.model.Status.Suspended;
import static org.kie.server.api.model.taskassigning.util.StatusConverter.convertToString;
import static org.kie.server.services.taskassigning.planning.util.TaskUtil.fromTaskData;
import static org.kie.server.services.taskassigning.planning.util.UserUtil.fromExternalUser;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolutionChangesBuilderTest {

    private static final long TASK_ID = 0L;
    private static final long PROCESS_INSTANCE_ID = 1L;
    private static final String PROCESS_ID = "PROCESS_ID";
    private static final String CONTAINER_ID = "CONTAINER_ID";
    private static final String NAME = "NAME";
    private static final int PRIORITY = 2;
    private static final Map<String, Object> INPUT_DATA = new HashMap<>();

    private static final String ACTUAL_OWNER_ENTITY_ID = "ACTUAL_OWNER_ENTITY_ID";

    private static final String USER_ENTITY_ID = "USER_ENTITY_ID";

    private static final String USER1_ID = "USER1_ID";
    private static final String USER2_ID = "USER2_ID";
    private static final String USER3_ID = "USER3_ID";

    private static final String GROUP1_ID = "GROUP1_ID";
    private static final String GROUP2_ID = "GROUP2_ID";

    private static final String ATTRIBUTE1_ID = "ATTRIBUTE1_ID";
    private static final String ATTRIBUTE1_VALUE = "ATTRIBUTE1_VALUE";
    private static final String ATTRIBUTE2_ID = "ATTRIBUTE2_ID";
    private static final String ATTRIBUTE2_VALUE = "ATTRIBUTE2_VALUE";
    private static final String SKILL1 = "SKILL1";
    private static final String AFFINITY1 = "AFFINITY1";
    private static final String SKILLS_ATTRIBUTE_NAME = "skills";
    private static final String AFFINITIES_ATTRIBUTE_NAME = "affinities";

    @Mock
    private UserSystemService userSystemService;

    @Mock
    private ScoreDirector<TaskAssigningSolution> scoreDirector;

    private SolverHandlerContext context;

    @Before
    public void setUp() {
        context = new SolverHandlerContext(2, 2000);
    }

    @Test
    public void addNewReadyTaskChange() {
        TaskData taskData = mockTaskData(TASK_ID, NAME, Ready, null);
        List<TaskData> taskDataList = mockTaskDataList(taskData);
        TaskAssigningSolution solution = mockSolution(Collections.emptyList(), Collections.emptyList());

        List<ProblemFactChange<TaskAssigningSolution>> result = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(taskDataList)
                .withUserSystem(userSystemService)
                .withContext(context)
                .build();

        AddTaskProblemFactChange expected = new AddTaskProblemFactChange(fromTaskData(taskData));
        assertChangeIsTheChangeSetId(result, 0);
        assertChange(result, 1, expected);
        assertTaskChangeRegistered(taskData.getTaskId(), taskData.getLastModificationDate());
    }

    @Test
    public void addNewReservedTaskChangeWithActualOwnerInSolution() {
        addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwnerInSolution(Reserved);
    }

    @Test
    public void addNewInProgressTaskChangeWithActualOwnerInSolution() {
        addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwnerInSolution(InProgress);
    }

    @Test
    public void addNewSuspendedTaskChangeWithActualOwnerInSolution() {
        addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwnerInSolution(Suspended);
    }

    @Test
    public void addNewReservedTaskChangeWithActualOwnerInExternalSystem() {
        addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwnerInExternalSystem(Reserved);
    }

    @Test
    public void addNewInProgressTaskChangeWithActualOwnerInExternalSystem() {
        addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwnerInExternalSystem(InProgress);
    }

    @Test
    public void addNewSuspendedTaskChangeWithActualOwnerInExternalSystem() {
        addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwnerInExternalSystem(Suspended);
    }

    @Test
    public void addNewReservedTaskChangeWithActualOwnerMissing() {
        addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwnerMissing(Reserved);
    }

    @Test
    public void addNewInProgressTaskChangeWithActualOwnerMissing() {
        addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwnerInExternalSystem(InProgress);
    }

    @Test
    public void addNewSuspendedTaskChangeWithActualOwnerMissing() {
        addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwnerInExternalSystem(Suspended);
    }

    @Test
    public void addReleasedTaskChange() {
        TaskData taskData = mockTaskData(TASK_ID, NAME, Ready, USER_ENTITY_ID);
        Task task = fromTaskData(taskData);
        task.setStatus(convertToString(Reserved));
        TaskAssigningSolution solution = mockSolution(Collections.singletonList(task), Collections.emptyList());

        List<ProblemFactChange<TaskAssigningSolution>> result = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(mockTaskDataList(taskData))
                .withUserSystem(userSystemService)
                .withContext(context)
                .build();

        assertChangeIsTheChangeSetId(result, 0);
        assertChange(result, 1, new ReleaseTaskProblemFactChange(task));
        assertTaskChangeRegistered(taskData.getTaskId(), taskData.getLastModificationDate());
    }

    @Test
    public void addRemoveReservedTaskChangeWhenActualOwnerNotPresent() {
        addRemoveReservedOrInProgressOrSuspendedTaskChangeWhenActualOwnerNotPresent(Reserved);
    }

    @Test
    public void addRemoveInProgressTaskChangeWhenActualOwnerNotPresent() {
        addRemoveReservedOrInProgressOrSuspendedTaskChangeWhenActualOwnerNotPresent(InProgress);
    }

    @Test
    public void addRemoveSuspendedTaskChangeWhenActualOwnerNotPresent() {
        addRemoveReservedOrInProgressOrSuspendedTaskChangeWhenActualOwnerNotPresent(Suspended);
    }

    @Test
    public void addReassignReservedTaskChangeWhenItWasManuallyReassignedWithActualOwnerInSolution() {
        addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassignedWithActualOwnerInSolution(Reserved);
    }

    @Test
    public void addReassignInProgressTaskChangeWhenItWasManuallyReassignedWithActualOwnerInSolution() {
        addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassignedWithActualOwnerInSolution(InProgress);
    }

    @Test
    public void addReassignSuspendedTaskChangeWhenItWasManuallyReassignedWithActualOwnerInSolution() {
        addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassignedWithActualOwnerInSolution(Suspended);
    }

    @Test
    public void addReassignReservedTaskWhenItWasManuallyReassignedWithActualOwnerInExternalSystem() {
        addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassignedWithActualOwnerInExternalSystem(Reserved);
    }

    @Test
    public void addReassignInProgressTaskWhenItWasManuallyReassignedWithActualOwnerInExternalSystem() {
        addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassignedWithActualOwnerInExternalSystem(InProgress);
    }

    @Test
    public void addReassignSuspendedTaskWhenItWasManuallyReassignedWithActualOwnerInExternalSystem() {
        addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassignedWithActualOwnerInExternalSystem(Suspended);
    }

    @Test
    public void addReassignReservedTaskWhenItWasManuallyReassignedWithActualOwnerMissing() {
        addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassignedWithActualOwnerMissing(Reserved);
    }

    @Test
    public void addReassignInProgressTaskWhenItWasManuallyReassignedWithActualOwnerMissing() {
        addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassignedWithActualOwnerMissing(InProgress);
    }

    @Test
    public void addReassignSuspendedTaskWhenItWasManuallyReassignedWithActualOwnerMissing() {
        addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassignedWithActualOwnerMissing(Suspended);
    }

    @Test
    public void addPinReservedTaskWhenPublishedAndNotYetPinnedWithActualOwnerInSolution() {
        addPinReservedOrInProgressOrSuspendedTaskWhenPublishedAndNotYetPinned(Reserved, true);
    }

    @Test
    public void addPinInProgressTaskWhenPublishedAndNotYetPinnedWithActualOwnerInSolution() {
        addPinReservedOrInProgressOrSuspendedTaskWhenPublishedAndNotYetPinned(InProgress, true);
    }

    @Test
    public void addPinSuspendedTaskWhenPublishedAndNotYetPinnedWithActualOwnerInSolution() {
        addPinReservedOrInProgressOrSuspendedTaskWhenPublishedAndNotYetPinned(Suspended, true);
    }

    @Test
    public void addPinReservedTaskWhenPublishedAndNotYetPinnedWithActualOwnerInExternalSystem() {
        addPinReservedOrInProgressOrSuspendedTaskWhenPublishedAndNotYetPinnedWithActualOwnerInExternalSystem(Reserved);
    }

    @Test
    public void addPinInProgressTaskWhenPublishedAndNotYetPinnedWithActualOwnerInExternalSystem() {
        addPinReservedOrInProgressOrSuspendedTaskWhenPublishedAndNotYetPinnedWithActualOwnerInExternalSystem(InProgress);
    }

    @Test
    public void addPinSuspendedTaskWhenPublishedAndNotYetPinnedWithActualOwnerInExternalSystem() {
        addPinReservedOrInProgressOrSuspendedTaskWhenPublishedAndNotYetPinnedWithActualOwnerInExternalSystem(Suspended);
    }

    @Test
    public void addPinReservedTaskWhenPublishedAndNotYetPinnedWithActualOwnerMissing() {
        addPinReservedOrInProgressOrSuspendedTaskWhenPublishedAndNotYetPinnedWithActualOwnerMissing(Reserved);
    }

    @Test
    public void addPinInProgressTaskWhenPublishedAndNotYetPinnedWithActualOwnerMissing() {
        addPinReservedOrInProgressOrSuspendedTaskWhenPublishedAndNotYetPinnedWithActualOwnerMissing(InProgress);
    }

    @Test
    public void addPinSuspendedTaskWhenPublishedAndNotYetPinnedWithActualOwnerMissing() {
        addPinReservedOrInProgressOrSuspendedTaskWhenPublishedAndNotYetPinnedWithActualOwnerMissing(Suspended);
    }

    @Test
    public void addStatusAndPriorityPropertyChange() {
        TaskData taskData = mockTaskData(TASK_ID, NAME, Reserved, ACTUAL_OWNER_ENTITY_ID);
        User actualOwner = mockUser(ACTUAL_OWNER_ENTITY_ID);
        Task task = fromTaskData(taskData);
        task.setUser(actualOwner);
        task.setPinned(true);
        task.setStatus(convertToString(InProgress));
        task.setPriority(taskData.getPriority() + 1);

        TaskAssigningSolution solution = mockSolution(Collections.singletonList(task), Collections.singletonList(actualOwner));

        List<ProblemFactChange<TaskAssigningSolution>> result = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(mockTaskDataList(taskData))
                .withUserSystem(userSystemService)
                .withContext(context)
                .build();

        TaskPropertyChangeProblemFactChange change = new TaskPropertyChangeProblemFactChange(task);
        change.setStatus(taskData.getStatus());
        change.setPriority(taskData.getPriority());

        assertChangeIsTheChangeSetId(result, 0);
        assertChange(result, 1, new TaskPropertyChangeProblemFactChange(task));
        assertTaskChangeRegistered(taskData.getTaskId(), taskData.getLastModificationDate());
    }

    @Test
    public void addRemoveTaskThatChangedToCompleted() {
        addRemoveTaskInSinkStatus(Completed);
    }

    @Test
    public void addRemoveTaskThatChangedToExited() {
        addRemoveTaskInSinkStatus(Exited);
    }

    @Test
    public void addRemoveTaskThatChangedToFailed() {
        addRemoveTaskInSinkStatus(Failed);
    }

    @Test
    public void addRemoveTaskThatChangedToError() {
        addRemoveTaskInSinkStatus(Status.Error);
    }

    @Test
    public void addRemoveTaskThatChangedToObsolete() {
        addRemoveTaskInSinkStatus(Obsolete);
    }

    @Test
    public void discardDuplicatedChanges() {
        TaskData taskData1 = mockTaskData(1, NAME, Ready, null);
        TaskData taskData2 = mockTaskData(2, NAME, InProgress, null);
        TaskData taskData3 = mockTaskData(3, NAME, Completed, null);
        TaskData taskData4 = mockTaskData(4, NAME, Suspended, null);
        Task task1 = mockTask(taskData1.getTaskId());
        Task task2 = mockTask(taskData2.getTaskId());
        Task task3 = mockTask(taskData3.getTaskId());
        Task task4 = mockTask(taskData4.getTaskId());

        List<TaskData> taskDataList = mockTaskDataList(taskData1, taskData2, taskData3, taskData4);
        TaskAssigningSolution solution = mockSolution(Arrays.asList(task1, task2, task3, task4), Collections.emptyList());

        taskDataList.forEach(taskData -> context.setTaskChangeTime(taskData.getTaskId(), taskData.getLastModificationDate()));

        List<ProblemFactChange<TaskAssigningSolution>> result = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(taskDataList)
                .withUserSystem(userSystemService)
                .withContext(context)
                .build();

        assertEquals(2, result.size());
        assertChangeIsTheChangeSetId(result, 0);
        assertChangeIsTheDummyTaskAssigned(result, ModelConstants.DUMMY_TASK_PLANNER_241, 1);
    }

    @Test
    public void addNewUserChange() {
        List<User> userList = mockUserList(mockUser(USER1_ID), mockUser(USER2_ID));
        org.kie.server.services.taskassigning.user.system.api.User newExternalUser = mockExternalUser(USER3_ID);
        org.kie.server.services.taskassigning.user.system.api.User newExternalUserRepeated1 = mockExternalUser(USER3_ID);
        org.kie.server.services.taskassigning.user.system.api.User newExternalUserRepeated2 = mockExternalUser(USER3_ID);
        List<org.kie.server.services.taskassigning.user.system.api.User> externalUserList = mockExternalUserList(newExternalUser,
                                                                                                                 newExternalUserRepeated1,
                                                                                                                 newExternalUserRepeated2);
        TaskAssigningSolution solution = mockSolution(Collections.emptyList(), userList);

        List<ProblemFactChange<TaskAssigningSolution>> result = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(Collections.emptyList())
                .withUserSystem(userSystemService)
                .withUsersUpdate(externalUserList)
                .withContext(context)
                .build();

        AddUserProblemFactChange expected = new AddUserProblemFactChange(fromExternalUser(newExternalUser));
        assertChangeIsTheChangeSetId(result, 0);
        assertOneChange(result, 1, expected);
    }

    @Test
    public void addUserPropertyChangeWhenNoChanges() {
        Pair<User, org.kie.server.services.taskassigning.user.system.api.User> userPair = buildUsersForEqualityCase();
        addUserPropertyChange(userPair.getLeft(), userPair.getRight(), false);
    }

    @Test
    public void addUserPropertyChangeWhenEnabledChanged() {
        Pair<User, org.kie.server.services.taskassigning.user.system.api.User> userPair = buildUsersForEqualityCase();
        userPair.getLeft().setEnabled(false);
        addUserPropertyChange(userPair.getLeft(), userPair.getRight(), true);
    }

    @Test
    public void addUserPropertyChangeWhenGroupsChanged() {
        Pair<User, org.kie.server.services.taskassigning.user.system.api.User> userPair = buildUsersForEqualityCase();
        userPair.getRight().getGroups().add(mockExternalGroup(GROUP2_ID));
        addUserPropertyChange(userPair.getLeft(), userPair.getRight(), true);
    }

    @Test
    public void addUserPropertyChangeWhenAttributesChanged() {
        Pair<User, org.kie.server.services.taskassigning.user.system.api.User> userPair = buildUsersForEqualityCase();
        userPair.getRight().getAttributes().put(ATTRIBUTE2_ID, ATTRIBUTE2_VALUE);
        addUserPropertyChange(userPair.getLeft(), userPair.getRight(), true);
    }

    @Test
    public void addUserPropertyChangeWhenLabelsChanged() {
        Pair<User, org.kie.server.services.taskassigning.user.system.api.User> userPair = buildUsersForEqualityCase();
        userPair.getLeft().getAttributes().put(AFFINITIES_ATTRIBUTE_NAME, AFFINITY1);
        userPair.getRight().getAttributes().put(AFFINITIES_ATTRIBUTE_NAME, AFFINITY1);
        addUserPropertyChange(userPair.getLeft(), userPair.getRight(), true);
    }

    @Test
    public void addDisableUserProblemFactChange() {
        List<User> userList = mockUserList(mockUser(USER1_ID));
        List<org.kie.server.services.taskassigning.user.system.api.User> externalUserList = Collections.emptyList();
        TaskAssigningSolution solution = mockSolution(Collections.emptyList(), userList);

        List<ProblemFactChange<TaskAssigningSolution>> result = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(Collections.emptyList())
                .withUserSystem(userSystemService)
                .withUsersUpdate(externalUserList)
                .withContext(context)
                .build();

        DisableUserProblemFactChange expected = new DisableUserProblemFactChange(userList.get(0));
        assertChangeIsTheChangeSetId(result, 0);
        assertChange(result, 1, expected);
    }

    @Test
    public void addRemoveUserWhenHasNoTasksChange() {
        List<User> userList = mockUserList(mockUser(USER1_ID));
        userList.get(0).setEnabled(false);
        TaskAssigningSolution solution = mockSolution(Collections.emptyList(), userList);

        List<ProblemFactChange<TaskAssigningSolution>> result = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(Collections.emptyList())
                .withUserSystem(userSystemService)
                .withContext(context)
                .build();

        RemoveUserProblemFactChange expected = new RemoveUserProblemFactChange(userList.get(0));
        assertChangeIsTheChangeSetId(result, 0);
        assertChange(result, 1, expected);
    }

    @Test
    public void addRemoveUserWhenHasNonPinnedTasksChange() {
        User user = mockUser(USER1_ID);
        user.setEnabled(false);
        Task nonPinnedTask = mockTask(TASK_ID);
        nonPinnedTask.setPinned(false);
        nonPinnedTask.setPreviousTaskOrUser(user);
        user.setNextTask(nonPinnedTask);

        List<User> userList = mockUserList(user);
        TaskAssigningSolution solution = mockSolution(Collections.singletonList(nonPinnedTask), userList);

        List<ProblemFactChange<TaskAssigningSolution>> result = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(Collections.emptyList())
                .withUserSystem(userSystemService)
                .withContext(context)
                .build();

        RemoveUserProblemFactChange expected = new RemoveUserProblemFactChange(userList.get(0));
        assertChangeIsTheChangeSetId(result, 0);
        assertChange(result, 1, expected);
    }

    @Test
    public void addRemoveUserWhenHasPinnedTasks() {
        User user = mockUser(USER1_ID);
        user.setEnabled(false);
        Task pinnedTask = mockTask(TASK_ID);
        pinnedTask.setPinned(true);
        pinnedTask.setPreviousTaskOrUser(user);
        user.setNextTask(pinnedTask);

        List<User> userList = mockUserList(user);
        TaskAssigningSolution solution = mockSolution(Collections.singletonList(pinnedTask), userList);

        List<ProblemFactChange<TaskAssigningSolution>> result = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(Collections.emptyList())
                .withUserSystem(userSystemService)
                .withContext(context)
                .build();

        assertChangeIsTheChangeSetId(result, 0);
        assertNoChange(result, RemoveUserProblemFactChange.class);
    }

    private Pair<User, org.kie.server.services.taskassigning.user.system.api.User> buildUsersForEqualityCase() {
        Map<String, Set<Object>> labelValues = new HashMap<>();
        labelValues.put(DefaultLabels.SKILLS.name(), Collections.singleton(SKILL1));
        labelValues.put(DefaultLabels.AFFINITIES.name(), Collections.emptySet());
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SKILLS_ATTRIBUTE_NAME, SKILL1);
        attributes.put(ATTRIBUTE1_ID, ATTRIBUTE1_VALUE);
        User user1 = mockUser(USER1_ID,
                              true,
                              Collections.singleton(new Group(GROUP1_ID.hashCode(), GROUP1_ID)),
                              attributes,
                              labelValues);

        Map<String, Object> externalUserAttributes = new HashMap<>();
        externalUserAttributes.put(ATTRIBUTE1_ID, ATTRIBUTE1_VALUE);
        externalUserAttributes.put(SKILLS_ATTRIBUTE_NAME, SKILL1);
        Set<org.kie.server.services.taskassigning.user.system.api.Group> externalUserGroups = new HashSet<>();
        externalUserGroups.add(mockExternalGroup(GROUP1_ID));
        org.kie.server.services.taskassigning.user.system.api.User externalUser = mockExternalUser(USER1_ID,
                                                                                                   externalUserGroups,
                                                                                                   externalUserAttributes);
        return Pair.of(user1, externalUser);
    }

    private void addUserPropertyChange(User currentUser, org.kie.server.services.taskassigning.user.system.api.User externalUser, boolean shouldHaveChanged) {
        List<User> userList = mockUserList(currentUser, mockUser("anotherUser"), mockUser("anotherUser1"));
        org.kie.server.services.taskassigning.user.system.api.User externalUserRepeated1 = mockExternalUser(externalUser.getId());
        org.kie.server.services.taskassigning.user.system.api.User newExternalUserRepeated2 = mockExternalUser(externalUser.getId());
        List<org.kie.server.services.taskassigning.user.system.api.User> externalUserList = mockExternalUserList(externalUser,
                                                                                                                 externalUserRepeated1,
                                                                                                                 newExternalUserRepeated2);
        TaskAssigningSolution solution = mockSolution(Collections.emptyList(), userList);

        List<ProblemFactChange<TaskAssigningSolution>> result = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(Collections.emptyList())
                .withUserSystem(userSystemService)
                .withUsersUpdate(externalUserList)
                .withContext(context)
                .build();

        User fromExternalUser = fromExternalUser(externalUser);
        UserPropertyChangeProblemFactChange expected = new UserPropertyChangeProblemFactChange(currentUser,
                                                                                               fromExternalUser.isEnabled(),
                                                                                               fromExternalUser.getAttributes(),
                                                                                               fromExternalUser.getAllLabelValues(),
                                                                                               fromExternalUser.getGroups());
        if (shouldHaveChanged) {
            assertChangeIsTheChangeSetId(result, 0);
            assertOneChange(result, 1, expected);
            UserPropertyChangeProblemFactChange createdChange = (UserPropertyChangeProblemFactChange) result.get(1);
            assertEquals(expected.getUser().getEntityId(), createdChange.getUser().getEntityId());
            assertEquals(expected.isEnabled(), createdChange.isEnabled());
            assertEquals(expected.getAttributes(), createdChange.getAttributes());
            assertEquals(expected.getGroups(), createdChange.getGroups());
            assertEquals(expected.getLabelValues(), createdChange.getLabelValues());
        } else {
            assertNoChange(result, UserPropertyChangeProblemFactChange.class);
        }
    }

    private void addRemoveTaskInSinkStatus(Status sinkStatus) {
        TaskData taskData = mockTaskData(TASK_ID, NAME, sinkStatus, ACTUAL_OWNER_ENTITY_ID);
        User actualOwner = mockUser(ACTUAL_OWNER_ENTITY_ID);
        Task task = fromTaskData(taskData);
        task.setUser(actualOwner);
        task.setStatus(convertToString(Reserved));

        TaskAssigningSolution solution = mockSolution(Collections.singletonList(task), Collections.singletonList(actualOwner));

        List<ProblemFactChange<TaskAssigningSolution>> result = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(mockTaskDataList(taskData))
                .withUserSystem(userSystemService)
                .withContext(context)
                .build();

        RemoveTaskProblemFactChange change = new RemoveTaskProblemFactChange(task);
        assertChangeIsTheChangeSetId(result, 0);
        assertChange(result, 1, change);
        assertTaskChangeRegistered(taskData.getTaskId(), taskData.getLastModificationDate());
    }

    private void addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwnerInSolution(Status status) {
        TaskData taskData = mockTaskData(TASK_ID, NAME, status, USER_ENTITY_ID);
        TaskAssigningSolution solution = mockSolution(Collections.emptyList(), Collections.singletonList(mockUser(USER_ENTITY_ID)));
        addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwner(solution, taskData);
    }

    private void addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwnerInExternalSystem(Status status) {
        TaskData taskData = mockTaskData(TASK_ID, NAME, status, USER_ENTITY_ID);
        org.kie.server.services.taskassigning.user.system.api.User externalUser = mockExternalUser(USER_ENTITY_ID);
        when(userSystemService.findUser(USER_ENTITY_ID)).thenReturn(externalUser);
        TaskAssigningSolution solution = mockSolution(Collections.emptyList(), Collections.emptyList());
        addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwner(solution, taskData);
        verify(userSystemService).findUser(USER_ENTITY_ID);
    }

    private void addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwnerMissing(Status status) {
        TaskData taskData = mockTaskData(TASK_ID, NAME, status, USER_ENTITY_ID);
        TaskAssigningSolution solution = mockSolution(Collections.emptyList(), Collections.emptyList());
        addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwner(solution, taskData);
        verify(userSystemService).findUser(USER_ENTITY_ID);
    }

    private void addNewReservedOrInProgressOrSuspendedTaskChangeWithActualOwner(TaskAssigningSolution solution, TaskData taskData) {
        List<TaskData> taskDataList = mockTaskDataList(taskData);
        List<ProblemFactChange<TaskAssigningSolution>> result = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(taskDataList)
                .withUserSystem(userSystemService)
                .withContext(context)
                .build();

        AssignTaskProblemFactChange expected = new AssignTaskProblemFactChange(fromTaskData(taskData), mockUser(USER_ENTITY_ID), true);
        assertChangeIsTheChangeSetId(result, 0);
        assertChange(result, 1, expected);
        assertTaskChangeRegistered(taskData.getTaskId(), taskData.getLastModificationDate());
    }

    private void addRemoveReservedOrInProgressOrSuspendedTaskChangeWhenActualOwnerNotPresent(Status status) {
        TaskData taskData = mockTaskData(TASK_ID, NAME, status, null);
        Task task = fromTaskData(taskData);

        TaskAssigningSolution solution = mockSolution(Collections.singletonList(task), Collections.emptyList());

        List<ProblemFactChange<TaskAssigningSolution>> result = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(mockTaskDataList(taskData))
                .withUserSystem(userSystemService)
                .withContext(context)
                .build();

        assertChangeIsTheChangeSetId(result, 0);
        assertChange(result, 1, new RemoveTaskProblemFactChange(task));
        assertTaskChangeRegistered(taskData.getTaskId(), taskData.getLastModificationDate());
    }

    private void addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassignedWithActualOwnerInSolution(Status status) {
        addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassigned(status, true);
    }

    private void addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassigned(Status status, boolean addActualOwnerToSolution) {
        TaskData taskData = mockTaskData(TASK_ID, NAME, status, ACTUAL_OWNER_ENTITY_ID);
        Task task = fromTaskData(taskData);
        User user = mockUser(USER_ENTITY_ID);
        task.setUser(user);

        List<User> userList = new ArrayList<>();
        userList.add(user);
        if (addActualOwnerToSolution) {
            User actualOwner = mockUser(ACTUAL_OWNER_ENTITY_ID);
            userList.add(actualOwner);
        }

        TaskAssigningSolution solution = mockSolution(Collections.singletonList(task), userList);

        List<ProblemFactChange<TaskAssigningSolution>> result = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(mockTaskDataList(taskData))
                .withUserSystem(userSystemService)
                .withContext(context)
                .build();

        assertChangeIsTheChangeSetId(result, 0);
        assertChange(result, 1, new AssignTaskProblemFactChange(task, mockUser(ACTUAL_OWNER_ENTITY_ID), true));
        assertTaskChangeRegistered(taskData.getTaskId(), taskData.getLastModificationDate());
    }

    private void addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassignedWithActualOwnerInExternalSystem(Status status) {
        org.kie.server.services.taskassigning.user.system.api.User externalUser = mockExternalUser(ACTUAL_OWNER_ENTITY_ID);
        when(userSystemService.findUser(ACTUAL_OWNER_ENTITY_ID)).thenReturn(externalUser);
        addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassigned(status, false);
        verify(userSystemService).findUser(ACTUAL_OWNER_ENTITY_ID);
    }

    private void addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassignedWithActualOwnerMissing(Status status) {
        when(userSystemService.findUser(ACTUAL_OWNER_ENTITY_ID)).thenReturn(null);
        addReassignReservedOrInProgressOrSuspendedTaskWhenItWasManuallyReassigned(status, false);
        verify(userSystemService).findUser(ACTUAL_OWNER_ENTITY_ID);
    }

    private void addPinReservedOrInProgressOrSuspendedTaskWhenPublishedAndNotYetPinned(Status status, boolean addActualOwnerToSolution) {
        TaskData taskData = mockTaskData(TASK_ID, NAME, status, ACTUAL_OWNER_ENTITY_ID);
        PlanningTask planningTask = mockPlanningTask(taskData.getTaskId(), true);
        planningTask.setIndex(0);
        taskData.setPlanningTask(planningTask);

        Task task = fromTaskData(taskData);
        task.setPinned(false);
        User actualOwner = mockUser(ACTUAL_OWNER_ENTITY_ID);
        task.setUser(actualOwner);

        List<User> userList = new ArrayList<>();
        if (addActualOwnerToSolution) {
            userList.add(actualOwner);
        }

        TaskAssigningSolution solution = mockSolution(Collections.singletonList(task), userList);

        List<ProblemFactChange<TaskAssigningSolution>> result = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(mockTaskDataList(taskData))
                .withUserSystem(userSystemService)
                .withContext(context)
                .build();

        assertChangeIsTheChangeSetId(result, 0);
        assertChange(result, 1, new AssignTaskProblemFactChange(task, mockUser(ACTUAL_OWNER_ENTITY_ID), true));
        assertTaskChangeRegistered(taskData.getTaskId(), taskData.getLastModificationDate());
    }

    private void addPinReservedOrInProgressOrSuspendedTaskWhenPublishedAndNotYetPinnedWithActualOwnerInExternalSystem(Status status) {
        org.kie.server.services.taskassigning.user.system.api.User externalUser = mockExternalUser(ACTUAL_OWNER_ENTITY_ID);
        when(userSystemService.findUser(ACTUAL_OWNER_ENTITY_ID)).thenReturn(externalUser);
        addPinReservedOrInProgressOrSuspendedTaskWhenPublishedAndNotYetPinned(status, false);
        verify(userSystemService).findUser(ACTUAL_OWNER_ENTITY_ID);
    }

    private void addPinReservedOrInProgressOrSuspendedTaskWhenPublishedAndNotYetPinnedWithActualOwnerMissing(Status status) {
        when(userSystemService.findUser(ACTUAL_OWNER_ENTITY_ID)).thenReturn(null);
        addPinReservedOrInProgressOrSuspendedTaskWhenPublishedAndNotYetPinned(status, false);
        verify(userSystemService).findUser(ACTUAL_OWNER_ENTITY_ID);
    }

    private void assertChange(List<ProblemFactChange<TaskAssigningSolution>> result, int index, AddTaskProblemFactChange expected) {
        AddTaskProblemFactChange change = (AddTaskProblemFactChange) result.get(index);
        assertTaskEquals(expected.getTask(), change.getTask());
    }

    private void assertChange(List<ProblemFactChange<TaskAssigningSolution>> result, int index, AssignTaskProblemFactChange expected) {
        AssignTaskProblemFactChange change = (AssignTaskProblemFactChange) result.get(index);
        assertTaskEquals(expected.getTask(), change.getTask());
        assertUserEquals(expected.getUser(), change.getUser());
    }

    private void assertChange(List<ProblemFactChange<TaskAssigningSolution>> result, int index, ReleaseTaskProblemFactChange expected) {
        ReleaseTaskProblemFactChange change = (ReleaseTaskProblemFactChange) result.get(index);
        assertTaskEquals(expected.getTask(), change.getTask());
    }

    private void assertChange(List<ProblemFactChange<TaskAssigningSolution>> result, int index, RemoveTaskProblemFactChange expected) {
        RemoveTaskProblemFactChange change = (RemoveTaskProblemFactChange) result.get(index);
        assertTaskEquals(expected.getTask(), change.getTask());
    }

    private void assertChange(List<ProblemFactChange<TaskAssigningSolution>> result, int index, DisableUserProblemFactChange expected) {
        DisableUserProblemFactChange change = (DisableUserProblemFactChange) result.get(index);
        assertUserEquals(expected.getUser(), change.getUser());
    }

    private void assertChange(List<ProblemFactChange<TaskAssigningSolution>> result, int index, RemoveUserProblemFactChange expected) {
        RemoveUserProblemFactChange change = (RemoveUserProblemFactChange) result.get(index);
        assertUserEquals(expected.getUser(), change.getUser());
    }

    private void assertOneChange(List<ProblemFactChange<TaskAssigningSolution>> result, int index, AddUserProblemFactChange expected) {
        AddUserProblemFactChange change = (AddUserProblemFactChange) result.get(index);
        assertUserEquals(expected.getUser(), change.getUser());
        assertEquals(1, result.stream().filter(calculated -> calculated instanceof AddUserProblemFactChange).count());
    }

    private void assertOneChange(List<ProblemFactChange<TaskAssigningSolution>> result, int index, UserPropertyChangeProblemFactChange expected) {
        UserPropertyChangeProblemFactChange change = (UserPropertyChangeProblemFactChange) result.get(index);
        assertUserEquals(expected.getUser(), change.getUser());
        assertEquals(1, result.stream().filter(calculated -> calculated instanceof UserPropertyChangeProblemFactChange).count());
    }

    private <T> void assertNoChange(List<ProblemFactChange<TaskAssigningSolution>> result, Class<T> notExpected) {
        assertFalse(result.stream().anyMatch(change -> notExpected.equals(change.getClass())));
    }

    private void assertChangeIsTheChangeSetId(List<ProblemFactChange<TaskAssigningSolution>> result, int index) {
        long currentChangeSetId = context.getCurrentChangeSetId();
        result.get(index).doChange(scoreDirector);
        assertEquals(currentChangeSetId + 1, context.getCurrentChangeSetId(), index);
    }

    private void assertChangeIsTheDummyTaskAssigned(List<ProblemFactChange<TaskAssigningSolution>> result, Task dummyTask, int index) {
        assertTrue(result.get(index) instanceof AssignTaskProblemFactChange);
        AssignTaskProblemFactChange change = (AssignTaskProblemFactChange) result.get(index);
        assertEquals(dummyTask, change.getTask());
    }

    private void assertChange(List<ProblemFactChange<TaskAssigningSolution>> result, int index, TaskPropertyChangeProblemFactChange expected) {
        TaskPropertyChangeProblemFactChange change = (TaskPropertyChangeProblemFactChange) result.get(index);
        assertTaskEquals(expected.getTask(), change.getTask());
        if (expected.getStatus() != null) {
            assertEquals(expected.getStatus(), change.getStatus());
        }
        if (expected.getPriority() != null) {
            assertEquals(expected.getPriority(), change.getPriority(), 0);
        }
    }

    private void assertTaskChangeRegistered(long taskId, LocalDateTime lastModificationDate) {
        context.isProcessedTaskChange(taskId, lastModificationDate);
    }

    private TaskData mockTaskData(long taskId, String name, Status status, String actualOwner) {
        return TaskData.builder()
                .taskId(taskId)
                .processInstanceId(PROCESS_INSTANCE_ID)
                .processId(PROCESS_ID)
                .containerId(CONTAINER_ID)
                .name(name)
                .priority(PRIORITY)
                .status(convertToString(status))
                .actualOwner(actualOwner)
                .lastModificationDate(LocalDateTime.now())
                .inputData(INPUT_DATA)
                .build();
    }

    private PlanningTask mockPlanningTask(long taskId, boolean published) {
        return PlanningTask.builder().taskId(taskId).published(published).build();
    }

    private void assertTaskEquals(Task t1, Task t2) {
        assertEquals(t1.getId(), t2.getId(), 0);
    }

    private void assertUserEquals(User user1, User user2) {
        assertEquals(user1.getEntityId(), user2.getEntityId());
        assertEquals(user1.getId(), user2.getId(), 0);
    }

    private User mockUser(String entityId) {
        return new User(entityId.hashCode(), entityId, true);
    }

    private User mockUser(String entityId, boolean enabled, Set<Group> groups, Map<String, Object> attributes, Map<String, Set<Object>> labelValues) {
        User result = new User(entityId.hashCode(), entityId, enabled);
        result.setGroups(groups);
        result.setAttributes(attributes);
        result.setAllLabelValues(labelValues);
        return result;
    }

    private org.kie.server.services.taskassigning.user.system.api.User mockExternalUser(String entityId) {
        org.kie.server.services.taskassigning.user.system.api.User externalUser = mock(org.kie.server.services.taskassigning.user.system.api.User.class);
        when(externalUser.getId()).thenReturn(entityId);
        return externalUser;
    }

    private org.kie.server.services.taskassigning.user.system.api.User mockExternalUser(String entityId, Set<org.kie.server.services.taskassigning.user.system.api.Group> groups, Map<String, Object> attributes) {
        org.kie.server.services.taskassigning.user.system.api.User externalUser = mockExternalUser(entityId);
        when(externalUser.getGroups()).thenReturn(groups);
        when(externalUser.getAttributes()).thenReturn(attributes);
        return externalUser;
    }

    private org.kie.server.services.taskassigning.user.system.api.Group mockExternalGroup(String groupId) {
        org.kie.server.services.taskassigning.user.system.api.Group externalGroup = mock(org.kie.server.services.taskassigning.user.system.api.Group.class);
        when(externalGroup.getId()).thenReturn(groupId);
        return externalGroup;
    }

    private TaskAssigningSolution mockSolution(List<Task> task, List<User> users) {
        return new TaskAssigningSolution(1L, users, task);
    }

    private List<TaskData> mockTaskDataList(TaskData... tasks) {
        return Arrays.asList(tasks);
    }

    private List<User> mockUserList(User... users) {
        return Arrays.asList(users);
    }

    private List<org.kie.server.services.taskassigning.user.system.api.User> mockExternalUserList(org.kie.server.services.taskassigning.user.system.api.User... users) {
        return Arrays.asList(users);
    }

    private Task mockTask(long taskId) {
        return new Task(taskId, "Task_" + taskId, 0);
    }
}
