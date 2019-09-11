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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import io.swagger.annotations.ResponseHeader;
import org.kie.server.api.model.instance.DocumentInstance;
import org.kie.server.api.model.instance.DocumentInstanceList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.remote.rest.common.util.RestUtils;
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

@Api(value="Documents")
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

    @ApiOperation(value="Retrieves document's content identified by given documentId",
            response=byte[].class, code=200, responseHeaders={@ResponseHeader(name="Content-Disposition", description="provides file name of the document")})
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Document with given id not found") })
    @GET
    @Path(DOCUMENT_INSTANCE_CONTENT_GET_URI)
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    public Response getDocumentContent(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "document id of a document that content should be retruned from", required = true, example = "xxx-yyy-zzz") @PathParam("documentId") String documentId) {
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

    @ApiOperation(value="Returns information about a specified document.",
            response=DocumentInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Document with given id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_DOCUMENT_RESPONSE_JSON)})) })
    @GET
    @Path(DOCUMENT_INSTANCE_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDocument(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "document id of a document that should be retruned", required = true, example = "xxx-yyy-zzz") @PathParam("documentId") String documentId) {
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

    @ApiOperation(value="Returns all documents from KIE Server.",
            response=DocumentInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_DOCUMENTS_RESPONSE_JSON)})) })
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response listDocuments(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
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

    @ApiOperation(value="Uploads a new document to KIE Server.",
            response=String.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CREATE_DOC_RESPONSE_JSON)}))})
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createDocument(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "document content represented as DocumentInstance", required = true, type="DocumentInstance", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=DOCUMENT_JSON),
                    @ExampleProperty(mediaType=XML, value=DOCUMENT_XML)})) String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {

            String identifier = documentServiceBase.storeDocument(payload, type);

            return createCorrectVariant(RestUtils.toIdentifier(identifier), headers, Response.Status.CREATED, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Updates a specified document in KIE Server.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Document with given id not found")})
    @PUT
    @Path(DOCUMENT_INSTANCE_PUT_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateDocument(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "document id of a document that should be updated", required = true, example = "xxx-yyy-zzz") @PathParam("documentId") String documentId, 
            @ApiParam(value = "document content represented as DocumentInstance", required = true, type="DocumentInstance", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=DOCUMENT_JSON),
                    @ExampleProperty(mediaType=XML, value=DOCUMENT_XML)})) String payload) {
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

    @ApiOperation(value="Deletes a specified document from KIE Server.",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Document with given id not found") })
    @DELETE
    @Path(DOCUMENT_INSTANCE_DELETE_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteDocument(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "document id of a document that should be deleted", required = true, example = "xxxx-yyy-zzz") @PathParam("documentId") String documentId) {
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
