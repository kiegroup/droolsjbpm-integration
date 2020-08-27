/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.kie.server.api.model.taskassigning.PlanningExecutionResult;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.user.system.api.UserSystemService;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.impl.solver.ProblemFactChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

/**
 * This class handles all the work regarding with determining when the solver needs to be created, the handling of
 * the produced solutions and the synchronization of the working solution with the changes that might be produced
 * in the jBPM runtime, etc., by coordinating the actions/results produced by the SolverExecutor, the SolutionProcessor
 * and the SolutionSynchronizer.
 */
public class SolverHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolverHandler.class);

    private static final long EXECUTOR_TERMINATION_TIMEOUT = 5;

    private final SolverDef solverDef;
    private final KieServerRegistry registry;
    private final TaskAssigningRuntimeDelegate delegate;
    private final UserSystemService userSystemService;
    private final ScheduledExecutorService executorService;
    private final SolverHandlerConfig config;

    /**
     * Synchronizes potential concurrent accesses by the SolverWorker, SolutionProcessor and SolutionSynchronizer.
     */
    private final ReentrantLock lock = new ReentrantLock();
    private AtomicReference<TaskAssigningSolution> currentSolution = new AtomicReference<>(null);
    private AtomicReference<TaskAssigningSolution> lastBestSolution = new AtomicReference<>(null);
    private AtomicBoolean onBackgroundImprovedSolutionSent = new AtomicBoolean(false);

    private AtomicReference<ScheduledFuture<?>> scheduledFuture = new AtomicReference<>(null);

    private SolverExecutor solverExecutor;
    private SolverHandlerContext context;
    private SolutionSynchronizer solutionSynchronizer;
    private SolutionProcessor solutionProcessor;

    public SolverHandler(final SolverDef solverDef,
                         final KieServerRegistry registry,
                         final TaskAssigningRuntimeDelegate delegate,
                         final UserSystemService userSystemService,
                         final ScheduledExecutorService executorService,
                         final SolverHandlerConfig config) {
        checkNotNull("solverDef", solverDef);
        checkNotNull("registry", registry);
        checkNotNull("delegate", delegate);
        checkNotNull("userSystemService", userSystemService);
        checkNotNull("executorService", executorService);
        checkNotNull("config", config);
        this.solverDef = solverDef;
        this.registry = registry;
        this.delegate = delegate;
        this.userSystemService = userSystemService;
        this.executorService = executorService;
        this.config = config;
        this.context = new SolverHandlerContext(config.getSyncQueriesShift());
    }

    public void start() {
        solverExecutor = createSolverExecutor(solverDef, registry, this::onBestSolutionChange);
        solutionSynchronizer = createSolutionSynchronizer(solverExecutor, delegate, userSystemService,
                                                          config.getSyncInterval(), config.getUsersSyncInterval(), context, this::onSolutionSynchronized);
        solutionProcessor = createSolutionProcessor(delegate, this::onSolutionProcessed, config.getTargetUserId(),
                                                    config.getPublishWindowSize());
        executorService.execute(solverExecutor); //is started/stopped on demand by the SolutionSynchronizer.
        executorService.execute(solutionSynchronizer);
        executorService.execute(solutionProcessor);
        solutionSynchronizer.initSolverExecutor();
    }

    public void destroy() {
        solverExecutor.destroy();
        solutionSynchronizer.destroy();
        solutionProcessor.destroy();

        executorService.shutdown();
        try {
            executorService.awaitTermination(EXECUTOR_TERMINATION_TIMEOUT, TimeUnit.SECONDS);
            LOGGER.debug("ExecutorService was successfully shutted down.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.debug("An exception was thrown during executionService graceful termination.", e);
            executorService.shutdownNow();
        }
    }

    SolverExecutor createSolverExecutor(SolverDef solverDef,
                                        KieServerRegistry registry,
                                        SolverEventListener<TaskAssigningSolution> listener) {
        return new SolverExecutor(solverDef, registry, listener);
    }

    SolutionSynchronizer createSolutionSynchronizer(SolverExecutor solverExecutor,
                                                    TaskAssigningRuntimeDelegate delegate,
                                                    UserSystemService userSystemService,
                                                    Duration syncInterval,
                                                    Duration usersSyncInterval,
                                                    SolverHandlerContext context,
                                                    Consumer<SolutionSynchronizer.Result> resultConsumer) {
        return new SolutionSynchronizer(solverExecutor, delegate, userSystemService, syncInterval, usersSyncInterval, context, resultConsumer);
    }

    SolutionProcessor createSolutionProcessor(TaskAssigningRuntimeDelegate delegate,
                                              Consumer<SolutionProcessor.Result> resultConsumer,
                                              String targetUserId,
                                              int publishWindowSize) {
        return new SolutionProcessor(delegate, resultConsumer, targetUserId, publishWindowSize);
    }

    private void addProblemFactChanges(List<ProblemFactChange<TaskAssigningSolution>> changes) {
        checkNotNull("changes", changes);
        if (!solverExecutor.isStarted()) {
            LOGGER.info("SolverExecutor has not been started. Changes will be discarded {}", changes);
            return;
        }
        if (!changes.isEmpty()) {
            onBackgroundImprovedSolutionSent.set(false);
            solverExecutor.addProblemFactChanges(changes);
        } else {
            LOGGER.info("It looks like an empty change list was provided. Nothing will be done since it has no effect on the solution.");
        }
    }

    /**
     * Invoked when the solver produces a new solution.
     * @param event event produced by the solver.
     */
    private void onBestSolutionChange(BestSolutionChangedEvent<TaskAssigningSolution> event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onBestSolutionChange: isEveryProblemFactChangeProcessed: {}, currentChangeSetId: {}," +
                                 " isCurrentChangeSetProcessed: {}, newBestSolution: {}",
                         event.isEveryProblemFactChangeProcessed(), context.getCurrentChangeSetId(),
                         context.isCurrentChangeSetProcessed(), event.getNewBestSolution());
        }

        TaskAssigningSolution newBestSolution = event.getNewBestSolution();
        if (event.isEveryProblemFactChangeProcessed() && newBestSolution.getScore().isSolutionInitialized()) {
            lastBestSolution.set(newBestSolution);
            if (hasWaitForImprovedSolutionDuration()) {
                scheduleOnBestSolutionChange(newBestSolution, config.getWaitForImprovedSolutionDuration().toMillis());
            } else {
                onBestSolutionChange(newBestSolution);
            }
        }
    }

    private void scheduleOnBestSolutionChange(TaskAssigningSolution chBestSolution, long delay) {
        if (scheduledFuture.get() == null && !context.isCurrentChangeSetProcessed()) {
            lock.lock();
            LOGGER.debug("Schedule execute solution change with previous chBestSolution: {}", chBestSolution);
            try {
                Supplier<TaskAssigningSolution> solutionSupplier = () -> lastBestSolution.get();
                ScheduledFuture<?> future = executorService.schedule(() -> executeSolutionChange(chBestSolution, solutionSupplier),
                                                                     delay,
                                                                     TimeUnit.MILLISECONDS);
                scheduledFuture.set(future);
            } finally {
                lock.unlock();
            }
        }
    }

    private void onBestSolutionChange(TaskAssigningSolution newBestSolution) {
        if (!context.isCurrentChangeSetProcessed()) {
            executeSolutionChange(newBestSolution);
        }
    }

    private void executeSolutionChange(TaskAssigningSolution chBestSolution, Supplier<TaskAssigningSolution> solutionSupplier) {
        lock.lock();
        try {
            TaskAssigningSolution currentLastBestSolution = solutionSupplier.get();
            LOGGER.debug("Executing delayed solution change for currentChangeSetId: {}, lastBestSolution: {}, lastBestSolution: {}",
                         context.getCurrentChangeSetId(), currentLastBestSolution.getScore(), currentLastBestSolution);

            if (chBestSolution == currentLastBestSolution) {
                LOGGER.debug("SAME SOLUTION: lastBestSolution is the same as the chBestSolution");
            } else {
                if (chBestSolution.getScore().compareTo(currentLastBestSolution.getScore()) < 0) {
                    LOGGER.debug("SCORE IMPROVEMENT: lastBestSolution has a better score than the chBestSolution: " +
                                         "currentChangeSetId: {}, chBestSolution: {}, chBestSolution: {}",
                                 context.getCurrentChangeSetId(), chBestSolution.getScore(), chBestSolution);
                } else {
                    LOGGER.debug("SAME SCORE: lastBestSolution is not the same as the chBestSolution BUT score has not improved" +
                                         ", currentChangeSetId: {}, chBestSolution: {}, chBestSolution: {}",
                                 context.getCurrentChangeSetId(), chBestSolution.getScore(), chBestSolution);
                }
            }
            executeSolutionChange(currentLastBestSolution);
        } finally {
            scheduledFuture.set(null);
            lock.unlock();
        }
    }

    private void executeSolutionChange(TaskAssigningSolution solution) {
        lock.lock();
        try {
            currentSolution.set(solution);
            context.setProcessedChangeSet(context.getCurrentChangeSetId());
            solutionProcessor.process(currentSolution.get());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Invoked when the last produced solution has been processed by the SolutionProcessor.
     * @param result result produced by the SolutionProcessor.
     */
    private void onSolutionProcessed(SolutionProcessor.Result result) {
        lock.lock();
        try {
            if (result.hasException() || (result.getExecutionResult().hasError() && !isRecoverableError(result.getExecutionResult().getError()))) {
                LOGGER.error("An error was produced during the solution processing. The solver will be restarted with"
                                     + " a recovered solution from the jBPM runtime.", result.hasException() ? result.getException() : result.getExecutionResult().getError());
                solverExecutor.stop();
                context.clearProcessedChangeSet();
                solutionSynchronizer.initSolverExecutor();
                currentSolution.set(null);
                lastBestSolution.set(null);
                onBackgroundImprovedSolutionSent.set(false);
            } else {
                LocalDateTime fromLastModificationDate;
                if (result.getExecutionResult().hasError()) {
                    LOGGER.debug("A recoverable error was produced during solution processing. errorCode: {}, message: {} " +
                                         "Solution will be properly updated on next refresh", result.getExecutionResult().getError(), result.getExecutionResult().getErrorMessage());
                    fromLastModificationDate = context.getPreviousQueryTime();
                    solutionSynchronizer.synchronizeSolution(currentSolution.get(), fromLastModificationDate);
                } else {
                    fromLastModificationDate = context.getNextQueryTime();
                    context.clearTaskChangeTimes(context.getPreviousQueryTime());
                    if (hasImproveSolutionOnBackgroundDuration() && !onBackgroundImprovedSolutionSent.get()) {
                        solutionSynchronizer.synchronizeSolution(currentSolution.get(), fromLastModificationDate, config.getImproveSolutionOnBackgroundDuration());
                    } else {
                        solutionSynchronizer.synchronizeSolution(currentSolution.get(), fromLastModificationDate);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Invoked every time the SolutionSynchronizer gets updated information from the jBPM runtime and there are changes
     * to apply, or when the configurable amount of time with no changes has elapsed.
     * @param result Contains the list of changes to apply.
     */
    private void onSolutionSynchronized(SolutionSynchronizer.Result result) {
        lock.lock();
        try {
            if (result.hasChanges()) {
                addProblemFactChanges(result.getChanges());
            } else {
                LOGGER.debug("Processing synchronization unchanged period timeout. Checking if there is a" +
                                     " lastBestSolution with an improved score to send");
                TaskAssigningSolution bestSolution = lastBestSolution.get();
                onBackgroundImprovedSolutionSent.set(true);
                if (bestSolution.getScore().compareTo(currentSolution.get().getScore()) > 0) {
                    LOGGER.debug("About to process lastBestSolution after improveSolutionOnBackgroundDuration timeout with score: {}, lastBestSolution: {}.",
                                 bestSolution.getScore(), bestSolution);
                    currentSolution.set(bestSolution);
                    solutionProcessor.process(currentSolution.get());
                } else {
                    LOGGER.debug("Looks like lastBestSolution is the same as the already sent currentSolution or has the same score" +
                                         ", nothing to do. Restarting synchronization");
                    LocalDateTime fromLastModificationDate = context.getNextQueryTime();
                    context.clearTaskChangeTimes(context.getPreviousQueryTime());
                    solutionSynchronizer.synchronizeSolution(currentSolution.get(), fromLastModificationDate);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private boolean isRecoverableError(PlanningExecutionResult.ErrorCode errorCode) {
        return errorCode == PlanningExecutionResult.ErrorCode.TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR;
    }

    protected boolean hasWaitForImprovedSolutionDuration() {
        return config.getWaitForImprovedSolutionDuration().toMillis() > 0;
    }

    protected boolean hasImproveSolutionOnBackgroundDuration() {
        return config.getImproveSolutionOnBackgroundDuration().toMillis() > 0;
    }
}
