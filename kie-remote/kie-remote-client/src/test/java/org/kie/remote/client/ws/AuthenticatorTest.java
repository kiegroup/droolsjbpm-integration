/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.remote.client.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.remote.client.api.RemoteRuntimeEngineFactory;
import org.kie.remote.client.api.exception.RemoteCommunicationException;
import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.client.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.ws.command.generated.CommandWebService;
import org.kie.remote.services.ws.command.generated.Execute;
import org.kie.remote.services.ws.command.generated.ExecuteResponse;
import org.kie.remote.services.ws.command.generated.ObjectFactory;
import org.kie.services.client.builder.redirect.AvailablePortFinder;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbStringListResponse;
import org.kie.services.shared.ServicesVersion;
import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.FileAllocator;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerTransportProcessor;
import org.simpleframework.http.parse.PrincipalParser;
import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.TransportProcessor;
import org.simpleframework.transport.TransportSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * This test doesn't test what it needs to, although it should.
 * </p>
 * In other words, it should test whether or not the AuthCache class is overriden so as not to cache
 * authentication info, but it doesn't (because it somehow skips the TCP/HTTP connection?!?).
 */
public class AuthenticatorTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticatorTest.class);

    private static final String DEFAULT_ENDPOINT = "/ws/CommandService?wsdl";
    private static final String DEFAULT_REAL_ENDPOINT = "/ws/CommandService";

    private static int port = -1;
    private TestServer server = null;

    @BeforeClass
    public static void setup() {
        port = AvailablePortFinder.getNextAvailable(1025);
    }

    @After
    public void cleanup() throws Exception {
        server.stop();
    }

    private static interface TestServer {
        public void start() throws Exception;

        public void stop() throws Exception;
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

    private static final String BAD_USER = "bad";
    private static final String BAD_PASSWORD  = "easy";
    private static final String GOOD_USER = "good-guy";
    private static final String GOOD_PASSWORD  = "a4os";

    /**
     * Scenario to test:
     * 1. Bad client creation : fails
     * 2. good client creation: succeeds
     * 3. good client request : succeeds
     * 4. bad client creation : SUCCEEDS BUT SHOULD FAIL!
     *
     * @throws Exception
     */
    @Test
    public void statefulAuthorizationTest() throws Exception {
        server = new BasicAuthServer(port);
        server.start();

        // bad client fails
        boolean exceptionThrown = false;
        try {
            createClient(BAD_USER, BAD_PASSWORD);
        } catch( RemoteCommunicationException rce) {
            exceptionThrown = true;
            assertTrue( rce.getMessage().contains("Status 401"));
        }
        assertTrue( "Exception should have been thrown upon client creation with bad login info", exceptionThrown );

        // good client creation
        CommandWebService cmdServiceClient = createClient(GOOD_USER, GOOD_PASSWORD);

        // good client request
        JaxbCommandResponse cmdResp = cmdServiceClient.execute(new JaxbCommandsRequest()).getResponses().get(0);

        String user = ((JaxbStringListResponse) cmdResp).getResult().get(0);
        String password = ((JaxbStringListResponse) cmdResp).getResult().get(1);
        assertEquals("User did not match request!", GOOD_USER, user);
        assertEquals("Password did not match request!", GOOD_PASSWORD, password);

        // bad client creation
        exceptionThrown = false;
        try {
            createClient(BAD_USER, BAD_PASSWORD);
        } catch( RemoteCommunicationException rce ) {
            exceptionThrown = true;
            assertTrue( rce.getMessage().contains("Status 401"));
        }
        assertTrue( "Exception should have been thrown upon 2nd client creation with bad login info", exceptionThrown );

    }

    private CommandWebService createClient(String user, String password) throws Exception {
        return RemoteRuntimeEngineFactory.newCommandWebServiceClientBuilder()
                .addUserName(user).addPassword(password)
                .addServerUrl("http://localhost:" + port + "/")
                .setWsdlLocationRelativePath(DEFAULT_ENDPOINT)
                .buildBasicAuthClient();
    }

    private static class BasicAuthServer implements Container, TestServer {

        private final Connection connection;
        private final SocketAddress address;
        private final int port;

        private static final String AUTH_HEADER_NAME = "Authorization: ";

        public BasicAuthServer(int port) throws Exception {
            this.port = port;
            Allocator allocator = new FileAllocator();
            TransportProcessor processor = new ContainerTransportProcessor(this, allocator, 5);
            SocketProcessor server = new TransportSocketProcessor(processor);

            this.connection = new SocketConnection(server);
            this.address = new InetSocketAddress("localhost", port);
        }

        @Override
        public void start() throws Exception {
            try {
                logger.debug("Starting basic auth server");
                connection.connect(address);
            } finally {
                logger.debug("Started basic auth server");
            }
        }

        @Override
        public void stop() throws Exception {
            connection.close();
        }


        public void handle( Request req, Response resp ) {
            PrintStream out = null;
            try {
                CharSequence allheaders = req.getHeader();
                String headers[] = allheaders.toString().split("\\r?\\n");
                String authHeader = null;
                for( String header : headers ) {
                    if( header.startsWith(AUTH_HEADER_NAME) ) {
                        authHeader = header.substring(AUTH_HEADER_NAME.length());
                        break;
                    }
                }
                PrincipalParser authHeaderParser = new PrincipalParser(authHeader);
                String user = authHeaderParser.getName();
                String pass = authHeaderParser.getPassword();

                out = resp.getPrintStream(1024);
                if( (user != null && pass != null ) && (! user.equals(GOOD_USER) || ! pass.equals(GOOD_PASSWORD)) ) {
                    resp.setCode(401);
                    String errorMsg = "Invalid user and/or password: " + user + "/" + pass;
                    out.print(errorMsg);
                } else {
                    logger.debug(headers[0] + ": " + user + "/" + pass);

                    String address = req.getAddress().toString();
                    if( address.equals(DEFAULT_ENDPOINT) ) {
                        resp.setValue(HttpHeaders.CONTENT_TYPE, "text/plain");

                        InputStream wsdlIn = this.getClass().getResourceAsStream("/wsdl/CommandService.wsdl");
                        assertNotNull(wsdlIn);
                        String wsdlContent = readInputStreamAsString(wsdlIn);
                        wsdlContent = wsdlContent.replaceAll("VERSION", ServicesVersion.VERSION);
                        wsdlContent = wsdlContent.replaceAll("REPLACE_WITH_ACTUAL_URL",
                                "http://localhost:" + port + "/" + DEFAULT_REAL_ENDPOINT);
                        out.print(wsdlContent);
                    } else if( address.endsWith(DEFAULT_REAL_ENDPOINT) ) {
                        JaxbCommandsRequest cmdRequest = deserializeAndUnwrapRequest(req.getContent());

                        JaxbCommandsResponse response = new JaxbCommandsResponse();
                        JaxbStringListResponse stringListResp = new JaxbStringListResponse();
                        response.getResponses().add(stringListResp);
                        List<String> userPassList = Arrays.asList(new String[] { user, pass });
                        stringListResp.setResult(userPassList);

                        String responseStr = createResponse(response);

                        // write to response
                        out.print(responseStr);
                    }
                }

            } catch( Exception e ) {
                e.printStackTrace();
            } finally {
                if( out != null ) {
                    out.close();
                }
                try {
                    req.getInputStream().close();
                    req.getChannel().close();
                } catch( IOException e ) {
                    e.printStackTrace();
                }
            }
        }

        private JaxbCommandsRequest deserializeAndUnwrapRequest( String requestContent ) throws WebServiceException {
            SOAPMessage msg;
            try {
                msg = MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(requestContent.getBytes()));

            } catch( Exception e ) {
                throw new WebServiceException("Unable to create SOAP message from request content: " + e.getMessage(), e);
            }

            Unmarshaller unmarshaller;
            try {
                unmarshaller = JAXBContext.newInstance(Execute.class).createUnmarshaller();
            } catch( JAXBException e ) {
                throw new WebServiceException("Could not create unmarshaller: " + e.getMessage(), e);
            }

            ByteArrayInputStream msgStream;
            try {
                ByteArrayOutputStream msgOutStream = new ByteArrayOutputStream();
                msg.writeTo(msgOutStream);
                byte[] soapMsgBytes = msgOutStream.toByteArray();
                String content = new String(soapMsgBytes);
                logger.debug("SOAP msg content:\n" + content);
                msgStream = new ByteArrayInputStream(soapMsgBytes);
            } catch( Exception e ) {
                throw new WebServiceException("Could not get content of SOAP message: " + e.getMessage(), e);
            }

            JaxbCommandsRequest request;
            try {
                XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
                StreamSource xmlSource = new StreamSource(msgStream);
                XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(xmlSource);
                xmlReader.nextTag();
                while( !xmlReader.getLocalName().equals("request") ) {
                    xmlReader.nextTag();
                }

                JAXBElement<JaxbCommandsRequest> jaxbCmdReqElem = unmarshaller.unmarshal(xmlReader, JaxbCommandsRequest.class);
                request = jaxbCmdReqElem.getValue();
            } catch( Exception e ) {
                throw new WebServiceException("Could not unmarshall request source: " + e.getMessage(), e);
            }

            return request;
        }

        private final MessageFactory msgFactory = MessageFactory.newInstance();

        private String createResponse( JaxbCommandsResponse resp ) throws Exception {
            SOAPMessage msg = msgFactory.createMessage();

            // empty response
            ExecuteResponse execResp = new ExecuteResponse();
            execResp.setReturn(resp);
            JAXBElement<ExecuteResponse> jaxbElem = (new ObjectFactory()).createExecuteResponse(execResp);

            // marshall
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Marshaller marshaller = JAXBContext.newInstance(ExecuteResponse.class).createMarshaller();
            marshaller.marshal(jaxbElem, document);
            msg.getSOAPBody().addDocument(document);

            // convert to stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            msg.writeTo(outputStream);

            return new String(outputStream.toByteArray());
        }
    }
}
