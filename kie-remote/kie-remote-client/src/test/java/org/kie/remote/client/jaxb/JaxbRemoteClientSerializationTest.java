package org.kie.remote.client.jaxb;

import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JaxbRemoteClientSerializationTest extends AbstractRemoteClientSerializationTest {

    protected static final Logger logger = LoggerFactory.getLogger(JaxbRemoteClientSerializationTest.class); 

    protected JaxbSerializationProvider jaxbProvider = JaxbSerializationProvider.clientSideInstance();
    { 
        jaxbProvider.setPrettyPrint(true);
    }

    public void addClassesToSerializationProvider(Class<?>... extraClass) {
        jaxbProvider.addJaxbClasses(true, extraClass);
    }

    public <T> T testRoundTrip(T in) throws Exception {
        String xmlObject = jaxbProvider.serialize(in);
        logger.debug(xmlObject);
        return (T) jaxbProvider.deserialize(xmlObject);
    }
    
} 