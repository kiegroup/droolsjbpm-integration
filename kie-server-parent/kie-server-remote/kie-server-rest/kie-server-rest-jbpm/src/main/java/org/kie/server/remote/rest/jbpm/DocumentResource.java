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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.MessageFormat;
import javax.mail.internet.MimeUtility;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Variant;

import org.kie.server.api.model.instance.DocumentInstance;
import org.kie.server.api.model.instance.DocumentInstanceList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.KieServerRuntimeException;
import org.kie.server.services.jbpm.DocumentServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;

import static org.kie.server.api.rest.RestURI.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;
import static org.kie.server.remote.rest.jbpm.resources.Messages.*;

@Api(value="jbpm-documents")
@Path("server/" + DOCUMENT_URI)
public class DocumentResource {

    public static final Logger logger = LoggerFactory.getLogger(DocumentResource.class);

    private DocumentServiceBase documentServiceBase;
    private KieServerRegistry context;

    public DocumentResource() {

    }

    public DocumentResource(DocumentServiceBase documentServiceBase, KieServerRegistry context) {
        this.documentServiceBase = documentServiceBase;
        this.context = context;
    }

    @GET
    @Path(DOCUMENT_INSTANCE_CONTENT_GET_URI)
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    public Response getDocumentContent(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("documentId") String documentId) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {

            final DocumentInstance document = documentServiceBase.getDocument(documentId);

            if (document == null) {
                return notFound("Document with id " + documentId + " not found", v, conversationIdHeader);
            }
            String fileName = MimeUtility.encodeWord(document.getName(), "utf-8", "Q");
            StreamingOutput entity = new StreamingOutput() {

                @Override
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    output.write(document.getContent());
                }
            };
            if (conversationIdHeader != null) {
                return Response.ok().entity(entity)
                        .header(conversationIdHeader.getName(), conversationIdHeader.getValue())
                        .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build();
            }

            return Response.ok().entity(entity).header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build();
        }  catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(DOCUMENT_INSTANCE_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDocument(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("documentId") String documentId) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {

            DocumentInstance document = documentServiceBase.getDocument(documentId);

            return createCorrectVariant(document, headers, Response.Status.OK, conversationIdHeader);
        } catch (KieServerRuntimeException e){
            return notFound("Document with id " + documentId + " not found", v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response listDocuments(@javax.ws.rs.core.Context HttpHeaders headers, @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {

            DocumentInstanceList documents = documentServiceBase.listDocuments(page, pageSize);

            return createCorrectVariant(documents, headers, Response.Status.OK, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createDocument(@javax.ws.rs.core.Context HttpHeaders headers, String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {

            String identifier = documentServiceBase.storeDocument(payload, type);

            return createCorrectVariant(identifier, headers, Response.Status.CREATED, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @PUT
    @Path(DOCUMENT_INSTANCE_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateDocument(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("documentId") String documentId, String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {

            documentServiceBase.updateDocument(documentId, payload, type);

            return createCorrectVariant("", headers, Response.Status.CREATED, conversationIdHeader);
        } catch (KieServerRuntimeException e){
            return notFound("Document with id " + documentId + " not found", v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @DELETE
    @Path(DOCUMENT_INSTANCE_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteDocument(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("documentId") String documentId) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {

            documentServiceBase.deleteDocument(documentId);

            // produce 204 NO_CONTENT response code
            return noContent(v, conversationIdHeader);
        } catch (KieServerRuntimeException e){
            return notFound("Document with id " + documentId + " not found", v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }
}
