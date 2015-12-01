package org.kie.server.remote.rest.jbpm;

import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.FORM_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_FORM_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_ID;
import static org.kie.server.api.rest.RestURI.TASK_FORM_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_ID;
import static org.kie.server.remote.rest.common.util.RestUtils.createResponse;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;
import static org.kie.server.remote.rest.jbpm.resources.Messages.UNEXPECTED_ERROR;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.kie.server.services.jbpm.FormServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("server/" + FORM_URI)
public class FormResource {

    public static final Logger logger = LoggerFactory.getLogger(FormResource.class);
    public static int PRETTY_PRINT_INDENT_FACTOR = 4;

    private FormServiceBase formServiceBase;

    public FormResource() {

    }

    public FormResource(FormServiceBase formServiceBase) {
        this.formServiceBase = formServiceBase;
    }

    @GET
    @Path(PROCESS_FORM_URI)
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getProcessForm(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(PROCESS_ID) String processId,
            @QueryParam("lang") @DefaultValue("en") String language) {
        Variant v = getVariant(headers);
        try {

            String response = formServiceBase.getFormDisplayProcess(containerId, processId, language);
            if (response != null && !response.isEmpty()) {
                JSONObject json = XML.toJSONObject(response);
                formatJSONResponse(json);
                response = json.toString(PRETTY_PRINT_INDENT_FACTOR);
            }

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(TASK_FORM_URI)
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getTaskForm(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(TASK_INSTANCE_ID) Long taskId,
            @QueryParam("lang") @DefaultValue("en") String language) {
        Variant v = getVariant(headers);
        try {

            String response = formServiceBase.getFormDisplayTask(taskId, language);
            JSONObject json = XML.toJSONObject(response);
            formatJSONResponse(json);
            response = json.toString(PRETTY_PRINT_INDENT_FACTOR);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }
    
    private void formatJSONResponse(JSONObject json) {
        try {
            JSONObject form = json.getJSONObject("form");
            putPropertyArrayToObject(form);
            JSONArray fields = form.getJSONArray("field");
            for (int i = 0; i<fields.length(); ++i) {
                JSONObject field = fields.getJSONObject(i);
                putPropertyArrayToObject(field);
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
