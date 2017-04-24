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

package org.kie.server.remote.rest.optaplanner;

import java.text.MessageFormat;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.api.rest.RestURI;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.remote.rest.optaplanner.resources.Messages;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.kie.server.services.optaplanner.SolverServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.SOLVER_BEST_SOLUTION;
import static org.kie.server.api.rest.RestURI.SOLVER_ID;
import static org.kie.server.api.rest.RestURI.SOLVER_ID_URI;
import static org.kie.server.api.rest.RestURI.SOLVER_STATE_RUNNING;
import static org.kie.server.api.rest.RestURI.SOLVER_STATE_TERMINATING;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.createResponse;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;

@Path("server/" + RestURI.SOLVER_URI)
public class SolverResource {

    public static final Logger logger = LoggerFactory.getLogger(SolverResource.class);

    private SolverServiceBase solverService;
    private MarshallerHelper marshallerHelper;

    public SolverResource() {
    }

    public SolverResource(SolverServiceBase solverServiceBase) {
        this.solverService = solverServiceBase;
        this.marshallerHelper = new MarshallerHelper(solverService.getKieServerRegistry());
    }

    @PUT
    @Path(RestURI.SOLVER_ID_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createSolver(
            @javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId,
            @PathParam(SOLVER_ID) String solverId,
            String payload) {
        logger.debug("About to create solver {} on container {}",
                     solverId,
                     containerId);
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId,
                                                                solverService.getKieServerRegistry(),
                                                                headers);
        try {
            String contentType = getContentType(headers);

            if (solverService.getKieServerRegistry().getContainer(containerId) == null) {
                ServiceResponse<SolverInstance> response = new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.FAILURE,
                                                                                               "Failed to create solver. Container does not exist: " + containerId);
                return createCorrectVariant(response,
                                            headers,
                                            Response.Status.BAD_REQUEST);
            }
            SolverInstance solverInstance = marshallerHelper.unmarshal(containerId,
                                                                       payload,
                                                                       contentType,
                                                                       SolverInstance.class);
            ServiceResponse<SolverInstance> response = solverService.createSolver(containerId,
                                                                                  solverId,
                                                                                  solverInstance);
            if (response.getType() == ServiceResponse.ResponseType.SUCCESS) {
                return createCorrectVariant(marshallerHelper,
                                            containerId,
                                            response.getResult(),
                                            headers,
                                            Response.Status.CREATED,
                                            conversationIdHeader);
            }
            return createCorrectVariant(marshallerHelper,
                                        containerId,
                                        response.getMsg(),
                                        headers,
                                        Response.Status.BAD_REQUEST,
                                        conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error creating solver '{}' on container '{}': {}",
                         solverId,
                         containerId,
                         e.getMessage(),
                         e);
            return internalServerError(MessageFormat.format(Messages.UNEXPECTED_ERROR,
                                                            e.getMessage()),
                                       v,
                                       conversationIdHeader);
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getSolvers(@javax.ws.rs.core.Context HttpHeaders headers,
                               @PathParam(CONTAINER_ID) String containerId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId,
                                                                solverService.getKieServerRegistry(),
                                                                headers);
        try {
            ServiceResponse<SolverInstanceList> result = solverService.getSolvers(containerId);
            if (result.getType() == ServiceResponse.ResponseType.SUCCESS) {
                return createCorrectVariant(marshallerHelper,
                                            containerId,
                                            result.getResult(),
                                            headers,
                                            Response.Status.OK,
                                            conversationIdHeader);
            }
            return createCorrectVariant(marshallerHelper,
                                        containerId,
                                        result.getMsg(),
                                        headers,
                                        Response.Status.NOT_FOUND,
                                        conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving solvers. Message: '{}'",
                         e.getMessage(),
                         e);
            return internalServerError(MessageFormat.format(Messages.UNEXPECTED_ERROR,
                                                            e.getMessage()),
                                       v,
                                       conversationIdHeader);
        }
    }

    @GET
    @Path(RestURI.SOLVER_ID_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getSolver(@javax.ws.rs.core.Context HttpHeaders headers,
                              @PathParam(CONTAINER_ID) String containerId,
                              @PathParam(SOLVER_ID) String solverId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId,
                                                                solverService.getKieServerRegistry(),
                                                                headers);
        try {
            ServiceResponse<SolverInstance> result = solverService.getSolver(containerId,
                                                                             solverId);
            if (result.getType() == ServiceResponse.ResponseType.SUCCESS) {
                return createCorrectVariant(marshallerHelper,
                                            containerId,
                                            result.getResult(),
                                            headers,
                                            Response.Status.OK,
                                            conversationIdHeader);
            }
            return createCorrectVariant(marshallerHelper,
                                        containerId,
                                        result.getMsg(),
                                        headers,
                                        Response.Status.NOT_FOUND,
                                        conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving solver state {}",
                         e.getMessage(),
                         e);
            return internalServerError(MessageFormat.format(Messages.UNEXPECTED_ERROR,
                                                            e.getMessage()),
                                       v,
                                       conversationIdHeader);
        }
    }

    @GET
    @Path(SOLVER_ID_URI + "/" + SOLVER_BEST_SOLUTION)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getSolverWithBestSolution(@javax.ws.rs.core.Context HttpHeaders headers,
                                              @PathParam(CONTAINER_ID) String containerId,
                                              @PathParam(SOLVER_ID) String solverId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId,
                                                                solverService.getKieServerRegistry(),
                                                                headers);
        try {
            ServiceResponse<SolverInstance> result = solverService.getSolverWithBestSolution(containerId,
                                                                                             solverId);
            if (result.getType() == ServiceResponse.ResponseType.SUCCESS) {
                return createCorrectVariant(marshallerHelper,
                                            containerId,
                                            result.getResult(),
                                            headers,
                                            Response.Status.OK,
                                            conversationIdHeader);
            }
            return createCorrectVariant(marshallerHelper,
                                        containerId,
                                        result.getMsg(),
                                        headers,
                                        Response.Status.NOT_FOUND,
                                        conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}",
                         e.getMessage(),
                         e);
            return internalServerError(MessageFormat.format(Messages.UNEXPECTED_ERROR,
                                                            e.getMessage()),
                                       v,
                                       conversationIdHeader);
        }
    }

    @POST
    @Path(RestURI.SOLVER_ID_URI + "/" + SOLVER_STATE_RUNNING)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response solvePlanningProblem(@javax.ws.rs.core.Context HttpHeaders headers,
                                         @PathParam(CONTAINER_ID) String containerId,
                                         @PathParam(SOLVER_ID) String solverId,
                                         String payload) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId,
                                                                solverService.getKieServerRegistry(),
                                                                headers);
        try {
            String contentType = getContentType(headers);

            Object planningProblem = marshallerHelper.unmarshal(containerId,
                                                                payload,
                                                                contentType,
                                                                Object.class);

            ServiceResponse<Void> result = solverService.solvePlanningProblem(containerId,
                                                                              solverId,
                                                                              planningProblem);
            if (result.getType() == ServiceResponse.ResponseType.SUCCESS) {
                return createResponse("",
                                      v,
                                      Response.Status.OK,
                                      conversationIdHeader);
            }
            return createCorrectVariant(marshallerHelper,
                                        containerId,
                                        result.getMsg(),
                                        headers,
                                        Response.Status.BAD_REQUEST,
                                        conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving solver state {}",
                         e.getMessage(),
                         e);
            return internalServerError(MessageFormat.format(Messages.UNEXPECTED_ERROR,
                                                            e.getMessage()),
                                       v,
                                       conversationIdHeader);
        }
    }

    @POST
    @Path(RestURI.SOLVER_ID_URI + "/" + SOLVER_STATE_TERMINATING)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response terminateSolverEarly(@javax.ws.rs.core.Context HttpHeaders headers,
                                         @PathParam(CONTAINER_ID) String containerId,
                                         @PathParam(SOLVER_ID) String solverId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId,
                                                                solverService.getKieServerRegistry(),
                                                                headers);
        try {
            ServiceResponse<Void> result = solverService.terminateSolverEarly(containerId,
                                                                              solverId);
            if (result.getType() == ServiceResponse.ResponseType.SUCCESS) {
                return createResponse("",
                                      v,
                                      Response.Status.OK,
                                      conversationIdHeader);
            }
            return createCorrectVariant(marshallerHelper,
                                        containerId,
                                        result.getMsg(),
                                        headers,
                                        Response.Status.BAD_REQUEST,
                                        conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving solver state {}",
                         e.getMessage(),
                         e);
            return internalServerError(MessageFormat.format(Messages.UNEXPECTED_ERROR,
                                                            e.getMessage()),
                                       v,
                                       conversationIdHeader);
        }
    }

    @DELETE
    @Path(RestURI.SOLVER_ID_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response disposeSolver(@javax.ws.rs.core.Context HttpHeaders headers,
                                  @PathParam(CONTAINER_ID) String containerId,
                                  @PathParam(SOLVER_ID) String solverId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId,
                                                                solverService.getKieServerRegistry(),
                                                                headers);
        try {
            ServiceResponse<Void> result = solverService.disposeSolver(containerId,
                                                                       solverId);
            if (result.getType() == ServiceResponse.ResponseType.SUCCESS) {
                return createResponse("",
                                      v,
                                      Response.Status.OK,
                                      conversationIdHeader);
            }
            return createCorrectVariant(marshallerHelper,
                                        containerId,
                                        result.getMsg(),
                                        headers,
                                        Response.Status.BAD_REQUEST,
                                        conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error disposing solver {} on container {}. Message: '{}'",
                         solverId,
                         containerId,
                         e.getMessage(),
                         e);
            return internalServerError(MessageFormat.format(Messages.UNEXPECTED_ERROR,
                                                            e.getMessage()),
                                       v,
                                       conversationIdHeader);
        }
    }
}
