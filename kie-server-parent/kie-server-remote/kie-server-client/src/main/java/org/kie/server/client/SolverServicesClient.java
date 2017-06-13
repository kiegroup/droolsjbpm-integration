/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.client;

import java.util.List;

import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.client.jms.ResponseHandler;
import org.optaplanner.core.impl.solver.ProblemFactChange;

public interface SolverServicesClient {

    SolverInstance createSolver(String containerId,
                                String solverId,
                                String configFile);

    List<SolverInstance> getSolvers(String containerId);

    SolverInstance getSolver(String containerId,
                             String solverId);

    SolverInstance getSolverWithBestSolution(String containerId,
                                             String solverId);

    void solvePlanningProblem(String containerId,
                              String solverId,
                              Object planningProblem);

    void terminateSolverEarly(String containerId,
                              String solverId);

    void addProblemFactChange(String containerId,
                              String solverId,
                              ProblemFactChange problemFactChange);

    void addProblemFactChanges(String containerId,
                              String solverId,
                              List<ProblemFactChange> problemFactChange);

    Boolean isEveryProblemFactChangeProcessed(String containerId,
                                              String solverId);

    void disposeSolver(String containerId,
                       String solverId);

    void setResponseHandler(ResponseHandler responseHandler);
}

