/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.services.optaplanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.ScoreWrapper;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.solver.ProblemFactChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OptaPlanner solver service.
 */
public class SolverServiceBase {

    private static final Logger logger = LoggerFactory.getLogger(SolverServiceBase.class);
    private final ExecutorService executor;

    private KieServerRegistry context;
    private Map<String, SolverInstanceContext> solvers = new ConcurrentHashMap<String, SolverInstanceContext>();

    public SolverServiceBase(KieServerRegistry context,
                             ExecutorService executorService) {
        this.context = context;
        this.executor = executorService;
    }

    public ServiceResponse<SolverInstance> createSolver(String containerId,
                                                        String solverId,
                                                        SolverInstance instance) {
        if (instance == null || instance.getSolverConfigFile() == null) {
            logger.error("Error creating solver. Configuration file name is null: " + instance);
            return new ServiceResponse<SolverInstance>(
                    ServiceResponse.ResponseType.FAILURE,
                    "Failed to create solver for container " + containerId +
                            ". Solver configuration file is null: " + instance);
        }
        instance.setContainerId(containerId);
        instance.setSolverId(solverId);

        try {
            KieContainerInstanceImpl ci = context.getContainer(containerId);
            if (ci == null) {
                logger.error("Error creating solver. Container does not exist: " + containerId);
                return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.FAILURE,
                                                           "Failed to create solver. Container does not exist: " + containerId);
            }

            // have to synchronize on the ci or a concurrent call to dispose may create inconsistencies
            synchronized (ci) {
                if (solvers.containsKey(instance.getSolverInstanceKey())) {
                    logger.error("Error creating solver. Solver '" + solverId + "' already exists for container '" + containerId + "'.");
                    return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.FAILURE,
                                                               "Failed to create solver. Solver '" + solverId +
                                                                       "' already exists for container '" + containerId + "'.");
                }
                SolverInstanceContext sic = new SolverInstanceContext(instance);
                if (instance.getStatus() == null) {
                    instance.setStatus(SolverInstance.SolverStatus.NOT_SOLVING);
                }

                try {
                    SolverFactory<Object> solverFactory = SolverFactory.createFromKieContainerXmlResource(ci.getKieContainer(),
                                                                                                     instance.getSolverConfigFile());

                    Solver<Object> solver = solverFactory.buildSolver();

                    sic.setSolver(solver);
                    updateSolverInstance(sic);

                    solvers.put(instance.getSolverInstanceKey(),
                                sic);

                    logger.info("Solver '" + solverId + "' successfully created in container '" + containerId + "'");
                    return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.SUCCESS,
                                                               "Solver '" + solverId + "' successfully created in container '" + containerId + "'",
                                                               instance);
                } catch (Exception e) {
                    logger.error("Error creating solver factory for solver " + instance,
                                 e);
                    return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.FAILURE,
                                                               "Error creating solver factory for solver: " + e.getMessage(),
                                                               instance);
                }
            }
        } catch (Exception e) {
            logger.error("Error creating solver '" + solverId + "' in container '" + containerId + "'",
                         e);
            return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.FAILURE,
                                                       "Error creating solver '" + solverId + "' in container '" + containerId + "': " + e.getMessage(),
                                                       instance);
        }
    }

    public ServiceResponse<SolverInstanceList> getSolvers(String containerId) {
        try {
            List<SolverInstance> sl = getSolversForContainer(containerId);
            return new ServiceResponse<SolverInstanceList>(
                    ServiceResponse.ResponseType.SUCCESS,
                    "Solvers list successfully retrieved from container '" + containerId + "'",
                    new SolverInstanceList(sl));
        } catch (Exception e) {
            logger.error("Error retrieving solvers list from container '" + containerId + "'",
                         e);
            return new ServiceResponse<SolverInstanceList>(
                    ServiceResponse.ResponseType.FAILURE,
                    "Error retrieving solvers list from container '" + containerId + "'" + e.getMessage(),
                    null);
        }
    }

    public ServiceResponse<SolverInstance> getSolver(String containerId,
                                                     String solverId) {
        try {
            SolverInstanceContext sic = solvers.get(SolverInstance.getSolverInstanceKey(containerId,
                                                                                        solverId));
            if (sic != null) {
                updateSolverInstance(sic);
                return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.SUCCESS,
                                                           "Solver '" + solverId + "' state successfully retrieved from container '" + containerId + "'",
                                                           sic.getInstance());
            } else {
                return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.FAILURE,
                                                           "Solver '" + solverId + "' not found in container '" + containerId + "'",
                                                           null);
            }
        } catch (Exception e) {
            logger.error("Error retrieving solver '" + solverId + "' state from container '" + containerId + "'",
                         e);
            return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.FAILURE,
                                                       "Error retrieving solver '" + solverId + "' state from container '" + containerId + "'" + e.getMessage(),
                                                       null);
        }
    }

    public ServiceResponse<SolverInstance> getSolverWithBestSolution(String containerId,
                                                                     String solverId) {
        try {
            SolverInstanceContext sic = solvers.get(SolverInstance.getSolverInstanceKey(containerId,
                                                                                        solverId));
            if (sic != null) {
                updateSolverInstance(sic);
                sic.getInstance().setBestSolution(sic.getSolver().getBestSolution());
                return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.SUCCESS,
                                                           "Best computed solution for '" + solverId + "' successfully retrieved from container '" + containerId + "'",
                                                           sic.getInstance());
            } else {
                return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.FAILURE,
                                                           "Solver '" + solverId + "' not found in container '" + containerId + "'",
                                                           null);
            }
        } catch (Exception e) {
            logger.error("Error retrieving solver '" + solverId + "' state from container '" + containerId + "'",
                         e);
            return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.FAILURE,
                                                       "Error retrieving solver '" + solverId + "' state from container '" + containerId + "'" + e.getMessage(),
                                                       null);
        }
    }

    public ServiceResponse<Void> solvePlanningProblem(String containerId,
                                                      String solverId,
                                                      Object planningProblem) {
        try {
            SolverInstanceContext sic = solvers.get(SolverInstance.getSolverInstanceKey(containerId,
                                                                                        solverId));
            if (sic != null) {
                synchronized (sic) {
                    switch (sic.getInstance().getStatus()) {
                        case SOLVING: {
                            return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                                         "Solver '" + solverId + "' on container '" + containerId + "' is already executing.");
                        }
                        case TERMINATING_EARLY: {
                            return new ServiceResponse<>(ServiceResponse.ResponseType.SUCCESS,
                                                         "Solver '" + solverId + "' on container '" + containerId + "' has already terminated.");
                        }
                        case NOT_SOLVING: {
                            if (planningProblem == null) {
                                return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                                             "Planning-problem is a mandatory field when starting the solver.");
                            }
                            updateSolverInstance(sic);
                            solvePlanningProblem(sic,
                                                 planningProblem);
                            return new ServiceResponse<>(ServiceResponse.ResponseType.SUCCESS,
                                                         "Solver '" + solverId + "' from container '" + containerId + "' successfully started.");
                        }
                        default: {
                            return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                                         "Solver '" + solverId + "' on container '" + containerId + "' is in unrecognized state '" + sic.getInstance().getStatus() + "'.");
                        }
                    }
                }
            } else {
                return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                             "Solver '" + solverId + "' not found in container '" + containerId + "'");
            }
        } catch (Exception e) {
            logger.error("Error retrieving solver '" + solverId + "' state from container '" + containerId + "'",
                         e);
            return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                         "Unknown error updating solver state.");
        }
    }

    public ServiceResponse<Void> terminateSolverEarly(String containerId,
                                                      String solverId) {
        try {
            SolverInstanceContext sic = solvers.get(SolverInstance.getSolverInstanceKey(containerId,
                                                                                        solverId));
            if (sic != null) {
                synchronized (sic) {
                    switch (sic.getInstance().getStatus()) {
                        case SOLVING: {
                            terminateSolverEarly(sic);
                            return new ServiceResponse<>(ServiceResponse.ResponseType.SUCCESS,
                                                         "Solver '" + solverId + "' on container '" + containerId + "' successfully terminated.");
                        }
                        case TERMINATING_EARLY: {
                            return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                                         "Solver '" + solverId + "' on container '" + containerId + "' already terminated.");
                        }
                        case NOT_SOLVING: {
                            return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                                         "Solver '" + solverId + "' from container '" + containerId + "' is not executing.");
                        }
                        default: {
                            return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                                         "Solver '" + solverId + "' on container '" + containerId + "' is in unrecognized state '" + sic.getInstance().getStatus() + "'.");
                        }
                    }
                }
            } else {
                return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                             "Solver '" + solverId + "' not found in container '" + containerId + "'");
            }
        } catch (Exception e) {
            logger.error("Error retrieving solver '" + solverId + "' state from container '" + containerId + "'",
                         e);
            return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                         "Unknown error updating solver state.");
        }
    }

    public ServiceResponse<Void> addProblemFactChanges(String containerId,
                                                       String solverId,
                                                       Object problemFactChanges) {
        try {
            SolverInstanceContext sic = solvers.get(SolverInstance.getSolverInstanceKey(containerId,
                                                                                        solverId));
            if (problemFactChanges instanceof List) {
                List problemFactChangesList = (List) problemFactChanges;
                Map<Integer, String> indexClassNameErrorMap = new HashMap<>();
                for (int i = 0; i < problemFactChangesList.size(); i++) {
                    Object problemFactChange = problemFactChangesList.get(i);

                    if (!(problemFactChange instanceof ProblemFactChange)) {
                        indexClassNameErrorMap.put(i,
                                                   problemFactChange.getClass().getName());
                    }
                }
                if (!indexClassNameErrorMap.isEmpty()) {
                    String errorItemsString = indexClassNameErrorMap
                            .entrySet()
                            .stream()
                            .map(e -> "[index " + e.getKey() + ", type: " + e.getValue().getClass() + "]")
                            .collect(Collectors.joining(","));
                    return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                                 "Items (" + errorItemsString + ") of supplied 'problemFactChangeObject' parameter are not instances of " + ProblemFactChange.class.getName() + ".");
                }
                return submitProblemFactChanges(sic,
                                                containerId,
                                                solverId,
                                                () -> sic.getSolver().addProblemFactChanges(problemFactChangesList));
            }
            if (!(problemFactChanges instanceof ProblemFactChange)) {
                return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                             "Supplied 'problemFactChangeObject' parameter is not an instance of " + ProblemFactChange.class.getName()
                                                     + ". Actual parameter type: " + problemFactChanges.getClass().getName());
            }
            return submitProblemFactChanges(sic,
                                            containerId,
                                            solverId,
                                            () -> sic.getSolver().addProblemFactChange((ProblemFactChange) problemFactChanges));
        } catch (Exception e) {
            logger.error("Error adding problem fact change'" + problemFactChanges + "' to solver '" + solverId + "' from container '" + containerId + "'",
                         e);
            return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                         "Unknown error while adding problem fact change.");
        }
    }

    private ServiceResponse<Void> submitProblemFactChanges(
            final SolverInstanceContext sic,
            final String containerId,
            final String solverId,
            final Runnable solverAction) {
        if (sic != null) {
            synchronized (sic) {
                switch (sic.getInstance().getStatus()) {
                    case SOLVING: {
                        solverAction.run();
                        return new ServiceResponse<>(ServiceResponse.ResponseType.SUCCESS,
                                                     "Problem fact changes have been successfully submitted to solver '" + solverId + "' on container '" + containerId + "'.");
                    }
                    case TERMINATING_EARLY: {
                        return new ServiceResponse<>(ServiceResponse.ResponseType.SUCCESS,
                                                     "Solver '" + solverId + "' on container '" + containerId + "' has already terminated.");
                    }
                    case NOT_SOLVING: {
                        return new ServiceResponse<>(ServiceResponse.ResponseType.SUCCESS,
                                                     "Solver '" + solverId + "' on container '" + containerId + "' is not running.");
                    }
                    default: {
                        return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                                     "Solver '" + solverId + "' on container '" + containerId + "' is in unrecognized state '" + sic.getInstance().getStatus() + "'.");
                    }
                }
            }
        } else {
            return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                         "Solver '" + solverId + "' not found in container '" + containerId + "'");
        }
    }

    public ServiceResponse<Boolean> isEveryProblemFactChangeProcessed(final String containerId,
                                                                      final String solverId) {
        try {
            SolverInstanceContext sic = solvers.get(SolverInstance.getSolverInstanceKey(containerId,
                                                                                        solverId));
            if (sic != null) {
                synchronized (sic) {
                    switch (sic.getInstance().getStatus()) {
                        case SOLVING: {
                            boolean everyProblemFactChangeProcessed = sic.getSolver().isEveryProblemFactChangeProcessed();
                            return new ServiceResponse<>(ServiceResponse.ResponseType.SUCCESS,
                                                         "Problem fact changes have been successfully queried from solver '" + solverId + "' on container '" + containerId + "'.",
                                                         everyProblemFactChangeProcessed);
                        }
                        case TERMINATING_EARLY: {
                            return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                                         "Solver '" + solverId + "' on container '" + containerId + "' already terminated.");
                        }
                        case NOT_SOLVING: {
                            return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                                         "Solver '" + solverId + "' from container '" + containerId + "' is not executing.");
                        }
                        default: {
                            return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                                         "Solver '" + solverId + "' on container '" + containerId + "' is in unrecognized state '" + sic.getInstance().getStatus() + "'.");
                        }
                    }
                }
            } else {
                return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                             "Solver '" + solverId + "' not found in container '" + containerId + "'");
            }
        } catch (Exception e) {
            logger.error("Error retrieving solver '" + solverId + "' state from container '" + containerId + "'",
                         e);
            return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                         "Unknown error querying problem facts processing state.");
        }
    }

    public ServiceResponse<Void> disposeSolver(String containerId,
                                               String solverId) {
        try {
            SolverInstanceContext sic = internalDisposeSolver(containerId,
                                                              solverId);
            if (sic != null) {
                return new ServiceResponse<>(ServiceResponse.ResponseType.SUCCESS,
                                             "Solver '" + solverId + "' successfully disposed from container '" + containerId + "'",
                                             null);
            }
            return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                         "Solver '" + solverId + "' from container '" + containerId + "' not found.",
                                         null);
        } catch (Exception e) {
            logger.error("Error disposing solver '" + solverId + "' from container '" + containerId + "'",
                         e);
            return new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                         "Error disposing solver '" + solverId + "' from container '" + containerId + "'. Message: " + e.getMessage(),
                                         null);
        }
    }

    public KieServerRegistry getKieServerRegistry() {
        return this.context;
    }

    public void disposeSolversForContainer(String containerId,
                                           KieContainerInstance kci) {
        List<SolverInstance> sfc = getSolversForContainer(containerId);
        for (SolverInstance si : sfc) {
            internalDisposeSolver(containerId,
                                  si.getSolverId());
        }
    }

    private List<SolverInstance> getSolversForContainer(String containerId) {
        List<SolverInstance> sl = new ArrayList<SolverInstance>(solvers.size());
        for (SolverInstanceContext sic : solvers.values()) {
            if (containerId.equalsIgnoreCase(sic.getInstance().getContainerId())) {
                updateSolverInstance(sic);
                sl.add(sic.getInstance());
            }
        }
        return sl;
    }

    private SolverInstanceContext internalDisposeSolver(String containerId,
                                                        String solverId) {
        // need to dispose resources here
        SolverInstanceContext sic = solvers.remove(SolverInstance.getSolverInstanceKey(containerId,
                                                                                       solverId));
        if (sic != null) {
            synchronized (sic) {
                if (sic.getInstance().getStatus() == SolverInstance.SolverStatus.SOLVING) {
                    terminateSolverEarly(sic);
                }
            }
        }
        return sic;
    }

    private void updateSolverInstance(SolverInstanceContext sic) {
        synchronized (sic) {
            // We keep track of the solver status ourselves, so there's no need to call buggy updateSolverStatus( sic );
            Score<?> bestScore = sic.getSolver().getBestScore();

            sic.getInstance().setScoreWrapper(new ScoreWrapper(bestScore));
        }
    }

    private void updateSolverStatus(SolverInstanceContext sic) {
        Solver<?> solver = sic.getSolver();
        if (!solver.isSolving()) {
            // TODO BUGGY because a solver might have been a scheduled to start, but not started yet
            // (especially immediately after solvePlanningProblem() call).
            sic.getInstance().setStatus(SolverInstance.SolverStatus.NOT_SOLVING);
        } else if (solver.isTerminateEarly()) {
            sic.getInstance().setStatus(SolverInstance.SolverStatus.TERMINATING_EARLY);
        } else {
            sic.getInstance().setStatus(SolverInstance.SolverStatus.SOLVING);
        }
    }

    private void solvePlanningProblem(final SolverInstanceContext sic,
                                      final Object planningSolution) {
        sic.getInstance().setBestSolution(null);
        sic.getInstance().setStatus(SolverInstance.SolverStatus.SOLVING);
        this.executor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // If the executor's queue is full, it's possible that the solver gets canceled before it starts
                            SolverInstance.SolverStatus status;
                            synchronized (sic) {
                                status = sic.getInstance().getStatus();
                                // TODO Race condition: status turns into non-solving before solver starts
                                // See https://issues.jboss.org/browse/PLANNER-540
                            }
                            if (status == SolverInstance.SolverStatus.SOLVING) {
                                sic.getSolver().solve(planningSolution);
                            }
                        } catch (Exception e) {
                            logger.error("Exception executing solver '" + sic.getInstance().getSolverId() + "' from container '" + sic.getInstance().getContainerId() + "'. Thread will terminate.",
                                         e);
                        } finally {
                            synchronized (sic) {
                                sic.getInstance().setStatus(SolverInstance.SolverStatus.NOT_SOLVING);
                            }
                        }
                    }
                });
    }

    private void terminateSolverEarly(SolverInstanceContext sic) {
        synchronized (sic) {
            if (sic.getInstance().getStatus() == SolverInstance.SolverStatus.SOLVING) {
                sic.getInstance().setStatus(SolverInstance.SolverStatus.TERMINATING_EARLY);
            }
        }
        sic.getSolver().terminateEarly();
    }
}
