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

package org.kie.remote.services.cdi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.drools.core.command.runtime.process.StartProcessCommand;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.remote.services.AcceptedServerCommands;
import org.kie.remote.services.exception.KieRemoteServicesDeploymentException;
import org.kie.services.client.serialization.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

@SuppressWarnings("unchecked")
public class ProcessRequestBeanTest {

    protected static final Logger logger = LoggerFactory.getLogger(ProcessRequestBean.class);
    
    @Test
    public void preprocessTest() throws Exception { 
        ProcessRequestBean procReqBean = spy(new ProcessRequestBean());
        
        for( Class serverClass : AcceptedServerCommands.SEND_OBJECT_PARAMETER_COMMANDS ) { 
            Object inst = serverClass.getConstructor(new Class[0]).newInstance(new Object[0]);
            procReqBean.preprocessCommand((Command) inst);
            logger.debug( "Are {} instances checked for user-defined classes?", serverClass.getSimpleName() );
            verify(procReqBean, atLeastOnce()).checkThatUserDefinedClassesWereUnmarshalled(any());
        }
    }


    @Test
    public void verifyNsElementImplFound() throws Exception { 
        StartProcessCommand cmd = new StartProcessCommand();
        cmd.setProcessId("deployment-forgot-class");
        Map<String, Object> params = new HashMap<String, Object>();
        
        params.put("test", new Person("bob"));
        cmd.setParameters(params);
       
        JAXBContext jaxbContext = JAXBContext.newInstance(StartProcessCommand.class, Person.class);
        String xmlStr = serialize(jaxbContext, true, cmd);
        jaxbContext = JAXBContext.newInstance(StartProcessCommand.class);
        StartProcessCommand copyCmd = (StartProcessCommand) deserialize(jaxbContext, xmlStr);
      
        ProcessRequestBean procReqBean = new ProcessRequestBean();
        String msg = null;
        try { 
            procReqBean.checkThatUserDefinedClassesWereUnmarshalled(copyCmd.getParameters()); 
        } catch( Exception e ) { 
           assertTrue( "Did not expect an " + e.getClass().getSimpleName() + " instance", e instanceof KieRemoteServicesDeploymentException );
           msg = e.getMessage();
        }
        assertNotNull( "Expected exception to be thrown", msg );
        msg = msg.replaceFirst("[^']*'", "");
        assertEquals( "Exception did not refernce class type correctly.", Person.class.getSimpleName().toLowerCase() + "'", msg );
    }
  
    
    @XmlRootElement
    @XmlType
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Person { 
      
        @XmlElement
        String name;
        
        public Person() { }
        public Person(String name ) { 
           this.name = name; 
        }
        
    }
   
    public static String serialize(JAXBContext jaxbContext, boolean prettyPrint, Object object) {
        Marshaller marshaller = null;
        try { 
            marshaller = jaxbContext.createMarshaller();
            if( prettyPrint ) { 
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            }
        } catch( JAXBException jaxbe ) { 
            throw new SerializationException("Unable to create JAXB marshaller", jaxbe);
        }
        
        try {
            marshaller.setProperty(CharacterEscapeHandler.class.getName(), new CharacterEscapeHandler() {
                public void escape(char[] ac, int i, int j, boolean flag, Writer writer) throws IOException {
                    writer.write( ac, i, j ); 
                }
            });
        } catch (PropertyException e) {
            throw new SerializationException("Unable to set CharacterEscapeHandler", e);
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

    public static Object deserialize(JAXBContext jaxbContext, String xmlStr) {
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

        return jaxbObj;
    }
}
