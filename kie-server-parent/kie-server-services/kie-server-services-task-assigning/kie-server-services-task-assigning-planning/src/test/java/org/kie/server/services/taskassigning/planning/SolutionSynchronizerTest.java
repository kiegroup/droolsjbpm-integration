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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.Test;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.user.system.api.User;
import org.kie.server.services.taskassigning.user.system.api.UserSystemService;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.optaplanner.core.impl.solver.ProblemFactChange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SolutionSynchronizerTest extends RunnableBaseTest<SolutionSynchronizer> {

    private static final Duration SYNCH_INTERVAL = Duration.ofMillis(2);
    private static final Duration USERS_SYNCH_INTERVAL = Duration.ofMillis(4000);
    private static final Duration QUERY_SHIFT = Duration.parse("PT2S");

    @Mock
    private SolverExecutor solverExecutor;

    @Mock
    private TaskAssigningRuntimeDelegate delegate;

    @Mock
    private UserSystemService userSystemService;

    @Mock
    private Consumer<SolutionSynchronizer.Result> resultConsumer;

    @Captor
    private ArgumentCaptor<TaskAssigningSolution> solutionCaptor;

    @Captor
    private ArgumentCaptor<SolutionSynchronizer.Result> resultCaptor;

    @Mock
    private TaskAssigningSolution generatedSolution;

    @Mock
    private TaskAssigningSolution emptySolution;

    @Mock
    private List<ProblemFactChange<TaskAssigningSolution>> generatedChanges;

    @Mock
    private List<ProblemFactChange<TaskAssigningSolution>> emptyChanges;

    private boolean usersSyncTime;

    private CountDownLatch queryExecutionsCountDown;

    private SolverHandlerContext context;

    @Override
    protected SolutionSynchronizer createRunnableBase() {
        context = new SolverHandlerContext(QUERY_SHIFT);
        return new SolutionSynchronizerMock(solverExecutor, delegate, userSystemService,
                                            SYNCH_INTERVAL, USERS_SYNCH_INTERVAL, context, resultConsumer);
    }

    @Test(timeout = TEST_TIMEOUT)
    @SuppressWarnings("unchecked")
    public void initSolverExecutor() throws Exception {
        CompletableFuture future = startRunnableBase();
        LocalDateTime firstQueryTime = LocalDateTime.now();

        List<User> userList = mockUserList();
        List<TaskAssigningRuntimeDelegate.FindTasksResult> results = mockTasksQueryExecutions(firstQueryTime);
        LocalDateTime firstSuccessfulQueryTime = results.get(results.size() - 1).getQueryTime();
        queryExecutionsCountDown = new CountDownLatch(results.size());
        prepareQueryExecutions(results);

        when(emptySolution.getTaskList()).thenReturn(Collections.emptyList());
        when(generatedSolution.getTaskList()).thenReturn(Collections.singletonList(new Task()));
        when(solverExecutor.isStopped()).thenReturn(true);
        when(userSystemService.findAllUsers()).thenReturn(userList);

        runnableBase.initSolverExecutor();

        // wait for the query executions to happen
        queryExecutionsCountDown.await();

        verify(delegate, times(results.size())).findTasks(anyList(), eq(null), anyObject());
        verify(solverExecutor).start(solutionCaptor.capture());
        assertEquals(generatedSolution, solutionCaptor.getValue());
        LocalDateTime nextQueryTime = context.shiftQueryTime(firstSuccessfulQueryTime.withNano(0));
        LocalDateTime previousQueryTime = context.shiftQueryTime(nextQueryTime);
        assertEquals(previousQueryTime, context.getPreviousQueryTime());
        assertEquals(nextQueryTime, context.getNextQueryTime());

        runnableBase.destroy();
        future.get();
        assertTrue(runnableBase.isDestroyed());
    }

    @Test(timeout = TEST_TIMEOUT)
    public void synchronizeSolution() throws Exception {
        CompletableFuture future = startRunnableBase();
        LocalDateTime startTime = LocalDateTime.now().withNano(0);
        List<TaskAssigningRuntimeDelegate.FindTasksResult> tasksQueryResults = mockTasksQueryExecutions(startTime);
        int executionsCount = tasksQueryResults.size();

        usersSyncTime = false;
        executeSynchronizeSolution(future, startTime, tasksQueryResults, Collections.emptyList(), executionsCount);

        // all tasks query executions were produced.
        verityTasksQueryExecutions(startTime, tasksQueryResults);
        // no users query executions were produced.
        verify(userSystemService, never()).findAllUsers();
        verify(resultConsumer).accept(resultCaptor.capture());
        assertEquals(generatedChanges, resultCaptor.getValue().getChanges());
    }

    @Test(timeout = TEST_TIMEOUT)
    public void synchronizeSolutionIncludingUsersSyncWithoutUserChanges() throws Exception {
        CompletableFuture future = startRunnableBase();
        LocalDateTime startTime = LocalDateTime.now().withNano(0);
        List<TaskAssigningRuntimeDelegate.FindTasksResult> tasksQueryResults = mockTasksQueryExecutions(startTime);
        List<List<User>> userQueryResults = mockEmptyQueryExecutions(tasksQueryResults.size(), Collections::emptyList);
        int executionsCount = tasksQueryResults.size();

        usersSyncTime = true;
        executeSynchronizeSolution(future, startTime, tasksQueryResults, userQueryResults, executionsCount);

        // all tasks query executions were produced.
        verityTasksQueryExecutions(startTime, tasksQueryResults);
        // user executions were produced executionsCount-2 times since the tasks queries fails two times.
        verify(userSystemService, times(executionsCount - 2)).findAllUsers();

        verify(resultConsumer).accept(resultCaptor.capture());
        assertEquals(generatedChanges, resultCaptor.getValue().getChanges());
    }

    @Test(timeout = TEST_TIMEOUT)
    @SuppressWarnings("unchecked")
    public void synchronizeSolutionIncludingUsersSyncWithUserChanges() throws Exception {
        CompletableFuture future = startRunnableBase();
        LocalDateTime startTime = LocalDateTime.now().withNano(0);
        List<TaskAssigningRuntimeDelegate.FindTasksResult> tasksQueryResults = mockTasksQueryExecutions(startTime);
        List<List<User>> userQueryResults = mockUsersQueryExecutions();
        int executionsCount = userQueryResults.size();

        usersSyncTime = true;
        executeSynchronizeSolution(future, startTime, tasksQueryResults, userQueryResults, executionsCount);

        // all user executions was executed, results were produced in last execution.
        verify(userSystemService, times(userQueryResults.size())).findAllUsers();

        /*
        Three task query executions were produced.
        initial setup queryTimes -> [startTime]
        execution0:
            execute query with lastModification = startTime
            execution was ok, but result0 has no results.
            nextQueryTime0 = context.shiftQueryTime(startTime)
        execution1:
            execute query with lastModification = nextQueryTime0
            execution is ok, but result1 has no values.
            nextQueryTime1 = context.shiftQueryTime(result1.getQueryTime())
        execution2:
            execute query with lastModification = nextQueryTime1
            execution is ok, but result2 has no values.
            nextQueryTime2 = context.shiftQueryTime(result2.getQueryTime())
        */

        verify(delegate, times(1)).findTasks(anyList(), eq(startTime), anyObject());
        verify(delegate, times(1)).findTasks(anyList(), eq(context.shiftQueryTime(tasksQueryResults.get(0).getQueryTime())), anyObject());
        verify(delegate, times(1)).findTasks(anyList(), eq(context.shiftQueryTime(tasksQueryResults.get(1).getQueryTime())), anyObject());

        assertEquals(context.shiftQueryTime(tasksQueryResults.get(2).getQueryTime()), context.getNextQueryTime());

        verify(resultConsumer).accept(resultCaptor.capture());
        assertEquals(generatedChanges, resultCaptor.getValue().getChanges());
    }

    private void executeSynchronizeSolution(CompletableFuture future, LocalDateTime startTime,
                                            List<TaskAssigningRuntimeDelegate.FindTasksResult> tasksQueryResults,
                                            List<List<User>> userQueryResults,
                                            int executionsCount) throws Exception {

        TaskAssigningSolution solution = new TaskAssigningSolution(1, new ArrayList<>(), new ArrayList<>());
        queryExecutionsCountDown = new CountDownLatch(executionsCount);
        prepareQueryExecutions(tasksQueryResults);
        prepareUserQueryExecutions(userQueryResults);

        when(generatedChanges.isEmpty()).thenReturn(false);
        when(emptyChanges.isEmpty()).thenReturn(true);
        when(solverExecutor.isStarted()).thenReturn(true);
        runnableBase.synchronizeSolution(solution, startTime);

        // wait for the query executions to happen
        queryExecutionsCountDown.await();

        verify(resultConsumer).accept(resultCaptor.capture());
        assertEquals(generatedChanges, resultCaptor.getValue().getChanges());
        runnableBase.destroy();
        future.get();
        assertTrue(runnableBase.isDestroyed());
    }

    /**
     * Mock the results of the findTasksQuery and emulate errors in the middle by producing null.
     * Last execution produces values.
     * execution 0, no results
     * execution 1, no results
     * execution 2, no results
     * execution 3, null -> an error is produced
     * execution 4, null -> an error is produced
     * execution 5, null -> no results
     * execution 6, finally results are produced.
     */
    private List<TaskAssigningRuntimeDelegate.FindTasksResult> mockTasksQueryExecutions(LocalDateTime startTime) {
        List<TaskData> taskDataList = mockTaskDataList();
        TaskAssigningRuntimeDelegate.FindTasksResult result0 = mockFindTaskResult(startTime.plusMinutes(1), new ArrayList<>());
        TaskAssigningRuntimeDelegate.FindTasksResult result1 = mockFindTaskResult(startTime.plusMinutes(2), new ArrayList<>());
        TaskAssigningRuntimeDelegate.FindTasksResult result2 = mockFindTaskResult(startTime.plusMinutes(3), new ArrayList<>());
        TaskAssigningRuntimeDelegate.FindTasksResult resultFailure3 = null;
        TaskAssigningRuntimeDelegate.FindTasksResult resultFailure4 = null;
        TaskAssigningRuntimeDelegate.FindTasksResult result5 = mockFindTaskResult(startTime.plusMinutes(4), new ArrayList<>());
        TaskAssigningRuntimeDelegate.FindTasksResult result6 = mockFindTaskResult(startTime.plusMinutes(5), taskDataList);
        return Arrays.asList(result0, result1, result2, resultFailure3, resultFailure4, result5, result6);
    }

    /**
     * Verify that the synchronization sequence defined in mockTasksQueryExecutions was executed.
     */
    @SuppressWarnings("unchecked")
    private void verityTasksQueryExecutions(LocalDateTime startTime, List<TaskAssigningRuntimeDelegate.FindTasksResult> results) {
        /*
        initial setup queryTimes -> [startTime]
        execution0:
            execute query with lastModification = startTime
            execution was ok, but result0 has no results.
            nextQueryTime0 = context.shiftQueryTime(result0.getQueryTime())
        execution1:
            execute query with lastModification = nextQueryTime0
            execution is ok, but result1 has no values.
            nextQueryTime1 = context.shiftQueryTime(result1.getQueryTime())
        execution2:
            execute query with lastModification = nextQueryTime1
            execution is ok, but result2 has no values.
            nextQueryTime2 = context.shiftQueryTime(result2.getQueryTime())
        execution3:
            execute query with lastModification = nextQueryTime2
            execution fails.
            a retry is produced.
        execution4:
            execute query with lastModification = nextQueryTime2
            execution fails.
            a retry is produced.
        execution5:
            execute query with lastModification = nextQueryTime2
            execution is ok, but result5 has no values
            nextQueryTime5 = context.shift(result5.getQueryTime())
        execution6:
            execute query with lastModification = nextQueryTime5
            execution is ok, and result6 has values!
            nextQueryTime6 = context.shiftQueryTime(result6.getQueryTime())
            End of loop since results are produced.
        */

        verify(delegate, times(1)).findTasks(anyList(), eq(startTime), anyObject());
        verify(delegate, times(1)).findTasks(anyList(), eq(context.shiftQueryTime(results.get(0).getQueryTime())), anyObject());
        verify(delegate, times(1)).findTasks(anyList(), eq(context.shiftQueryTime(results.get(1).getQueryTime())), anyObject());
        verify(delegate, times(3)).findTasks(anyList(), eq(context.shiftQueryTime(results.get(2).getQueryTime())), anyObject());
        verify(delegate, times(results.size())).findTasks(anyList(), anyObject(), anyObject());
    }

    /**
     * Mock the results of the findAllUsers method execution and emulate errors in the middle by producing null.
     * Last execution produces values.
     * execution 0, null
     * execution 1, no results
     * execution 2, results are produced.
     */
    private List<List<User>> mockUsersQueryExecutions() {
        User user = mock(User.class);
        List<User> result0Failure = null;
        List<User> result1 = Collections.emptyList();
        List<User> result2 = Collections.singletonList(user);
        return Arrays.asList(result0Failure, result1, result2);
    }

    private <T> List<T> mockEmptyQueryExecutions(int size, Supplier<T> emptyResultSupplier) {
        List<T> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(emptyResultSupplier.get());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void prepareQueryExecutions(List<TaskAssigningRuntimeDelegate.FindTasksResult> results) {
        doAnswer(createExecutions(results)).when(delegate).findTasks(anyList(), anyObject(), anyObject());
    }

    private void prepareUserQueryExecutions(List<List<User>> results) {
        doAnswer(createExecutions(results)).when(userSystemService).findAllUsers();
    }

    private <T> Answer createExecutions(List<T> results) {
        return new Answer() {
            private int invocations = 0;

            public Object answer(InvocationOnMock invocation) {
                T result = results.get(invocations++);
                if (result == null) {
                    throw new RuntimeException("Emulate a connection error. The synchronizer must retry.");
                }
                return result;
            }
        };
    }

    private List<TaskData> mockTaskDataList() {
        return Collections.singletonList(new TaskData());
    }

    private List<User> mockUserList() {
        return new ArrayList<>();
    }

    private TaskAssigningRuntimeDelegate.FindTasksResult mockFindTaskResult(LocalDateTime queryTime, List<TaskData> taskDataList) {
        return new TaskAssigningRuntimeDelegate.FindTasksResult(queryTime, taskDataList);
    }

    private class SolutionSynchronizerMock extends SolutionSynchronizer {

        private SolutionSynchronizerMock(SolverExecutor solverExecutor,
                                         TaskAssigningRuntimeDelegate delegate,
                                         UserSystemService userSystem,
                                         Duration syncInterval,
                                         Duration usersSyncInterval,
                                         SolverHandlerContext context,
                                         Consumer<Result> resultConsumer) {
            super(solverExecutor, delegate, userSystem, syncInterval, usersSyncInterval, context, resultConsumer);
        }

        @Override
        protected List<ProblemFactChange<TaskAssigningSolution>> buildChanges(TaskAssigningSolution solution, List<TaskData> updatedTaskDataList) {
            return updatedTaskDataList.isEmpty() ? emptyChanges : generatedChanges;
        }

        @Override
        protected List<ProblemFactChange<TaskAssigningSolution>> buildChanges(TaskAssigningSolution solution, List<TaskData> updatedTaskDataList, List<User> updatedUserList) {
            return updatedTaskDataList.isEmpty() && updatedUserList.isEmpty() ? emptyChanges : generatedChanges;
        }

        @Override
        protected TaskAssigningSolution buildSolution(List<TaskData> taskDataList, List<User> externalUsers) {
            return taskDataList.isEmpty() ? emptySolution : generatedSolution;
        }

        @Override
        protected boolean isUsersSyncTime() {
            return usersSyncTime;
        }

        @Override
        Action doInitSolverExecutor() {
            Action result = super.doInitSolverExecutor();
            queryExecutionsCountDown.countDown();
            return result;
        }

        @Override
        Action doSynchronizeSolution() {
            Action result = super.doSynchronizeSolution();
            queryExecutionsCountDown.countDown();
            return result;
        }
    }
}
