/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.services.client.builder.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.remote.client.api.RemoteRuntimeEngineFactory;
import org.kie.remote.client.jaxb.ClientJaxbSerializationProvider;
import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.client.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.FileAllocator;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerTransportProcessor;
import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.TransportProcessor;
import org.simpleframework.transport.TransportSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class HeaderTest {

    private static final Logger logger = LoggerFactory.getLogger(HeaderTest.class);

    private static final String DEFAULT_ENPOINT = "/rest/execute";

    private static final String TEST_HEADER_NAME = UUID.randomUUID().toString();
    private static final String ANOTHER_TEST_HEADER_NAME = UUID.randomUUID().toString();
    private static final String NOT_SENT_HEADER_NAME = UUID.randomUUID().toString();

    private static final Random random = new Random();

    private HttpHeaderServer server;
    private int port;

    @Before
    public void before() throws Exception {
        port = AvailablePortFinder.getNextAvailable(1025);
        server = new HttpHeaderServer(port);

        server.start();
    }

    @After
    public void after() throws Exception {
        server.stop();
    }

    @Test
    public void testSetHeader() throws Exception {
        // @formatter:off
        RuntimeEngine runtimeEngine =  RemoteRuntimeEngineFactory.newRestBuilder()
            .addPassword("user").addUserName("pass")
            .addDeploymentId("deploymentId")
            .addUrl(new URL("http://localhost:" + port + "/" ))
            .build();
        // @formatter:on

        // make sure our server works..
        ProcessInstance procInst = runtimeEngine.getKieSession().startProcess("test");
        assertNotNull( "Test server is not working..", procInst );
        assertTrue( "No header, so no event types should be in the returned process instance!", procInst.getEventTypes() == null || procInst.getEventTypes().length == 0 );

        String headerValueOne = String.valueOf(random.nextLong());
        String headerValueTwo = String.valueOf(random.nextLong());

        // @formater:off
        runtimeEngine =  RemoteRuntimeEngineFactory.newRestBuilder()
            .addPassword("user").addUserName("pass")
            .addDeploymentId("deploymentId")
            .addUrl(new URL("http://localhost:" + port + "/" ))
            .addHeader(NOT_SENT_HEADER_NAME, String.valueOf(random.nextLong()))
            .clearHeaderFields()
            .addHeader(TEST_HEADER_NAME, headerValueOne)
            .addHeader(ANOTHER_TEST_HEADER_NAME, headerValueTwo)
            .build();
        // @formatter:on

        procInst = runtimeEngine.getKieSession().startProcess("test");
        assertNotNull( "Test server is not working..", procInst );
        assertEquals( "Number of event types (corresponds to test headers in request)", 2, procInst.getEventTypes().length );

        for( String headerValue : procInst.getEventTypes() ) {
            assertTrue( "Unexpected header value: " + headerValue,
                    headerValue.equals(headerValueOne) || headerValue.equals(headerValueTwo));
        }
    }

    private static class HttpHeaderServer implements Container {

        private final Connection connection;
        private final SocketAddress address;

        private final JaxbSerializationProvider jaxbSerializationProvider = ClientJaxbSerializationProvider.newInstance();

        public HttpHeaderServer(int port) throws Exception {
            Allocator allocator = new FileAllocator();
            TransportProcessor processor = new ContainerTransportProcessor(this, allocator, 5);
            SocketProcessor server = new TransportSocketProcessor(processor);

            this.connection = new SocketConnection(server);
            this.address = new InetSocketAddress(port);
        }

        public void start() throws Exception {
            try {
                logger.debug("Starting redirect server");
                connection.connect(address);
            } finally {
                logger.debug("Started redirect server");
            }
        }

        public void stop() throws Exception {
            connection.close();
        }

        public void handle( Request req, Response resp ) {
            try {
                PrintStream out = resp.getPrintStream(1024);
                String address = req.getAddress().toString();
                if( address.equals(DEFAULT_ENPOINT) ) {
                    String content = readInputStreamAsString(req.getInputStream());

                    JaxbCommandsRequest cmdsReq = (JaxbCommandsRequest) jaxbSerializationProvider.deserialize(content);

                    String [] headerNames = {
                            TEST_HEADER_NAME,
                            ANOTHER_TEST_HEADER_NAME,
                            NOT_SENT_HEADER_NAME
                    };

                    List<String> headerValues = new ArrayList<String>();
                    for( String headerName : headerNames ) {
                       String headerVal = req.getValue(headerName);
                       if( headerVal != null ) {
                           headerValues.add(headerVal);
                       }
                    }
                    String output = handleJaxbCommandsRequest(cmdsReq, headerValues);
                    resp.setCode(HttpURLConnection.HTTP_OK);
                    out.print(output);
                } else {
                    resp.setCode(HttpURLConnection.HTTP_BAD_REQUEST);
                }

                out.close();
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }

        private String handleJaxbCommandsRequest( JaxbCommandsRequest cmdsReq, Collection<String> headerValues ) {
            JaxbCommandsResponse cmdsResp = new JaxbCommandsResponse(cmdsReq);

            JaxbProcessInstanceResponse procInstResp = new JaxbProcessInstanceResponse();
            procInstResp.setId((long) random.nextInt(Integer.MAX_VALUE));

            List<String> eventTypes = new ArrayList<String>();
            for( String headerValue : headerValues ) {
                eventTypes.add(headerValue);
            }
            procInstResp.setEventTypes(eventTypes);

            cmdsResp.getResponses().add(procInstResp);
            return jaxbSerializationProvider.serialize(cmdsResp);
        }

    }

    public static String readInputStreamAsString( InputStream in ) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while( result != -1 ) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toString();
    }
}
