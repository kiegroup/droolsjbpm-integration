package org.kie.remote.client.jaxb;

import org.junit.Ignore;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
// Ignored because this test fails on Jenkins (but not locally)
// It probably fails on Jenkins because the port for the H2 database is not available 
// (possibly because another test did not stop it's H2 instance?)
public class JaxbRemoteClientSerializationTest extends AbstractRemoteClientSerializationTest {

    protected static final Logger logger = LoggerFactory.getLogger(JaxbRemoteClientSerializationTest.class); 

    protected JaxbSerializationProvider jaxbProvider = ClientJaxbSerializationProvider.newInstance();
    { 
        jaxbProvider.setPrettyPrint(true);
    }

    public void addClassesToSerializationProvider(Class<?>... extraClass) {
        jaxbProvider.addJaxbClassesAndReinitialize(extraClass);
    }

    public <T> T testRoundTrip(T in) throws Exception {
        String xmlObject = jaxbProvider.serialize(in);
        logger.debug(xmlObject);
        return (T) jaxbProvider.deserialize(xmlObject);
    }
    
} 