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
import static org.kie.server.api.rest.RestURI.RUN_FILTERED_QUERY_DEF_POST_URI;
import static org.kie.server.api.rest.RestURI.RUN_QUERY_DEF_GET_URI;
import static org.kie.server.remote.rest.common.util.RestUtils.alreadyExists;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.createResponse;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;
import static org.kie.server.remote.rest.common.util.RestUtils.noContent;
import static org.kie.server.remote.rest.common.util.RestUtils.notFound;
import static org.kie.server.remote.rest.jbpm.resources.Messages.QUERY_ALREADY_EXISTS;
import static org.kie.server.remote.rest.jbpm.resources.Messages.QUERY_NOT_FOUND;
import static org.kie.server.remote.rest.jbpm.resources.Messages.UNEXPECTED_ERROR;

import java.text.MessageFormat;

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

import org.jbpm.services.api.query.QueryAlreadyRegisteredException;
import org.jbpm.services.api.query.QueryNotFoundException;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryDefinitionList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.QueryDataServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("server/" + QUERY_DEF_URI)
public class QueryDataResource {

    public static final Logger logger = LoggerFactory.getLogger(QueryDataResource.class);

    private QueryDataServiceBase queryDataServiceBase;
    private KieServerRegistry context;

    public QueryDataResource() {

    }

    public QueryDataResource(QueryDataServiceBase delegate, KieServerRegistry context) {
        this.queryDataServiceBase = delegate;
        this.context = context;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getQueries(@Context HttpHeaders headers, @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        QueryDefinitionList result = queryDataServiceBase.getQueries(page, pageSize);
        logger.debug("Returning result of get queries definition: {}", result);

        return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);
    }

    @POST
    @Path(CREATE_QUERY_DEF_POST_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createQueryDefinition(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("queryName") String queryName, String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {
            queryDataServiceBase.registerQuery(queryName, payload, type);

            logger.debug("Returning CREATED response after registering query with name {}", queryName);
            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (QueryAlreadyRegisteredException e) {
            return alreadyExists(MessageFormat.format(QUERY_ALREADY_EXISTS, queryName), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @PUT
    @Path(REPLACE_QUERY_DEF_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response replaceQueryDefinition(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("queryName") String queryName, String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {
            queryDataServiceBase.replaceQuery(queryName, payload, type);
            logger.debug("Returning CREATED response after registering query with name {}", queryName);
            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @DELETE
    @Path(DROP_QUERY_DEF_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response dropQueryDefinition(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("queryName") String queryName) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {
            queryDataServiceBase.unregisterQuery(queryName);
            // produce 204 NO_CONTENT response code
            return noContent(v, conversationIdHeader);
        } catch (QueryNotFoundException e) {
            return notFound(MessageFormat.format(QUERY_NOT_FOUND, queryName), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(QUERY_DEF_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getQuery(@Context HttpHeaders headers, @PathParam("queryName") String queryName) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {
            QueryDefinition queryDefinition = queryDataServiceBase.getQuery(queryName);

            return createCorrectVariant(queryDefinition, headers, Response.Status.OK, conversationIdHeader);
        } catch (QueryNotFoundException e) {
            return notFound(MessageFormat.format(QUERY_NOT_FOUND, queryName), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(RUN_QUERY_DEF_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response runQuery(@Context HttpHeaders headers, @PathParam("queryName") String queryName, @QueryParam("mapper") String mapper, @QueryParam("orderBy") String orderBy, @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        Object result = queryDataServiceBase.query(queryName, mapper, orderBy, page, pageSize);
        logger.debug("Returning result of process instance search: {}", result);

        return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);
    }

    @POST
    @Path(RUN_FILTERED_QUERY_DEF_POST_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response runQueryFiltered(@Context HttpHeaders headers, @PathParam("queryName") String queryName, @QueryParam("mapper") String mapper, @QueryParam("builder") String builder, @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize, String payload) {

        String type = getContentType(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        Object result = null;

        if (builder != null && !builder.isEmpty()) {
            result = queryDataServiceBase.queryFilteredWithBuilder(queryName, mapper, builder, page, pageSize, payload, type);
        } else {
            result = queryDataServiceBase.queryFiltered(queryName, mapper, page, pageSize, payload, type);
        }
        logger.debug("Returning result of process instance search: {}", result);

        return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);
    }

}
