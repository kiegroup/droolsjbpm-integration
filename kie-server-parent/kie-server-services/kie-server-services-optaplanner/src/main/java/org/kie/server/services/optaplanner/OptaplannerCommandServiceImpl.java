/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.optaplanner;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.optaplanner.*;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OptaplannerCommandServiceImpl
        implements KieContainerCommandService {

    private static final Logger logger = LoggerFactory.getLogger( OptaplannerCommandServiceImpl.class );

    private KieServerRegistry context;

    private SolverServiceBase solverService;

    public OptaplannerCommandServiceImpl(
            KieServerRegistry context, SolverServiceBase solverService) {

        this.context = context;
        this.solverService = solverService;
    }

    @Override
    public ServiceResponse<String> callContainer(String containerId, String payload, MarshallingFormat marshallingFormat, String classType) {
        return null;
    }

    @Override
    public ServiceResponsesList executeScript(CommandScript commands, MarshallingFormat marshallingFormat, String classType) {
        List<ServiceResponse<? extends Object>> responses = new ArrayList<ServiceResponse<? extends Object>>();

        for ( KieServerCommand command : commands.getCommands() ) {
            try {
                ServiceResponse<?> response = null;
                logger.debug( "About to execute command: {}", command );
                if ( command instanceof CreateSolverCommand ) {
                    CreateSolverCommand csc = (CreateSolverCommand) command;
                    SolverInstance instance = new SolverInstance();
                    instance.setContainerId( csc.getContainerId() );
                    instance.setSolverId( csc.getSolverId() );
                    instance.setSolverConfigFile( csc.getSolverConfigFile() );
                    response = solverService.createSolver( csc.getContainerId(), csc.getSolverId(), instance );
                } else if (command instanceof GetSolversCommand ) {
                    GetSolversCommand gss = (GetSolversCommand) command;
                    response = solverService.getSolvers( gss.getContainerId() );
                } else if (command instanceof GetSolverStateCommand) {
                    GetSolverStateCommand gss = (GetSolverStateCommand) command;
                    response = solverService.getSolverState( gss.getContainerId(), gss.getSolverId() );
                } else if (command instanceof GetBestSolutionCommand ) {
                    GetBestSolutionCommand gss = (GetBestSolutionCommand) command;
                    response = solverService.getBestSolution( gss.getContainerId(), gss.getSolverId() );
                } else if (command instanceof UpdateSolverStateCommand ) {
                    UpdateSolverStateCommand uss = (UpdateSolverStateCommand) command;
                    KieContainerInstanceImpl kc = context.getContainer( uss.getContainerId() );
                    Marshaller marshaller = kc.getMarshaller( marshallingFormat );
                    SolverInstance si = marshaller.unmarshall( uss.getInstance(), SolverInstance.class );
                    response = solverService.updateSolverState( uss.getContainerId(), uss.getSolverId(), si );
                } else if (command instanceof DisposeSolverCommand ) {
                    DisposeSolverCommand ds = (DisposeSolverCommand) command;
                    response = solverService.disposeSolver( ds.getContainerId(), ds.getSolverId() );
                } else {
                    throw new IllegalStateException( "Unsupported command: " + command );
                }

                logger.debug( "Service returned response {}", response );

                // return successful result
                responses.add( response );
            } catch ( Throwable e ) {
                logger.error( "Error while processing {} command", command, e );
                // return failure result
                responses.add( new ServiceResponse( ServiceResponse.ResponseType.FAILURE, e.getMessage() ) );
            }
        }
        logger.debug( "About to return responses '{}'", responses );
        return new ServiceResponsesList( responses );
    }

}
