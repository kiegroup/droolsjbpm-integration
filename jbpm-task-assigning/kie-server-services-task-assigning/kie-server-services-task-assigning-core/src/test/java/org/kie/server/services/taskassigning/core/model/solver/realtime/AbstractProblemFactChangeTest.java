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

package org.kie.server.services.taskassigning.core.model.solver.realtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.kie.server.services.taskassigning.core.AbstractTaskAssigningCoreTest;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.impl.solver.ProblemFactChange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class AbstractProblemFactChangeTest extends AbstractTaskAssigningCoreTest {

    final Random random = new Random();

    protected class ProgrammedProblemFactChange<C extends ProblemFactChange<TaskAssigningSolution>> {

        private TaskAssigningSolution solutionAfterChange;

        private C change;

        public ProgrammedProblemFactChange() {
        }

        public ProgrammedProblemFactChange(C change) {
            this.change = change;
        }

        public TaskAssigningSolution getSolutionAfterChange() {
            return solutionAfterChange;
        }

        public void setSolutionAfterChange(TaskAssigningSolution solutionAfterChange) {
            this.solutionAfterChange = solutionAfterChange;
        }

        public C getChange() {
            return change;
        }

        public void setChange(C change) {
            this.change = change;
        }
    }

    protected TaskAssigningSolution executeSequentialChanges(TaskAssigningSolution solution, List<? extends ProgrammedProblemFactChange> changes) throws Exception {
        Solver<TaskAssigningSolution> solver = createDaemonSolver();

        //store the first solution that was produced by the solver for knowing how things looked like at the very
        //beginning before any change was produced.
        final TaskAssigningSolution[] initialSolution = {null};
        final AtomicBoolean changesInProgress = new AtomicBoolean(false);

        final Semaphore programNextChange = new Semaphore(0);
        final Semaphore allChangesWereProduced = new Semaphore(0);

        //prepare the list of changes to program
        List<ProgrammedProblemFactChange> programmedChanges = new ArrayList<>(changes);
        List<ProgrammedProblemFactChange> scheduledChanges = new ArrayList<>();

        int totalProgrammedChanges = programmedChanges.size();
        int[] pendingChanges = {programmedChanges.size()};

        solver.addEventListener(event -> {
            if (initialSolution[0] == null) {
                //store the first produced solution for knowing how things looked like at the very beginning.
                initialSolution[0] = event.getNewBestSolution();
                //let the problem fact changes start being produced.
                programNextChange.release();
            } else if (event.isEveryProblemFactChangeProcessed() && changesInProgress.compareAndSet(true, false)) {
                ProgrammedProblemFactChange programmedChange = scheduledChanges.get(scheduledChanges.size() - 1);
                programmedChange.setSolutionAfterChange(event.getNewBestSolution());

                if (pendingChanges[0] > 0) {
                    //let the Programmed changes producer produce next change
                    programNextChange.release();
                } else {
                    solver.terminateEarly();
                    allChangesWereProduced.release();
                }
            }
        });

        //Programmed changes producer Thread.
        CompletableFuture.runAsync(() -> {
            boolean hasMoreChanges = true;
            while (hasMoreChanges) {
                try {
                    //wait until next problem fact change can be added to the solver.
                    //by construction the lock is only released when no problem fact change is in progress.
                    programNextChange.acquire();
                    ProgrammedProblemFactChange programmedChange = programmedChanges.remove(0);
                    hasMoreChanges = !programmedChanges.isEmpty();
                    pendingChanges[0] = programmedChanges.size();
                    scheduledChanges.add(programmedChange);
                    changesInProgress.set(true);
                    solver.addProblemFactChange(programmedChange.getChange());
                } catch (InterruptedException e) {
                    LOGGER.error("It looks like the test Future was interrupted.", e);
                }
            }
            try {
                //wait until the solver listener has processed all the changes.
                allChangesWereProduced.acquire();
            } catch (InterruptedException e) {
                LOGGER.error("It looks like the test Future was interrupted while waiting to finish.", e);
            }
        });

        solver.solve(solution);

        assertTrue(programmedChanges.isEmpty());
        assertEquals(totalProgrammedChanges, scheduledChanges.size());
        assertEquals(0, pendingChanges[0]);
        return initialSolution[0];
    }

    protected <T extends ProgrammedProblemFactChange> void writeProblemFactChangesTestFiles(TaskAssigningSolution initialSolution,
                                                                                            String solutionResource,
                                                                                            String filePrefix,
                                                                                            String testType,
                                                                                            List<T> programmedChanges,
                                                                                            Function<T, String> solutionBeforeChange,
                                                                                            Function<T, String> solutionAfterChange) throws Exception {

        String resourceName = solutionResource.substring(solutionResource.lastIndexOf("/") + 1);
        writeToTempFile(filePrefix + "." + testType + ".InitialSolution_", printSolution(initialSolution));
        for (int i = 0; i < programmedChanges.size(); i++) {
            T scheduledChange = programmedChanges.get(i);
            try {
                writeToTempFile(filePrefix + "." + testType + ".WorkingSolutionBeforeChange_" + resourceName + "_" + i + "__", solutionBeforeChange.apply(scheduledChange));
                writeToTempFile(filePrefix + "." + testType + ".SolutionAfterChange_" + resourceName + "_" + i + "__", solutionAfterChange.apply(scheduledChange));
            } catch (Exception e) {
                LOGGER.error("An error was produced during test files writing.", e);
            }
        }
    }
}
