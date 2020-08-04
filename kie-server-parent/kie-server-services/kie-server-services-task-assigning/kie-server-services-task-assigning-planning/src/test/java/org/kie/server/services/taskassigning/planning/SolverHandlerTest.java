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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.api.model.taskassigning.PlanningExecutionResult;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.solver.realtime.TaskPropertyChangeProblemFactChange;
import org.kie.server.services.taskassigning.user.system.api.UserSystemService;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.optaplanner.core.api.score.buildin.bendablelong.BendableLongScore;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.impl.solver.ProblemFactChange;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.IMPROVE_SOLUTION_ON_BACKGROUND_DURATION;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.PUBLISH_WINDOW_SIZE;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.SYNC_INTERVAL;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.SYNC_QUERIES_SHIFT;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.TARGET_USER;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.USERS_SYNC_INTERVAL;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.WAIT_FOR_IMPROVED_SOLUTION_DURATION;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SolverHandlerTest {

    @Mock
    private SolverDef solverDef;

    @Mock
    private KieServerRegistry registry;

    @Mock
    private TaskAssigningRuntimeDelegate delegate;

    @Mock
    private UserSystemService userSystemService;

    @Mock
    private ScheduledExecutorService executorService;

    private SolverHandler handler;

    private SolverHandlerConfig handlerConfig;

    @Mock
    private SolverExecutor solverExecutor;

    @Mock
    private SolutionSynchronizer solutionSynchronizer;

    @Mock
    private SolutionProcessor solutionProcessor;

    @Captor
    private ArgumentCaptor<SolverEventListener<TaskAssigningSolution>> listenerCaptor;

    @Captor
    private ArgumentCaptor<Consumer<SolutionSynchronizer.Result>> synchronizerConsumerCaptor;

    @Captor
    private ArgumentCaptor<Consumer<SolutionProcessor.Result>> processorConsumerCaptor;

    @Captor
    private ArgumentCaptor<SolverHandlerContext> contextCaptor;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    @Captor
    private ArgumentCaptor<Duration> improveSolutionOnBackgroundDurationCaptor;

    @Mock
    private ScheduledFuture future;

    private LocalDateTime previousQueryTime;

    private LocalDateTime nextQueryTime;

    @Before
    public void setUp() {
        handlerConfig = spy(new SolverHandlerConfig(TARGET_USER, PUBLISH_WINDOW_SIZE, SYNC_INTERVAL,
                                                    SYNC_QUERIES_SHIFT, USERS_SYNC_INTERVAL,
                                                    WAIT_FOR_IMPROVED_SOLUTION_DURATION,
                                                    IMPROVE_SOLUTION_ON_BACKGROUND_DURATION));
        previousQueryTime = LocalDateTime.now();
        nextQueryTime = previousQueryTime.plusMinutes(2);
        this.handler = spy(new SolverHandler(solverDef, registry, delegate, userSystemService, executorService, handlerConfig));
        doReturn(solverExecutor).when(handler).createSolverExecutor(eq(solverDef), eq(registry), any());
        doReturn(solutionSynchronizer).when(handler).createSolutionSynchronizer(eq(solverExecutor), eq(delegate), eq(userSystemService), any(), any(), any(), any());
        doReturn(solutionProcessor).when(handler).createSolutionProcessor(eq(delegate), any(), eq(TARGET_USER), anyInt());
    }

    @Test
    public void hasWaitForImprovedSolutionDuration() {
        assertThat(handler.hasWaitForImprovedSolutionDuration()).isTrue();
    }

    @Test
    public void hasImproveSolutionOnBackgroundDuration() {
        assertThat(handler.hasImproveSolutionOnBackgroundDuration()).isTrue();
    }

    @Test
    public void start() {
        prepareStart();
        verify(executorService).execute(solverExecutor);
        verify(executorService).execute(solutionSynchronizer);
        verify(executorService).execute(solutionProcessor);
        verify(solutionSynchronizer).initSolverExecutor();
    }

    @Test
    public void destroy() throws Exception {
        prepareStart();
        handler.destroy();
        verifyDestroyCommonActions();
    }

    @Test(timeout = 5000)
    public void destroyWithTerminationError() throws Exception {
        // ensure this code is executed on a separate thread since a Thread.currentThread().interrupt(); is produced
        // in this use case when we emulate the executorService.awaitTermination throwing an Exception, otherwise
        // the interruption is caused on the JUnit execution thread "very bad thing".
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                prepareStart();
                doThrow(new InterruptedException("Test Generated Error")).when(executorService.awaitTermination(anyInt(), any()));
                handler.destroy();

                verifyDestroyCommonActions();
                verify(executorService).shutdownNow();
            } catch (Exception e) {
                //will never happen, see verifyDestroyCommonActions executorService is a mock..
            }
        }).get();
        executor.shutdown();
    }

    @Test
    public void onBestSolutionChangeWithWaitForImprovedSolutionDuration() {
        BestSolutionChangedEvent<TaskAssigningSolution> event = mockEvent(true, true);
        prepareOnBestSolutionChangeWithWaitForImprovedSolutionDuration(WAIT_FOR_IMPROVED_SOLUTION_DURATION);
        listenerCaptor.getValue().bestSolutionChanged(event);
        verify(executorService).schedule(runnableCaptor.capture(), eq(WAIT_FOR_IMPROVED_SOLUTION_DURATION.toMillis()), eq(TimeUnit.MILLISECONDS));
        runnableCaptor.getValue().run();
        verify(solutionProcessor).process(event.getNewBestSolution());
    }

    @Test
    public void onBestSolutionChangeWithWaitForImprovedSolutionDurationTaskAlreadyScheduled() {
        BestSolutionChangedEvent<TaskAssigningSolution> event1 = mockEvent(true, true);
        BestSolutionChangedEvent<TaskAssigningSolution> event2 = mockEvent(true, true);
        BestSolutionChangedEvent<TaskAssigningSolution> event3 = mockEvent(true, true);

        prepareOnBestSolutionChangeWithWaitForImprovedSolutionDuration(WAIT_FOR_IMPROVED_SOLUTION_DURATION);
        listenerCaptor.getValue().bestSolutionChanged(event1);
        listenerCaptor.getValue().bestSolutionChanged(event2);
        listenerCaptor.getValue().bestSolutionChanged(event3);
        verify(executorService).schedule(runnableCaptor.capture(), eq(WAIT_FOR_IMPROVED_SOLUTION_DURATION.toMillis()), eq(TimeUnit.MILLISECONDS));
        runnableCaptor.getValue().run();
        verify(solutionProcessor).process(event3.getNewBestSolution());
    }

    @Test
    public void onBestSolutionChangeWithWaitForImprovedSolutionDurationWhenChangeSetAlreadyProcessed() {
        BestSolutionChangedEvent<TaskAssigningSolution> event = mockEvent(true, true);
        prepareOnBestSolutionChangeWithWaitForImprovedSolutionDuration(WAIT_FOR_IMPROVED_SOLUTION_DURATION);
        SolverHandlerContext context = contextCaptor.getValue();
        context.setProcessedChangeSet(context.getCurrentChangeSetId());
        listenerCaptor.getValue().bestSolutionChanged(event);
        verify(executorService, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    private void prepareOnBestSolutionChangeWithWaitForImprovedSolutionDuration(Duration waitForImprovedSolutionDuration) {
        prepareStart();
        SolverHandlerContext context = contextCaptor.getValue();
        long changeSet = context.nextChangeSetId();
        context.setCurrentChangeSetId(changeSet);
        doReturn(waitForImprovedSolutionDuration).when(handlerConfig).getWaitForImprovedSolutionDuration();
        long durationMillis = waitForImprovedSolutionDuration.toMillis();
        doReturn(future).when(executorService).schedule(any(Runnable.class), eq(durationMillis), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void onBestSolutionChangeWhenAllChangesNotProcessed() {
        onBestSolutionChangeEventNotProcessed(mockEvent(false, true));
    }

    @Test
    public void onBestSolutionChangeWhenSolutionNotInitialized() {
        onBestSolutionChangeEventNotProcessed(mockEvent(true, false));
    }

    @Test
    public void onBestSolutionChangeWithWaitForImprovedSolutionDurationZero() {
        doReturn(Duration.ZERO).when(handlerConfig).getWaitForImprovedSolutionDuration();

        BestSolutionChangedEvent<TaskAssigningSolution> event = mockEvent(true, true);
        prepareStart();

        SolverHandlerContext context = contextCaptor.getValue();
        long changeSet = context.nextChangeSetId();
        context.setCurrentChangeSetId(changeSet);
        listenerCaptor.getValue().bestSolutionChanged(event);
        verify(solutionProcessor).process(event.getNewBestSolution());
        verify(executorService, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        assertTrue(context.isProcessedChangeSet(changeSet));
    }

    @Test
    public void onBestSolutionChangeWhenChangeSetAlreadyProcessed() {
        prepareStart();
        BestSolutionChangedEvent<TaskAssigningSolution> event = mockEvent(true, true);

        SolverHandlerContext context = contextCaptor.getValue();
        long changeSet = context.nextChangeSetId();
        context.setCurrentChangeSetId(changeSet);
        context.setProcessedChangeSet(changeSet);

        listenerCaptor.getValue().bestSolutionChanged(event);
        verify(solutionProcessor, never()).process(any());
    }

    @Test
    public void onSolutionProcessedWithImproveSolutionOnBackgroundDurationZero() {
        SolutionProcessor.Result result = new SolutionProcessor.Result(PlanningExecutionResult.builder().build());
        doReturn(Duration.ZERO).when(handlerConfig).getImproveSolutionOnBackgroundDuration();
        onSolutionProcessedSuccessful(result, false, false);
    }

    @Test
    public void onSolutionProcessedWithImproveSolutionOnBackgroundDuration() {
        SolutionProcessor.Result result = new SolutionProcessor.Result(PlanningExecutionResult.builder().build());
        doReturn(Duration.ofMillis(1234)).when(handlerConfig).getImproveSolutionOnBackgroundDuration();
        onSolutionProcessedSuccessful(result, false, true);
    }

    @Test
    public void onSolutionProcessedWithRecoverableError() {
        SolutionProcessor.Result result = new SolutionProcessor.Result(PlanningExecutionResult.builder()
                                                                               .error(PlanningExecutionResult.ErrorCode.TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR)
                                                                               .build());
        onSolutionProcessedSuccessful(result, true, false);
    }

    @Test
    public void onSolutionProcessedWithException() {
        SolutionProcessor.Result result = new SolutionProcessor.Result(new Exception("Emulate an un-managed exception"));
        onSolutionProcessedWithError(result);
    }

    @Test
    public void onSolutionProcessedWithUnRecoverableError() {
        SolutionProcessor.Result result = new SolutionProcessor.Result(PlanningExecutionResult.builder()
                                                                               .error(PlanningExecutionResult.ErrorCode.UNEXPECTED_ERROR)
                                                                               .build());
        onSolutionProcessedWithError(result);
    }

    @Test
    public void onSolutionSynchronizedWithChanges() {
        prepareStart();
        List<ProblemFactChange<TaskAssigningSolution>> changes = new ArrayList<>();
        changes.add(new TaskPropertyChangeProblemFactChange(new Task()));

        SolutionSynchronizer.Result result = SolutionSynchronizer.Result.forChanges(changes);

        doReturn(true).when(solverExecutor).isStarted();

        synchronizerConsumerCaptor.getValue().accept(result);
        verify(solverExecutor).addProblemFactChanges(changes);
        verify(solutionProcessor, never()).process(any(TaskAssigningSolution.class));
        verify(solutionSynchronizer, never()).synchronizeSolution(any(TaskAssigningSolution.class), any(LocalDateTime.class));
    }

    @Test
    public void onSolutionSynchronizedWithUnchangedPeriodTimeoutAndSameBestSolution() {
        TaskAssigningSolution initialSolution = prepareStartAndASolutionProduced();
        BestSolutionChangedEvent<TaskAssigningSolution> event = mockEvent(true, true);
        doReturn(initialSolution).when(event).getNewBestSolution();
        listenerCaptor.getValue().bestSolutionChanged(event);

        SolutionSynchronizer.Result result = SolutionSynchronizer.Result.forUnchangedPeriodTimeout();
        synchronizerConsumerCaptor.getValue().accept(result);

        verify(solverExecutor, never()).addProblemFactChanges(any(List.class));
        verify(solutionSynchronizer).synchronizeSolution(eq(initialSolution), eq(nextQueryTime));
    }

    @Test
    public void onSolutionSynchronizedWithUnchangedPeriodTimeoutAndSameScoreBestSolution() {
        TaskAssigningSolution initialSolution = prepareStartAndASolutionProduced();
        BestSolutionChangedEvent<TaskAssigningSolution> event = mockEvent(true, true);
        BendableLongScore initialSolutionScore = initialSolution.getScore();
        TaskAssigningSolution nextBestSolution = event.getNewBestSolution();
        doReturn(initialSolutionScore).when(nextBestSolution).getScore();
        listenerCaptor.getValue().bestSolutionChanged(event);

        SolutionSynchronizer.Result result = SolutionSynchronizer.Result.forUnchangedPeriodTimeout();
        synchronizerConsumerCaptor.getValue().accept(result);

        verify(solverExecutor, never()).addProblemFactChanges(any(List.class));
        verify(solutionSynchronizer).synchronizeSolution(eq(initialSolution), eq(nextQueryTime));
    }

    @Test
    public void onSolutionSynchronizedWithUnchangedPeriodTimeoutAndDifferentScoreBestSolution() {
        TaskAssigningSolution initialSolution = prepareStartAndASolutionProduced();
        BestSolutionChangedEvent<TaskAssigningSolution> event = mockEvent(true, true);
        TaskAssigningSolution nextBestSolution = event.getNewBestSolution();
        BendableLongScore initialSolutionScore = initialSolution.getScore();
        BendableLongScore modifiedScore = initialSolutionScore.multiply(123);
        doReturn(modifiedScore).when(nextBestSolution).getScore();
        listenerCaptor.getValue().bestSolutionChanged(event);

        SolutionSynchronizer.Result result = SolutionSynchronizer.Result.forUnchangedPeriodTimeout();
        synchronizerConsumerCaptor.getValue().accept(result);

        verify(solverExecutor, never()).addProblemFactChanges(any(List.class));
        verify(solutionProcessor).process(nextBestSolution);
        verify(solutionSynchronizer, never()).synchronizeSolution(any(TaskAssigningSolution.class), any(LocalDateTime.class));
    }

    @Test
    public void onUpdateSolutionSolverNotStarted() {
        prepareStart();
        List<ProblemFactChange<TaskAssigningSolution>> changes = new ArrayList<>();
        changes.add(new TaskPropertyChangeProblemFactChange(new Task()));

        SolutionSynchronizer.Result result = SolutionSynchronizer.Result.forChanges(changes);

        doReturn(false).when(solverExecutor).isStarted();

        synchronizerConsumerCaptor.getValue().accept(result);
        verify(solverExecutor, never()).addProblemFactChanges(changes);
    }

    @Test
    public void onUpdateSolutionWithEmptyChanges() {
        prepareStart();
        List<ProblemFactChange<TaskAssigningSolution>> changes = new ArrayList<>();

        SolutionSynchronizer.Result result = SolutionSynchronizer.Result.forChanges(changes);

        doReturn(true).when(solverExecutor).isStarted();

        synchronizerConsumerCaptor.getValue().accept(result);
        verify(solverExecutor, never()).addProblemFactChanges(changes);
    }

    @SuppressWarnings("unchecked")
    private BestSolutionChangedEvent<TaskAssigningSolution> mockEvent(boolean allChangesProcessed, boolean solutionInitialized) {
        BestSolutionChangedEvent<TaskAssigningSolution> event = mock(BestSolutionChangedEvent.class);
        doReturn(allChangesProcessed).when(event).isEveryProblemFactChangeProcessed();
        TaskAssigningSolution solution = mock(TaskAssigningSolution.class);
        BendableLongScore score = BendableLongScore.zero(1, 1).withInitScore(solutionInitialized ? 1 : -1);
        doReturn(score).when(solution).getScore();
        doReturn(solution).when(event).getNewBestSolution();
        return event;
    }

    private void onBestSolutionChangeEventNotProcessed(BestSolutionChangedEvent<TaskAssigningSolution> event) {
        prepareStart();
        SolverHandlerContext context = contextCaptor.getValue();
        long changeSet = context.nextChangeSetId();
        listenerCaptor.getValue().bestSolutionChanged(event);
        verify(executorService, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        verify(solutionProcessor, never()).process(any());
        assertFalse(context.isProcessedChangeSet(changeSet));
    }

    private void onSolutionProcessedSuccessful(SolutionProcessor.Result result, boolean withRecoverableError, boolean withImproveSolutionOnBackgroundDuration) {
        TaskAssigningSolution solution = prepareStartAndASolutionProduced();
        processorConsumerCaptor.getValue().accept(result);
        SolverHandlerContext context = contextCaptor.getValue();
        assertEquals(nextQueryTime, context.getNextQueryTime());
        if (withRecoverableError) {
            verify(solutionSynchronizer).synchronizeSolution(eq(solution), eq(previousQueryTime));
        } else if (withImproveSolutionOnBackgroundDuration) {
            verify(solutionSynchronizer).synchronizeSolution(eq(solution), eq(nextQueryTime), improveSolutionOnBackgroundDurationCaptor.capture());
            assertThat(handlerConfig.getImproveSolutionOnBackgroundDuration()).isEqualTo(improveSolutionOnBackgroundDurationCaptor.getValue());
        } else {
            verify(solutionSynchronizer).synchronizeSolution(eq(solution), eq(nextQueryTime));
        }
    }

    private void onSolutionProcessedWithError(SolutionProcessor.Result result) {
        prepareStartAndASolutionProduced();
        processorConsumerCaptor.getValue().accept(result);
        verify(solverExecutor).stop();
        verify(solutionSynchronizer, times(2)).initSolverExecutor();

        assertFalse(contextCaptor.getValue().isProcessedChangeSet(0));
    }

    private void prepareStart() {
        handler.start();
        verify(handler).createSolverExecutor(eq(solverDef), eq(registry), listenerCaptor.capture());

        verify(handler).createSolutionSynchronizer(eq(solverExecutor), eq(delegate), eq(userSystemService),
                                                   eq(SYNC_INTERVAL), eq(USERS_SYNC_INTERVAL), contextCaptor.capture(),
                                                   synchronizerConsumerCaptor.capture());

        verify(handler).createSolutionProcessor(eq(delegate), processorConsumerCaptor.capture(), eq(TARGET_USER), eq(PUBLISH_WINDOW_SIZE));
    }

    private TaskAssigningSolution prepareStartAndASolutionProduced() {
        prepareStart();
        doReturn(false).when(handler).hasWaitForImprovedSolutionDuration();
        BestSolutionChangedEvent<TaskAssigningSolution> event = mockEvent(true, true);
        SolverHandlerContext context = contextCaptor.getValue();
        long changeSet = context.nextChangeSetId();
        context.setCurrentChangeSetId(changeSet);
        context.setPreviousQueryTime(previousQueryTime);
        context.setNextQueryTime(nextQueryTime);
        listenerCaptor.getValue().bestSolutionChanged(event);
        return event.getNewBestSolution();
    }

    private void verifyDestroyCommonActions() throws Exception {
        verify(solverExecutor).destroy();
        verify(solutionSynchronizer).destroy();
        verify(solutionProcessor).destroy();
        verify(executorService).shutdown();
        verify(executorService).awaitTermination(5, TimeUnit.SECONDS);
    }
}
