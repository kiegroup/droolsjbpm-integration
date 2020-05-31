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
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import org.kie.server.api.model.taskassigning.PlanningExecutionResult;
import org.kie.server.api.model.taskassigning.PlanningItem;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.services.taskassigning.planning.RunnableBase.Status.STARTED;
import static org.kie.server.services.taskassigning.planning.RunnableBase.Status.STARTING;
import static org.kie.server.services.taskassigning.planning.RunnableBase.Status.STOPPED;
import static org.kie.server.services.taskassigning.planning.TraceHelper.tracePublishedTasks;
import static org.kie.server.services.taskassigning.planning.TraceHelper.traceSolution;
import static org.kie.soup.commons.validation.PortablePreconditions.checkCondition;
import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

/**
 * This class manges the processing of new a solution produced by the solver.
 */
public class SolutionProcessor extends RunnableBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolutionProcessor.class);

    private final TaskAssigningRuntimeDelegate delegate;
    private final Consumer<Result> resultConsumer;
    private final String targetUserId;
    private final int publishWindowSize;

    private final Semaphore solutionResource = new Semaphore(0);

    private TaskAssigningSolution solution;

    public static class Result {

        private Exception exception;

        private PlanningExecutionResult executionResult;

        private Result() {

        }

        public Result(Exception exception) {
            this.exception = exception;
        }

        public Result(PlanningExecutionResult executionResult) {
            this.executionResult = executionResult;
        }

        public boolean hasException() {
            return exception != null;
        }

        public Exception getException() {
            return exception;
        }

        public PlanningExecutionResult getExecutionResult() {
            return executionResult;
        }
    }

    /**
     * @param delegate a TaskAssigningRuntimeDelegate instance for executing methods into the jBPM runtime.
     * @param resultConsumer a consumer for processing the results.
     * @param targetUserId a user identifier for using as the "on behalf of" user when interacting with the jBPM runtime.
     * @param publishWindowSize Integer value > 0 that indicates the number of tasks to be published.
     */
    public SolutionProcessor(final TaskAssigningRuntimeDelegate delegate,
                             final Consumer<Result> resultConsumer,
                             final String targetUserId,
                             final int publishWindowSize) {
        checkNotNull("delegate", delegate);
        checkNotNull("resultConsumer", resultConsumer);
        checkNotNull("targetUserId", targetUserId);
        checkCondition("publishWindowSize", publishWindowSize > 0);
        this.delegate = delegate;
        this.resultConsumer = resultConsumer;
        this.targetUserId = targetUserId;
        this.publishWindowSize = publishWindowSize;
    }

    /**
     * @return true if a solution is being processed at this time, false in any other case.
     */
    public boolean isProcessing() {
        Status currentStatus = status.get();
        return STARTING == currentStatus || STARTED == currentStatus;
    }

    /**
     * This method is invoked from a different thread for doing the processing of a solution.
     * If a solution is being processed an exception is thrown, so it's expected that any synchronization required
     * between the isProcessing() and process() methods is performed by the caller.
     * Since only one solution can be processed at time, the caller should typically execute in the following sequence.
     * if (!solutionProcessor.isProcessing()) {
     * solutionProcessor.process(solution);
     * } else {
     * //invoke at a later time.
     * }
     * A null value will throw an exception too.
     * @param solution a solution to process.
     */
    public void process(final TaskAssigningSolution solution) {
        checkNotNull("solution", solution);
        if (!status.compareAndSet(STOPPED, STARTING)) {
            throw new IllegalStateException("SolutionProcessor process method can only be invoked when the status is STOPPED");
        }
        this.solution = solution;
        solutionResource.release();
    }

    @Override
    public void destroy() {
        super.destroy();
        solutionResource.release(); //un-lock in case it was waiting for a solution to process.
    }

    @Override
    public void run() {
        while (isAlive()) {
            try {
                solutionResource.acquire();
                if (isAlive() && status.compareAndSet(STARTING, STARTED)) {
                    doProcess(solution);
                }
            } catch (InterruptedException e) {
                super.destroy();
                Thread.currentThread().interrupt();
                LOGGER.error("Solution Processor was interrupted", e);
            }
        }
        super.destroy();
        LOGGER.debug("Solution Processor finished");
    }

    protected void doProcess(final TaskAssigningSolution solution) {
        LOGGER.debug("Starting processing of solution: {}", solution);
        final List<PlanningItem> publishedTasks = buildPlanning(solution, publishWindowSize);
        if (LOGGER.isTraceEnabled()) {
            traceSolution(LOGGER, solution);
            tracePublishedTasks(LOGGER, publishedTasks);
        }

        Result result;
        try {
            PlanningExecutionResult executeResult = delegate.executePlanning(publishedTasks, targetUserId);
            result = new Result(executeResult);
        } catch (Exception e) {
            LOGGER.error("An error was produced during solution processing, planning execution failed.", e);
            result = new Result(e);
        }

        LOGGER.debug("Solution processing finished: {}", solution);
        if (isAlive()) {
            resultConsumer.accept(result);
            status.compareAndSet(STARTED, STOPPED);
        }
    }

    List<PlanningItem> buildPlanning(TaskAssigningSolution solution, int publishWindowSize) {
        return PlanningBuilder.create()
                .withSolution(solution)
                .withPublishWindowSize(publishWindowSize)
                .build();
    }
}