/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.remote.jaxb.gen;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.jbpm.services.task.commands.ActivateTaskCommand;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

public class GeneratedClassesCompatibilityTest {

    protected static final Logger logger = LoggerFactory.getLogger(GeneratedClassesCompatibilityTest.class);
    
    private static Reflections origRefs = new Reflections(ClasspathHelper.forPackage("org.drools.core.command"),
            ClasspathHelper.forPackage("org.jbpm.services.task.commands"),
            ClasspathHelper.forPackage("org.jbpm.process.audit.command"), ClasspathHelper.forPackage("org.kie.remote.jaxb.gen"),
            new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new SubTypesScanner());

    @Test
    public void compareSerializedStringsTest() throws Exception {
        ActivateTaskCommand origCmd = new ActivateTaskCommand();
        origCmd.setTaskId(23l);
        origCmd.setUserId("Olaf");
        origCmd.setTargetEntityId("target");
        List<String> groups = new ArrayList<String>();
        groups.add("one");
        origCmd.setGroupsIds(groups);

        String origOut = serialize(origCmd, ActivateTaskCommand.class);
        Class activateTaskCmdClass = getClass("org.kie.remote.jaxb.gen.ActivateTaskCommand");
        Object flatCmdobj = deserialize(origOut, activateTaskCmdClass);
        Class taskCmdClass = getClass("org.kie.remote.jaxb.gen.TaskCommand");
        assertEquals(origCmd.getUserId(), getField("userId", taskCmdClass, flatCmdobj));
        assertEquals(origCmd.getTargetEntityId(), getField("targetEntityId", taskCmdClass, flatCmdobj));
        assertEquals(origCmd.getTaskId(), getField("taskId", taskCmdClass, flatCmdobj));
        assertArrayEquals(origCmd.getGroupsIds().toArray(), ((List) getField("groupIds", taskCmdClass, flatCmdobj)).toArray());
    }

    private Class getClass( String className ) throws Exception {
        return Class.forName(className);
    }

    private Object getField( String fieldName, Class objClass, Object obj ) throws Exception {
        Field field = objClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    private String serialize( Object object, Class cmdClass ) throws Exception {
        Marshaller marshaller = null;
        marshaller = getJaxbContext(cmdClass).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        marshaller.setProperty(CharacterEscapeHandler.class.getName(), new CharacterEscapeHandler() {
            public void escape( char[] ac, int i, int j, boolean flag, Writer writer ) throws IOException {
                writer.write(ac, i, j);
            }
        });

        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(object, stringWriter);
        String output = stringWriter.toString();

        return output;
    }

    private <T> T deserialize( String xmlStr, Class<T> cmdClass ) throws Exception {
        Unmarshaller unmarshaller = null;
        unmarshaller = getJaxbContext(cmdClass).createUnmarshaller();
        ByteArrayInputStream xmlStrInputStream = new ByteArrayInputStream(xmlStr.getBytes(Charset.forName("UTF-8")));
        T jaxbObj = (T) unmarshaller.unmarshal(xmlStrInputStream);
        return jaxbObj;
    }

    private JAXBContext getJaxbContext( Class cmdClass ) throws Exception {
        Class[] classes = { cmdClass };
        return JAXBContext.newInstance(classes);
    }

    @Test
    public void roundTripTest() {
        Set<Class<?>> classes = origRefs.getTypesAnnotatedWith(XmlAccessorType.class);
        assertFalse(classes.isEmpty());

        Set<Class<?>> genCmdClasses = new HashSet<Class<?>>();
        Set<Class<?>> origCmdClasses = new HashSet<Class<?>>();
        for( Class<?> cmdClass : classes ) {
            if( !cmdClass.getSimpleName().endsWith("Command") ) {
                continue;
            }
            if( cmdClass.getName().startsWith("org.kie.remote.jaxb.gen") ) {
                genCmdClasses.add(cmdClass);
            } else {
                origCmdClasses.add(cmdClass);
            }
        }
        assertFalse(genCmdClasses.isEmpty());

        Map<Class, Class> origCmdGenCmdClassMap = new HashMap<Class, Class>();
        for( Class<?> cmdClass : origCmdClasses ) {
            if( !cmdClass.getSimpleName().endsWith("Command") ) {
                continue;
            }
            Class<?> genCmdClass = null;
            String genClassName = "org.kie.remote.jaxb.gen." + cmdClass.getSimpleName();
            try {
                genCmdClass = Class.forName(genClassName);
                genCmdClasses.remove(genCmdClass);
                origCmdGenCmdClassMap.put(cmdClass, genCmdClass);
            } catch( Exception e ) {
                // do nothing
            }
        }
        String problemClassName = genCmdClasses.isEmpty() ? "" : genCmdClasses.iterator().next().getSimpleName();
        assertTrue("This class (and others?) does not have a generated/flat equivalent: " + problemClassName,
                genCmdClasses.isEmpty());

        for( Entry<Class, Class> origGenClassEntry : origCmdGenCmdClassMap.entrySet() ) {
            Class genCmdClass = origGenClassEntry.getValue();
            
            Class origCmdClass = origGenClassEntry.getKey();
            List<Field> origFields = new ArrayList<Field>(Arrays.asList(origCmdClass.getDeclaredFields()));
            List<Field> genFields = new ArrayList<Field>(Arrays.asList(genCmdClass.getDeclaredFields()));
           
            Iterator<Field> iter = origFields.iterator();
            while( iter.hasNext() ) { 
                Field field = iter.next();
                if( Modifier.isStatic(field.getModifiers()) ) { 
                    iter.remove();
                    continue;
                }
                if( field.getAnnotation(XmlTransient.class) != null ) { 
                   iter.remove();
                   continue;
                }
            }
            if( origFields.size() != genFields.size() ) { 
                for( Field field : origFields ) { 
                    logger.debug( origCmdClass.getSimpleName() + "." + field.getName() );
                }
            }
            assertEquals(origCmdClass.getSimpleName() + " fields", origFields.size(), genFields.size());
        }
    }
}
