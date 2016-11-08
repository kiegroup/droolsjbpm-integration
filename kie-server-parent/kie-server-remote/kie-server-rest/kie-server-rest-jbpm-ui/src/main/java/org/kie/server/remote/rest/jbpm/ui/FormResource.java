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

import org.apache.commons.lang3.StringUtils;
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
            @QueryParam("lang") @DefaultValue("en") String language, @QueryParam("filter") boolean filter,
            @QueryParam("type") @DefaultValue("ANY") String formType, @QueryParam("marshallContent") boolean marshallContent) {

        Variant variant = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String response = formServiceBase.getFormDisplayProcess(containerId, processId, language, filter, formType);

            if ( marshallContent ) {
                response = marshallFormContent( response, formType, variant);
            }

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, variant, Response.Status.OK, conversationIdHeader);

        } catch (IllegalStateException e) {
            return notFound("Form for process id " + processId + " not found", variant, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format("Unexpected error encountered", e.getMessage()), variant, conversationIdHeader);
        }
    }

    @GET
    @Path(TASK_FORM_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskForm(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(TASK_INSTANCE_ID) Long taskId,
            @QueryParam("lang") @DefaultValue("en") String language, @QueryParam("filter") boolean filter,
            @QueryParam("type") @DefaultValue("ANY") String formType, @QueryParam("marshallContent") boolean marshallContent ) {

        Variant variant = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String response = formServiceBase.getFormDisplayTask(taskId, language, filter, formType);

            if ( marshallContent ) {
                response = marshallFormContent( response, formType, variant);
            }

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, variant, Response.Status.OK, conversationIdHeader);

        } catch (IllegalStateException e) {
            return notFound("Form for task id " + taskId + " not found", variant, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format("Unexpected error encountered", e.getMessage()), variant, conversationIdHeader);
        }
    }

    protected String marshallFormContent( String formContent, String formType, Variant variant ) throws Exception {

        if ( StringUtils.isEmpty( formContent ) ) {
            return formContent;
        }

        FormServiceBase.FormType actualFormType = FormServiceBase.FormType.fromName(formType);

        String actualContentType = actualFormType.getContentType();

        if ( actualContentType == null ) {
            actualContentType = getMediaTypeForFormContent( formContent );
        }

        if (variant.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE) && !MediaType.APPLICATION_JSON_TYPE.getSubtype().equals( actualContentType )) {
            JSONObject json = XML.toJSONObject(formContent);
            formatJSONResponse(json);
            formContent = json.toString(PRETTY_PRINT_INDENT_FACTOR);
        } else if (variant.getMediaType().equals(MediaType.APPLICATION_XML_TYPE) && !MediaType.APPLICATION_XML_TYPE.getSubtype().equals( actualContentType )) {
            Object json = parseToJSON(formContent);
            formContent = XML.toString(json);
        }

        return formContent;
    }

    protected String getMediaTypeForFormContent( String contentType ) {
        if ( contentType != null ) {
            if ( contentType.startsWith( "{" ) || contentType.startsWith( "[" ) ) {
                return MediaType.APPLICATION_JSON_TYPE.getSubtype();
            }
            if ( contentType.startsWith( "<" ) ) {
                return MediaType.APPLICATION_XML_TYPE.getSubtype();
            }
        }
        return null;
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
            logger.debug("exception while formatting :: {}", e.getMessage(), e);
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

    private Object parseToJSON(String content) throws JSONException{
        try {
            return new JSONArray(content);
        } catch (JSONException e) {
            return new JSONObject(content);
        }
    }
}
