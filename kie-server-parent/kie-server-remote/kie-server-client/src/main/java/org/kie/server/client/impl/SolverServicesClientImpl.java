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

package org.kie.server.client.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.optaplanner.AddProblemFactChangeCommand;
import org.kie.server.api.commands.optaplanner.AddProblemFactChangesCommand;
import org.kie.server.api.commands.optaplanner.CreateSolverCommand;
import org.kie.server.api.commands.optaplanner.DisposeSolverCommand;
import org.kie.server.api.commands.optaplanner.IsEveryProblemFactChangeProcessedCommand;
import org.kie.server.api.commands.optaplanner.GetSolverCommand;
import org.kie.server.api.commands.optaplanner.GetSolverWithBestSolutionCommand;
import org.kie.server.api.commands.optaplanner.GetSolversCommand;
import org.kie.server.api.commands.optaplanner.SolvePlanningProblemCommand;
import org.kie.server.api.commands.optaplanner.TerminateSolverEarlyCommand;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.api.model.type.JaxbBoolean;
import org.kie.server.api.rest.RestURI;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.SolverServicesClient;
import org.optaplanner.core.impl.solver.ProblemFactChange;

public class SolverServicesClientImpl
        extends AbstractKieServicesClientImpl
        implements SolverServicesClient {

    public SolverServicesClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public SolverServicesClientImpl(KieServicesConfiguration config,
                                    ClassLoader classLoader) {
        super(config,
              classLoader);
    }

    @Override
    public List<SolverInstance> getSolvers(String containerId) {
        checkMandatoryParameter("ContainerID",
                                containerId);

        final SolverInstanceList solverInstanceList;
        if (config.isRest()) {
            String uri = getURI(containerId);
            solverInstanceList = makeHttpGetRequestAndCreateCustomResponse(uri,
                                                                           SolverInstanceList.class);
        } else {
            CommandScript script = new CommandScript(Collections.singletonList(new GetSolversCommand(containerId)));
            ServiceResponse<SolverInstanceList> response = (ServiceResponse<SolverInstanceList>) executeJmsCommand(script,
                                                                                                                   GetSolversCommand.class.getName(),
                                                                                                                   KieServerConstants.CAPABILITY_BRP,
                                                                                                                   containerId).getResponses().get(0);
            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            solverInstanceList = response.getResult();
        }
        if (solverInstanceList != null && solverInstanceList.getContainers() != null) {
            return new ArrayList<>(solverInstanceList.getContainers());
        }

        return Collections.emptyList();
    }

    @Override
    public SolverInstance createSolver(String containerId,
                                       String solverId,
                                       String configFile) {
        checkMandatoryParameter("ContainerId",
                                containerId);
        checkMandatoryParameter("SolverId",
                                solverId);
        checkMandatoryParameter("ConfigFile",
                                configFile);
        if (config.isRest()) {
            String uri = getURI(containerId,
                                solverId);

            SolverInstance instance = new SolverInstance();
            instance.setSolverConfigFile(configFile);
            instance.setContainerId(containerId);
            instance.setSolverId(solverId);

            return makeHttpPutRequestAndCreateCustomResponse(uri,
                                                             instance,
                                                             SolverInstance.class,
                                                             Collections.emptyMap());
        } else {
            CommandScript script = new CommandScript(Collections.singletonList(new CreateSolverCommand(containerId,
                                                                                                       solverId,
                                                                                                       configFile)));
            ServiceResponse<SolverInstance> response = (ServiceResponse<SolverInstance>) executeJmsCommand(script,
                                                                                                           CreateSolverCommand.class.getName(),
                                                                                                           KieServerConstants.CAPABILITY_BRP,
                                                                                                           containerId).getResponses().get(0);
            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response.getResult();
        }
    }

    @Override
    public SolverInstance getSolver(String containerId,
                                    String solverId) {
        checkMandatoryParameter("ContainerID",
                                containerId);
        checkMandatoryParameter("SolverId",
                                solverId);
        if (config.isRest()) {
            String uri = getURI(containerId,
                                solverId);
            return makeHttpGetRequestAndCreateCustomResponse(uri,
                                                             SolverInstance.class);
        } else {
            CommandScript script = new CommandScript(Collections.singletonList(new GetSolverCommand(containerId,
                                                                                                    solverId)));
            ServiceResponse<SolverInstance> response = (ServiceResponse<SolverInstance>) executeJmsCommand(script,
                                                                                                           GetSolverCommand.class.getName(),
                                                                                                           KieServerConstants.CAPABILITY_BRP,
                                                                                                           containerId).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response.getResult();
        }
    }

    @Override
    public SolverInstance getSolverWithBestSolution(String containerId,
                                                    String solverId) {
        checkMandatoryParameter("ContainerID",
                                containerId);
        checkMandatoryParameter("SolverId",
                                solverId);
        if (config.isRest()) {
            String uri = getURI(containerId,
                                solverId) + "/" + RestURI.SOLVER_BEST_SOLUTION;
            return makeHttpGetRequestAndCreateCustomResponse(uri,
                                                             SolverInstance.class);
        } else {
            CommandScript script = new CommandScript(Collections.singletonList(new GetSolverWithBestSolutionCommand(containerId,
                                                                                                                    solverId)));
            ServiceResponse<SolverInstance> response = (ServiceResponse<SolverInstance>) executeJmsCommand(script,
                                                                                                           GetSolverWithBestSolutionCommand.class.getName(),
                                                                                                           KieServerConstants.CAPABILITY_BRP,
                                                                                                           containerId).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response.getResult();
        }
    }

    @Override
    public void solvePlanningProblem(String containerId,
                                     String solverId,
                                     Object planningProblem) {
        checkMandatoryParameter("ContainerID",
                                containerId);
        checkMandatoryParameter("SolverId",
                                solverId);
        checkMandatoryParameter("planningSolution",
                                planningProblem);

        if (config.isRest()) {
            String uri = getURI(containerId,
                                solverId) + "/" + RestURI.SOLVER_STATE_RUNNING;
            makeHttpPostRequestAndCreateCustomResponse(uri,
                                                       planningProblem,
                                                       ServiceResponse.class,
                                                       getHeaders(planningProblem));
        } else {
            CommandScript script = new CommandScript(Collections.singletonList(new SolvePlanningProblemCommand(containerId,
                                                                                                               solverId,
                                                                                                               serialize(planningProblem))));
            ServiceResponse<Void> response = (ServiceResponse<Void>) executeJmsCommand(script,
                                                                                       SolvePlanningProblemCommand.class.getName(),
                                                                                       KieServerConstants.CAPABILITY_BRP,
                                                                                       containerId).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void terminateSolverEarly(String containerId,
                                     String solverId) {
        checkMandatoryParameter("ContainerID",
                                containerId);
        checkMandatoryParameter("SolverId",
                                solverId);

        if (config.isRest()) {

            String uri = getURI(containerId,
                                solverId) + "/" + RestURI.SOLVER_STATE_TERMINATING;

            makeHttpPostRequestAndCreateCustomResponse(uri,
                                                       "",
                                                       ServiceResponse.class);
        } else {
            CommandScript script = new CommandScript(Collections.singletonList(new TerminateSolverEarlyCommand(containerId,
                                                                                                               solverId)));
            ServiceResponse<Void> response = (ServiceResponse<Void>) executeJmsCommand(script,
                                                                                       TerminateSolverEarlyCommand.class.getName(),
                                                                                       KieServerConstants.CAPABILITY_BRP,
                                                                                       containerId).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void addProblemFactChange(String containerId,
                                     String solverId,
                                     ProblemFactChange problemFactChange) {
        checkMandatoryParameter("containerId",
                                containerId);
        checkMandatoryParameter("solverId",
                                solverId);
        checkMandatoryParameter("problemFactChange",
                                problemFactChange);

        if (config.isRest()) {
            String uri = getURI(containerId,
                                solverId) + "/" + RestURI.SOLVER_PROBLEM_FACTS_CHANGED;
            makeHttpPostRequestAndCreateCustomResponse(uri,
                                                       problemFactChange,
                                                       ServiceResponse.class,
                                                       getHeaders(problemFactChange));
        } else {
            CommandScript script = new CommandScript(Collections.singletonList(new AddProblemFactChangeCommand(containerId,
                                                                                                               solverId,
                                                                                                               problemFactChange)));
            ServiceResponse<Void> response = (ServiceResponse<Void>) executeJmsCommand(script,
                                                                                       AddProblemFactChangeCommand.class.getName(),
                                                                                       KieServerConstants.CAPABILITY_BRP,
                                                                                       containerId).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void addProblemFactChanges(String containerId,
                                      String solverId,
                                      List<ProblemFactChange> problemFactChanges) {
        checkMandatoryParameter("containerId",
                                containerId);
        checkMandatoryParameter("solverId",
                                solverId);
        checkMandatoryParameter("problemFactChange",
                                problemFactChanges);

        if (config.isRest()) {
            String uri = getURI(containerId,
                                solverId) + "/" + RestURI.SOLVER_PROBLEM_FACTS_CHANGED;
            makeHttpPostRequestAndCreateCustomResponse(uri,
                                                       problemFactChanges,
                                                       ServiceResponse.class,
                                                       getHeaders(problemFactChanges));
        } else {
            CommandScript script = new CommandScript(Collections.singletonList(new AddProblemFactChangesCommand(containerId,
                                                                                                                solverId,
                                                                                                                problemFactChanges)));
            ServiceResponse<Void> response = (ServiceResponse<Void>) executeJmsCommand(script,
                                                                                       AddProblemFactChangeCommand.class.getName(),
                                                                                       KieServerConstants.CAPABILITY_BRP,
                                                                                       containerId).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public Boolean isEveryProblemFactChangeProcessed(String containerId,
                                                     String solverId) {
        checkMandatoryParameter("ContainerID",
                                containerId);
        checkMandatoryParameter("SolverId",
                                solverId);

        Object result = null;
        if (config.isRest()) {
            String uri = getURI(containerId,
                                solverId) + "/" + RestURI.SOLVER_PROBLEM_FACTS_CHANGED_PROCESSED;

            result = makeHttpGetRequestAndCreateCustomResponse(uri,
                                                               Object.class);
        } else {
            CommandScript script = new CommandScript(Collections.singletonList(new IsEveryProblemFactChangeProcessedCommand(containerId,
                                                                                                                            solverId)));
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand(script,
                                                                                           IsEveryProblemFactChangeProcessedCommand.class.getName(),
                                                                                           KieServerConstants.CAPABILITY_BRP,
                                                                                           containerId).getResponses().get(0);
            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result instanceof JaxbBoolean) {
            return ((JaxbBoolean) result).unwrap();
        }
        return (Boolean) result;
    }

    @Override
    public void disposeSolver(String containerId,
                              String solverId) {
        checkMandatoryParameter("ContainerID",
                                containerId);
        checkMandatoryParameter("SolverId",
                                solverId);
        if (config.isRest()) {
            String uri = getURI(containerId,
                                solverId);
            makeHttpDeleteRequestAndCreateCustomResponse(uri,
                                                         ServiceResponse.class);
        } else {
            CommandScript script = new CommandScript(Collections.singletonList(new DisposeSolverCommand(containerId,
                                                                                                        solverId)));
            ServiceResponse<Void> response = (ServiceResponse<Void>) executeJmsCommand(script,
                                                                                       DisposeSolverCommand.class.getName(),
                                                                                       KieServerConstants.CAPABILITY_BRP,
                                                                                       containerId).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    private String getURI(String containerId) {
        return (loadBalancer.getUrl() + "/" + RestURI.SOLVER_URI).replace("{" + RestURI.CONTAINER_ID + "}",
                                                                          containerId);
    }

    private String getURI(String containerId,
                          String solverId) {
        return (loadBalancer.getUrl() + "/" + RestURI.SOLVER_URI + "/" + RestURI.SOLVER_ID_URI).replace("{" + RestURI.CONTAINER_ID + "}",
                                                                                                        containerId).replace("{" + RestURI.SOLVER_ID + "}",
                                                                                                                             solverId);
    }

    private void checkMandatoryParameter(String parameterName,
                                         Object parameter) {
        if (parameter == null || ((parameter instanceof String) && ((String) parameter).isEmpty())) {
            throw new IllegalArgumentException(parameterName + " can not be null or empty.");
        }
    }

    protected void throwExceptionOnFailure(ServiceResponse<?> serviceResponse) {
        if (serviceResponse != null && ServiceResponse.ResponseType.FAILURE.equals(serviceResponse.getType())) {
            throw new KieServicesException(serviceResponse.getMsg());
        }
    }
}
