package org.kie.server.remote.rest.common.util;

import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.remote.common.rest.RestEasy960Util;
import org.kie.server.api.KieServerConstants;

public class RestUtils {

    public static Variant defaultVariant 
        = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE).add().build().get(0);
    
    public static Response createCorrectVariant(Object responseObj, HttpHeaders headers) {
        return createCorrectVariant(responseObj, headers, null);
    }

    public static Response createCorrectVariant(Object responseObj, HttpHeaders headers, javax.ws.rs.core.Response.Status status) {
        Response.ResponseBuilder responseBuilder = null;
        Variant v = getVariant(headers);
        if( status != null ) {
            responseBuilder = Response.status(status).entity(responseObj).variant(v);
        } else {
            responseBuilder = Response.ok(responseObj, v);
        }
        return responseBuilder.build();
    }
    
    public static Response createResponse(Object responseObj, Variant v, javax.ws.rs.core.Response.Status status) {
        Response.ResponseBuilder responseBuilder = null;
        if( status != null ) {
            responseBuilder = Response.status(status).entity(responseObj).variant(v);
        } else {
            responseBuilder = Response.ok(responseObj, v);
        }
        return responseBuilder.build();
    }

    public static Response createResponse(Object responseObj, Map<String, String> headers, Variant v, javax.ws.rs.core.Response.Status status) {
        Response.ResponseBuilder responseBuilder = null;
        if( status != null ) {
            responseBuilder = Response.status(status).entity(responseObj).variant(v);
        } else {
            responseBuilder = Response.ok(responseObj, v);
        }
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                responseBuilder.header(entry.getKey(), entry.getValue());
            }
        }
        return responseBuilder.build();
    }
    
    public static Variant getVariant(HttpHeaders headers) { 
        Variant v = RestEasy960Util.getVariant(headers);
        if( v == null ) {
            v = defaultVariant;
        }
        return v;
    }

    public static String getClassType(HttpHeaders headers) {
        String classType = Object.class.getName();

        List<String> header = headers.getRequestHeader(KieServerConstants.CLASS_TYPE_HEADER);
        if (header != null && !header.isEmpty()) {
            classType = header.get(0);
        }

        return classType;
    }
}
