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

package org.kie.server.remote.rest.jbpm.ui;


import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.FORM_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_FORM_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_ID;
import static org.kie.server.api.rest.RestURI.TASK_FORM_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_ID;
import static org.kie.server.remote.rest.common.util.RestUtils.*;

import java.text.MessageFormat;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.jbpm.services.task.exception.PermissionDeniedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.ui.FormServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("server/" + FORM_URI)
public class FormResource {

    private static final Logger logger = LoggerFactory.getLogger(FormResource.class);
    private static int PRETTY_PRINT_INDENT_FACTOR = 4;

    private FormServiceBase formServiceBase;
    private KieServerRegistry context;

    public FormResource() {

    }

    public FormResource(FormServiceBase formServiceBase, KieServerRegistry context) {
        this.formServiceBase = formServiceBase;
        this.context = context;
    }

    @GET
    @Path(PROCESS_FORM_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessForm(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(PROCESS_ID) String processId,
            @QueryParam("lang") @DefaultValue("en") String language, @QueryParam("filter") boolean filter) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String response = formServiceBase.getFormDisplayProcess(containerId, processId, language, filter);
            if (response != null && !response.isEmpty()) {

                if (v.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
                    JSONObject json = XML.toJSONObject(response);
                    formatJSONResponse(json);
                    response = json.toString(PRETTY_PRINT_INDENT_FACTOR);
                }
            }

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);

        } catch (IllegalStateException e) {
            return notFound("Form for process id " + processId + " not found", v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format("Unexpected error encountered", e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(TASK_FORM_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskForm(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(TASK_INSTANCE_ID) Long taskId,
            @QueryParam("lang") @DefaultValue("en") String language, @QueryParam("filter") boolean filter) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String response = formServiceBase.getFormDisplayTask(containerId, taskId, language, filter);
            if (v.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
                JSONObject json = XML.toJSONObject(response);
                formatJSONResponse(json);
                response = json.toString(PRETTY_PRINT_INDENT_FACTOR);
            }

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);

        } catch (IllegalStateException e) {
            return notFound("Form for task id " + taskId + " not found", v, conversationIdHeader);
        } catch (PermissionDeniedException e) {
            return permissionDenied(e.getMessage(), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format("Unexpected error encountered", e.getMessage()), v, conversationIdHeader);
        }
    }

    private void formatJSONResponse(JSONObject json) {
        try {
            JSONObject form = json.getJSONObject("form");
            putPropertyArrayToObject(form);
            Object fields = form.get("field");
            if (fields instanceof JSONArray) {
                for (int i = 0; i < ((JSONArray)fields).length(); ++i) {
                    JSONObject field = ((JSONArray)fields).getJSONObject(i);
                    putPropertyArrayToObject(field);
                }
            } else {
                putPropertyArrayToObject((JSONObject)fields);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void putPropertyArrayToObject(JSONObject obj) throws JSONException {
        JSONArray properties = obj.getJSONArray("property");
        for (int j = 0; j<properties.length(); ++j) {
            JSONObject property = properties.getJSONObject(j);
            obj.put(property.getString("name"), property.get("value"));
        }
        obj.remove("property");
    }

}
