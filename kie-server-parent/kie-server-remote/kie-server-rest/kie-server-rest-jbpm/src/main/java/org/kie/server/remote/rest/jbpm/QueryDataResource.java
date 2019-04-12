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
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;
import static org.kie.server.remote.rest.common.util.RestUtils.noContent;
import static org.kie.server.remote.rest.common.util.RestUtils.notFound;
import static org.kie.server.remote.rest.common.util.RestUtils.errorMessage;
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

import java.text.MessageFormat;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dashbuilder.dataset.exception.DataSetLookupException;
import org.jbpm.services.api.query.QueryAlreadyRegisteredException;
import org.jbpm.services.api.query.QueryNotFoundException;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryDefinitionList;
import org.kie.server.common.rest.HttpStatusCodeException;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.QueryDataServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;

@Api(value="Custom queries")
@Path("server/" + QUERY_DEF_URI)
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

    @ApiOperation(value="Returns all custom query definitions.",
            response=QueryDefinitionList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=QUERY_DEF_LIST_RESPONSE_JSON)})) })
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getQueries( @Context HttpHeaders headers,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize ) {
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

    @ApiOperation(value="Registers a custom query definition.",
            response=QueryDefinition.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 409, message = "Query with given name already exists"), 
            @ApiResponse(code = 201, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=QUERY_DEF_RESPONSE_JSON)}))})
    @POST
    @Path(CREATE_QUERY_DEF_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createQueryDefinition( @javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "identifier of the query definition to be registered", required = true, example = "customQuery") @PathParam("queryName") String queryName,
            @ApiParam(value = "query definition represented as QueryDefinition", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=QUERY_DEF_JSON),
                    @ExampleProperty(mediaType=XML, value=QUERY_DEF_XML)})) String payload ) {
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

    @ApiOperation(value="Replaces existing custom query definition or registers it as new if the query does not already exist.",
            response=QueryDefinition.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 201, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=QUERY_DEF_RESPONSE_JSON)}))})
    @PUT
    @Path(REPLACE_QUERY_DEF_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response replaceQueryDefinition( @javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "identifier of the query definition to be replaced", required = true, example = "customQuery") @PathParam("queryName") String queryName,
            @ApiParam(value = "query definition represented as QueryDefinition", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=QUERY_DEF_JSON),
                    @ExampleProperty(mediaType=XML, value=QUERY_DEF_XML)})) String payload ) {
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

    @ApiOperation(value="Deletes a specified custom query.",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Query definition with given name not found")})
    @DELETE
    @Path(DROP_QUERY_DEF_DELETE_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response dropQueryDefinition( @javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "identifier of the query definition to be deleted", required = true, example = "customQuery") @PathParam("queryName") String queryName ) {
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

    @ApiOperation(value="Returns information about a specified custom query.",
            response=QueryDefinition.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Query definition with given name not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=QUERY_DEF_RESPONSE_JSON)}))})
    @GET
    @Path(QUERY_DEF_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getQuery( @Context HttpHeaders headers,
            @ApiParam(value = "identifier of the query definition to be retrieved", required = true, example = "customQuery") @PathParam("queryName") String queryName ) {
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

    @ApiOperation(value="Returns the results of a specified custom query.",
            response=Object.class, responseContainer="List", code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Query definition with given name not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCES_RESPONSE_JSON)}))})
    @GET
    @Path(RUN_QUERY_DEF_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response runQuery( @Context HttpHeaders headers,
            @ApiParam(value = "identifier of the query definition to be used for query", required = true, example = "customQuery") @PathParam("queryName") String queryName,
            @ApiParam(value = "identifier of the query mapper to be used when transforming results", required = true) @QueryParam("mapper") String mapper,
            @ApiParam(value = "optional sort order", required = false) @QueryParam("orderBy") String orderBy,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize ) {

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

    @ApiOperation(value="Returns the results of a specified custom query and filters the results based on a provided builder or filter request body.",
            response=Object.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 400, message = "Query parameters or filter spec provide invalid conditions"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCES_RESPONSE_JSON)}))})
    @POST
    @Path(RUN_FILTERED_QUERY_DEF_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response runQueryFiltered( @Context HttpHeaders headers,
            @ApiParam(value = "identifier of the query definition to be used for query", required = true, example = "customQuery") @PathParam("queryName") String queryName,
            @ApiParam(value = "identifier of the query mapper to be used when transforming results", required = true) @QueryParam("mapper") String mapper,
            @ApiParam(value = "optional identifier of the query builder to be used for query conditions", required = false)  @QueryParam("builder") String builder,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional query filter specification represented as QueryFilterSpec", required = false, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=QUERY_FILTER_SPEC_JSON),
                    @ExampleProperty(mediaType=XML, value=QUERY_FILTER_SPEC_XML)})) String payload ) {
        
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

    @ApiOperation(value="Returns the results of a specified custom query on a specified KIE container and filters the results based on a provided builder or filter request body.",
            response=Object.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 400, message = "Query parameters or filter spec provide invalid conditions"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCES_RESPONSE_JSON)}))})
    @POST
    @Path(RUN_FILTERED_QUERY_DEF_BY_CONTAINER_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response runQueryFilteredByDeploymentId( @Context HttpHeaders headers,
                                                    @ApiParam(value = "container id to filter queries", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
                                                    @ApiParam(value = "identifier of the query definition to be used for query", required = true, example = "customQuery") @PathParam("queryName") String queryName,
                                                    @ApiParam(value = "identifier of the query mapper to be used when transforming results", required = true) @QueryParam("mapper") String mapper,
                                                    @ApiParam(value = "optional identifier of the query builder to be used for query conditions", required = false)  @QueryParam("builder") String builder,
                                                    @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
                                                    @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
                                                    @ApiParam(value = "optional query filter specification represented as QueryFilterSpec", required = false, examples=@Example(value= {
                                                            @ExampleProperty(mediaType=JSON, value=QUERY_FILTER_SPEC_JSON),
                                                            @ExampleProperty(mediaType=XML, value=QUERY_FILTER_SPEC_XML)})) String payload ) {

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
