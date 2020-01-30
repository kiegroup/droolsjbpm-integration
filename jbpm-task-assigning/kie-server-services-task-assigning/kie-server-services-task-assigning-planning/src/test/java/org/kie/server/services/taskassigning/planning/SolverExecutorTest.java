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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SolverExecutorTest extends RunnableBaseTest<SolverExecutor> {

    @Mock
    private SolverDef solverDef;

    @Mock
    private KieServerRegistry registry;

    private Solver<TaskAssigningSolution> solver;

    @Mock
    private TaskAssigningSolution solution;

    @Mock
    private SolverEventListener<TaskAssigningSolution> eventListener;

    @Captor
    private ArgumentCaptor<SolverEventListener<TaskAssigningSolution>> eventListenerCaptor;

    @Mock
    private BestSolutionChangedEvent<TaskAssigningSolution> event;

    @Override
    protected SolverExecutor createRunnableBase() {
        solver = spy(new SolverMock());
        return new SolverExecutorMock(solverDef, registry, eventListener);
    }

    @Test(timeout = TEST_TIMEOUT)
    //TODO, In fix process. Temporary commented due to an issue probably related with JUnit.
    @Ignore
    public void start() throws Exception {
        CompletableFuture future = startRunnableBase();
        runnableBase.start(solution);
        // give some time for the start method to execute.
        Thread.sleep(1000);

        assertTrue(runnableBase.isStarted());

        verify(solver).addEventListener(eventListenerCaptor.capture());
        eventListenerCaptor.getValue().bestSolutionChanged(event);
        verify(eventListener).bestSolutionChanged(event);

        runnableBase.destroy();
        future.get();
    }

    @Test(timeout = TEST_TIMEOUT)
    //TODO, In fix process. Temporary commented due to an issue probably related with JUnit.
    @Ignore
    public void startWithFailure() throws Exception {
        CompletableFuture future = startRunnableBase();
        runnableBase.start(solution);
        Assertions.assertThatThrownBy(() -> runnableBase.start(solution))
                .hasMessage("SolverExecutor start method can only be invoked when the status is STOPPED");

        runnableBase.destroy();
        future.get();
    }

    @Test(timeout = TEST_TIMEOUT)
    //TODO, In fix process. Temporary commented due to an issue probably related with JUnit.
    @Ignore
    public void startWithBuildFailure() throws Exception {
        CompletableFuture future = startRunnableBase();
        RuntimeException error = new RuntimeException("An error was produced...!");
        ((SolverExecutorMock) runnableBase).setBuildError(error);
        Assertions.assertThatThrownBy(() -> runnableBase.start(solution))
                .hasMessage(error.getMessage());

        assertTrue(runnableBase.isStopped());
        runnableBase.destroy();
        future.get();
    }

    @Test(timeout = TEST_TIMEOUT)
    //TODO, In fix process. Temporary commented due to an issue probably related with JUnit.
    @Ignore
    public void stop() throws Exception {
        CompletableFuture future = startRunnableBase();
        // give some time for the start method to execute.
        runnableBase.start(solution);
        Thread.sleep(1000);
        assertTrue(runnableBase.isStarted());

        runnableBase.stop();
        assertFalse(runnableBase.isStopped());
        // give some time for the stop method to execute.
        Thread.sleep(1000);
        assertTrue(runnableBase.isStopped());

        runnableBase.start(solution);
        // give some time for the start method to execute.
        Thread.sleep(1000);
        assertTrue(runnableBase.isStarted());

        verify(solver, times(2)).addEventListener(eventListenerCaptor.capture());
        eventListenerCaptor.getAllValues().get(0).bestSolutionChanged(event);
        eventListenerCaptor.getAllValues().get(1).bestSolutionChanged(event);
        verify(eventListener, times(2)).bestSolutionChanged(event);

        runnableBase.destroy();
        future.get();
    }

    @Test(timeout = TEST_TIMEOUT)
    //TODO, In fix process. Temporary commented due to an issue probably related with JUnit.
    @Ignore
    public void addProblemFactChanges() throws Exception {
        CompletableFuture future = startRunnableBase();
        runnableBase.start(solution);
        // give some time for the start method to execute.
        Thread.sleep(1000);
        List<ProblemFactChange<TaskAssigningSolution>> changes = Collections.emptyList();
        runnableBase.addProblemFactChanges(changes);
        verify(solver).addProblemFactChanges(changes);

        runnableBase.destroy();
        future.get();
    }

    @Test(timeout = TEST_TIMEOUT)
    //TODO, In fix process. Temporary commented due to an issue probably related with JUnit.
    @Ignore
    public void addProblemFactChangesWithFailure() throws Exception {
        CompletableFuture future = startRunnableBase();
        List<ProblemFactChange<TaskAssigningSolution>> changes = Collections.emptyList();

        Assertions.assertThatThrownBy(() -> runnableBase.addProblemFactChanges(changes))
                .hasMessage("SolverExecutor has not been started. Be sure it's started and not stopped or destroyed prior to executing this method");

        runnableBase.destroy();
        future.get();
    }

    private class SolverExecutorMock extends SolverExecutor {

        private RuntimeException buildError;

        public SolverExecutorMock(SolverDef solverDef,
                                  KieServerRegistry registry,
                                  SolverEventListener<TaskAssigningSolution> eventListener) {
            super(solverDef, registry, eventListener);
        }

        public void setBuildError(RuntimeException buildError) {
            this.buildError = buildError;
        }

        @Override
        protected Solver<TaskAssigningSolution> buildSolver(SolverDef solverDef, KieServerRegistry registry) {
            if (buildError != null) {
                throw buildError;
            }
            return solver;
        }
    }

    private class SolverMock implements Solver<TaskAssigningSolution> {

        private final Semaphore finishSolverWork = new Semaphore(0);
        private CompletableFuture action;

        @Override
        public TaskAssigningSolution solve(TaskAssigningSolution problem) {
            action = CompletableFuture.runAsync(() -> {
                try {
                    // emulate a solver working in demon mode.
                    finishSolverWork.acquire();
                    // emulate some time to finish
                    Thread.sleep(100);
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
        public TaskAssigningSolution getBestSolution() {
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
        public boolean addProblemFactChange(ProblemFactChange<TaskAssigningSolution> problemFactChange) {
            return false;
        }

        @Override
        public boolean addProblemFactChanges(List<ProblemFactChange<TaskAssigningSolution>> problemFactChanges) {
            return false;
        }

        @Override
        public boolean isEveryProblemFactChangeProcessed() {
            return false;
        }

        @Override
        public void addEventListener(SolverEventListener<TaskAssigningSolution> eventListener) {

        }

        @Override
        public void removeEventListener(SolverEventListener<TaskAssigningSolution> eventListener) {

        }

        @Override
        public ScoreDirectorFactory<TaskAssigningSolution> getScoreDirectorFactory() {
            return null;
        }
    }
}
