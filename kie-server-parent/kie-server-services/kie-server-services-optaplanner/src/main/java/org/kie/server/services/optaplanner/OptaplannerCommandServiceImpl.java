/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
import java.util.List;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.optaplanner.AddProblemFactChangeCommand;
import org.kie.server.api.commands.optaplanner.AddProblemFactChangesCommand;
import org.kie.server.api.commands.optaplanner.CreateSolverCommand;
import org.kie.server.api.commands.optaplanner.DisposeSolverCommand;
import org.kie.server.api.commands.optaplanner.GetSolverCommand;
import org.kie.server.api.commands.optaplanner.GetSolverWithBestSolutionCommand;
import org.kie.server.api.commands.optaplanner.GetSolversCommand;
import org.kie.server.api.commands.optaplanner.IsEveryProblemFactChangeProcessedCommand;
import org.kie.server.api.commands.optaplanner.SolvePlanningProblemCommand;
import org.kie.server.api.commands.optaplanner.TerminateSolverEarlyCommand;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.marshalling.ModelWrapper;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.locator.ContainerLocatorProvider;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptaplannerCommandServiceImpl
        implements KieContainerCommandService<String> {

    private static final Logger logger = LoggerFactory.getLogger(OptaplannerCommandServiceImpl.class);

    private KieServerRegistry context;

    private SolverServiceBase solverService;

    private MarshallerHelper marshallerHelper;

    public OptaplannerCommandServiceImpl(
            KieServerRegistry context,
            SolverServiceBase solverService) {

        this.context = context;
        this.solverService = solverService;
        this.marshallerHelper = new MarshallerHelper(solverService.getKieServerRegistry());
    }

    @Override
    public ServiceResponse<String> callContainer(String containerId,
                                                 String payload,
                                                 MarshallingFormat marshallingFormat,
                                                 String classType) {
        return null;
    }

    @Override
    public ServiceResponsesList executeScript(CommandScript commands,
                                              MarshallingFormat marshallingFormat,
                                              String classType) {
        List<ServiceResponse<? extends Object>> responses = new ArrayList<ServiceResponse<? extends Object>>();

        for (KieServerCommand command : commands.getCommands()) {
            try {
                ServiceResponse<?> response = null;
                logger.debug("About to execute command: {}",
                             command);
                if (command instanceof CreateSolverCommand) {
                    CreateSolverCommand createSolverCommand = (CreateSolverCommand) command;
                    String containerId = context.getContainerId(createSolverCommand.getContainerId(),
                                                                ContainerLocatorProvider.get().getLocator());
                    SolverInstance instance = new SolverInstance();
                    instance.setContainerId(containerId);
                    instance.setSolverId(createSolverCommand.getSolverId());
                    instance.setSolverConfigFile(createSolverCommand.getSolverConfigFile());
                    response = solverService.createSolver(containerId,
                                                          createSolverCommand.getSolverId(),
                                                          instance);
                } else if (command instanceof GetSolversCommand) {
                    GetSolversCommand getSolversCommand = (GetSolversCommand) command;
                    String containerId = context.getContainerId(getSolversCommand.getContainerId(),
                                                                ContainerLocatorProvider.get().getLocator());
                    response = solverService.getSolvers(containerId);
                } else if (command instanceof GetSolverCommand) {
                    GetSolverCommand getSolverCommand = (GetSolverCommand) command;
                    String containerId = context.getContainerId(getSolverCommand.getContainerId(),
                                                                ContainerLocatorProvider.get().getLocator());
                    response = solverService.getSolver(containerId,
                                                       getSolverCommand.getSolverId());
                } else if (command instanceof GetSolverWithBestSolutionCommand) {
                    GetSolverWithBestSolutionCommand getSolverWithBestSolutionCommand
                            = (GetSolverWithBestSolutionCommand) command;
                    String containerId = context.getContainerId(getSolverWithBestSolutionCommand.getContainerId(),
                                                                ContainerLocatorProvider.get().getLocator());
                    response = solverService.getSolverWithBestSolution(containerId,
                                                                       getSolverWithBestSolutionCommand.getSolverId());
                } else if (command instanceof SolvePlanningProblemCommand) {
                    SolvePlanningProblemCommand solvePlanningProblemCommand = (SolvePlanningProblemCommand) command;
                    String containerId = context.getContainerId(solvePlanningProblemCommand.getContainerId(),
                                                                ContainerLocatorProvider.get().getLocator());
                    KieContainerInstanceImpl kc = context.getContainer(containerId);
                    Marshaller marshaller = kc.getMarshaller(marshallingFormat);
                    Object planningProblem = marshaller.unmarshall(solvePlanningProblemCommand.getPlanningProblem(),
                                                                   Object.class);
                    response = solverService.solvePlanningProblem(containerId,
                                                                  solvePlanningProblemCommand.getSolverId(),
                                                                  planningProblem);
                } else if (command instanceof TerminateSolverEarlyCommand) {
                    TerminateSolverEarlyCommand terminateSolverEarlyCommand = (TerminateSolverEarlyCommand) command;
                    String containerId = context.getContainerId(terminateSolverEarlyCommand.getContainerId(),
                                                                ContainerLocatorProvider.get().getLocator());
                    response = solverService.terminateSolverEarly(containerId,
                                                                  terminateSolverEarlyCommand.getSolverId());
                } else if (command instanceof AddProblemFactChangeCommand) {
                    AddProblemFactChangeCommand addProblemFactChangeCommand = (AddProblemFactChangeCommand) command;
                    String containerId = context.getContainerId(addProblemFactChangeCommand.getContainerId(),
                                                                ContainerLocatorProvider.get().getLocator());
                    response = solverService.addProblemFactChanges(containerId,
                                                                   addProblemFactChangeCommand.getSolverId(),
                                                                   addProblemFactChangeCommand.getProblemFactChange());
                } else if (command instanceof AddProblemFactChangesCommand) {
                    AddProblemFactChangesCommand addProblemFactChangesCommand = (AddProblemFactChangesCommand) command;
                    String containerId = context.getContainerId(addProblemFactChangesCommand.getContainerId(),
                                                                ContainerLocatorProvider.get().getLocator());
                    response = solverService.addProblemFactChanges(containerId,
                                                                   addProblemFactChangesCommand.getSolverId(),
                                                                   addProblemFactChangesCommand.getProblemFactChanges());
                } else if (command instanceof IsEveryProblemFactChangeProcessedCommand) {
                    IsEveryProblemFactChangeProcessedCommand isEveryProblemFactChangeProcessedCommand
                            = (IsEveryProblemFactChangeProcessedCommand) command;
                    String containerId = context
                            .getContainerId(isEveryProblemFactChangeProcessedCommand.getContainerId(),
                                            ContainerLocatorProvider.get().getLocator());
                    ServiceResponse<Boolean> everyProblemFactChangeProcessedResponse = solverService
                            .isEveryProblemFactChangeProcessed(containerId,
                                                               isEveryProblemFactChangeProcessedCommand.getSolverId());
                    if (marshallingFormat.equals(MarshallingFormat.JAXB)) {
                        Object wrappedResult = ModelWrapper.wrap(everyProblemFactChangeProcessedResponse.getResult());
                        response = new ServiceResponse<>(everyProblemFactChangeProcessedResponse.getType(),
                                                         everyProblemFactChangeProcessedResponse.getMsg(),
                                                         wrappedResult);
                    } else {
                        response = everyProblemFactChangeProcessedResponse;
                    }
                } else if (command instanceof DisposeSolverCommand) {
                    DisposeSolverCommand disposeSolverCommand = (DisposeSolverCommand) command;
                    String containerId = context.getContainerId(disposeSolverCommand.getContainerId(),
                                                                ContainerLocatorProvider.get().getLocator());
                    response = solverService.disposeSolver(containerId,
                                                           disposeSolverCommand.getSolverId());
                } else {
                    throw new IllegalStateException("Unsupported command: " + command);
                }

                logger.debug("Service returned response {}",
                             response);

                // return successful result
                responses.add(response);
            } catch (Throwable e) {
                logger.error("Error while processing {} command",
                             command,
                             e);
                // return failure result
                responses.add(new ServiceResponse<>(ServiceResponse.ResponseType.FAILURE,
                                                    e.getMessage()));
            }
        }
        logger.debug("About to return responses '{}'",
                     responses);
        return new ServiceResponsesList(responses);
    }
}
