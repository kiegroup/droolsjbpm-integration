/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.remote.services.ws.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.ws.security.SecurityConstants;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.client.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.ws.command.generated.CommandServiceBasicAuthClient;
import org.kie.remote.services.ws.command.generated.CommandWebService;
import org.kie.remote.services.ws.command.test.TestCommandBasicAuthImpl;
import org.kie.remote.services.ws.command.test.TestServerPasswordCallback;
import org.kie.services.shared.ServicesVersion;
import org.kie.test.util.network.AvailablePortFinder;

public class CommandServiceTest {

    protected static URL[] wsdlURL = new URL[2];
    protected static QName[] serviceName = new QName[2];
    protected static QName[] portName = new QName[2];

    private final static Random random = new Random();
    private final static AtomicLong idGen = new AtomicLong(random.nextInt(10000));

    public final static String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";

    static {
        serviceName[0] = new QName(NAMESPACE, "CommandServiceBasicAuth");
        portName[0] = new QName(NAMESPACE, "CommandServiceBasicAuthPort");
    }

    protected static Endpoint[] eps = new Endpoint[2];

    public static Endpoint setupCommandServiceEndpoint( String address, CommandWebService webServiceImpl ) {
        EndpointImpl ep = (EndpointImpl) Endpoint.create(webServiceImpl);
        ep.setAddress(address);
        ep.getProperties().put(SecurityConstants.CALLBACK_HANDLER, new TestServerPasswordCallback());

        ep.publish();
        return ep;
    }

    @BeforeClass
    public static void setUp() throws Exception {
        int port = AvailablePortFinder.getNextAvailable(1025);
        String serverAdress = "http://localhost:" + port;
        String address = serverAdress + "/ws/CommandService";
        URL url = CommandServiceTest.class.getResource("/wsdl/CommandService.wsdl");
        assertNotNull("Null URL for wsdl resource", url);

        // replace "VERSION" with actual version
        File file = new File(url.toURI());
        String content = IOUtils.toString(new FileInputStream(new File(url.toURI())));
        content = content.replaceAll("VERSION", ServicesVersion.VERSION);
        IOUtils.write(content, new FileOutputStream(file), "UTF-8");

        CommandWebService webServiceImpl = new TestCommandBasicAuthImpl();
        eps[0] = setupCommandServiceEndpoint(address, webServiceImpl);
        wsdlURL[0] = new URL(address + "?wsdl");
    }

    @AfterClass
    public static void tearDown() {
        for( Endpoint ep : eps ) {
            try {
                if( ep != null ) {
                    ep.stop();
                }
            } catch( Throwable t ) {
                t.printStackTrace();
                System.out.println("Error thrown: " + t.getMessage());
            }
        }
    }

    private CommandServiceBasicAuthClient getPlainTextServiceClient( URL wsdlURL ) {
        return new CommandServiceBasicAuthClient(wsdlURL, serviceName[0]);
    }

    private JaxbCommandsRequest createRequest() {
        String depId = UUID.randomUUID().toString();
        JaxbCommandsRequest req = new JaxbCommandsRequest();
        req.setDeploymentId(depId);
        req.setVersion(ServicesVersion.VERSION);
        req.setUser("test");
        return req;
    }

    @Test
    public void testBasicAuthCommandWebServiceImpl() throws Exception {
        // setup
        CommandServiceBasicAuthClient psc = getPlainTextServiceClient(wsdlURL[0]);
        CommandWebService pws = psc.getCommandServiceBasicAuthPort();
        BindingProvider bindingProxy = (BindingProvider) pws;

        /**
         * no way to test auth here
         * // setup auth
         * bindingProxy.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "mary");
         * bindingProxy.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "mary123@");
         **/

        // request with auth
        JaxbCommandsRequest req = createRequest();
        JaxbCommandsResponse resp = pws.execute(req);

        // test response
        assertNotNull("Null response", resp);
        assertEquals("Deployment id", req.getDeploymentId(), resp.getDeploymentId());

        // request without auth
        psc = getPlainTextServiceClient(wsdlURL[0]);
        pws = psc.getCommandServiceBasicAuthPort();
        bindingProxy = (BindingProvider) pws;

        /**
         * do request without auth
         * try {
         * resp = pws.execute(req);
         * fail("The WS call should have failed without authentication");
         * } catch( SOAPFaultException soapfe ) {
         * assertTrue( soapfe.getMessage().contains("No username") );
         * }
         **/
    }

}
