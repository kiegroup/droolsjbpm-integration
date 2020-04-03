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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SolutionSynchronizerTest extends RunnableBaseTest<SolutionSynchronizer> {

    private static final long SYNCH_INTERVAL = 2;
    private static final int QUERY_TIMES_SIZE = 2;
    private static final long QUERY_MINIMUM_DISTANCE = 2000;

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

    private CountDownLatch queryExecutionsCountDown;

    private SolverHandlerContext context;

    @Override
    protected SolutionSynchronizer createRunnableBase() {
        context = new SolverHandlerContext(QUERY_TIMES_SIZE, QUERY_MINIMUM_DISTANCE);
        return new SolutionSynchronizerMock(solverExecutor, delegate, userSystemService,
                                            SYNCH_INTERVAL, context, resultConsumer);
    }

    @Test(timeout = TEST_TIMEOUT)
    @SuppressWarnings("unchecked")
    public void initSolverExecutor() throws Exception {
        CompletableFuture future = startRunnableBase();
        LocalDateTime firstQueryTime = LocalDateTime.now();

        List<User> userList = mockUserList();
        List<TaskAssigningRuntimeDelegate.FindTasksResult> results = mockQueryExecutions(firstQueryTime);
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
        LocalDateTime initializationQueryTime = firstSuccessfulQueryTime.withNano(0).minusHours(1);
        for (int i = 0; i < QUERY_TIMES_SIZE - 1; i++) {
            assertEquals(initializationQueryTime, context.pollNextQueryTime());
        }
        assertEquals(initializationQueryTime, context.getPreviousQueryTime());
        assertEquals(firstSuccessfulQueryTime.withNano(0), context.peekLastQueryTime());

        runnableBase.destroy();
        future.get();
        assertTrue(runnableBase.isDestroyed());
    }

    @Test(timeout = TEST_TIMEOUT)
    @SuppressWarnings("unchecked")
    public void synchronizeSolution() throws Exception {
        CompletableFuture future = startRunnableBase();

        TaskAssigningSolution solution = new TaskAssigningSolution(1, new ArrayList<>(), new ArrayList<>());
        List<User> userList = mockUserList();
        context.resetQueryTimes(LocalDateTime.now().withNano(0));
        LocalDateTime startTime = context.pollNextQueryTime();
        List<TaskAssigningRuntimeDelegate.FindTasksResult> results = mockQueryExecutions(startTime);
        queryExecutionsCountDown = new CountDownLatch(results.size());
        prepareQueryExecutions(results);

        when(generatedChanges.isEmpty()).thenReturn(false);
        when(emptyChanges.isEmpty()).thenReturn(true);
        when(solverExecutor.isStarted()).thenReturn(true);
        when(userSystemService.findAllUsers()).thenReturn(userList);

        runnableBase.synchronizeSolution(solution, startTime);

        // wait for the query executions to happen
        queryExecutionsCountDown.await();

        // initial setup queryTimes -> [startTime]
        // execution0:
        //    execute query with lastModification = startTime
        //    execution was ok, but result0 has no results.
        //    nextQueryTime = startTime since minimum distance with result0.getQueryTime() is not met -> queryTimes -> [startTime, startTime]
        // execution1:
        //     execute query with lastModification = startTime
        //     execution is ok, but result1 has no values.
        //     nextQueryTime = result1.getQueryTime() since minimum distance with startTime is met -> [startTime, result1.getQueryTime()]
        // execution2:
        //     execute query with lastModification = startTime
        //     execution is ok, but result2 has no values.
        //     nextQueryTime = result1.getQueryTime() since minimum distance with result2.getQueryTime() is not met -> [result1.getQueryTime(), result1.getQueryTime()]
        // execution3:
        //     execute query with lastModification = result1.getQueryTime()
        //     execution fails.
        //     a retry is produced. -> [result1.getQueryTime(), result1.getQueryTime()]
        // execution4:
        //     execute query with lastModification = result1.getQueryTime()
        //     execution fails.
        //     a retry is produced. -> [result1.getQueryTime(), result1.getQueryTime()]
        // execution5:
        //     execute query with lastModification = result1.getQueryTime()
        //     execution is ok, but result5 has no values
        //     nextQueryTime = result5.getQueryTime() since minimum distance with result1.getQueryTime() is met -> [result1.getQueryTime(), result5.getQueryTime()]
        // execution6:
        //     execute query with lastModification = result1.getQueryTime()
        //     execution is ok, and result6 has values!
        //     nextQueryTime = result6.getQueryTime() since minimum distance is met -> [result5.getQueryTime(), result6.getQueryTime()]
        //     End of loop since results are produced.

        verify(delegate, times(3)).findTasks(anyList(), eq(startTime), anyObject());
        verify(delegate, times(4)).findTasks(anyList(), eq(results.get(1).getQueryTime()), anyObject());
        assertEquals(results.get(5).getQueryTime(), context.pollNextQueryTime());
        assertEquals(results.get(6).getQueryTime(), context.pollNextQueryTime());

        verify(delegate, times(results.size())).findTasks(anyList(), anyObject(), anyObject());
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
    private List<TaskAssigningRuntimeDelegate.FindTasksResult> mockQueryExecutions(LocalDateTime startTime) {
        List<TaskData> taskDataList = mockTaskDataList();
        TaskAssigningRuntimeDelegate.FindTasksResult result0 = mockFindTaskResult(startTime.plus(QUERY_MINIMUM_DISTANCE / 2, ChronoUnit.MILLIS), new ArrayList<>());
        TaskAssigningRuntimeDelegate.FindTasksResult result1 = mockFindTaskResult(startTime.plus(QUERY_MINIMUM_DISTANCE + QUERY_MINIMUM_DISTANCE / 2, ChronoUnit.MILLIS), new ArrayList<>());
        TaskAssigningRuntimeDelegate.FindTasksResult result2 = mockFindTaskResult(startTime.plus(QUERY_MINIMUM_DISTANCE * 2, ChronoUnit.MILLIS), new ArrayList<>());
        TaskAssigningRuntimeDelegate.FindTasksResult resultFailure3 = null;
        TaskAssigningRuntimeDelegate.FindTasksResult resultFailure4 = null;
        TaskAssigningRuntimeDelegate.FindTasksResult result5 = mockFindTaskResult(startTime.plus(QUERY_MINIMUM_DISTANCE * 5, ChronoUnit.SECONDS), new ArrayList<>());
        TaskAssigningRuntimeDelegate.FindTasksResult result6 = mockFindTaskResult(startTime.plus(QUERY_MINIMUM_DISTANCE * 8, ChronoUnit.SECONDS), taskDataList);
        return Arrays.asList(result0, result1, result2, resultFailure3, resultFailure4, result5, result6);
    }

    @SuppressWarnings("unchecked")
    private void prepareQueryExecutions(List<TaskAssigningRuntimeDelegate.FindTasksResult> results) {
        doAnswer(new Answer() {
            private int invocations = 0;

            public Object answer(InvocationOnMock invocation) {
                TaskAssigningRuntimeDelegate.FindTasksResult result = results.get(invocations++);
                if (result == null) {
                    throw new RuntimeException("Emulate a connection error. The synchronizer must retry.");
                }
                return result;
            }
        }).when(delegate).findTasks(anyList(), anyObject(), anyObject());
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
                                         long syncInterval,
                                         SolverHandlerContext context,
                                         Consumer<Result> resultConsumer) {
            super(solverExecutor, delegate, userSystem, syncInterval, context, resultConsumer);
        }

        @Override
        protected List<ProblemFactChange<TaskAssigningSolution>> buildChanges(TaskAssigningSolution solution, List<TaskData> updatedTaskDataList) {
            return updatedTaskDataList.isEmpty() ? emptyChanges : generatedChanges;
        }

        @Override
        protected TaskAssigningSolution buildSolution(List<TaskData> taskDataList, List<User> externalUsers) {
            return taskDataList.isEmpty() ? emptySolution : generatedSolution;
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
