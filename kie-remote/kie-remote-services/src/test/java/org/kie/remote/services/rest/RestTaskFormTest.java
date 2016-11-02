/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.remote.services.rest;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jbpm.services.task.commands.GetTaskCommand;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.task.model.Task;
import org.kie.internal.identity.IdentityProvider;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.remote.services.util.FormURLGenerator;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskFormResponse;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.kie.remote.services.MockSetupTestHelper.*;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class RestTaskFormTest extends TaskResourceImpl {

    private HttpServletRequest httpRequestMock;


    @Before
    public void before() {

        this.formURLGenerator = new FormURLGenerator();

        httpRequestMock = mock( HttpServletRequest.class );
        setHttpServletRequest( httpRequestMock );

        this.identityProvider = mock( IdentityProvider.class );
        doReturn( USER ).when( identityProvider ).getName();

        this.processRequestBean = mock( ProcessRequestBean.class );
        when( processRequestBean.doRestTaskOperation( any( Long.class ),
                                                      anyString(),
                                                      any( Long.class ),
                                                      any( Task.class ),
                                                      any( GetTaskCommand.class ) ) ).thenAnswer(
                new Answer<Object>() {
                    @Override
                    public Object answer( InvocationOnMock invocation ) throws Throwable {
                        return new Object();
                    }
                }
        );

        HttpHeaders headersMock = mock( HttpHeaders.class );
        this.headers = headersMock;

        List host = new ArrayList();
        host.add( "localhost" );

        when( headersMock.getRequestHeader( "host" ) ).thenReturn( host );

        when( headersMock.getRequestHeaders() ).thenReturn( new MultivaluedMapImpl<String, String>() );
    }

    @Test
    public void testShowTaskForm() {
        doReturn( new String( "http://localhost:8080/test/rest/task/" + TASK_ID + "/showTaskForm" ) ).when(
                httpRequestMock ).getRequestURI();

        Response response = getTaskFormByTaskId( TASK_ID );

        JaxbTaskFormResponse taskFormResponse = (JaxbTaskFormResponse) response.getEntity();

        assertNotNull( "Response cannot be null", taskFormResponse );

        assertNotNull( "FormUrl cannot be null", taskFormResponse.getFormUrl() );

        String baseURL = getBaseUri() + "/" + FormURLGenerator.KIE_WB_JSP;

        assertTrue( "FormUrl must start with '" + baseURL + "'", taskFormResponse.getFormUrl().startsWith( baseURL ) );
    }

    @Test( expected = KieRemoteRestOperationException.class )
    public void testShowStartTaskFormFailure() throws Exception {
        doReturn( new String( "http://localhost:8080/test/rest/task/" + TASK_ID + "/showTaskForm" ) ).when(
                httpRequestMock ).getRequestURI();


        Response resp = getTaskFormByTaskId( new Long( -1 ) );

        fail( "We shouldn't be here" );
    }

    @Override
    protected String getBaseUri() {
        return "http://localhost:8080/kie-wb";
    }

    protected String getRequestUri() {
        return "http://localhost:8080/myremoteapp";
    }
}
