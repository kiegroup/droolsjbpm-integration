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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;
import org.optaplanner.core.impl.solver.ProblemFactChange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class SolverExecutorTest extends RunnableBaseTest<SolverExecutor> {

    @Mock
    private SolverDef solverDef;

    @Mock
    private KieServerRegistry registry;

    private Solver<TaskAssigningSolution<?>> solver;

    @Mock
    private TaskAssigningSolution solution;

    @Mock
    private SolverEventListener<TaskAssigningSolution<?>> eventListener;

    @Captor
    private ArgumentCaptor<SolverEventListener<TaskAssigningSolution<?>>> eventListenerCaptor;

    @Mock
    private BestSolutionChangedEvent<TaskAssigningSolution<?>> event;

    private CountDownLatch startFinished = new CountDownLatch(1);

    @Override
    protected SolverExecutor createRunnableBase() {
        solver = spy(new SolverMock());
        return spy(new SolverExecutorMock(solverDef, registry, eventListener));
    }

    @Test(timeout = TEST_TIMEOUT)
    public void start() throws Exception {
        CompletableFuture future = startRunnableBase();
        runnableBase.start(solution);

        // wait for the start initialization to finish
        startFinished.await();

        assertTrue(runnableBase.isStarted());

        verify(solver).addEventListener(eventListenerCaptor.capture());
        eventListenerCaptor.getValue().bestSolutionChanged(event);
        verify(eventListener).bestSolutionChanged(event);

        runnableBase.destroy();
        future.get();
        assertTrue(runnableBase.isDestroyed());
    }

    @Test
    public void startWithFailure() {
        runnableBase.start(solution);
        Assertions.assertThatThrownBy(() -> runnableBase.start(solution))
                .hasMessage("SolverExecutor start method can only be invoked when the status is STOPPED");
    }

    @Test
    public void startWithBuildFailure() {
        RuntimeException error = new RuntimeException("An error was produced...!");
        doThrow(error).when(runnableBase).buildSolver(solverDef, registry);
        Assertions.assertThatThrownBy(() -> runnableBase.start(solution))
                .hasMessage(error.getMessage());

        assertTrue(runnableBase.isStopped());
    }

    @Test(timeout = TEST_TIMEOUT)
    public void stopWithSolverStarted() throws Exception {
        CompletableFuture future = startRunnableBase();
        runnableBase.start(solution);
        // wait for the start initialization to finish
        startFinished.await();

        assertTrue(runnableBase.isStarted());

        verify(solver).addEventListener(eventListenerCaptor.capture());
        eventListenerCaptor.getValue().bestSolutionChanged(event);
        verify(eventListener).bestSolutionChanged(event);

        runnableBase.stop();

        runnableBase.destroy();
        future.get();
        assertTrue(runnableBase.isDestroyed());
        verify(solver).terminateEarly();
    }

    @Test
    public void stopWithSolverNotStarted() {
        runnableBase.stop();
        runnableBase.destroy();
        assertTrue(runnableBase.isDestroyed());
        verify(solver, never()).terminateEarly();
    }

    @Test(timeout = TEST_TIMEOUT)
    public void addProblemFactChanges() throws Exception {
        CompletableFuture future = startRunnableBase();
        runnableBase.start(solution);

        // wait for the start initialization to finish
        startFinished.await();

        List<ProblemFactChange<TaskAssigningSolution<?>>> changes = Collections.emptyList();
        runnableBase.addProblemFactChanges(changes);
        verify(solver).addProblemFactChanges(changes);

        runnableBase.destroy();
        future.get();
        assertTrue(runnableBase.isDestroyed());
    }

    @Test
    public void addProblemFactChangesWithFailure() {
        List<ProblemFactChange<TaskAssigningSolution<?>>> changes = Collections.emptyList();
        Assertions.assertThatThrownBy(() -> runnableBase.addProblemFactChanges(changes))
                .hasMessage("SolverExecutor has not been started. Be sure it's started and not stopped or destroyed prior to executing this method");
    }

    @Test
    public void getSolverDef() {
        assertEquals(solverDef, runnableBase.getSolverDef());
    }

    @After
    public void cleanUp() {
        disposeSolver();
    }

    private void disposeSolver() {
        //ensure the emulated solver dies in cases where we the solver termination wasn't explicitly executed as part of test.
        ((SolverMock) solver).dispose();
    }

    private class SolverExecutorMock extends SolverExecutor {

        public SolverExecutorMock(SolverDef solverDef,
                                  KieServerRegistry registry,
                                  SolverEventListener<TaskAssigningSolution<?>> eventListener) {
            super(solverDef, registry, eventListener);
        }

        @Override
        protected Solver<TaskAssigningSolution<?>> buildSolver(SolverDef solverDef, KieServerRegistry registry) {
            return solver;
        }
    }

    private class SolverMock implements Solver<TaskAssigningSolution<?>> {

        private final Semaphore finishSolverWork = new Semaphore(0);
        private CompletableFuture action;

        public void dispose() {
            finishSolverWork.release();
        }

        @Override
        public TaskAssigningSolution solve(TaskAssigningSolution<?> problem) {
            startFinished.countDown();
            action = CompletableFuture.runAsync(() -> {
                try {
                    // emulate a solver working in demon mode.
                    finishSolverWork.acquire();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.debug(e.getMessage());
                }
            });
            try {
                action.get();
            } catch (ExecutionException e) {
                LOGGER.debug(e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.debug(e.getMessage());
            }
            return null;
        }

        @Override
        public boolean terminateEarly() {
            // emulate solver termination.
            finishSolverWork.release();
            return true;
        }

        @Override
        public TaskAssigningSolution<?> getBestSolution() {
            return null;
        }

        @Override
        public Score getBestScore() {
            return null;
        }

        @Override
        public String explainBestScore() {
            return null;
        }

        @Override
        public long getTimeMillisSpent() {
            return 0;
        }

        @Override
        public boolean isSolving() {
            return false;
        }

        @Override
        public boolean isTerminateEarly() {
            return false;
        }

        @Override
        public boolean addProblemFactChange(ProblemFactChange<TaskAssigningSolution<?>> problemFactChange) {
            return false;
        }

        @Override
        public boolean addProblemFactChanges(List<ProblemFactChange<TaskAssigningSolution<?>>> problemFactChanges) {
            return false;
        }

        @Override
        public boolean isEveryProblemFactChangeProcessed() {
            return false;
        }

        @Override
        public void addEventListener(SolverEventListener<TaskAssigningSolution<?>> eventListener) {

        }

        @Override
        public void removeEventListener(SolverEventListener<TaskAssigningSolution<?>> eventListener) {

        }

        @Override
        public ScoreDirectorFactory<TaskAssigningSolution<?>> getScoreDirectorFactory() {
            return null;
        }
    }
}
