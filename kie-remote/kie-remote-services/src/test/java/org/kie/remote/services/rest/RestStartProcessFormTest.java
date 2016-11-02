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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.drools.core.command.runtime.process.GetProcessIdsCommand;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.remote.services.util.FormURLGenerator;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceFormResponse;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.kie.remote.services.MockSetupTestHelper.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class RestStartProcessFormTest extends RuntimeResourceImpl {

    public static final String TEST_PROCESS_DEF_NAME = "org.test.mock.process";

    private HttpServletRequest httpRequestMock;

    private Map<String, String[]> queryParams;

    @Before
    public void initTest() {
        this.queryParams = new HashMap<String, String[]>();
        this.deploymentId = DEPLOYMENT_ID;

        this.formURLGenerator = new FormURLGenerator();

        httpRequestMock = mock( HttpServletRequest.class );

        setHttpServletRequest( httpRequestMock );

        doReturn( queryParams ).when( httpRequestMock ).getParameterMap();

        this.processRequestBean = mock( ProcessRequestBean.class );
        when( processRequestBean.doKieSessionOperation( any( GetProcessIdsCommand.class ),
                                                        anyString(),
                                                        anyList(),
                                                        any( Long.class ) ) ).thenAnswer(
                new Answer<List<String>>() {
                    @Override
                    public List<String> answer( InvocationOnMock invocation ) throws Throwable {
                        List<String> response = new ArrayList<String>();
                        response.add( TEST_PROCESS_DEF_NAME );
                        return response;
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
    public void testShowStartProcessForm() throws Exception {
        doReturn( new String( "http://localhost:8080/test/rest/process/" + TEST_PROCESS_DEF_NAME + "/startform" ) ).when(
                httpRequestMock )
                .getRequestURI();

        Response resp = getProcessInstanceStartForm( TEST_PROCESS_DEF_NAME );
        // verify ksession is called
        JaxbProcessInstanceFormResponse processFormResponse = (JaxbProcessInstanceFormResponse) resp.getEntity();

        assertNotNull( "Response cannot be null", processFormResponse );

        assertNotNull( "FormUrl cannot be null", processFormResponse.getFormUrl() );

        String baseURL = getBaseUri() + "/" + FormURLGenerator.KIE_WB_JSP;

        assertTrue( "FormUrl must start with '" + baseURL + "'",
                    processFormResponse.getFormUrl().startsWith( baseURL ) );
    }

    @Test( expected = KieRemoteRestOperationException.class )
    public void testShowStartProcessFormFailure() throws Exception {
        doReturn( new String( "http://localhost:8080/test/rest/process/" + TEST_PROCESS_DEF_NAME + "/startform" ) ).when(
                httpRequestMock )
                .getRequestURI();
        String[] corrProps = {"anton"};
        this.queryParams.put( "corrProp", corrProps );

        Response resp = getProcessInstanceStartForm( "wrong-id" );

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
