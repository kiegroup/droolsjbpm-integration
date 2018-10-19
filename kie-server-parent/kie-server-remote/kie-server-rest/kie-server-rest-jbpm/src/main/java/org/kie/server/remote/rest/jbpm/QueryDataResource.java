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
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.QUERY_DEF_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.QUERY_DEF_XML;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.QUERY_FILTER_SPEC_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.QUERY_FILTER_SPEC_XML;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.XML;
import static org.kie.server.remote.rest.jbpm.resources.Messages.BAD_REQUEST;
import static org.kie.server.remote.rest.jbpm.resources.Messages.QUERY_ALREADY_EXISTS;
import static org.kie.server.remote.rest.jbpm.resources.Messages.QUERY_NOT_FOUND;
import static org.kie.server.remote.rest.jbpm.resources.Messages.UNEXPECTED_ERROR;

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

@Api(value="Custom queries :: BPM")
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

    @ApiOperation(value="Retruns all custom queries defined in the system",
            response=QueryDefinitionList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error") })
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
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

    @ApiOperation(value="Registers new query definition in the system with given queryName",
            response=QueryDefinition.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 409, message = "Query with given name already exists")})
    @POST
    @Path(CREATE_QUERY_DEF_POST_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createQueryDefinition( @javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "identifier of the query definition to be registered", required = true) @PathParam("queryName") String queryName,
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
            return internalServerError( MessageFormat.format( UNEXPECTED_ERROR,
                                                              e.getMessage() ),
                                        v,
                                        conversationIdHeader );
        }
    }

    @ApiOperation(value="Replaces existing query definition or registers new if not exists in the system with given queryName",
            response=QueryDefinition.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @PUT
    @Path(REPLACE_QUERY_DEF_PUT_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response replaceQueryDefinition( @javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "identifier of the query definition to be replaced", required = true) @PathParam("queryName") String queryName,
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
            return internalServerError( MessageFormat.format( UNEXPECTED_ERROR,
                                                              e.getMessage() ),
                                        v,
                                        conversationIdHeader );
        }
    }

    @ApiOperation(value="Deletes existing query definition from the system with given queryName",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Query definition with given name not found")})
    @DELETE
    @Path(DROP_QUERY_DEF_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response dropQueryDefinition( @javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "identifier of the query definition to be deleted", required = true) @PathParam("queryName") String queryName ) {
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
            return internalServerError( MessageFormat.format( UNEXPECTED_ERROR,
                                                              e.getMessage() ),
                                        v,
                                        conversationIdHeader );
        }
    }

    @ApiOperation(value="Retrieves existing query definition from the system with given queryName",
            response=QueryDefinition.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Query definition with given name not found")})
    @GET
    @Path(QUERY_DEF_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getQuery( @Context HttpHeaders headers,
            @ApiParam(value = "identifier of the query definition to be retrieved", required = true) @PathParam("queryName") String queryName ) {
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
            return internalServerError( MessageFormat.format( UNEXPECTED_ERROR,
                                                              e.getMessage() ),
                                        v,
                                        conversationIdHeader );
        }
    }

    @ApiOperation(value="Queries using query definition identified by queryName. Maps the result to concrete objects based on provided mapper.",
            response=Object.class, responseContainer="List", code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Query definition with given name not found")})
    @GET
    @Path(RUN_QUERY_DEF_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response runQuery( @Context HttpHeaders headers,
            @ApiParam(value = "identifier of the query definition to be used for query", required = true) @PathParam("queryName") String queryName,
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

    @ApiOperation(value="Queries using query definition identified by queryName. Maps the result to concrete objects based on provided mapper. Query is additional altered by the filter spec and/or builder",
            response=Object.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 400, message = "Query parameters or filter spec provide invalid conditions")})
    @POST
    @Path(RUN_FILTERED_QUERY_DEF_POST_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response runQueryFiltered( @Context HttpHeaders headers,
            @ApiParam(value = "identifier of the query definition to be used for query", required = true) @PathParam("queryName") String queryName,
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
                logger.error( "{}",
                              MessageFormat.format( UNEXPECTED_ERROR,
                                                    e.getMessage() ),
                              e );

                return internalServerError( MessageFormat.format( UNEXPECTED_ERROR,
                                                                  e.getMessage() ),
                                            getVariant( headers ),
                                            conversationIdHeader );
            }
        }
    }

    @ApiOperation(value="Queries using query definition identified by queryName filtered by container. Maps the result to concrete objects based on provided mapper. Query is additional altered by the filter spec and/or builder",
            response=Object.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 400, message = "Query parameters or filter spec provide invalid conditions")})
    @POST
    @Path(RUN_FILTERED_QUERY_DEF_BY_CONTAINER_POST_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response runQueryFilteredByDeploymentId( @Context HttpHeaders headers,
                                                    @ApiParam(value = "container id to filter queries", required = true) @PathParam("id") String containerId,
                                                    @ApiParam(value = "identifier of the query definition to be used for query", required = true) @PathParam("queryName") String queryName,
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
                logger.error( "{}",
                              MessageFormat.format( UNEXPECTED_ERROR,
                                                    e.getMessage() ),
                              e );

                return internalServerError( MessageFormat.format( UNEXPECTED_ERROR,
                                                                  e.getMessage() ),
                                            getVariant( headers ),
                                            conversationIdHeader );
            }
        }
    }

}
