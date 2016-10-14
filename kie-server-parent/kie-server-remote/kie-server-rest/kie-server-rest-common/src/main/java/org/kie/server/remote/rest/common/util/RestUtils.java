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

package org.kie.server.remote.rest.common.util;

import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.server.common.rest.RestEasy960Util;
import org.kie.server.api.ConversationId;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.marshal.MarshallerHelper;

public class RestUtils {

    private static MarshallerHelper marshallerHelper = new MarshallerHelper(null);
    
    public static Response createCorrectVariant(Object responseObj, HttpHeaders headers, Header... customHeaders) {
        return createCorrectVariant(responseObj, headers, null, customHeaders);
    }

    public static Response createCorrectVariant(Object responseObj, HttpHeaders headers, javax.ws.rs.core.Response.Status status, Header... customHeaders) {
        Response.ResponseBuilder responseBuilder = null;
        Variant v = getVariant(headers);
        String contentType = getContentType(headers);

        if( status != null ) {
            responseBuilder = Response.status(status).entity(marshallerHelper.marshal(contentType, responseObj)).variant(v);
        } else {
            responseBuilder = Response.ok(marshallerHelper.marshal(contentType, responseObj), v);
        }
        applyCustomHeaders(responseBuilder, customHeaders);
        return responseBuilder.build();
    }

    public static Response createCorrectVariant(MarshallerHelper marshallerHelper, String containerId, Object responseObj, HttpHeaders headers, javax.ws.rs.core.Response.Status status, Header... customHeaders) {
        Response.ResponseBuilder responseBuilder = null;
        Variant v = getVariant(headers);
        String contentType = getContentType(headers);

        String marshalledResponse;
        if (marshallerHelper.getRegistry().getContainer(containerId) == null) {
            marshalledResponse = marshallerHelper.marshal(contentType, responseObj);
        } else {
            marshalledResponse = marshallerHelper.marshal(containerId, contentType, responseObj);
        }
        if( status != null ) {
            responseBuilder = Response.status(status).entity(marshalledResponse).variant(v);
        } else {
            responseBuilder = Response.ok(marshalledResponse, v);
        }
        applyCustomHeaders(responseBuilder, customHeaders);
        return responseBuilder.build();
    }
    
    public static Response createResponse(Object responseObj, Variant v, javax.ws.rs.core.Response.Status status, Header... customHeaders) {
        Response.ResponseBuilder responseBuilder = null;
        if( status != null ) {
            responseBuilder = Response.status(status).entity(responseObj).variant(v);
        } else {
            responseBuilder = Response.ok(responseObj, v);
        }
        applyCustomHeaders(responseBuilder, customHeaders);
        return responseBuilder.build();
    }

    
    public static Variant getVariant(HttpHeaders headers) { 
        Variant v = RestEasy960Util.getVariant(headers);
        if( v == null ) {
            v = Variant.mediaTypes(getMediaType(headers)).add().build().get(0);
        }
        return v;
    }

    public static String getClassType(HttpHeaders headers) {
        String classType = null;

        List<String> header = headers.getRequestHeader(KieServerConstants.CLASS_TYPE_HEADER);
        if (header != null && !header.isEmpty()) {
            classType = header.get(0);
        }

        return classType;
    }

    public static String getContentType(HttpHeaders headers) {
        // default to application/xml
        String contentType = MediaType.APPLICATION_XML_TYPE.toString();
        // check variant that is based on accept header important in case of GET as then Content-Type might not be given
        Variant v = RestEasy960Util.getVariant(headers);
        if (v != null) {
            // set the default to selected variant
            contentType = v.getMediaType().toString();
        }
        // now look for actual Content-Type header
        List<String> contentTypeHeader = headers.getRequestHeader(HttpHeaders.CONTENT_TYPE);
        if (contentTypeHeader != null && !contentTypeHeader.isEmpty()) {
            contentType = contentTypeHeader.get(0);

        }
        List<String> kieContentTypeHeader = headers.getRequestHeader(KieServerConstants.KIE_CONTENT_TYPE_HEADER);
        if (kieContentTypeHeader != null && !kieContentTypeHeader.isEmpty()) {
            contentType = kieContentTypeHeader.get(0);
        }

        return contentType;
    }

    public static MediaType getMediaType(HttpHeaders httpHeaders) {
        String contentType = getContentType(httpHeaders);
        try {
            return MediaType.valueOf(contentType);
        } catch (IllegalArgumentException e) {
            MarshallingFormat format = MarshallingFormat.fromType(contentType);
            switch (format) {
                case JAXB:
                    return MediaType.APPLICATION_XML_TYPE;

                case XSTREAM:
                    return MediaType.APPLICATION_XML_TYPE;

                case JSON:
                    return MediaType.APPLICATION_JSON_TYPE;

                default:
                    return MediaType.APPLICATION_XML_TYPE;
            }
        }
    }

    public static Response notFound(String message, Variant v, Header... customHeaders) {
        return createResponse(message, v, Response.Status.NOT_FOUND, customHeaders);
    }

    public static Response internalServerError(String message, Variant v, Header... customHeaders) {
        return createResponse(message, v, Response.Status.INTERNAL_SERVER_ERROR, customHeaders);
    }

    public static Response alreadyExists(String message, Variant v, Header... customHeaders) {
        return createResponse(message, v, Response.Status.CONFLICT, customHeaders);
    }

    public static Response badRequest(String reason, Variant v, Header... customHeaders) {
        return createResponse(reason, v, Response.Status.BAD_REQUEST, customHeaders);
    }

    public static Response noContent(Variant v, Header... customHeaders) {
        return createResponse("", v, Response.Status.NO_CONTENT, customHeaders);
    }

    protected static void applyCustomHeaders(Response.ResponseBuilder builder, Header... customHeaders) {
        if (customHeaders != null && customHeaders.length > 0) {
            for (Header header : customHeaders) {
                if (header != null) {
                    builder.header(header.getName(), header.getValue());
                }
            }
        }
    }

    public static Header buildConversationIdHeader(String containerId, KieServerRegistry registry, HttpHeaders headers) {
        List<String> conversationIdHeader = headers.getRequestHeader(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER);
        if (conversationIdHeader != null && !conversationIdHeader.isEmpty()) {
            return new Header(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER, conversationIdHeader.get(0));
        }

        KieContainerInstanceImpl container = registry.getContainer(containerId);
        if (container != null && KieContainerStatus.STARTED.equals(container.getStatus())) {
            ReleaseId releaseId = container.getResource().getResolvedReleaseId();
            if (releaseId == null) {
                releaseId = container.getResource().getReleaseId();
            }

            String conversationId = ConversationId.from(KieServerEnvironment.getServerId(), containerId, releaseId).toString();

            return new Header(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER, conversationId);
        }

        return null;
    }
}
