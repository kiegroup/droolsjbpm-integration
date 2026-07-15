/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.remote.rest.jbpm;

import java.text.MessageFormat;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dashbuilder.dataset.exception.DataSetLookupException;
import org.jbpm.services.api.query.QueryAlreadyRegisteredException;
import org.jbpm.services.api.query.QueryNotFoundException;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryDefinitionList;
import org.kie.server.common.rest.HttpStatusCodeException;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.remote.rest.common.marker.KieServerEndpoint;
import org.kie.server.remote.rest.common.marker.KieServerEndpoint.EndpointType;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.QueryDataServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.CREATE_QUERY_DEF_POST_URI;
import static org.kie.server.api.rest.RestURI.DROP_QUERY_DEF_DELETE_URI;
import static org.kie.server.api.rest.RestURI.QUERY_DEF_GET_URI;
import static org.kie.server.api.rest.RestURI.QUERY_DEF_URI;
import static org.kie.server.api.rest.RestURI.REPLACE_QUERY_DEF_PUT_URI;
import static org.kie.server.api.rest.RestURI.RUN_FILTERED_QUERY_DEF_BY_CONTAINER_POST_URI;
import static org.kie.server.api.rest.RestURI.RUN_FILTERED_QUERY_DEF_POST_URI;
import static org.kie.server.api.rest.RestURI.RUN_QUERY_DEF_GET_URI;
import static org.kie.server.remote.rest.common.util.RestUtils.alreadyExists;
import static org.kie.server.remote.rest.common.util.RestUtils.badRequest;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.errorMessage;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;
import static org.kie.server.remote.rest.common.util.RestUtils.noContent;
import static org.kie.server.remote.rest.common.util.RestUtils.notFound;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_PROCESS_INSTANCES_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.QUERY_DEF_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.QUERY_DEF_LIST_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.QUERY_DEF_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.QUERY_DEF_XML;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.QUERY_FILTER_SPEC_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.QUERY_FILTER_SPEC_XML;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.XML;
import static org.kie.server.remote.rest.jbpm.resources.Messages.BAD_REQUEST;
import static org.kie.server.remote.rest.jbpm.resources.Messages.QUERY_ALREADY_EXISTS;
import static org.kie.server.remote.rest.jbpm.resources.Messages.QUERY_NOT_FOUND;

@Tag(name="Custom queries")
@Path("server/" + QUERY_DEF_URI)
@KieServerEndpoint(categories = {EndpointType.DEFAULT, EndpointType.HISTORY})
public class QueryDataResource {

    public static final Logger logger = LoggerFactory.getLogger( QueryDataResource.class );

    private QueryDataServiceBase queryDataServiceBase;
    private KieServerRegistry context;

    public QueryDataResource() {

    }

    public QueryDataResource( QueryDataServiceBase delegate,
                              KieServerRegistry context ) {
        this.queryDataServiceBase = delegate;
        this.context = context;
    }

    @Operation(summary="Returns all custom query definitions.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"), 
            @ApiResponse(responseCode = "200", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = QUERY_DEF_LIST_RESPONSE_JSON)})
            }) })
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getQueries( @Context HttpHeaders headers,
            @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
            @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize ) {
        // no container id available so only used to transfer conversation id if
        // given by client
        Header conversationIdHeader = buildConversationIdHeader( "",
                                                                 context,
                                                                 headers );
        QueryDefinitionList result = queryDataServiceBase.getQueries( page,
                                                                      pageSize );
        logger.debug( "Returning result of get queries definition: {}",
                      result );

        return createCorrectVariant( result,
                                     headers,
                                     Response.Status.OK,
                                     conversationIdHeader );
    }

    @Operation(summary="Registers a custom query definition.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"),
            @ApiResponse(responseCode = "409", description = "Query with given name already exists"), 
            @ApiResponse(responseCode = "201", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = QUERY_DEF_RESPONSE_JSON)})
            })})
    @POST
    @Path(CREATE_QUERY_DEF_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createQueryDefinition( @jakarta.ws.rs.core.Context HttpHeaders headers,
            @Parameter(description = "identifier of the query definition to be registered", required = true, example = "customQuery") @PathParam("queryName") String queryName,
            @Parameter(description = "query definition represented as QueryDefinition", required = true, content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = QUERY_DEF_JSON)}),
                @Content(mediaType = XML, examples = {@ExampleObject(value = QUERY_DEF_XML)})
            }) String payload ) {
        Variant v = getVariant( headers );
        String type = getContentType( headers );
        // no container id available so only used to transfer conversation id if
        // given by client
        Header conversationIdHeader = buildConversationIdHeader( "",
                                                                 context,
                                                                 headers );
        try {
            QueryDefinition def = queryDataServiceBase.registerQuery( queryName,
                                                payload,
                                                type );

            logger.debug( "Returning CREATED response after registering query with name {}",
                          queryName );
            return createCorrectVariant( def,
                                         headers,
                                         Response.Status.CREATED,
                                         conversationIdHeader );
        } catch ( QueryAlreadyRegisteredException e ) {
            return alreadyExists( MessageFormat.format( QUERY_ALREADY_EXISTS,
                                                        queryName ),
                                  v,
                                  conversationIdHeader );
        } catch ( Exception e ) {
            logger.error( "Unexpected error during processing {}",
                          e.getMessage(),
                          e );
            return internalServerError( errorMessage(e),
                                        v,
                                        conversationIdHeader );
        }
    }

    @Operation(summary="Replaces existing custom query definition or registers it as new if the query does not already exist.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"), 
            @ApiResponse(responseCode = "201", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = QUERY_DEF_RESPONSE_JSON)})
            })})
    @PUT
    @Path(REPLACE_QUERY_DEF_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response replaceQueryDefinition( @jakarta.ws.rs.core.Context HttpHeaders headers,
            @Parameter(description = "identifier of the query definition to be replaced", required = true, example = "customQuery") @PathParam("queryName") String queryName,
            @Parameter(description = "query definition represented as QueryDefinition", required = true, content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = QUERY_DEF_JSON)}),
                @Content(mediaType = XML, examples = {@ExampleObject(value = QUERY_DEF_XML)})
            }) String payload ) {
        Variant v = getVariant( headers );
        String type = getContentType( headers );
        // no container id available so only used to transfer conversation id if
        // given by client
        Header conversationIdHeader = buildConversationIdHeader( "",
                                                                 context,
                                                                 headers );
        try {
            QueryDefinition def = queryDataServiceBase.replaceQuery( queryName,
                                               payload,
                                               type );
            logger.debug( "Returning CREATED response after registering query with name {}",
                          queryName );            
            return createCorrectVariant( def,
                                  headers,
                                  Response.Status.CREATED,
                                  conversationIdHeader );
        } catch ( Exception e ) {
            logger.error( "Unexpected error during processing {}",
                          e.getMessage(),
                          e );
            return internalServerError( errorMessage(e),
                                        v,
                                        conversationIdHeader );
        }
    }

    @Operation(summary="Deletes a specified custom query.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"),
            @ApiResponse(responseCode = "404", description = "Query definition with given name not found")})
    @DELETE
    @Path(DROP_QUERY_DEF_DELETE_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response dropQueryDefinition( @jakarta.ws.rs.core.Context HttpHeaders headers,
            @Parameter(description = "identifier of the query definition to be deleted", required = true, example = "customQuery") @PathParam("queryName") String queryName ) {
        Variant v = getVariant( headers );
        // no container id available so only used to transfer conversation id if
        // given by client
        Header conversationIdHeader = buildConversationIdHeader( "",
                                                                 context,
                                                                 headers );
        try {
            queryDataServiceBase.unregisterQuery( queryName );
            // produce 204 NO_CONTENT response code
            return noContent( v,
                              conversationIdHeader );
        } catch ( QueryNotFoundException e ) {
            return notFound( MessageFormat.format( QUERY_NOT_FOUND,
                                                   queryName ),
                             v,
                             conversationIdHeader );
        } catch ( Exception e ) {
            logger.error( "Unexpected error during processing {}",
                          e.getMessage(),
                          e );
            return internalServerError( errorMessage(e),
                                        v,
                                        conversationIdHeader );
        }
    }

    @Operation(summary="Returns information about a specified custom query.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"),
            @ApiResponse(responseCode = "404", description = "Query definition with given name not found"), 
            @ApiResponse(responseCode = "200", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = QUERY_DEF_RESPONSE_JSON)})
            })})
    @GET
    @Path(QUERY_DEF_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getQuery( @Context HttpHeaders headers,
            @Parameter(description = "identifier of the query definition to be retrieved", required = true, example = "customQuery") @PathParam("queryName") String queryName ) {
        Variant v = getVariant( headers );
        // no container id available so only used to transfer conversation id if
        // given by client
        Header conversationIdHeader = buildConversationIdHeader( "",
                                                                 context,
                                                                 headers );
        try {
            QueryDefinition queryDefinition = queryDataServiceBase.getQuery( queryName );

            return createCorrectVariant( queryDefinition,
                                         headers,
                                         Response.Status.OK,
                                         conversationIdHeader );
        } catch ( QueryNotFoundException e ) {
            return notFound( MessageFormat.format( QUERY_NOT_FOUND,
                                                   queryName ),
                             v,
                             conversationIdHeader );
        } catch ( Exception e ) {
            logger.error( "Unexpected error during processing {}",
                          e.getMessage(),
                          e );
            return internalServerError( errorMessage(e),
                                        v,
                                        conversationIdHeader );
        }
    }

    @Operation(summary="Returns the results of a specified custom query.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"),
            @ApiResponse(responseCode = "404", description = "Query definition with given name not found"), 
            @ApiResponse(responseCode = "200", responseContainer = "List", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = GET_PROCESS_INSTANCES_RESPONSE_JSON)})
            })})
    @GET
    @Path(RUN_QUERY_DEF_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response runQuery( @Context HttpHeaders headers,
            @Parameter(description = "identifier of the query definition to be used for query", required = true, example = "customQuery") @PathParam("queryName") String queryName,
            @Parameter(description = "identifier of the query mapper to be used when transforming results", required = true) @QueryParam("mapper") String mapper,
            @Parameter(description = "optional sort order", required = false) @QueryParam("orderBy") String orderBy,
            @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
            @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize ) {

        // no container id available so only used to transfer conversation id if
        // given by client
        Header conversationIdHeader = buildConversationIdHeader( "",
                                                                 context,
                                                                 headers );

        Object result = queryDataServiceBase.query( queryName,
                                                    mapper,
                                                    orderBy,
                                                    page,
                                                    pageSize );
        logger.debug( "Returning result of process instance search: {}",
                      result );

        return createCorrectVariant( result,
                                     headers,
                                     Response.Status.OK,
                                     conversationIdHeader );
    }

    @Operation(summary="Returns the results of a specified custom query and filters the results based on a provided builder or filter request body.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"),
            @ApiResponse(responseCode = "400", description = "Query parameters or filter spec provide invalid conditions"), 
            @ApiResponse(responseCode = "200", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = GET_PROCESS_INSTANCES_RESPONSE_JSON)})
            })})
    @POST
    @Path(RUN_FILTERED_QUERY_DEF_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response runQueryFiltered( @Context HttpHeaders headers,
            @Parameter(description = "identifier of the query definition to be used for query", required = true, example = "customQuery") @PathParam("queryName") String queryName,
            @Parameter(description = "identifier of the query mapper to be used when transforming results", required = true) @QueryParam("mapper") String mapper,
            @Parameter(description = "optional identifier of the query builder to be used for query conditions", required = false)  @QueryParam("builder") String builder,
            @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
            @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @Parameter(description = "optional query filter specification represented as QueryFilterSpec", required = false, content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = QUERY_FILTER_SPEC_JSON)}),
                @Content(mediaType = XML, examples = {@ExampleObject(value = QUERY_FILTER_SPEC_XML)})
            }) String payload ) {
        
        String type = getContentType( headers );
        // no container id available so only used to transfer conversation id if
        // given by client
        Header conversationIdHeader = buildConversationIdHeader( "",
                                                                 context,
                                                                 headers );
        Object result = null;

        try {
            if ( builder != null && !builder.isEmpty() ) {
                result = queryDataServiceBase.queryFilteredWithBuilder( queryName,
                                                                        mapper,
                                                                        builder,
                                                                        page,
                                                                        pageSize,
                                                                        payload,
                                                                        type );
            } else {
                result = queryDataServiceBase.queryFiltered( queryName,
                                                             mapper,
                                                             page,
                                                             pageSize,
                                                             payload,
                                                             type );
            }
            logger.debug( "Returning result of process instance search: {}",
                          result );

            return createCorrectVariant( result,
                                         headers,
                                         Response.Status.OK,
                                         conversationIdHeader );
        } catch ( Exception e ) {
            Throwable root = ExceptionUtils.getRootCause( e );
            if ( root == null ) {
                root = e;
            }
            if ( HttpStatusCodeException.BAD_REQUEST.contains( root.getClass() ) || e instanceof DataSetLookupException) {

                logger.error( "{}",
                              MessageFormat.format( BAD_REQUEST,
                                                    root.getMessage() ),
                              e );

                return badRequest( MessageFormat.format( BAD_REQUEST,
                                                         root.getMessage() ),
                                   getVariant( headers ),
                                   conversationIdHeader );
            } else {
                logger.error( "Unexpected error",
                              e );

                return internalServerError( errorMessage(e),
                                            getVariant( headers ),
                                            conversationIdHeader );
            }
        }
    }

    @Operation(summary="Returns the results of a specified custom query on a specified KIE container and filters the results based on a provided builder or filter request body.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"),
            @ApiResponse(responseCode = "400", description = "Query parameters or filter spec provide invalid conditions"), 
            @ApiResponse(responseCode = "200", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = GET_PROCESS_INSTANCES_RESPONSE_JSON)})
            })})
    @POST
    @Path(RUN_FILTERED_QUERY_DEF_BY_CONTAINER_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response runQueryFilteredByDeploymentId( @Context HttpHeaders headers,
                                                    @Parameter(description = "container id to filter queries", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
                                                    @Parameter(description = "identifier of the query definition to be used for query", required = true, example = "customQuery") @PathParam("queryName") String queryName,
                                                    @Parameter(description = "identifier of the query mapper to be used when transforming results", required = true) @QueryParam("mapper") String mapper,
                                                    @Parameter(description = "optional identifier of the query builder to be used for query conditions", required = false)  @QueryParam("builder") String builder,
                                                    @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
                                                    @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
                                                    @Parameter(description = "optional query filter specification represented as QueryFilterSpec", required = false, content = {
                                                        @Content(mediaType = JSON, examples = {@ExampleObject(value = QUERY_FILTER_SPEC_JSON)}),
                                                        @Content(mediaType = XML, examples = {@ExampleObject(value = QUERY_FILTER_SPEC_XML)})
                                                    }) String payload ) {

        String type = getContentType( headers );
        Header conversationIdHeader = buildConversationIdHeader( containerId,
                                                                 context,
                                                                 headers );
        Object result = null;

        try {
            if ( builder != null && !builder.isEmpty() ) {
                result = queryDataServiceBase.queryFilteredWithBuilder( containerId,
                                                                        queryName,
                                                                        mapper,
                                                                        builder,
                                                                        page,
                                                                        pageSize,
                                                                        payload,
                                                                        type );
            } else {
                result = queryDataServiceBase.queryFiltered( containerId,
                                                             queryName,
                                                             mapper,
                                                             page,
                                                             pageSize,
                                                             payload,
                                                             type );
            }
            logger.debug( "Returning result of process instance search: {}",
                          result );

            return createCorrectVariant( result,
                                         headers,
                                         Response.Status.OK,
                                         conversationIdHeader );
        } catch ( Exception e ) {
            Throwable root = ExceptionUtils.getRootCause( e );
            if ( root == null ) {
                root = e;
            }
            if ( HttpStatusCodeException.BAD_REQUEST.contains( root.getClass() ) || e instanceof DataSetLookupException) {

                logger.error( "{}",
                              MessageFormat.format( BAD_REQUEST,
                                                    root.getMessage() ),
                              e );

                return badRequest( MessageFormat.format( BAD_REQUEST,
                                                         root.getMessage() ),
                                   getVariant( headers ),
                                   conversationIdHeader );
            } else {
                logger.error( "Unexpected error",
                              e );

                return internalServerError( errorMessage(e),
                                            getVariant( headers ),
                                            conversationIdHeader );
            }
        }
    }

}
