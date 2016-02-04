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

import org.kie.api.command.Command;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.api.rest.RestURI;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.client.SolverServicesClient;
import org.optaplanner.core.api.domain.solution.Solution;

import java.util.Collections;

public class SolverServicesClientImpl
        extends AbstractKieServicesClientImpl implements SolverServicesClient {

    public SolverServicesClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public SolverServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
    }

    @Override
    public ServiceResponse<SolverInstanceList> getSolvers(String containerId) {
        checkMandatoryParameter( "ContainerID", containerId );
        if( config.isRest() ) {
            String uri = getURI( containerId );
            return makeHttpGetRequestAndCreateServiceResponse( uri, SolverInstanceList.class );
        } else {
            //            CommandScript script = new CommandScript( Collections.singletonList((KieServerCommand) new CallContainerCommand(id, payload)) );
            //            return (ServiceResponse<String>) executeJmsCommand( script ).getResponses().get( 0 );
            throw new UnsupportedOperationException( "JMS is not supported at the moment." );
        }
    }

    @Override
    public ServiceResponse<SolverInstance> createSolver(String containerId, String solverId, SolverInstance instance) {
        checkMandatoryParameter( "ContainerID", containerId );
        checkMandatoryParameter( "SolverId", solverId );
        if( config.isRest() ) {
            String uri = getURI( containerId, solverId );
            return makeHttpPutRequestAndCreateServiceResponse( uri, instance, SolverInstance.class );
        } else {
//            CommandScript script = new CommandScript( Collections.singletonList((KieServerCommand) new CallContainerCommand(id, payload)) );
//            return (ServiceResponse<String>) executeJmsCommand( script ).getResponses().get( 0 );
            throw new UnsupportedOperationException( "JMS is not supported at the moment." );
        }
    }

    @Override
    public ServiceResponse<SolverInstance> getSolverState(String containerId, String solverId) {
        checkMandatoryParameter( "ContainerID", containerId );
        checkMandatoryParameter( "SolverId", solverId );
        if( config.isRest() ) {
            String uri = getURI( containerId, solverId );
            return makeHttpGetRequestAndCreateServiceResponse( uri, SolverInstance.class );
        } else {
            //            CommandScript script = new CommandScript( Collections.singletonList((KieServerCommand) new CallContainerCommand(id, payload)) );
            //            return (ServiceResponse<String>) executeJmsCommand( script ).getResponses().get( 0 );
            throw new UnsupportedOperationException( "JMS is not supported at the moment." );
        }
    }

    @Override
    public ServiceResponse<SolverInstance> getSolverBestSolution(String containerId, String solverId) {
        checkMandatoryParameter( "ContainerID", containerId );
        checkMandatoryParameter( "SolverId", solverId );
        if( config.isRest() ) {
            String uri = getURI( containerId, solverId ) + RestURI.SOLVER_BEST_SOLUTION;
            return makeHttpGetRequestAndCreateServiceResponse( uri, SolverInstance.class );
        } else {
            //            CommandScript script = new CommandScript( Collections.singletonList((KieServerCommand) new CallContainerCommand(id, payload)) );
            //            return (ServiceResponse<String>) executeJmsCommand( script ).getResponses().get( 0 );
            throw new UnsupportedOperationException( "JMS is not supported at the moment." );
        }
    }

    @Override
    public ServiceResponse<SolverInstance> updateSolverState(String containerId, String solverId, SolverInstance instance) {
        checkMandatoryParameter( "ContainerID", containerId );
        checkMandatoryParameter( "SolverId", solverId );
        if( config.isRest() ) {
            String uri = getURI( containerId, solverId );
            return makeHttpPostRequestAndCreateServiceResponse( uri, instance, SolverInstance.class, getHeaders( instance ) );
        } else {
            //            CommandScript script = new CommandScript( Collections.singletonList((KieServerCommand) new CallContainerCommand(id, payload)) );
            //            return (ServiceResponse<String>) executeJmsCommand( script ).getResponses().get( 0 );
            throw new UnsupportedOperationException( "JMS is not supported at the moment." );
        }
    }

    @Override
    public ServiceResponse<Void> disposeSolver(String containerId, String solverId) {
        checkMandatoryParameter( "ContainerID", containerId );
        checkMandatoryParameter( "SolverId", solverId );
        if( config.isRest() ) {
            String uri = getURI( containerId, solverId );
            return makeHttpDeleteRequestAndCreateServiceResponse( uri, Void.class );
        } else {
            //            CommandScript script = new CommandScript( Collections.singletonList((KieServerCommand) new CallContainerCommand(id, payload)) );
            //            return (ServiceResponse<String>) executeJmsCommand( script ).getResponses().get( 0 );
            throw new UnsupportedOperationException( "JMS is not supported at the moment." );
        }
    }

    private String getURI(String containerId) {
        return (baseURI + "/" + RestURI.SOLVER_URI ).replace( "{"+RestURI.CONTAINER_ID+"}", containerId );
    }

    private String getURI(String containerId, String solverId) {
        return (baseURI + "/" + RestURI.SOLVER_URI + RestURI.SOLVER_ID_URI).replace( "{"+RestURI.CONTAINER_ID+"}", containerId ).replace( "{"+RestURI.SOLVER_ID+"}", solverId );
    }

    private void checkMandatoryParameter(String parameterName, String parameter ) {
        if ( parameter == null || parameter.isEmpty() ) {
            throw new IllegalArgumentException( parameterName + " can not be null or empty." );
        }
    }

}
