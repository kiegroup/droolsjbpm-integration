/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import java.util.List;

import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.impl.solver.ProblemFactChange;

/**
 * Provides integration for OptaPlanner to solve planning problems real time 
 * based on support of adding problem facts.
 *
 */
public interface RealtimePlanning {

    /**
     * Invoked at the initialization phase of OptaPlanner Extension to identify planning that are active.
     * @return returns true if this planning was successfully activated, false otherwise. In case of false returned 
     * this instance of planning will not be called anymore.
     */
    boolean init();
    
    /**
     * Invoked when OptaPlanner Extension is being shutdown.
     */
    void close();
    
    /**
     * Returns expected solver id to be used when initiating.
     * @return solver id to be used
     */
    String getSolverId();
    
    /**
     * Returns solver config path to be used when bootstraping
     * @return solver config path
     */
    String getSolverConfigPath();
    
    /**
     * Asserts if given containerId is accepted by this planning.
     * @param containerId id of the container that is being initiated
     * @return true if given container id is accepted otherwise false
     */
    boolean accept(String containerId);
    
    /**
     * Returns callback that should be used by solver when best solution was found.
     * @return actual implementation of the event listener
     */
    SolverEventListener<Object> getCallback();
    
    /**
     * Invoked upon solver start to load initial set of problem facts to be solved.
     * @return non null list of problem facts
     */
    List<ProblemFactChange<Object>> loadFacts();
    
    /**
     * Returns planning problem to start solving
     * @return actual planning problem to be solved
     */
    Object getPlanningProblem();
}
