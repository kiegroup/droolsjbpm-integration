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

package org.kie.remote.services.rest.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.kie.remote.services.rest.jaxb.JavaCompilerTest.getClassFromSource;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.DeploymentUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieContainer;
import org.kie.services.client.serialization.SerializationException;

/**
 * This tests test scenarios where:
 * 1. different deployments have different versions of the same class
 *
 *
 */
@SuppressWarnings("unchecked")
public class JaxbContextResolverTest {

    private JaxbContextResolver resolver;
    private DynamicJaxbContext dynamicJaxbContext = new DynamicJaxbContext();

    private Map<String, Collection<Class<?>>> deploymentClassesMap;
    private MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String, String>();

    private static final String DEPLOYMENT_ID = "org.kie.remote:test:1.0";
    private static final String OTHER_DEPLOYMENT_ID = "org.kie.remote:other-test:1.0";

    @Before
    public void before() throws URISyntaxException {
       resolver = new JaxbContextResolver();

       // Only created once to simulate Application scope
       resolver.dynamicContext = dynamicJaxbContext;
       deploymentClassesMap = dynamicJaxbContext.deploymentClassesMap;
    }

    @After
    public void cleanUp() {
        DynamicJaxbContext.clearDeploymentJaxbContext();
    }

    // Helper methods -------------------------------------------------------------------------------------------------------------

    private void undeploy(String deploymentId) {
        DeploymentEvent event = new DeploymentEvent(deploymentId, null);
        dynamicJaxbContext.removeOnUnDeploy(event);
    }

    private void deploy(String deploymentId, Class<?>... clazz) {
        // path
       pathParams.putSingle("deploymentId", deploymentId);

        // classes
       Collection<Class<?>> depClasses = new HashSet<Class<?>>();
       depClasses.addAll(Arrays.asList(clazz));

       // deploy
       DeployedUnit deployedUnit = mock(DeployedUnit.class);
       when(deployedUnit.getDeployedClasses()).thenReturn(depClasses);
       KModuleDeploymentUnit  deploymentUnit = mock(KModuleDeploymentUnit.class);
       when(deployedUnit.getDeploymentUnit()).thenReturn(deploymentUnit);
       KieContainer kieContainer = mock(KieContainer.class);
       when(deploymentUnit.getKieContainer()).thenReturn(kieContainer);
       when(kieContainer.getClassLoader()).thenReturn(this.getClass().getClassLoader());

       DeploymentEvent event = new DeploymentEvent(deploymentId, deployedUnit);
       resolver.dynamicContext.addOnDeploy(event);
    }

    public String serialize(JAXBContext jaxbContext, Object object) {
        Marshaller marshaller = null;
        try {
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        } catch( JAXBException jaxbe ) {
            throw new SerializationException("Unable to create JAXB marshaller.", jaxbe);
        }
        StringWriter stringWriter = new StringWriter();

        try {
            marshaller.marshal(object, stringWriter);
        } catch( JAXBException jaxbe ) {
            throw new SerializationException("Unable to marshall " + object.getClass().getSimpleName() + " instance.", jaxbe);
        }
        String output = stringWriter.toString();

        return output;
    }

    public <T> T deserialize(JAXBContext jaxbContext, String xmlStr, Class<T> type) {
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch( JAXBException jaxbe ) {
            throw new SerializationException("Unable to create unmarshaller.", jaxbe);
        }
        ByteArrayInputStream xmlStrInputStream = new ByteArrayInputStream(xmlStr.getBytes(Charset.forName("UTF-8")));

        Object jaxbObj = null;
        try {
            jaxbObj = unmarshaller.unmarshal(xmlStrInputStream);
        } catch( JAXBException jaxbe ) {
           throw new SerializationException("Unable to unmarshal string.", jaxbe);
        }

        return (T) jaxbObj;
    }

    private void verifyMyTypeInstance(Class myTypeClass, Object myTypeObj, Object roundTripTypeObj) throws Exception {
        Method getText = myTypeClass.getMethod("getText");
        String origText = (String) getText.invoke(myTypeObj);
        String copyText = (String) getText.invoke(roundTripTypeObj);
        assertEquals( origText, copyText );

        Method getData = myTypeClass.getMethod("getData");
        Integer origData = (Integer) getData.invoke(myTypeObj);
        Integer copyData = (Integer) getData.invoke(roundTripTypeObj);
        assertEquals( origData, copyData );
    }

    private void verifyNewMyTypeInstance(Class newMyTypeClass, Object newMyTypeObj, Object roundTripNewTypeObj) throws Exception {
        Method getNotText = newMyTypeClass.getMethod("getNotText");
        String origText = (String) getNotText.invoke(newMyTypeObj);
        String copyText = (String) getNotText.invoke(roundTripNewTypeObj);
        assertEquals( origText, copyText );
    }

    // TESTS ---------------------------------------------------------------------------------------------------------------------

    @Test
    public void simpleJaxbDeserializeTest() throws Exception {
        Class<?> myTypeClass = getClassFromSource("MyType.java", "MyType", "MyTypeChild.java", "MyTypeChild");
        deploy(DEPLOYMENT_ID, myTypeClass);

        // after request
        DynamicJaxbContext.setDeploymentJaxbContext(DEPLOYMENT_ID);

        JAXBContext jaxbContext = resolver.getContext(myTypeClass);

        Constructor<?> myTypeCstr = myTypeClass.getConstructor(String.class, int.class);
        Object myTypeObj = myTypeCstr.newInstance("og & \"<>", 23);

        String xmlStr = serialize(jaxbContext, myTypeObj);
        Object roundTripTypeObj = deserialize(jaxbContext, xmlStr, myTypeClass);

        verifyMyTypeInstance(myTypeClass, myTypeObj, roundTripTypeObj);

        // after request
        DynamicJaxbContext.clearDeploymentJaxbContext();
    }

    @Test
    public void cacheJaxbContextTest() throws Exception {
        // setup
        Class<?> myTypeClass = getClassFromSource("MyType.java", "MyType", "MyTypeChild.java", "MyTypeChild");
        deploy(DEPLOYMENT_ID, myTypeClass);

        // before request
        DynamicJaxbContext.setDeploymentJaxbContext(DEPLOYMENT_ID);

        // setup request
        JAXBContext jaxbContext = resolver.getContext(myTypeClass);

        Constructor<?> myTypeCstr = myTypeClass.getConstructor(String.class, int.class);
        Object myTypeObj = myTypeCstr.newInstance("og", 23);

        // "do" request
        String xmlStr = serialize(jaxbContext, myTypeObj);
        Object roundTripTypeObj = deserialize(jaxbContext, xmlStr, myTypeClass);

        // verify deserialized objects are correct
        verifyMyTypeInstance(myTypeClass, myTypeObj, roundTripTypeObj);

        JAXBContext cachedJaxbContext = resolver.getContext(myTypeClass);
        assertTrue( "JAXBContext was not cached!", jaxbContext == cachedJaxbContext);
    }

    /**
     * This test tests whether or not whether requests to different deployment-based resources
     * succeed, when 2 deployments contain identically named classes that are structured differently.
     *
     * In other words, do we make sure that the classpath for serialization is isolated per deployment?
     * @throws Exception
     */
    @Test
    public void multipleDeploymentsTest() throws Exception {
        // setup deployment
        Class<?> myTypeClass = getClassFromSource("MyType.java", "MyType",
                                                  "MyTypeChild.java", "MyTypeChild");
        deploy(DEPLOYMENT_ID, myTypeClass);

        {
        // before request
        DynamicJaxbContext.setDeploymentJaxbContext(DEPLOYMENT_ID);

        // get jaxb context
        JAXBContext jaxbContext = resolver.getContext(myTypeClass);

        // create instance of object
        Constructor<?> myTypeCstr = myTypeClass.getConstructor(String.class, int.class);
        Object myTypeObj = myTypeCstr.newInstance("og", 23);

        // round trip serialization
        String xmlStr = serialize(jaxbContext, myTypeObj);
        Object roundTripTypeObj = deserialize(jaxbContext, xmlStr, myTypeClass);

        // check object content/round-trip correctness
        verifyMyTypeInstance(myTypeClass, myTypeObj, roundTripTypeObj);

        // after request
        DynamicJaxbContext.clearDeploymentJaxbContext();
        }

        // setup OTHER deployment
        Class<?> newMyTypeClass = getClassFromSource("NewMyType.java", "MyType",
                                                     "NewMyTypeChild.java", "MyTypeChild");
        deploy(OTHER_DEPLOYMENT_ID, newMyTypeClass);

        {
        // before request
        DynamicJaxbContext.setDeploymentJaxbContext(OTHER_DEPLOYMENT_ID);
        // get jaxb context
        JAXBContext jaxbContext = resolver.getContext(newMyTypeClass);

        // instance of new object -- which should only be accessible in other deployment
        Constructor<?> newMyTypeCstr = newMyTypeClass.getConstructor(String.class);
        Object newMyTypeObj = newMyTypeCstr.newInstance("og");

        String xmlStr = serialize(jaxbContext, newMyTypeObj);
        Object roundTripNewMyTypeObject = deserialize(jaxbContext, xmlStr, newMyTypeClass);

        // check object content/round-trip correctness
        verifyNewMyTypeInstance(newMyTypeClass, newMyTypeObj, roundTripNewMyTypeObject);
        // after request
        DynamicJaxbContext.clearDeploymentJaxbContext();
        }
    }

    @Test
    public void deployUndeployDeployTest() throws Exception {
        String depId = "org.kie.remote:test:1.0";

        {
        // setup deployment
        Class<?> myTypeClass = getClassFromSource("MyType.java", "MyType", "MyTypeChild.java", "MyTypeChild");
        deploy(DEPLOYMENT_ID, myTypeClass);

        // before request
        DynamicJaxbContext.setDeploymentJaxbContext(DEPLOYMENT_ID);

        // get jaxb context
        JAXBContext jaxbContext = resolver.getContext(myTypeClass);

        // create instance of object
        Constructor<?> myTypeCstr = myTypeClass.getConstructor(String.class, int.class);
        Object myTypeObj = myTypeCstr.newInstance("og", 23);

        // use jaxb context for round trip serialization
        String xmlStr = serialize(jaxbContext, myTypeObj);
        Object roundTripTypeObj = deserialize(jaxbContext, xmlStr, myTypeClass);

        // compare round trip results
        verifyMyTypeInstance(myTypeClass, myTypeObj, roundTripTypeObj);
        // after request
        DynamicJaxbContext.clearDeploymentJaxbContext();
        }

        // undeploy!
        undeploy(DEPLOYMENT_ID);

        {
        // new deployment of same deployment -- with a different class definition
        Class<?> newMyTypeClass = getClassFromSource("NewMyType.java", "MyType", "NewMyTypeChild.java", "MyTypeChild");
        deploy(DEPLOYMENT_ID, newMyTypeClass);

        // before request
        DynamicJaxbContext.setDeploymentJaxbContext(DEPLOYMENT_ID);
        // get jaxb context
        JAXBContext jaxbContext = resolver.getContext(newMyTypeClass);

        // create new instance (of new class!)
        Constructor<?> newMyTypeCstr = newMyTypeClass.getConstructor(String.class);
        Object newMyTypeObj = newMyTypeCstr.newInstance("og");

        // round trip serialization
        String xmlStr = serialize(jaxbContext, newMyTypeObj);
        Object roundTripNewTypeObj = deserialize(jaxbContext, xmlStr, newMyTypeClass);

        // check object content/round-trip correctness
        verifyNewMyTypeInstance(newMyTypeClass, newMyTypeObj, roundTripNewTypeObj);

        try {
            newMyTypeClass.getMethod("getData");
            fail( "This method does not exist in the new definition of the class!");
        } catch( Exception e ) {
            // ignore
        }
        // after request
        DynamicJaxbContext.clearDeploymentJaxbContext();
        }
    }

    @Test
    public void requestDeploymentIdParsingTest() throws IOException {
        // setup
       HttpServletRequest mockRequest = mock(HttpServletRequest.class);

       // tests
       String deploymentId = "org.test:kjar:1.0";
       String requestUri = "/rest/deployment/"+ deploymentId + "/deploy";
       doReturn(requestUri).when(mockRequest).getRequestURI();
       doReturn(null).when(mockRequest).getParameter("deploymentId");
       String parsedDepId = DynamicJaxbContextFilter.getDeploymentId(mockRequest);
       assertEquals( "deployment operation URL: deployment id", deploymentId, parsedDepId);

       requestUri = "/rest/history/instance/23/variable";
       doReturn(requestUri).when(mockRequest).getRequestURI();
       doReturn(null).when(mockRequest).getParameter("deploymentId");
       parsedDepId = DynamicJaxbContextFilter.getDeploymentId(mockRequest);
       assertEquals( "history operation URL: (default) deployment id",
               DynamicJaxbContextFilter.DEFAULT_JAXB_CONTEXT_ID, parsedDepId);

       requestUri = "/rest/runtime/" + deploymentId + "/execute";
       doReturn(requestUri).when(mockRequest).getRequestURI();
       doReturn(null).when(mockRequest).getParameter("deploymentId");
       parsedDepId = DynamicJaxbContextFilter.getDeploymentId(mockRequest);
       assertEquals( "runtime operation URL: deployment id", deploymentId, parsedDepId);

       requestUri = "/rest/execute?deploymentId=" + deploymentId;
       doReturn(requestUri).when(mockRequest).getRequestURI();
       doReturn(deploymentId).when(mockRequest).getParameter("deploymentId");
       parsedDepId = DynamicJaxbContextFilter.getDeploymentId(mockRequest);
       assertEquals( "task operation URL (with parameter): deployment id", deploymentId, parsedDepId);
    }
}
