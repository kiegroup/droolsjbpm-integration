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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.user.system.api.UserSystemService;
import org.kie.server.api.model.taskassigning.ExecutePlanningResult;
import org.kie.server.services.api.KieServerRegistry;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.impl.solver.ProblemFactChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.JBPM_TASK_ASSIGNING_PUBLISH_WINDOW_SIZE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.JBPM_TASK_ASSIGNING_SYNC_INTERVAL;
import static org.kie.server.services.taskassigning.planning.util.PropertyUtil.readSystemProperty;
import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

/**
 * This class handles all the work regarding with determining when the solver needs to be created, the handling of
 * the produced solutions and the synchronization of the working solution with the changes that might be produced
 * in the jBPM runtime, etc., by coordinating the actions/results produced by the SolverExecutor, the SolutionProcessor
 * and the SolutionSynchronizer.
 */
public class SolverHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolverHandler.class);

    private final SolverDef solverDef;
    private final KieServerRegistry registry;
    private final TaskAssigningRuntimeDelegate delegate;
    private final UserSystemService userSystemService;
    private final ExecutorService executorService;

    /**
     * Synchronizes potential concurrent accesses by the SolverWorker, SolutionProcessor and SolutionSynchronizer.
     */
    private final ReentrantLock lock = new ReentrantLock();
    private TaskAssigningSolution currentSolution = null;

    private SolverExecutor solverExecutor;
    private SolverHandlerContext context;
    private SolutionSynchronizer solutionSynchronizer;
    private SolutionProcessor solutionProcessor;
    private final String targetUserId = readSystemProperty(JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER, null, value -> value);
    private static final int publishWindowSize = readSystemProperty(JBPM_TASK_ASSIGNING_PUBLISH_WINDOW_SIZE, 2, Integer::parseInt);
    private static final long syncInterval = readSystemProperty(JBPM_TASK_ASSIGNING_SYNC_INTERVAL, 5000L, Long::parseLong);

    public SolverHandler(final SolverDef solverDef,
                         final KieServerRegistry registry,
                         final TaskAssigningRuntimeDelegate delegate,
                         final UserSystemService userSystemService,
                         final ExecutorService executorService) {
        checkNotNull("solverDef", solverDef);
        checkNotNull("registry", registry);
        checkNotNull("delegate", delegate);
        checkNotNull("userSystemService", userSystemService);
        checkNotNull("executorService", executorService);
        this.solverDef = solverDef;
        this.registry = registry;
        this.delegate = delegate;
        this.userSystemService = userSystemService;
        this.executorService = executorService;
        this.context = new SolverHandlerContext();
    }

    public void start() {
        solverExecutor = new SolverExecutor(solverDef, registry, this::onBestSolutionChange);
        solutionSynchronizer = new SolutionSynchronizer(solverExecutor, delegate, userSystemService,
                                                        syncInterval, context, this::onUpdateSolution);

        solutionProcessor = new SolutionProcessor(delegate, this::onSolutionProcessed, targetUserId,
                                                  publishWindowSize);
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
            executorService.awaitTermination(5, TimeUnit.SECONDS);
            LOGGER.debug("ExecutorService was successfully shutted down.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.debug("An exception was thrown during executionService graceful termination.", e);
            executorService.shutdownNow();
        }
    }

    private void addProblemFactChanges(List<ProblemFactChange<TaskAssigningSolution>> changes) {
        checkNotNull("changes", changes);
        if (!solverExecutor.isStarted()) {
            LOGGER.info("SolverExecutor has not been started. Changes will be discarded", changes);
            return;
        }
        if (!changes.isEmpty()) {
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
        LOGGER.debug("onBestSolutionChange: isEveryProblemFactChangeProcessed: " + event.isEveryProblemFactChangeProcessed() +
                             ", currentChangeSetId: " + context.getCurrentChangeSetId() +
                             ", isCurrentChangeSetProcssed: " + context.isProcessedChangeSet(context.getCurrentChangeSetId()));
        if (event.isEveryProblemFactChangeProcessed() &&
                event.getNewBestSolution().getScore().isSolutionInitialized() &&
                !context.isProcessedChangeSet(context.getCurrentChangeSetId())) {
            lock.lock();
            try {
                LOGGER.debug("A new solution has been produced for changeSetId: {}", context.getCurrentChangeSetId());
                currentSolution = event.getNewBestSolution();
                context.setProcessedChangeSet(context.getCurrentChangeSetId());
                solutionProcessor.process(currentSolution);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Invoked when the last produced solution has been processed by the SolutionProcessor.
     * @param result result produced by the SolutionProcessor.
     */
    private void onSolutionProcessed(SolutionProcessor.Result result) {
        lock.lock();
        try {
            if (result.hasException() || (result.getExecuteResult().hasError() && !isRecoverableError(result.getExecuteResult().getError()))) {
                LOGGER.error("An error was produced during the solution processing. The solver will be restarted with"
                                     + " a recovered solution from the jBPM runtime.", result.hasException() ? result.getException() : result.getExecuteResult().getError());
                solverExecutor.stop();
                context.clearProcessedChangeSet();
                solutionSynchronizer.initSolverExecutor();
                currentSolution = null;
            } else {
                if (result.getExecuteResult().hasError()) {
                    LOGGER.debug("A recoverable error was produced during solution processing. errorCode: {}, message: {} " +
                                         "Solution will be properly updated on next refresh", result.getExecuteResult().getError(), result.getExecuteResult().getErrorMessage());
                }
                solutionSynchronizer.synchronizeSolution(currentSolution, context.getLastModificationDate());
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Invoked every time the SolutionSynchronizer gets updated information from the jBPM runtime and there are changes
     * to apply.
     * @param result Contains the list of changes to apply.
     */
    private void onUpdateSolution(SolutionSynchronizer.Result result) {
        lock.lock();
        try {
            addProblemFactChanges(result.getChanges());
        } finally {
            lock.unlock();
        }
    }

    private boolean isRecoverableError(ExecutePlanningResult.ErrorCode errorCode) {
        return errorCode == ExecutePlanningResult.ErrorCode.TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR;
    }
}
