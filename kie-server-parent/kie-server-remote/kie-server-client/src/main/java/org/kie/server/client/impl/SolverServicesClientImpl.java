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

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.optaplanner.*;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.api.rest.RestURI;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesException;
import org.kie.server.client.SolverServicesClient;

import java.util.Collections;

public class SolverServicesClientImpl
        extends AbstractKieServicesClientImpl
        implements SolverServicesClient {

    public SolverServicesClientImpl(KieServicesConfiguration config) {
        super( config );
    }

    public SolverServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super( config, classLoader );
    }

    @Override
    public ServiceResponse<SolverInstanceList> getSolvers(String containerId) {
        checkMandatoryParameter( "ContainerID", containerId );
        if ( config.isRest() ) {
            String uri = getURI( containerId );
            return makeHttpGetRequestAndCreateServiceResponse( uri, SolverInstanceList.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new GetSolversCommand( containerId ) ) );
            ServiceResponse<SolverInstanceList> response = (ServiceResponse<SolverInstanceList>) executeJmsCommand( script, GetSolversCommand.class.getName(), KieServerConstants.CAPABILITY_BRP, containerId ).getResponses().get( 0 );

            throwExceptionOnFailure( response );
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response;
        }
    }

    @Override
    public ServiceResponse<SolverInstance> createSolver(String containerId, String solverId, String configFile) {
        checkMandatoryParameter( "ContainerId", containerId );
        checkMandatoryParameter( "SolverId", solverId );
        checkMandatoryParameter( "ConfigFile", configFile );
        if ( config.isRest() ) {
            String uri = getURI( containerId, solverId );
            SolverInstance instance = new SolverInstance();
            instance.setSolverConfigFile( configFile );
            instance.setContainerId( containerId );
            instance.setSolverId( solverId );
            return makeHttpPutRequestAndCreateServiceResponse( uri, instance, SolverInstance.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new CreateSolverCommand( containerId, solverId, configFile ) ) );
            ServiceResponse<SolverInstance> response = (ServiceResponse<SolverInstance>) executeJmsCommand( script, CreateSolverCommand.class.getName(), KieServerConstants.CAPABILITY_BRP, containerId ).getResponses().get( 0 );
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response;
        }
    }

    @Override
    public ServiceResponse<SolverInstance> getSolverState(String containerId, String solverId) {
        checkMandatoryParameter( "ContainerID", containerId );
        checkMandatoryParameter( "SolverId", solverId );
        if ( config.isRest() ) {
            String uri = getURI( containerId, solverId );
            return makeHttpGetRequestAndCreateServiceResponse( uri, SolverInstance.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new GetSolverStateCommand( containerId, solverId ) ) );
            ServiceResponse<SolverInstance> response = (ServiceResponse<SolverInstance>) executeJmsCommand( script, GetSolverStateCommand.class.getName(), KieServerConstants.CAPABILITY_BRP, containerId ).getResponses().get( 0 );

            throwExceptionOnFailure( response );
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response;
        }
    }

    @Override
    public ServiceResponse<SolverInstance> getSolverBestSolution(String containerId, String solverId) {
        checkMandatoryParameter( "ContainerID", containerId );
        checkMandatoryParameter( "SolverId", solverId );
        if ( config.isRest() ) {
            String uri = getURI( containerId, solverId ) + RestURI.SOLVER_BEST_SOLUTION;
            return makeHttpGetRequestAndCreateServiceResponse( uri, SolverInstance.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new GetBestSolutionCommand( containerId, solverId ) ) );
            ServiceResponse<SolverInstance> response = (ServiceResponse<SolverInstance>) executeJmsCommand( script, GetBestSolutionCommand.class.getName(), KieServerConstants.CAPABILITY_BRP, containerId ).getResponses().get( 0 );

            throwExceptionOnFailure( response );
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response;
        }
    }

    @Override
    public ServiceResponse<SolverInstance> updateSolverState(String containerId, String solverId, SolverInstance instance) {
        checkMandatoryParameter( "ContainerID", containerId );
        checkMandatoryParameter( "SolverId", solverId );
        checkMandatoryParameter( "instance", instance );
        if ( config.isRest() ) {
            String uri = getURI( containerId, solverId );
            return makeHttpPostRequestAndCreateServiceResponse( uri, instance, SolverInstance.class, getHeaders( instance ) );
        } else {
            instance.setContainerId( containerId );
            instance.setSolverId( solverId );
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new UpdateSolverStateCommand( containerId, solverId, serialize( instance ) ) ) );
            ServiceResponse<SolverInstance> response = (ServiceResponse<SolverInstance>) executeJmsCommand( script, UpdateSolverStateCommand.class.getName(), KieServerConstants.CAPABILITY_BRP, containerId ).getResponses().get( 0 );

            throwExceptionOnFailure( response );
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response;
        }
    }

    @Override
    public ServiceResponse<Void> disposeSolver(String containerId, String solverId) {
        checkMandatoryParameter( "ContainerID", containerId );
        checkMandatoryParameter( "SolverId", solverId );
        if ( config.isRest() ) {
            String uri = getURI( containerId, solverId );
            return makeHttpDeleteRequestAndCreateCustomResponse( uri, ServiceResponse.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DisposeSolverCommand( containerId, solverId ) ) );
            ServiceResponse<Void> response = (ServiceResponse<Void>) executeJmsCommand( script, DisposeSolverCommand.class.getName(), KieServerConstants.CAPABILITY_BRP, containerId ).getResponses().get( 0 );

            throwExceptionOnFailure( response );
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response;
        }
    }

    private String getURI(String containerId) {
        return (baseURI + "/" + RestURI.SOLVER_URI).replace( "{" + RestURI.CONTAINER_ID + "}", containerId );
    }

    private String getURI(String containerId, String solverId) {
        return (baseURI + "/" + RestURI.SOLVER_URI + RestURI.SOLVER_ID_URI).replace( "{" + RestURI.CONTAINER_ID + "}", containerId ).replace( "{" + RestURI.SOLVER_ID + "}", solverId );
    }

    private void checkMandatoryParameter(String parameterName, Object parameter) {
        if ( parameter == null || ((parameter instanceof String) && ((String) parameter).isEmpty()) ) {
            throw new IllegalArgumentException( parameterName + " can not be null or empty." );
        }
    }

    protected void throwExceptionOnFailure(ServiceResponse<?> serviceResponse) {
        if (serviceResponse != null && ServiceResponse.ResponseType.FAILURE.equals(serviceResponse.getType())){
            throw new KieServicesException(serviceResponse.getMsg());
        }
    }
}
