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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.user.system.api.UserSystemService;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.api.model.taskassigning.TaskInputVariablesReadMode;
import org.kie.server.services.taskassigning.user.system.api.User;
import org.optaplanner.core.impl.solver.ProblemFactChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.model.taskassigning.TaskStatus.InProgress;
import static org.kie.server.api.model.taskassigning.TaskStatus.Ready;
import static org.kie.server.api.model.taskassigning.TaskStatus.Reserved;
import static org.kie.server.api.model.taskassigning.TaskStatus.Suspended;
import static org.kie.soup.commons.validation.PortablePreconditions.checkCondition;
import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

/**
 * This class manages the periodical reading (polling strategy) of current tasks from the jBPM runtime and depending
 * on the "action" INIT_SOLVER_EXECUTOR / SYNCHRONIZE_SOLUTION determines if the solver executor must be restarted with
 * a fully recovered solution or instead the tasks updated information is used for calculating the required changes
 * for the proper solution update. If any changes are calculated they are notified to the resultConsumer.
 * This class implements proper retries in case of connection issues with the target jBPM runtime, etc.
 */
public class SolutionSynchronizer extends RunnableBase {

    public static final int INIT_SOLVER_EXECUTOR = 0;
    public static final int SYNCHRONIZE_SOLUTION = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(SolutionSynchronizer.class);

    private final SolverExecutor solverExecutor;
    private final TaskAssigningRuntimeDelegate delegate;
    private final UserSystemService userSystemService;
    private final long syncInterval;
    private final SolverHandlerContext context;
    private final Consumer<Result> resultConsumer;
    private int solverExecutorStarts = 0;
    private TaskAssigningSolution solution;
    private LocalDateTime fromLastModificationDate;
    private int action = INIT_SOLVER_EXECUTOR;

    private final Semaphore startPermit = new Semaphore(0);

    public static class Result {

        private List<ProblemFactChange<TaskAssigningSolution>> changes;

        public Result(List<ProblemFactChange<TaskAssigningSolution>> changes) {
            this.changes = changes;
        }

        public List<ProblemFactChange<TaskAssigningSolution>> getChanges() {
            return changes;
        }
    }

    public SolutionSynchronizer(final SolverExecutor solverExecutor,
                                final TaskAssigningRuntimeDelegate delegate,
                                final UserSystemService userSystem,
                                final long syncInterval,
                                final SolverHandlerContext context,
                                final Consumer<Result> resultConsumer) {
        checkNotNull("solverExecutor", solverExecutor);
        checkNotNull("delegate", delegate);
        checkNotNull("userSystem", userSystem);
        checkCondition("syncInterval", syncInterval > 0);
        checkNotNull("context", context);
        checkNotNull("resultConsumer", resultConsumer);

        this.solverExecutor = solverExecutor;
        this.delegate = delegate;
        this.userSystemService = userSystem;
        this.syncInterval = syncInterval;
        this.context = context;
        this.resultConsumer = resultConsumer;
    }

    public void initSolverExecutor() {
        this.action = INIT_SOLVER_EXECUTOR;
        startPermit.release();
    }

    public void synchronizeSolution(TaskAssigningSolution solution, LocalDateTime fromLastModificationDate) {
        this.solution = solution;
        this.fromLastModificationDate = fromLastModificationDate;
        this.action = SYNCHRONIZE_SOLUTION;
        LOGGER.debug("Start synchronizeSolution fromLastModificationDate: " + fromLastModificationDate);
        startPermit.release();
    }

    /**
     * Starts the synchronizing finalization, that will be produced as soon as possible.
     * It's a non thread-safe method, but only first invocation has effect.
     */
    @Override
    public void destroy() {
        super.destroy();
        startPermit.release(); //in case it's waiting for start.
    }

    @Override
    public void run() {
        LOGGER.debug("Solution Synchronizer Started");
        boolean waitForNextAction = true;
        while (isAlive()) {
            try {
                if (waitForNextAction) {
                    startPermit.acquire();
                }
                if (isAlive()) {
                    if (action == INIT_SOLVER_EXECUTOR) {
                        try {
                            LOGGER.debug("Solution Synchronizer will recover the solution from the jBPM runtime for starting the solver.");
                            if (!solverExecutor.isStopped()) {
                                LOGGER.debug("Previous solver instance has not yet finished, let's wait for it to stop." +
                                                     " Next attempt will be in " + syncInterval + " milliseconds.");
                                waitForNextAction = false;
                            } else {
                                final TaskAssigningSolution recoveredSolution = recoverSolution();
                                if (isAlive() && !solverExecutor.isDestroyed()) {
                                    if (!recoveredSolution.getTaskList().isEmpty()) {
                                        solverExecutor.start(recoveredSolution);
                                        waitForNextAction = true;
                                        LOGGER.debug("Solution was successfully recovered. Solver was started for #{} time.", ++solverExecutorStarts);
                                        if (solverExecutorStarts > 1) {
                                            LOGGER.debug("It looks like it was necessary to restart the solver. It might" +
                                                                 " have been caused due to errors during the solution applying in the jBPM runtime");
                                        }
                                    } else {
                                        waitForNextAction = false;
                                        LOGGER.debug("It looks like there are no tasks for recovering the solution at this moment." +
                                                             " Next attempt will be in " + syncInterval + " milliseconds");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            waitForNextAction = false;
                            LOGGER.error("An error was produced during solution recovering." +
                                                 " Next attempt will be in " + syncInterval + " milliseconds", e);
                        }
                    } else if (solverExecutor.isStarted()) {
                        try {
                            LOGGER.debug("Refreshing solution status from external repository.");
                            final List<TaskData> updatedTaskDataList = loadTasksForUpdate(fromLastModificationDate);
                            LOGGER.debug("Status was read successful.");
                            if (isAlive()) {
                                final List<ProblemFactChange<TaskAssigningSolution>> changes = new SolutionChangesBuilder()
                                        .withSolution(solution)
                                        .withTasks(updatedTaskDataList)
                                        .withUserSystem(userSystemService)
                                        .withContext(context)
                                        .build();

                                if (!changes.isEmpty()) {
                                    LOGGER.debug("Current solution will be updated with {} changes from last synchronization", changes.size());
                                    waitForNextAction = true;
                                    resultConsumer.accept(new Result(changes));
                                } else {
                                    LOGGER.debug("There are no changes to apply from last synchronization.");
                                    fromLastModificationDate = context.getLastModificationDate();
                                    waitForNextAction = false;
                                }
                            }
                        } catch (Exception e) {
                            waitForNextAction = false;
                            LOGGER.error("An error was produced during solution status refresh from external repository." +
                                                 " Next attempt will be in " + syncInterval + " milliseconds", e);
                        }
                    }
                    if (!waitForNextAction) {
                        Thread.sleep(syncInterval);
                    }
                }
            } catch (InterruptedException e) {
                super.destroy();
                LOGGER.error("Solution Synchronizer was interrupted.", e);
            }
        }
        LOGGER.debug("Solution Synchronizer finished");
    }

    private TaskAssigningSolution recoverSolution() {
        final TaskAssigningRuntimeDelegate.FindTasksResult result = delegate.findTasks(Arrays.asList(Ready,
                                                                                                     Reserved,
                                                                                                     InProgress,
                                                                                                     Suspended),
                                                                                       null,
                                                                                       TaskInputVariablesReadMode.READ_FOR_ALL);
        context.setLastModificationDate(result.getQueryTime());
        final List<TaskData> taskDataList = result.getTasks();
        LOGGER.debug("{} tasks where loaded for solution recovery, with result.queryTime: {}", taskDataList.size(), result.getQueryTime());
        final List<User> externalUsers = userSystemService.findAllUsers();
        return new SolutionBuilder()
                .withTasks(taskDataList)
                .withUsers(externalUsers)
                .build();
    }

    private List<TaskData> loadTasksForUpdate(LocalDateTime fromLastModificationDate) {
        // trim to 0 milliseconds to avoid falling into https://issues.redhat.com/browse/JBPM-8970 (but note that
        // the trimming is still good to avoid any other potential date or timestamp DBMS dependent issue)
        LocalDateTime _fromLastModificationDate = fromLastModificationDate.withNano(0);
        final TaskAssigningRuntimeDelegate.FindTasksResult result = delegate.findTasks(null,
                                                                                       _fromLastModificationDate,
                                                                                       TaskInputVariablesReadMode.READ_WHEN_PLANNING_TASK_IS_NULL);
        context.setLastModificationDate(result.getQueryTime());
        LOGGER.debug("Total modifications found: {} since fromLastModificationDate: {}, with result.queryTime: {}", result.getTasks().size(), _fromLastModificationDate, result.getQueryTime());
        return result.getTasks();
    }
}
