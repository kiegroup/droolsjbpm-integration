package org.kie.remote.services.rest.jaxb;

import static org.junit.Assert.*;
import static org.kie.remote.services.rest.jaxb.JavaCompilerTest.getClassFromSource;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jbpm.kie.services.impl.event.DeploymentEvent;
import org.junit.Before;
import org.junit.Test;
import org.kie.services.client.serialization.SerializationException;
import org.kie.remote.services.cdi.DeploymentInfoBean;

public class JaxbContextManagerTest {

    private JaxbContextResolver resolver;
    private JaxbContextManager jaxbContextMgr;
    
    private DeploymentInfoBean mockDepInfoBean;
    private Map<String, Collection<Class<?>>> deploymentIdClassesMap = new HashMap<String, Collection<Class<?>>>();
    private MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String, String>();
    
    @Before
    public void before() { 
       resolver = new JaxbContextResolver();
       
       UriInfo mockUriInfo = mock(UriInfo.class);
       doReturn(pathParams).when(mockUriInfo).getPathParameters();
       resolver.uriInfo = mockUriInfo;
       
       jaxbContextMgr = new JaxbContextManager();
       resolver.dynamicContext = jaxbContextMgr;
       mockDepInfoBean = mock(DeploymentInfoBean.class);
       jaxbContextMgr.deploymentClassNameBean = mockDepInfoBean;
       
    }
   
    // Helper methods -------------------------------------------------------------------------------------------------------------
   
    private void undeploy(String deploymentid) { 
        jaxbContextMgr.cleanUpOnUndeploy(new DeploymentEvent(deploymentid, null));
        deploymentIdClassesMap.remove(deploymentid);
    }
    
    private void addClassesToDeployment(String deploymentId, Class<?>... clazz) { 
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
    }
    
    private void setDeploymentId(String deploymentId) { 
       pathParams.putSingle("deploymentId", deploymentId); 
       jaxbContextMgr.createDeploymentLockObjectOnDeploy(new DeploymentEvent(deploymentId, null));
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
        addClassesToDeployment(depId, myTypeClass);

        JAXBContext jaxbContext = resolver.getContext(myTypeClass);
        
        Constructor<?> myTypeCstr = myTypeClass.getConstructor(String.class, int.class);
        Object myTypeObj = myTypeCstr.newInstance("og", 23);
        
        String xmlStr = serialize(jaxbContext, myTypeObj);
        Object roundTripTypeObj = deserialize(jaxbContext, xmlStr, myTypeClass);
       
        verifyMyTypeInstance(myTypeClass, myTypeObj, roundTripTypeObj);
    }
   
    @Test
    public void cacheJaxbContextTest() throws Exception { 
        String depId = "org.kie.remote:test:1.0";
        setDeploymentId(depId);
        Class<?> myTypeClass = getClassFromSource("MyType.java");
        addClassesToDeployment(depId, myTypeClass);

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
        addClassesToDeployment(depId, myTypeClass);

        { 
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
        }
        
        // setup OTHER deployment
        String otherDepId = "org.kie.remote:other-test:1.0";
        setDeploymentId(otherDepId);
        Class<?> newMyTypeClass = getClassFromSource("NewMyType.java");
        addClassesToDeployment(otherDepId, newMyTypeClass);
      
        {
        // get jaxb context
        JAXBContext jaxbContext = resolver.getContext(newMyTypeClass);
     
        // instance of new object -- which should only be accessible in other deployment
        Constructor<?> newMyTypeCstr = newMyTypeClass.getConstructor(String.class);
        Object newMyTypeObj = newMyTypeCstr.newInstance("og");
        
        String xmlStr = serialize(jaxbContext, newMyTypeObj);
        Object roundTripNewMyTypeObject = deserialize(jaxbContext, xmlStr, newMyTypeClass);
       
        // check object content/round-trip correctness
        verifyNewMyTypeInstance(newMyTypeClass, newMyTypeObj, roundTripNewMyTypeObject);
        }
    }
    
    @Test
    public void deployUndeployDeployTest() throws Exception { 
        String depId = "org.kie.remote:test:1.0";
       
        { 
        // setup deployment
        setDeploymentId(depId);
        Class<?> myTypeClass = getClassFromSource("MyType.java");
        addClassesToDeployment(depId, myTypeClass);

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
        }
        
        // undeploy!
        undeploy(depId);
       
        {
        // new deployment of same deployment -- with a different class definition
        setDeploymentId(depId);
        Class<?> newMyTypeClass = getClassFromSource("NewMyType.java");
        addClassesToDeployment(depId, newMyTypeClass);
        
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
        }
    }
}