/*
 * Copyright 2015 JBoss Inc
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

package org.kie.services.client.builder.redirect;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;

import javax.ws.rs.core.HttpHeaders;

import org.junit.Test;
import org.kie.remote.client.api.RemoteRuntimeEngineFactory;
import org.kie.remote.client.api.exception.RemoteCommunicationException;
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

public class RedirectTest {

    private static final Logger logger = LoggerFactory.getLogger(RedirectTest.class);

    private static final String DEFAULT_ENPOINT = "/ws/CommandService?wsdl";
    private static final String REAL_ENDPOINT_PATH = "/real/endpoint";
    private static final String REDIRECT_URL = "/redirect/url";
    private static final String REDIRECT_PATH = "/redirect/path";
    private static final String REDIRECT_DOUBLE = "/redirect/double";
    private static final String REDIRECT_ENDLESS = "/redirect/endless";
    private static final String REDIRECT_LOOP = "/redirect/loop/1";
    private static final String REDIRECT_LOOP_2 = "/redirect/loop/2";

    @Test
    public void testRedirect() throws Exception {
        int port = AvailablePortFinder.getNextAvailable(1025);
        RedirectServer server = new RedirectServer(port);

        server.start();

        // @formatter:off
        // 1. test only setting the endpoint
        RemoteRuntimeEngineFactory.newCommandWebServiceClientBuilder()
            .addPassword("user").addUserName("pass")
            .addServerUrl("http://localhost:" + port + "/")
            .setWsdlLocationRelativePath(REAL_ENDPOINT_PATH)
            .buildBasicAuthClient();
        
        // 2. test failure because no redirect
        try { 
            RemoteRuntimeEngineFactory.newCommandWebServiceClientBuilder()
                .addPassword("user").addUserName("pass")
                .addServerUrl("http://localhost:" + port + "/")
                .setWsdlLocationRelativePath("/redirect/url")
                .buildBasicAuthClient();
            fail("An execption should have been thrown: redirect was not set");
        } catch(RemoteCommunicationException rce) { 
           assertTrue( rce.getMessage().contains("HTTP Redirect is not set but") );
        }
       
        // 3. test redirect 
        RemoteRuntimeEngineFactory.newCommandWebServiceClientBuilder()
            .addPassword("user").addUserName("pass")
            .addServerUrl("http://localhost:" + port + "/")
            .useHttpRedirect()
            .buildBasicAuthClient();
        
        // 4. test set endpoint with redirect URL 
        RemoteRuntimeEngineFactory.newCommandWebServiceClientBuilder()
            .addPassword("user").addUserName("pass")
            .addServerUrl("http://localhost:" + port + "/")
            .useHttpRedirect()
            .setWsdlLocationRelativePath(REDIRECT_URL)
            .buildBasicAuthClient();
        
        // 5. test set endpoint with redirect path
        RemoteRuntimeEngineFactory.newCommandWebServiceClientBuilder()
            .addPassword("user").addUserName("pass")
            .addServerUrl("http://localhost:" + port + "/")
            .useHttpRedirect()
            .setWsdlLocationRelativePath(REDIRECT_PATH)
            .buildBasicAuthClient();
        
        // 6. test set endpoint with 2x redirect 
        RemoteRuntimeEngineFactory.newCommandWebServiceClientBuilder()
            .addPassword("user").addUserName("pass")
            .addServerUrl("http://localhost:" + port + "/")
            .useHttpRedirect()
            .setWsdlLocationRelativePath(REDIRECT_DOUBLE)
            .buildBasicAuthClient();
        
        // 6. test set endpoint with endless redirect 
        try { 
        RemoteRuntimeEngineFactory.newCommandWebServiceClientBuilder()
            .addPassword("user").addUserName("pass")
            .addServerUrl("http://localhost:" + port + "/")
            .useHttpRedirect()
            .setWsdlLocationRelativePath(REDIRECT_ENDLESS)
            .buildBasicAuthClient();
        } catch(RemoteCommunicationException rce) { 
            assertTrue( rce.getMessage().contains("Unable to verify WSDL URL"));
        }
        
        // 6. test set endpoint with endless redirect 
        try { 
        RemoteRuntimeEngineFactory.newCommandWebServiceClientBuilder()
            .addPassword("user").addUserName("pass")
            .addServerUrl("http://localhost:" + port + "/")
            .useHttpRedirect()
            .setWsdlLocationRelativePath(REDIRECT_LOOP)
            .buildBasicAuthClient();
        } catch(RemoteCommunicationException rce) { 
            assertTrue( rce.getMessage().contains("Unable to verify WSDL URL"));
        }
        // @formatter:on

        server.stop();
    }

    private static class RedirectServer implements Container {

        private final Connection connection;
        private final SocketAddress address;
        private final int port;

        public RedirectServer(int port) throws Exception {
            this.port = port;
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
                if( address.equals(REDIRECT_PATH) ) {
                    resp.setValue(HttpHeaders.LOCATION, REAL_ENDPOINT_PATH);
                    resp.setCode(HttpURLConnection.HTTP_MOVED_PERM);
                } else if( address.equals(REDIRECT_URL) ) {
                    resp.setValue(HttpHeaders.LOCATION, "http://localhost:" + port + REAL_ENDPOINT_PATH);
                    resp.setCode(HttpURLConnection.HTTP_MOVED_TEMP);
                } else if( address.equals(REDIRECT_DOUBLE) ) {
                    resp.setValue(HttpHeaders.LOCATION, "http://localhost:" + port + REDIRECT_URL);
                    resp.setCode(HttpURLConnection.HTTP_SEE_OTHER);
                } else if( address.equals(REDIRECT_ENDLESS) ) {
                    resp.setValue(HttpHeaders.LOCATION, "http://localhost:" + port + REDIRECT_ENDLESS);
                    resp.setCode(HttpURLConnection.HTTP_SEE_OTHER);
                } else if( address.equals(REDIRECT_LOOP) ) {
                    resp.setValue(HttpHeaders.LOCATION, "http://localhost:" + port + REDIRECT_LOOP_2);
                    resp.setCode(HttpURLConnection.HTTP_MOVED_PERM);
                } else if( address.equals(REDIRECT_LOOP_2) ) {
                    resp.setValue(HttpHeaders.LOCATION, "http://localhost:" + port + REDIRECT_LOOP);
                    resp.setCode(HttpURLConnection.HTTP_MOVED_PERM);
                } else if( address.equals(DEFAULT_ENPOINT) ) {
                    resp.setValue(HttpHeaders.LOCATION, "http://localhost:" + port + REDIRECT_URL);
                    resp.setCode(HttpURLConnection.HTTP_MOVED_PERM);
                } else if( address.equals("/real/endpoint") ) {
                    resp.setValue(HttpHeaders.CONTENT_TYPE, "text/plain");

                    InputStream wsdlIn = this.getClass().getResourceAsStream("/wsdl/CommandService.wsdl");
                    assertNotNull(wsdlIn);
                    String wsdlContent = readInputStreamAsString(wsdlIn);
                    out.print(wsdlContent);
                }

                out.close();
            } catch( Exception e ) {
                e.printStackTrace();
            }
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
