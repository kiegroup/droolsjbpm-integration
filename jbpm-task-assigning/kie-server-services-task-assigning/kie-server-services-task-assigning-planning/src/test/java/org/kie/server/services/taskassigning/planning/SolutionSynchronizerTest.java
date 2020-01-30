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
import java.util.function.Consumer;

import org.junit.Ignore;
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

    @Override
    protected SolutionSynchronizer createRunnableBase() {
        return new SolutionSynchronizerMock(solverExecutor, delegate, userSystemService,
                                            SYNCH_INTERVAL, new SolverHandlerContext(), resultConsumer);
    }

    @Test(timeout = TEST_TIMEOUT)
    //TODO, In fix process. Temporary commented due to an issue probably related with JUnit.
    @Ignore
    @SuppressWarnings("unchecked")
    public void initSolverExecutor() throws Exception {
        CompletableFuture future = startRunnableBase();

        List<User> userList = mockUserList();
        List<TaskAssigningRuntimeDelegate.FindTasksResult> results = mockQueryExecutions(LocalDateTime.now());
        prepareQueryExecutions(results);

        when(emptySolution.getTaskList()).thenReturn(Collections.emptyList());
        when(generatedSolution.getTaskList()).thenReturn(Collections.singletonList(new Task()));
        when(solverExecutor.isStopped()).thenReturn(true);
        when(userSystemService.findAllUsers()).thenReturn(userList);

        runnableBase.initSolverExecutor();

        // give some time for the executions to happen.
        Thread.sleep(1000);

        verify(delegate, times(results.size())).findTasks(anyList(), eq(null), anyObject());
        verify(solverExecutor).start(solutionCaptor.capture());
        assertEquals(generatedSolution, solutionCaptor.getValue());

        runnableBase.destroy();
        assertTrue(runnableBase.isDestroyed());
        future.get();
    }

    @Test(timeout = TEST_TIMEOUT)
    //TODO, In fix process. Temporary commented due to an issue probably related with JUnit.
    @Ignore
    @SuppressWarnings("unchecked")
    public void synchronizeSolution() throws Exception {
        CompletableFuture future = startRunnableBase();

        TaskAssigningSolution solution = new TaskAssigningSolution(1, new ArrayList<>(), new ArrayList<>());
        List<User> userList = mockUserList();
        LocalDateTime startTime = LocalDateTime.now();
        List<TaskAssigningRuntimeDelegate.FindTasksResult> results = mockQueryExecutions(startTime);
        prepareQueryExecutions(results);

        when(generatedChanges.isEmpty()).thenReturn(false);
        when(emptyChanges.isEmpty()).thenReturn(true);
        when(solverExecutor.isStarted()).thenReturn(true);
        when(userSystemService.findAllUsers()).thenReturn(userList);

        runnableBase.synchronizeSolution(solution, startTime);

        Thread.sleep(1000);

        //execution0: initial query execution was ok, but result0 has no results.
        verify(delegate).findTasks(anyList(), eq(startTime.withNano(0)), anyObject());
        //execution1: next retry is with result0.queryTime, but result1 hs no results.
        verify(delegate).findTasks(anyList(), eq(results.get(0).getQueryTime().withNano(0)), anyObject());
        //execution2: next retry is with result1.queryTime, but result2 has no results.
        verify(delegate).findTasks(anyList(), eq(results.get(1).getQueryTime().withNano(0)), anyObject());
        //execution3: next retry is with result2.queryTime. but execution3 and execution4 failed.
        verify(delegate, times(3)).findTasks(anyList(), eq(results.get(2).getQueryTime().withNano(0)), anyObject());
        //execution5: executed ok., but result5 has no results. next retry is with result5.queryTime
        verify(delegate).findTasks(anyList(), eq(results.get(5).getQueryTime().withNano(0)), anyObject());
        //result6, executed ok and produced changes.

        verify(delegate, times(results.size())).findTasks(anyList(), anyObject(), anyObject());
        verify(resultConsumer).accept(resultCaptor.capture());
        assertEquals(generatedChanges, resultCaptor.getValue().getChanges());

        runnableBase.destroy();
        assertTrue(runnableBase.isDestroyed());

        future.get();
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
        TaskAssigningRuntimeDelegate.FindTasksResult result0 = mockFindTaskResult(startTime.plus(1, ChronoUnit.SECONDS), new ArrayList<>());
        TaskAssigningRuntimeDelegate.FindTasksResult result1 = mockFindTaskResult(startTime.plus(2, ChronoUnit.SECONDS), new ArrayList<>());
        TaskAssigningRuntimeDelegate.FindTasksResult result2 = mockFindTaskResult(startTime.plus(3, ChronoUnit.SECONDS), new ArrayList<>());
        TaskAssigningRuntimeDelegate.FindTasksResult resultFailure3 = null;
        TaskAssigningRuntimeDelegate.FindTasksResult resultFailure4 = null;
        TaskAssigningRuntimeDelegate.FindTasksResult result5 = mockFindTaskResult(startTime.plus(4, ChronoUnit.SECONDS), new ArrayList<>());
        TaskAssigningRuntimeDelegate.FindTasksResult result6 = mockFindTaskResult(startTime.plus(5, ChronoUnit.SECONDS), taskDataList);
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
    }
}
