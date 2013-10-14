package org.kie.services.client.jaxb;

import static junit.framework.Assert.*;
import org.junit.Test;
import org.kie.services.client.SerializationTest;
import org.kie.services.client.api.command.AcceptedCommands;
import org.kie.services.client.serialization.jaxb.JaxbSerializationProvider;

public class JaxbSerializationTest extends SerializationTest {

    public TestType getType() { 
        return TestType.JAXB;
    }
    
    public Object testRoundtrip(Object in) throws Exception {
        String xmlObject = JaxbSerializationProvider.convertJaxbObjectToString(in);
        log.debug(xmlObject);
        return JaxbSerializationProvider.convertStringToJaxbObject(xmlObject);
    }

    @Test
    public void acceptedCommandsTest() throws Exception { 
       for( Class<?> cmdClass : AcceptedCommands.getSet() ) { 
           try { 
               cmdClass.getConstructor(new Class[0]);
           } catch(Exception e) { 
               fail( "Class " + cmdClass.getSimpleName() + " does not have a no-arg constructor.");
           }
       }
    }
}
