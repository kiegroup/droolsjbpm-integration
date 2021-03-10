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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.api.model.taskassigning.TaskInputVariablesReadMode;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.user.system.api.User;
import org.kie.server.services.taskassigning.user.system.api.UserSystemService;
import org.optaplanner.core.impl.solver.ProblemFactChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.api.task.model.Status.InProgress;
import static org.kie.api.task.model.Status.Ready;
import static org.kie.api.task.model.Status.Reserved;
import static org.kie.api.task.model.Status.Suspended;
import static org.kie.server.services.taskassigning.planning.RunnableBase.Status.STARTED;
import static org.kie.server.services.taskassigning.planning.RunnableBase.Status.STOPPED;
import static org.kie.soup.commons.validation.PortablePreconditions.checkGreaterOrEqualTo;
import static org.kie.soup.commons.validation.PortablePreconditions.checkGreaterThan;
import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

/**
 * This class manages the periodical reading (polling strategy) of current tasks from the jBPM runtime and depending
 * on the "action" INIT_SOLVER_EXECUTOR / SYNCHRONIZE_SOLUTION determines if the solver executor must be restarted with
 * a fully recovered solution or instead the tasks updated information is used for calculating the required changes
 * for the proper solution update. If any changes are calculated they are notified to the resultConsumer.
 * This class implements proper retries in case of connection issues with the target jBPM runtime, etc.
 */
public class SolutionSynchronizer extends RunnableBase {

    enum Action {
        INIT_SOLVER_EXECUTOR,
        SYNCHRONIZE_SOLUTION,
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SolutionSynchronizer.class);

    private final SolverExecutor solverExecutor;
    private final TaskAssigningRuntimeDelegate delegate;
    private final UserSystemService userSystemService;
    private final Duration syncInterval;
    private final Duration usersSyncInterval;
    private long nextUsersSyncTime;
    private Duration unchangedPeriodTimeout;
    private long nextUnchangedPeriodTime;
    private final SolverHandlerContext context;
    private final Consumer<Result> resultConsumer;
    private int solverExecutorStarts = 0;
    private TaskAssigningSolution solution;
    private LocalDateTime fromLastModificationDate;
    private AtomicReference<Action> action = new AtomicReference<>(null);
    private final Semaphore startPermit = new Semaphore(0);

    public static class Result {

        private enum ResultType {
            CHANGES_FOUND,
            UNCHANGED_PERIOD_TIMEOUT
        }

        private List<ProblemFactChange<TaskAssigningSolution>> changes;

        private ResultType type;

        private Result() {
        }

        private Result(ResultType type) {
            this.type = type;
        }

        private Result(List<ProblemFactChange<TaskAssigningSolution>> changes) {
            this(ResultType.CHANGES_FOUND);
            this.changes = changes;
        }

        static Result forChanges(List<ProblemFactChange<TaskAssigningSolution>> changes) {
            return new Result(changes);
        }

        static Result forUnchangedPeriodTimeout() {
            return new Result(ResultType.UNCHANGED_PERIOD_TIMEOUT);
        }

        public boolean hasChanges() {
            return ResultType.CHANGES_FOUND == type;
        }

        public List<ProblemFactChange<TaskAssigningSolution>> getChanges() {
            return changes;
        }
    }

    public SolutionSynchronizer(final SolverExecutor solverExecutor,
                                final TaskAssigningRuntimeDelegate delegate,
                                final UserSystemService userSystem,
                                final Duration syncInterval,
                                final Duration usersSyncInterval,
                                final SolverHandlerContext context,
                                final Consumer<Result> resultConsumer) {
        checkNotNull("solverExecutor", solverExecutor);
        checkNotNull("delegate", delegate);
        checkNotNull("userSystem", userSystem);
        checkNotNull("context", context);
        checkNotNull("resultConsumer", resultConsumer);
        checkGreaterThan("syncInterval", syncInterval, Duration.ZERO);
        checkGreaterOrEqualTo("usersSyncInterval", usersSyncInterval, Duration.ZERO);
        this.solverExecutor = solverExecutor;
        this.delegate = delegate;
        this.userSystemService = userSystem;
        this.syncInterval = syncInterval;
        this.usersSyncInterval = usersSyncInterval;
        this.context = context;
        this.resultConsumer = resultConsumer;
        this.nextUsersSyncTime = calculateNextUsersSyncTime();
    }

    public void initSolverExecutor() {
        if (!status.compareAndSet(STOPPED, STARTED)) {
            throw new IllegalStateException("SolutionSynchronizer initSolverExecutor method can only be invoked when the status is STOPPED");
        }
        action.set(Action.INIT_SOLVER_EXECUTOR);
        startPermit.release();
    }

    /**
     * Starts the synchronization of the solution from the indicated last modification date.
     * @param solution a non null solution instance to synchronize.
     * @param fromLastModificationDate filtering parameter for reading the modifications.
     */
    public void synchronizeSolution(TaskAssigningSolution solution, LocalDateTime fromLastModificationDate) {
        synchronizeSolution(solution, fromLastModificationDate, Duration.ofMillis(0));
    }

    /**
     * Starts the synchronization of the solution from the indicated last modification date.
     * @param solution a non null solution instance to synchronize.
     * @param fromLastModificationDate filtering parameter for reading the modifications.
     * @param unchangedPeriodTimeout a non null period of time for returning from the synchronization if no changes were
     * produced during that period. A negative or zero period is ignored.
     */
    public void synchronizeSolution(TaskAssigningSolution solution, LocalDateTime fromLastModificationDate, Duration unchangedPeriodTimeout) {
        checkNotNull("solution", solution);
        checkGreaterOrEqualTo("unchangedPeriodTimeout", unchangedPeriodTimeout, Duration.ZERO);
        if (!status.compareAndSet(STOPPED, STARTED)) {
            throw new IllegalStateException("SolutionSynchronizer synchronizeSolution method can only be invoked when the status is STOPPED");
        }
        this.solution = solution;
        this.fromLastModificationDate = fromLastModificationDate;
        this.unchangedPeriodTimeout = unchangedPeriodTimeout;
        this.nextUnchangedPeriodTime = calculateNextUnchangedPeriodTime(unchangedPeriodTimeout);
        action.set(Action.SYNCHRONIZE_SOLUTION);
        LOGGER.debug("Start synchronizeSolution fromLastModificationDate: {}", fromLastModificationDate);
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
        Pair<Action, Result> nextActionOrResult;
        while (isAlive()) {
            try {
                startPermit.acquire();
                if (isAlive()) {
                    nextActionOrResult = executeAction(action.get());
                    action.set(nextActionOrResult.getLeft());
                    if (action.get() != null) {
                        Thread.sleep(syncInterval.toMillis());
                        startPermit.release();
                    } else if (isAlive() && status.compareAndSet(STARTED, STOPPED) && nextActionOrResult.getRight() != null) {
                        applyResult(nextActionOrResult.getRight());
                    }
                }
            } catch (InterruptedException e) {
                super.destroy();
                Thread.currentThread().interrupt();
                LOGGER.error("Solution Synchronizer was interrupted.", e);
            }
        }
        super.destroy();
        LOGGER.debug("Solution Synchronizer finished");
    }

    Pair<Action, Result> executeAction(Action action) {
        Pair<Action, Result> resultPair = Pair.of(null, null);
        if (action == Action.INIT_SOLVER_EXECUTOR) {
            resultPair = doInitSolverExecutor();
        } else if (action == Action.SYNCHRONIZE_SOLUTION) {
            resultPair = doSynchronizeSolution();
        }
        return resultPair;
    }

    Pair<Action, Result> doInitSolverExecutor() {
        Pair<Action, Result> nextActionOrResult = Pair.of(null, null);
        try {
            LOGGER.debug("Solution Synchronizer will recover the solution from the jBPM runtime for starting the solver.");
            if (!solverExecutor.isStopped()) {
                LOGGER.debug("Previous solver instance has not yet finished, let's wait for it to stop." +
                                     " Next attempt will be in a period of {}.", syncInterval);
                nextActionOrResult = Pair.of(Action.INIT_SOLVER_EXECUTOR, null);
            } else {
                final TaskAssigningSolution recoveredSolution = recoverSolution();
                if (isAlive() && !solverExecutor.isDestroyed()) {
                    if (!recoveredSolution.getTaskList().isEmpty()) {
                        solverExecutor.start(recoveredSolution);
                        LOGGER.debug("Solution was successfully recovered. Solver was started for #{} time.", ++solverExecutorStarts);
                        if (solverExecutorStarts > 1) {
                            LOGGER.debug("It looks like it was necessary to restart the solver. It might" +
                                                 " have been caused due to errors during the solution applying in the jBPM runtime");
                        }
                    } else {
                        nextActionOrResult = Pair.of(Action.INIT_SOLVER_EXECUTOR, null);
                        LOGGER.debug("It looks like there are no tasks for recovering the solution at this moment." +
                                             " Next attempt will be in a period of {}.", syncInterval);
                    }
                }
            }
        } catch (Exception e) {
            final String msg = String.format("An error was produced during solution recovering." +
                                                     " Next attempt will be in a period of %s, error: %s", syncInterval, e.getMessage());
            LOGGER.warn(msg);
            LOGGER.debug(msg, e);
            nextActionOrResult = Pair.of(Action.INIT_SOLVER_EXECUTOR, null);
        }
        return nextActionOrResult;
    }

    Pair<Action, Result> doSynchronizeSolution() {
        Pair<Action, Result> nextActionOrResult = Pair.of(null, null);
        try {
            if (solverExecutor.isStarted()) {
                LOGGER.debug("Synchronizing solution status from the jBPM runtime.");
                final Pair<List<TaskData>, LocalDateTime> tasksUpdateResult = loadTasksForUpdate(fromLastModificationDate);
                Pair<Boolean, List<User>> usersUpdateResult = null;
                if (isAlive() && isUsersSyncTime()) {
                    usersUpdateResult = loadUsersForUpdate();
                }
                LOGGER.debug("Status was read successful.");
                if (isAlive()) {
                    final List<ProblemFactChange<TaskAssigningSolution>> changes = buildChanges(solution, tasksUpdateResult, usersUpdateResult);
                    context.setPreviousQueryTime(fromLastModificationDate);
                    LocalDateTime nextQueryTime = context.shiftQueryTime(trimMillis(tasksUpdateResult.getRight()));
                    context.setNextQueryTime(nextQueryTime);
                    if (!changes.isEmpty()) {
                        LOGGER.debug("Current solution will be updated with {} changes from last synchronization", changes.size());
                        nextActionOrResult = Pair.of(null, Result.forChanges(changes));
                    } else if (isUnchangedPeriodTime()) {
                        LOGGER.debug("There were no changes during the unchangedPeriodTimeout period of: {}, notify consumer.", unchangedPeriodTimeout);
                        nextActionOrResult = Pair.of(null, Result.forUnchangedPeriodTimeout());
                    } else {
                        LOGGER.debug("There are no changes to apply from last synchronization.");
                        fromLastModificationDate = nextQueryTime;
                        nextActionOrResult = Pair.of(Action.SYNCHRONIZE_SOLUTION, null);
                    }
                }
            }
        } catch (Exception e) {
            final String msg = String.format("An error was produced during solution status synchronization from the jBPM runtime." +
                                                     " Next attempt will be in a period of %s, error: %s", syncInterval, e.getMessage());
            LOGGER.warn(msg);
            LOGGER.debug(msg, e);
            nextActionOrResult = Pair.of(Action.SYNCHRONIZE_SOLUTION, null);
        }
        return nextActionOrResult;
    }

    protected void applyResult(Result result) {
        resultConsumer.accept(result);
    }

    private Pair<Boolean, List<User>> loadUsersForUpdate() {
        try {
            LOGGER.debug("Loading users information from the external UserSystemService");
            final List<User> userList = userSystemService.findAllUsers();
            final int userListSize = userList != null ? userList.size() : 0;
            LOGGER.debug("Users information was loaded successful: {} users were returned from external system, next synchronization will be in a period of {}",
                         userListSize, usersSyncInterval);
            nextUsersSyncTime = calculateNextUsersSyncTime();
            return Pair.of(true, userList);
        } catch (Exception e) {
            final String msg = String.format("An error was produced during users information loading from the external UserSystem repository." +
                                                     " Tasks status will still be updated and users synchronization next attempt will be in a period of %s, error: %s",
                                             syncInterval, e.getMessage());
            LOGGER.warn(msg);
            LOGGER.debug(msg, e);
            return Pair.of(false, Collections.emptyList());
        }
    }

    protected boolean isUsersSyncTime() {
        return usersSyncInterval.toMillis() > 0 && getSystemTime() > nextUsersSyncTime;
    }

    protected long calculateNextUsersSyncTime() {
        return getSystemTime() + usersSyncInterval.toMillis();
    }

    protected boolean isUnchangedPeriodTime() {
        return unchangedPeriodTimeout.toMillis() > 0 && getSystemTime() > nextUnchangedPeriodTime;
    }

    protected long calculateNextUnchangedPeriodTime(Duration unchangedPeriodTimeoutShift) {
        return getSystemTime() + unchangedPeriodTimeoutShift.toMillis();
    }

    protected long getSystemTime() {
        return System.currentTimeMillis();
    }

    protected List<ProblemFactChange<TaskAssigningSolution>> buildChanges(TaskAssigningSolution solution,
                                                                          Pair<List<TaskData>, LocalDateTime> tasksUpdateResult,
                                                                          Pair<Boolean, List<User>> usersUpdateResult) {
        if (usersUpdateResult != null && usersUpdateResult.getLeft()) {
            return buildChanges(solution, tasksUpdateResult.getLeft(), usersUpdateResult.getRight());
        } else {
            return buildChanges(solution, tasksUpdateResult.getLeft());
        }
    }

    protected List<ProblemFactChange<TaskAssigningSolution>> buildChanges(TaskAssigningSolution solution,
                                                                          List<TaskData> updatedTaskDataList) {
        return buildChanges(solution, updatedTaskDataList, null);
    }

    protected List<ProblemFactChange<TaskAssigningSolution>> buildChanges(TaskAssigningSolution solution,
                                                                          List<TaskData> updatedTaskDataList,
                                                                          List<User> updatedUserList) {
        SolutionChangesBuilder builder = SolutionChangesBuilder.create()
                .withSolution(solution)
                .withTasks(updatedTaskDataList)
                .withUserSystem(userSystemService)
                .withContext(context);
        if (updatedUserList != null) {
            builder.withUsersUpdate(updatedUserList);
        }
        return builder.build();
    }

    private TaskAssigningSolution recoverSolution() {
        final TaskAssigningRuntimeDelegate.FindTasksResult result = delegate.findTasks(Arrays.asList(Ready,
                                                                                                     Reserved,
                                                                                                     InProgress,
                                                                                                     Suspended),
                                                                                       null,
                                                                                       TaskInputVariablesReadMode.READ_FOR_ALL);

        final LocalDateTime nextQueryTime = context.shiftQueryTime(trimMillis(result.getQueryTime()));
        final LocalDateTime adjustedFirstQueryTime = context.shiftQueryTime(nextQueryTime);
        context.setPreviousQueryTime(adjustedFirstQueryTime);
        context.setNextQueryTime(nextQueryTime);
        context.clearTaskChangeTimes();
        final List<TaskData> taskDataList = result.getTasks();
        LOGGER.debug("{} tasks where loaded for solution recovery, with result.queryTime: {}", taskDataList.size(), result.getQueryTime());
        final List<User> externalUsers = userSystemService.findAllUsers();
        return buildSolution(taskDataList, externalUsers);
    }

    protected TaskAssigningSolution buildSolution(List<TaskData> taskDataList, List<User> externalUsers) {
        return SolutionBuilder.create()
                .withTasks(taskDataList)
                .withUsers(externalUsers)
                .withContext(context)
                .build();
    }

    private Pair<List<TaskData>, LocalDateTime> loadTasksForUpdate(LocalDateTime fromLastModificationDate) {
        final TaskAssigningRuntimeDelegate.FindTasksResult result = delegate.findTasks(null,
                                                                                       fromLastModificationDate,
                                                                                       TaskInputVariablesReadMode.READ_FOR_ACTIVE_TASKS_WITH_NO_PLANNING_ENTITY);
        LOGGER.debug("Total modifications found: {} since fromLastModificationDate: {}, with result.queryTime: {}",
                     result.getTasks().size(), fromLastModificationDate, result.getQueryTime());
        return Pair.of(result.getTasks(), result.getQueryTime());
    }

    private static LocalDateTime trimMillis(LocalDateTime localDateTime) {
        // trim to 0 milliseconds to avoid falling into https://issues.redhat.com/browse/JBPM-8970 (but note that
        // the trimming is still good to avoid any other potential date or timestamp DBMS dependent issue)
        return localDateTime != null ? localDateTime.withNano(0) : null;
    }
}
