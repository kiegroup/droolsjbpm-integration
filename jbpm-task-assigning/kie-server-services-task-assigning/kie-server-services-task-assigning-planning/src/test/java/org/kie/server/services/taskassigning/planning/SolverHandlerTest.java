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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.impl.solver.ProblemFactChange;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.JBPM_TASK_ASSIGNING_PUBLISH_WINDOW_SIZE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.JBPM_TASK_ASSIGNING_SYNC_INTERVAL;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolverHandlerTest {

    // surefire configured system property
    private static final String TARGET_USER = System.getProperty(JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER, "TARGET_USER");
    // surefire configured system property
    private static final int PUBLISH_WINDOW_SIZE = Integer.valueOf(System.getProperty(JBPM_TASK_ASSIGNING_PUBLISH_WINDOW_SIZE, "2"));
    // surefire configured system property
    private static final long SYNC_INTERVAL = Long.valueOf(System.getProperty(JBPM_TASK_ASSIGNING_SYNC_INTERVAL, "3000"));

    @Mock
    private SolverDef solverDef;

    @Mock
    private KieServerRegistry registry;

    @Mock
    private TaskAssigningRuntimeDelegate delegate;

    @Mock
    private UserSystemService userSystemService;

    @Mock
    private ExecutorService executorService;

    private SolverHandler handler;

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

    @Before
    public void setUp() {
        this.handler = spy(new SolverHandler(solverDef, registry, delegate, userSystemService, executorService));
        doReturn(solverExecutor).when(handler).createSolverExecutor(eq(solverDef), eq(registry), any());
        doReturn(solutionSynchronizer).when(handler).createSolutionSynchronizer(eq(solverExecutor), eq(delegate), eq(userSystemService), anyInt(), any(), any());
        doReturn(solutionProcessor).when(handler).createSolutionProcessor(eq(delegate), any(), eq(TARGET_USER), anyInt());
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
                when(executorService.awaitTermination(anyInt(), any())).thenThrow(new InterruptedException("Test Generated Error"));
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
    public void onBestSolutionChange() {
        BestSolutionChangedEvent<TaskAssigningSolution> event = mockEvent(true, true);
        prepareStart();

        SolverHandlerContext context = contextCaptor.getValue();
        long changeSet = context.nextChangeSetId();
        context.setCurrentChangeSetId(changeSet);
        listenerCaptor.getValue().bestSolutionChanged(event);
        verify(solutionProcessor).process(event.getNewBestSolution());
        assertTrue(context.isProcessedChangeSet(changeSet));
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
    public void onSolutionProcessed() {
        SolutionProcessor.Result result = new SolutionProcessor.Result(PlanningExecutionResult.builder().build());
        onSolutionProcessedSuccessful(result);
    }

    @Test
    public void onSolutionProcessedWithRecoverableError() {
        SolutionProcessor.Result result = new SolutionProcessor.Result(PlanningExecutionResult.builder()
                                                                               .error(PlanningExecutionResult.ErrorCode.TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR)
                                                                               .build());
        onSolutionProcessedSuccessful(result);
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
    public void onUpdateSolution() {
        prepareStart();
        List<ProblemFactChange<TaskAssigningSolution>> changes = new ArrayList<>();
        changes.add(new TaskPropertyChangeProblemFactChange(new Task()));

        SolutionSynchronizer.Result result = new SolutionSynchronizer.Result(changes);

        when(solverExecutor.isStarted()).thenReturn(true);

        synchronizerConsumerCaptor.getValue().accept(result);
        verify(solverExecutor).addProblemFactChanges(changes);
    }

    @Test
    public void onUpdateSolutionSolverNotStarted() {
        prepareStart();
        List<ProblemFactChange<TaskAssigningSolution>> changes = new ArrayList<>();
        changes.add(new TaskPropertyChangeProblemFactChange(new Task()));

        SolutionSynchronizer.Result result = new SolutionSynchronizer.Result(changes);

        when(solverExecutor.isStarted()).thenReturn(false);

        synchronizerConsumerCaptor.getValue().accept(result);
        verify(solverExecutor, never()).addProblemFactChanges(changes);
    }

    @Test
    public void onUpdateSolutionWithEmptyChanges() {
        prepareStart();
        List<ProblemFactChange<TaskAssigningSolution>> changes = new ArrayList<>();

        SolutionSynchronizer.Result result = new SolutionSynchronizer.Result(changes);

        when(solverExecutor.isStarted()).thenReturn(true);

        synchronizerConsumerCaptor.getValue().accept(result);
        verify(solverExecutor, never()).addProblemFactChanges(changes);
    }

    @SuppressWarnings("unchecked")
    private BestSolutionChangedEvent<TaskAssigningSolution> mockEvent(boolean allChangesProcessed, boolean solutionInitialized) {
        BestSolutionChangedEvent<TaskAssigningSolution> event = mock(BestSolutionChangedEvent.class);
        when(event.isEveryProblemFactChangeProcessed()).thenReturn(allChangesProcessed);
        TaskAssigningSolution solution = mock(TaskAssigningSolution.class);
        BendableScore score = BendableScore.zero(1, 1).withInitScore(solutionInitialized ? 1 : -1);
        when(solution.getScore()).thenReturn(score);
        when(event.getNewBestSolution()).thenReturn(solution);
        return event;
    }

    private void onBestSolutionChangeEventNotProcessed(BestSolutionChangedEvent<TaskAssigningSolution> event) {
        prepareStart();
        SolverHandlerContext context = contextCaptor.getValue();
        long changeSet = context.nextChangeSetId();
        listenerCaptor.getValue().bestSolutionChanged(event);
        verify(solutionProcessor, never()).process(any());
        assertFalse(context.isProcessedChangeSet(changeSet));
    }

    private void onSolutionProcessedSuccessful(SolutionProcessor.Result result) {
        TaskAssigningSolution solution = prepareStartAndASolutionProduced();
        processorConsumerCaptor.getValue().accept(result);
        verify(solutionSynchronizer).synchronizeSolution(eq(solution), eq(contextCaptor.getValue().getLastModificationDate()));
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
                                                   eq(SYNC_INTERVAL), contextCaptor.capture(), synchronizerConsumerCaptor.capture());
        verify(handler).createSolutionProcessor(eq(delegate), processorConsumerCaptor.capture(), eq(TARGET_USER), eq(PUBLISH_WINDOW_SIZE));
    }

    private TaskAssigningSolution prepareStartAndASolutionProduced() {
        prepareStart();
        BestSolutionChangedEvent<TaskAssigningSolution> event = mockEvent(true, true);
        SolverHandlerContext context = contextCaptor.getValue();
        long changeSet = context.nextChangeSetId();
        context.setCurrentChangeSetId(changeSet);
        context.setLastModificationDate(LocalDateTime.now());

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
