package org.kie.remote.services.rest;

import static org.junit.Assert.assertEquals;
import static org.kie.remote.services.MockSetupTestHelper.TASK_ID;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.remote.services.rest.exception.DescriptiveExceptionHandler;
import org.kie.workbench.common.services.rest.RestOperationException;
import org.slf4j.Logger;


public class DescriptiveExceptionHandlerTest extends DescriptiveExceptionHandler {

    @BeforeClass
    public static void beforeClass() { 
        // turn off logging
        DescriptiveExceptionHandler.logger = mock(Logger.class);
    }
    
    @Test
    public void kieServicesVsDroolsWbExceptionHandlingTest() throws Exception { 
        // setup
        this.headers = mock(HttpHeaders.class);
        MultivaluedMap<String, String> requestHeaders = new MultivaluedMapImpl<String, String>();
        List<String> acceptHeaders = new ArrayList<String>();
        requestHeaders.put(HttpHeaders.ACCEPT, acceptHeaders);
        doReturn(requestHeaders).when(headers).getRequestHeaders();
       
        // task path
        this.uriInfo = mock(UriInfo.class);
        doReturn(new URI("http://localhost:8080/test/rest/task/" + TASK_ID + "/start")).when(uriInfo).getRequestUri();
        
        Exception e = RestOperationException.badRequest("test");
        Response resp = toResponse(e);
        MultivaluedMap<String, Object> meta = resp.getMetadata();
        assertEquals( "Incorrect format used", MediaType.APPLICATION_XML_TYPE, meta.getFirst(HttpHeaders.CONTENT_TYPE));
       
        // runtime path
        this.uriInfo = mock(UriInfo.class);
        doReturn(new URI("http://localhost:8080/test/rest/runtime/org.kie.test/execute")).when(uriInfo).getRequestUri();
        
        resp = toResponse(e);
        meta = resp.getMetadata();
        assertEquals( "Incorrect format used", MediaType.APPLICATION_XML_TYPE, meta.getFirst(HttpHeaders.CONTENT_TYPE));
        
        // history path
        this.uriInfo = mock(UriInfo.class);
        doReturn(new URI("http://localhost:8080/test/rest/history/clear")).when(uriInfo).getRequestUri();
        
        resp = toResponse(e);
        meta = resp.getMetadata();
        assertEquals( "Incorrect format used", MediaType.APPLICATION_XML_TYPE, meta.getFirst(HttpHeaders.CONTENT_TYPE));

        
        // drools-wb-rest path
        this.uriInfo = mock(UriInfo.class);
        doReturn(new URI("http://localhost:8080/test/rest/repositories")).when(uriInfo).getRequestUri();
        
        resp = toResponse(e);
        meta = resp.getMetadata();
        assertEquals( "Incorrect format used", MediaType.APPLICATION_JSON_TYPE, meta.getFirst(HttpHeaders.CONTENT_TYPE));
    }
}
