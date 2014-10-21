package org.kie.remote.services.rest.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.kie.remote.services.rest.jaxb.JavaCompilerTest.getClassFromSource;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.remote.services.cdi.DeploymentInfoBean;
import org.kie.remote.services.cdi.DeploymentProcessedEvent;
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
    
    private DeploymentInfoBean mockDepInfoBean;
    private Map<String, Collection<Class<?>>> deploymentIdClassesMap = new HashMap<String, Collection<Class<?>>>();
    private MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String, String>();
    
    @Before
    public void before() throws URISyntaxException { 
       resolver = new JaxbContextResolver();
       
       // Only created once to simulate Application scope
       resolver.dynamicContext = dynamicJaxbContext;
       mockDepInfoBean = mock(DeploymentInfoBean.class);
       dynamicJaxbContext.deploymentInfoBean = mockDepInfoBean;
    }
    
    @After
    public void cleanUp() { 
        DynamicJaxbContext.clearDeploymentJaxbContext();
    }
   
    // Helper methods -------------------------------------------------------------------------------------------------------------
   
    private void undeploy(String deploymentid) { 
        dynamicJaxbContext.cleanUpOnUndeploy(new DeploymentProcessedEvent(deploymentid));
        deploymentIdClassesMap.remove(deploymentid);
    }
    
    private void addClassesToDeploymentAndInitializeDeploymentJaxbContext(String deploymentId, Class<?>... clazz) { 
       Collection<Class<?>> depClasses = deploymentIdClassesMap.get(deploymentId);
       boolean initialize = false;
       if( depClasses == null ) { 
           initialize = true;
           depClasses = new HashSet<Class<?>>();
           deploymentIdClassesMap.put(deploymentId, depClasses);
       }
       depClasses.addAll(Arrays.asList(clazz));
       if( initialize ) { 
           doReturn(depClasses).when(mockDepInfoBean).getDeploymentClasses(deploymentId);
       }
       // setup jaxb context instance for deployment 
       dynamicJaxbContext.setupDeploymentJaxbContext(new DeploymentProcessedEvent(deploymentId));
    }
    
    private void setDeploymentId(String deploymentId) { 
       pathParams.putSingle("deploymentId", deploymentId); 
       dynamicJaxbContext.setupDeploymentJaxbContext(new DeploymentProcessedEvent(deploymentId));
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
        String depId = "org.kie.remote:test:1.0";
        setDeploymentId(depId);
        Class<?> myTypeClass = getClassFromSource("MyType.java");
        addClassesToDeploymentAndInitializeDeploymentJaxbContext(depId, myTypeClass);

        // after request
        DynamicJaxbContext.setDeploymentJaxbContext(depId);
        
        JAXBContext jaxbContext = resolver.getContext(myTypeClass);
        
        Constructor<?> myTypeCstr = myTypeClass.getConstructor(String.class, int.class);
        Object myTypeObj = myTypeCstr.newInstance("og", 23);
        
        String xmlStr = serialize(jaxbContext, myTypeObj);
        Object roundTripTypeObj = deserialize(jaxbContext, xmlStr, myTypeClass);
       
        verifyMyTypeInstance(myTypeClass, myTypeObj, roundTripTypeObj);
        
        // after request
        DynamicJaxbContext.clearDeploymentJaxbContext();
    }
   
    @Test
    public void cacheJaxbContextTest() throws Exception { 
        // setup
        String depId = "org.kie.remote:test:1.0";
        setDeploymentId(depId);
        Class<?> myTypeClass = getClassFromSource("MyType.java");
        addClassesToDeploymentAndInitializeDeploymentJaxbContext(depId, myTypeClass);
       
        // before request
        DynamicJaxbContext.setDeploymentJaxbContext(depId);

        JAXBContext jaxbContext = resolver.getContext(myTypeClass);
        
        Constructor<?> myTypeCstr = myTypeClass.getConstructor(String.class, int.class);
        Object myTypeObj = myTypeCstr.newInstance("og", 23);
        
        String xmlStr = serialize(jaxbContext, myTypeObj);
        Object roundTripTypeObj = deserialize(jaxbContext, xmlStr, myTypeClass);
       
        verifyMyTypeInstance(myTypeClass, myTypeObj, roundTripTypeObj);
        
        JAXBContext cachedJaxbContext = resolver.getContext(myTypeClass);
        assertTrue( "JAXBContext was not cached!", jaxbContext == cachedJaxbContext);
    }
    
    @Test
    public void multipleDeploymentsTest() throws Exception { 
        // setup deployment
        String depId = "org.kie.remote:test:1.0";
        setDeploymentId(depId);
        Class<?> myTypeClass = getClassFromSource("MyType.java");
        addClassesToDeploymentAndInitializeDeploymentJaxbContext(depId, myTypeClass);

        { 
        // before request
        DynamicJaxbContext.setDeploymentJaxbContext(depId);

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
        String otherDepId = "org.kie.remote:other-test:1.0";
        setDeploymentId(otherDepId);
        Class<?> newMyTypeClass = getClassFromSource("NewMyType.java");
        addClassesToDeploymentAndInitializeDeploymentJaxbContext(otherDepId, newMyTypeClass);
      
        {
        // before request
        DynamicJaxbContext.setDeploymentJaxbContext(otherDepId);
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
        setDeploymentId(depId);
        Class<?> myTypeClass = getClassFromSource("MyType.java");
        addClassesToDeploymentAndInitializeDeploymentJaxbContext(depId, myTypeClass);

        // before request
        DynamicJaxbContext.setDeploymentJaxbContext(depId);
        
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
        undeploy(depId);
       
        {
        // new deployment of same deployment -- with a different class definition
        setDeploymentId(depId);
        Class<?> newMyTypeClass = getClassFromSource("NewMyType.java");
        addClassesToDeploymentAndInitializeDeploymentJaxbContext(depId, newMyTypeClass);
        
        // before request
        DynamicJaxbContext.setDeploymentJaxbContext(depId);
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
    public void requestDeploymentIdParsingTest() { 
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
       
       requestUri = "/rest/task/execute?deploymentId=" + deploymentId;
       doReturn(requestUri).when(mockRequest).getRequestURI();
       doReturn(deploymentId).when(mockRequest).getParameter("deploymentId");
       parsedDepId = DynamicJaxbContextFilter.getDeploymentId(mockRequest);
       assertEquals( "task operation URL (with parameter): deployment id", deploymentId, parsedDepId);
    }
}