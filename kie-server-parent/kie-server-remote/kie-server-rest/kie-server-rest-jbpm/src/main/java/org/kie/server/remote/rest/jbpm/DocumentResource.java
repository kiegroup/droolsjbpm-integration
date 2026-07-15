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
import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.mail.internet.MimeUtility;
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
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.Variant;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.kie.server.api.model.instance.DocumentInstance;
import org.kie.server.api.model.instance.DocumentInstanceList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.KieServerRuntimeException;
import org.kie.server.services.jbpm.DocumentServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.DOCUMENT_INSTANCE_CONTENT_GET_URI;
import static org.kie.server.api.rest.RestURI.DOCUMENT_INSTANCE_DELETE_URI;
import static org.kie.server.api.rest.RestURI.DOCUMENT_INSTANCE_GET_URI;
import static org.kie.server.api.rest.RestURI.DOCUMENT_INSTANCE_PUT_URI;
import static org.kie.server.api.rest.RestURI.DOCUMENT_URI;
import static org.kie.server.api.rest.RestURI.DOCUMENT_ID;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.errorMessage;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;
import static org.kie.server.remote.rest.common.util.RestUtils.noContent;
import static org.kie.server.remote.rest.common.util.RestUtils.notFound;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.CREATE_DOC_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.DOCUMENT_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.DOCUMENT_XML;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_DOCUMENTS_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_DOCUMENT_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.XML;

@Tag(name="Documents")
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

    @Operation(summary="Retrieves document's content identified by given documentId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    headers = {
                            @io.swagger.v3.oas.annotations.headers.Header(name = "Content-Disposition", description = "provides file name of the document",
                                    schema = @Schema(implementation = String.class))
                    },
                    content = @Content(
                            schema = @Schema(implementation = byte[].class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Unexpected error"),
            @ApiResponse(responseCode = "404", description = "Document with given id not found") })
    @GET
    @Path(DOCUMENT_INSTANCE_CONTENT_GET_URI)
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    public Response getDocumentContent(@jakarta.ws.rs.core.Context HttpHeaders headers, 
            @Parameter(description = "document id of a document that content should be returned from", required = true, example = "xxx-yyy-zzz") @PathParam(DOCUMENT_ID) String documentId) {
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
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @Operation(summary="Returns information about a specified document.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"),
            @ApiResponse(responseCode = "404", description = "Document with given id not found"), 
            @ApiResponse(responseCode = "200", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = GET_DOCUMENT_RESPONSE_JSON)})
            }) })
    @GET
    @Path(DOCUMENT_INSTANCE_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDocument(@jakarta.ws.rs.core.Context HttpHeaders headers, 
            @Parameter(description = "document id of a document that should be retruned", required = true, example = "xxx-yyy-zzz") @PathParam(DOCUMENT_ID) String documentId) {
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
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @Operation(summary="Returns all documents from KIE Server.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"), 
            @ApiResponse(responseCode = "200", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = GET_DOCUMENTS_RESPONSE_JSON)})
            }) })
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response listDocuments(@jakarta.ws.rs.core.Context HttpHeaders headers, 
            @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {

            DocumentInstanceList documents = documentServiceBase.listDocuments(page, pageSize);

            return createCorrectVariant(documents, headers, Response.Status.OK, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @Operation(summary="Uploads a new document to KIE Server.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"), 
            @ApiResponse(responseCode = "201", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = CREATE_DOC_RESPONSE_JSON)})
            })})
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createDocument(@jakarta.ws.rs.core.Context HttpHeaders headers, 
            @Parameter(description = "document content represented as DocumentInstance", required = true, content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = DOCUMENT_JSON)}),
                @Content(mediaType = XML, examples = {@ExampleObject(value = DOCUMENT_XML)})
            }) String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {

            String identifier = documentServiceBase.storeDocument(payload, type);

            return createCorrectVariant(identifier, headers, Response.Status.CREATED, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @Operation(summary="Updates a specified document in KIE Server.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"),
            @ApiResponse(responseCode = "404", description = "Document with given id not found")})
    @PUT
    @Path(DOCUMENT_INSTANCE_PUT_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateDocument(@jakarta.ws.rs.core.Context HttpHeaders headers, 
            @Parameter(description = "document id of a document that should be updated", required = true, example = "xxx-yyy-zzz") @PathParam(DOCUMENT_ID) String documentId, 
            @Parameter(description = "document content represented as DocumentInstance", required = true, content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = DOCUMENT_JSON)}),
                @Content(mediaType = XML, examples = {@ExampleObject(value = DOCUMENT_XML)})
            }) String payload) {
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
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @Operation(summary="Deletes a specified document from KIE Server.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"),
            @ApiResponse(responseCode = "404", description = "Document with given id not found") })
    @DELETE
    @Path(DOCUMENT_INSTANCE_DELETE_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteDocument(@jakarta.ws.rs.core.Context HttpHeaders headers, 
            @Parameter(description = "document id of a document that should be deleted", required = true, example = "xxxx-yyy-zzz") @PathParam(DOCUMENT_ID) String documentId) {
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
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }
}
