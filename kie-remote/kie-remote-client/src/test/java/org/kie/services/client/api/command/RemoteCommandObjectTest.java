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

package org.kie.services.client.api.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;
import org.kie.internal.jaxb.StringKeyObjectValueMap;
import org.kie.internal.jaxb.StringKeyObjectValueMapXmlAdapter;
import org.kie.remote.client.jaxb.AcceptedClientCommands;
import org.kie.remote.jaxb.gen.ActivateTaskCommand;
import org.kie.remote.jaxb.gen.DeleteCommand;
import org.kie.remote.jaxb.gen.JaxbStringObjectPairArray;
import org.kie.remote.jaxb.gen.QueryCriteria;
import org.kie.remote.jaxb.gen.TaskCommand;
import org.kie.remote.jaxb.gen.util.JaxbStringObjectPair;
import org.mockito.Mockito;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

public class RemoteCommandObjectTest {

    private static final Logger logger = LoggerFactory.getLogger(RemoteCommandObjectTest.class);

    private static Reflections reflections = new Reflections(ClasspathHelper.forPackage("org.kie.remote.command"),
            ClasspathHelper.forPackage("org.jbpm.services.task.commands"),
            ClasspathHelper.forPackage("org.jbpm.process.audit.command"),
            ClasspathHelper.forPackage("org.kie.remote.jaxb.gen"),
            new TypeAnnotationsScanner(),
            new FieldAnnotationsScanner(), new SubTypesScanner());

    private DatatypeFactory datatypeFactory;

    public RemoteCommandObjectTest() throws Exception {
        datatypeFactory = DatatypeFactory.newInstance();
    }

    @Test
    public void isAcceptableCommandTest() throws Exception {
       assertTrue( "The .isAcceptedCommandClass method is not functioning correctly: generated classes",
               AcceptedClientCommands.isAcceptedCommandClass(org.kie.remote.jaxb.gen.AddTaskCommand.class));
       assertFalse( "The .isAcceptedCommandClass method is not functioning correctly: original command classes",
               AcceptedClientCommands.isAcceptedCommandClass(org.jbpm.services.task.commands.AddTaskCommand.class));
    }

    @Test
    public void reflectionMappingTest() throws Exception {
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(XmlAccessorType.class);
        assertFalse(classes.isEmpty());

        List<Class<?>> classList = new LinkedList<Class<?>>(classes);
        Collections.sort(classList, new Comparator<Class<?>>() {
            @Override
            public int compare( Class<?> o1, Class<?> o2 ) {
                return o1.getSimpleName().compareTo(o2.getSimpleName());
            }
        });
        classes = new LinkedHashSet<Class<?>>(classList);

        Map<Class, Class> kieCmdGenCmdClassMap = new LinkedHashMap<Class, Class>();
        for( Class<?> cmdClass : classes ) {
            if( ! cmdClass.getSimpleName().endsWith("Command") ) {
               continue;
            }
            if( ! AcceptedClientCommands.isAcceptedCommandClass(cmdClass) ) {
                continue;
            }
            Class genCmdClass = Class.forName("org.kie.remote.jaxb.gen." + cmdClass.getSimpleName());
            assertTrue( genCmdClass.getSimpleName() + " is not an accepted command?",
                    AcceptedClientCommands.isAcceptedCommandClass(genCmdClass) );
            kieCmdGenCmdClassMap.put(cmdClass, genCmdClass);
        }
        assertFalse("No generated command classes could be found to test.",  kieCmdGenCmdClassMap.isEmpty());

        for( Entry<Class, Class> classEntry : kieCmdGenCmdClassMap.entrySet() ) {
            compareKieClassInstanceToGenClassInstance(classEntry.getKey(), classEntry.getValue());
        }
    }

    private void compareKieClassInstanceToGenClassInstance( Class kieCmdClass, Class genCmdClass ) throws Exception {
        Object genCmd = fillCommand(genCmdClass);
        if( DeleteCommand.class.equals(genCmdClass) ) {
            ((DeleteCommand) genCmd).setFactHandle( ":1:2:3:4:5:TRAIT" );
        }
        Object copyKieCmd = roundTripFromFlatToOrigCmd(genCmd, genCmdClass, kieCmdClass);

        for( Field field : kieCmdClass.getDeclaredFields() ) {
            if( field.getAnnotation(XmlTransient.class) != null) {
               continue;
            }
            field.setAccessible(true);
            Object kieCmdFieldVal = field.get(copyKieCmd);
            assertNotNull( kieCmdClass.getSimpleName() + "."  + field.getName(), kieCmdFieldVal );
        }

    }

    private static Random random = new Random();

    private Object fillCommand( Class cmdClass ) throws Exception {
        Object cmdObj = cmdClass.getConstructor().newInstance();
        Field[] fields = cmdClass.getDeclaredFields();
        for( Field field : fields ) {
            if( Modifier.isStatic(field.getModifiers()) ) {
                continue;
            }
            fillField(field, cmdObj);
        }
        if( cmdClass.getSuperclass() != null ) {
            for( Field taskField : cmdClass.getSuperclass().getDeclaredFields() ) {
                if( Modifier.isStatic(taskField.getModifiers()) ) {
                    continue;
                }
                fillField(taskField, cmdObj);

            }
        }
        return cmdObj;
    }

    private static DatatypeFactory factory;
    static {
        try {
            factory = DatatypeFactory.newInstance();
        } catch( DatatypeConfigurationException e ) {
            // do nothing
        }
    }

    private void fillField( Field field, Object obj ) throws Exception {
        field.setAccessible(true);
        String fieldTypeName = field.getType().getName();
        Class fieldType = field.getType();
        if( fieldTypeName.startsWith("java") || !fieldTypeName.contains(".") ) {
            if( fieldType.equals(String.class) ) {
                if( "className".equals(field.getName()) ) {
                    field.set(obj, String.class.getName() );
                } else {
                    field.set(obj, UUID.randomUUID().toString());
                }
            } else if( fieldType.equals(Integer.class) || fieldType.equals(int.class) ) {
                field.set(obj, random.nextInt());
            } else if( fieldType.equals(Long.class) || fieldType.equals(long.class) ) {
                field.set(obj, random.nextLong());
            } else if( fieldType.equals(Boolean.class) || fieldType.equals(boolean.class) ) {
                field.set(obj, random.nextBoolean());
            } else if( fieldType.equals(Map.class) ) {
                Map<String, Object> val = new HashMap<String, Object>();
                for( int i = 0; i < 3; ++i ) {
                    val.put(UUID.randomUUID().toString(), random.nextLong());
                }
                field.set(obj, val);
            } else if( fieldType.equals(Date.class) ) {
                field.set(obj, new Date());
            } else if( fieldType.equals(List.class) ) {
                ParameterizedType fieldGenericType = (ParameterizedType) field.getGenericType();
                Type listType = fieldGenericType.getActualTypeArguments()[0];
                List<Object> list = new ArrayList<Object>();
                Class listItemClass = Class.forName(((Class) listType).getName());
                for( int i = 0; i < 2; ++i ) {
                    Object listItem = null;
                    if( listItemClass.isEnum() ) {
                        Object[] enumConstants = listItemClass.getEnumConstants();
                        listItem = enumConstants[random.nextInt(enumConstants.length)];
                    } else if( listItemClass.getName().startsWith("java") ) {
                        if( Long.class.equals(listItemClass) ) {
                            listItem = random.nextLong();
                        } else if( String.class.equals(listItemClass) ) {
                            listItem = UUID.randomUUID().toString();
                        } else if( Object.class.equals(listItemClass) ) {
                            listItem = "Object";
                        } else if( XMLGregorianCalendar.class.equals(listItemClass) ) {
                            listItem = factory.newXMLGregorianCalendar(new GregorianCalendar());
                        } else {
                            fail("Please add logic to deal with the " + listItemClass.getName());
                        }
                    } else if( listItemClass.equals(QueryCriteria.class) ) {
                            // do nothing
                    } else {
                        if( TaskCommand.class.equals(listItemClass) ) {
                            ActivateTaskCommand cmd = new ActivateTaskCommand();
                            cmd.setTaskId(random.nextLong());
                            cmd.setUserId(UUID.randomUUID().toString());
                            listItem = cmd;
                        } else {
                            listItem = listItemClass.getConstructor().newInstance();
                            for( Field listItemField : listItemClass.getDeclaredFields() ) {
                                fillField(listItemField, listItem);
                            }
                        }
                    }
                    list.add(listItem);
                }
                field.set(obj, list);
            } else if( fieldType.equals(XMLGregorianCalendar.class) ) {
                XMLGregorianCalendar date = datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar());
                field.set(obj, date);
            } else if( fieldType.equals(BigInteger.class) ) {
                field.set(obj, new BigInteger("23"));
            } else if( fieldType.equals(Object.class) ) {
                field.set(obj, "Object");
            } else if( fieldType.equals(byte[].class) ) {
                byte [] value = StringKeyObjectValueMapXmlAdapter.serializeObject("Object", "test");
                field.set(obj, value);
            } else {
                fail("> " + obj.getClass().getSimpleName() + "." + field.getName() + ": " + fieldType.getName());
            }
        } else if( fieldType.isEnum() ) {
            Object [] enumConstants = fieldType.getEnumConstants();
            field.set(obj, enumConstants[random.nextInt(enumConstants.length)]);
        } else {
            if( fieldType.equals(org.kie.remote.jaxb.gen.TaskCommand.class) ) {
                ActivateTaskCommand cmd = new ActivateTaskCommand();
                cmd.setTaskId(2l);
                cmd.setUserId(UUID.randomUUID().toString());
                field.set(obj, cmd);
            } else {
                if( field.getAnnotation(XmlTransient.class) != null ) {
                    return;
                }
                 Object subObj = fieldType.getConstructor().newInstance();
                 field.set(obj, subObj);
                 for( Field subField : field.getType().getDeclaredFields() ) {
                     fillField( subField, subObj);
                 }
                // fail(kieCmd.getClass().getSimpleName() + "." + field.getName() + ": " + field.getType());
            }
        }
    }

    private Object roundTripFromFlatToOrigCmd( Object flatCmd, Class flatCmdClass, Class origCmdClass ) throws Exception {
        String xmlStr = serialize(flatCmd, flatCmdClass);
        logger.debug("{}:\n{}---", flatCmdClass.getSimpleName(), xmlStr);
        return deserialize(xmlStr, origCmdClass);
    }

    private String serialize( Object object, Class cmdClass ) throws Exception {
        Marshaller marshaller = null;
        marshaller = getJaxbContext(cmdClass).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        marshaller.setProperty(CharacterEscapeHandler.class.getName(), new CharacterEscapeHandler() {
            @Override
            public void escape( char[] ac, int i, int j, boolean flag, Writer writer ) throws IOException {
                writer.write(ac, i, j);
            }
        });

        StringWriter stringWriter = new StringWriter();
        try {
            marshaller.marshal(object, stringWriter);
        } catch( Exception e ) {
            e.printStackTrace();
            fail( "Could not marshal " + cmdClass.getSimpleName() );
        }
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

    private JAXBContext getJaxbContext( Class... classes ) throws Exception {
        return JAXBContext.newInstance(classes);
    }

    @Test
    public void preprocessTest() throws Throwable {
       Set<Class<?>> xmlClasses = reflections.getTypesAnnotatedWith(XmlAccessorType.class);

       // get all command classes
       Set<Class> cmdClasses = new HashSet<Class>(xmlClasses.size());
       for( Class<?> clazz : xmlClasses ) {
           if( clazz.getName().endsWith("Command") && clazz.getPackage().getName().startsWith("org.kie.remote.jaxb.gen")) {
             XmlRootElement xmlRootElemAnno = clazz.getAnnotation(XmlRootElement.class);
             if( ! Modifier.isAbstract(clazz.getModifiers()) ) {
                 assertNotNull( clazz.getSimpleName() + " is missing @" + XmlRootElement.class.getSimpleName(),
                         xmlRootElemAnno );
                 cmdClasses.add(clazz);
             }
           }
       }

       // get all command classes that have parameters
       Set<Class<?>> paramCmdClasses = new HashSet<Class<?>>(cmdClasses.size());
       for( Class cmdClass : cmdClasses ) {
           Field [] cmdFields = cmdClass.getDeclaredFields();
           for( Field field : cmdFields ) {
               field.setAccessible(true);
              if( field.getType().equals(List.class) )  {
                  if( field.getGenericType() instanceof ParameterizedType ) {
                      ParameterizedType pType = (ParameterizedType) field.getGenericType();
                      Type [] listTypes = pType.getActualTypeArguments();
                      if( listTypes.length > 0 && ! ((Class) listTypes[0]).equals(Object.class) ) {
                         continue;
                      }
                  }
                  paramCmdClasses.add(cmdClass);
              } else if( field.getType().equals(JaxbStringObjectPairArray.class) ) {
                  paramCmdClasses.add(cmdClass);
              } else if( field.getType().equals(Object.class) ) {
                  paramCmdClasses.add(cmdClass);
              }
           }
       }

        RemoteConfiguration config = new RemoteConfiguration("adsf", new URL("http://localhost:80808"), "user", "pwd" );


        List<Object> objList = new ArrayList<Object>();
        String fieldName = "sendObjectParameterCommandClasses";
        Field paramClassesField = AcceptedClientCommands.class.getDeclaredField(fieldName);
        paramClassesField.setAccessible(true);
        Set<Class> sendObjectParameterClasses = (Set<Class>) paramClassesField.get(null);

        // verify that the found classes are in the AcceptedClientCommands.sendObjectParameterCommandClasses field
        if( sendObjectParameterClasses.size() != paramCmdClasses.size() ) {
           for( Class foundParamCmdClass : paramCmdClasses ) {
              assertTrue( "The " + AcceptedClientCommands.class.getSimpleName() + "." + fieldName
                      + " does not contain the " + foundParamCmdClass.getSimpleName(),
                      sendObjectParameterClasses.contains(foundParamCmdClass));
           }
        }

        // verify that the parameter command classes are handled in the
        for( Class clientClass : sendObjectParameterClasses ) {
            KieSessionClientCommandObject spyCmdObj = Mockito.spy(new KieSessionClientCommandObject(config));
            Object inst = clientClass.getConstructor(new Class[0]).newInstance(new Object[0]);
            spyCmdObj.preprocessParameterCommand(inst, objList);
            logger.debug( "Are {} instances checked for user-defined classes?", clientClass.getSimpleName() );
            try {
                verify(spyCmdObj, atLeastOnce()).addPossiblyNullObject(any(), any(List.class));
            } catch( Throwable t ) {
                throw new AssertionError( clientClass.getSimpleName() );
            }
        }
    }

    @Test
    public void addPossiblyNullObjectTest() throws Throwable {
        // mock setup
        RemoteConfiguration config = new RemoteConfiguration(RemoteConfiguration.Type.CONSTRUCTOR);
        TaskServiceClientCommandObject taskServiceClient = new TaskServiceClientCommandObject(config);
        List<Object> objectList = new ArrayList<Object>();

        // Verify adding object
        Object inputObject = new Object();
        taskServiceClient.addPossiblyNullObject(inputObject, objectList);
        assertEquals(1, objectList.size());
        assertEquals(inputObject, objectList.get(0));

        // Verify adding null object
        inputObject = null;
        objectList = new ArrayList<Object>();
        taskServiceClient.addPossiblyNullObject(inputObject, objectList);
        assertEquals(0, objectList.size());

        // Verify adding list containing null object
        inputObject = new Object();
        List<Object> inputObjectList = new ArrayList<Object>();
        inputObjectList.add(null);
        inputObjectList.add(inputObject);
        objectList = new ArrayList<Object>();
        taskServiceClient.addPossiblyNullObject(inputObjectList, objectList);
        assertEquals(1, objectList.size());
        assertEquals(inputObject, objectList.get(0));

        // Verify adding JaxbStringObjectPairArray containing null object
        inputObject = new Object();
        JaxbStringObjectPair inputObjectPair = new JaxbStringObjectPair("one", inputObject);
        JaxbStringObjectPair inputObjectNullPair = new JaxbStringObjectPair("two", null);
        JaxbStringObjectPairArray inputObjectArray = new JaxbStringObjectPairArray();
        inputObjectArray.getItems().add(null);
        inputObjectArray.getItems().add(inputObjectPair);
        inputObjectArray.getItems().add(inputObjectNullPair);
        objectList = new ArrayList<Object>();
        taskServiceClient.addPossiblyNullObject(inputObjectArray, objectList);
        assertEquals(1, objectList.size());
        assertEquals(inputObject, objectList.get(0));

        // Verify adding StringKeyObjectValueMap containing null object
        inputObject = new Object();
        StringKeyObjectValueMap inputObjectValueMap = new StringKeyObjectValueMap();
        inputObjectValueMap.put("one", null);
        objectList = new ArrayList<Object>();
        taskServiceClient.addPossiblyNullObject(inputObjectValueMap, objectList);
        assertEquals(0, objectList.size());
        
        // Verify recursive list does not cause stack overflow
        inputObject = new Object();
        inputObjectList = new ArrayList<Object>();
        inputObjectList.add(null);
        inputObjectList.add(inputObject);
        inputObjectList.add(inputObjectList);
        objectList = new ArrayList<Object>();
        taskServiceClient.addPossiblyNullObject(inputObjectList, objectList);
        assertEquals(1, objectList.size());
        assertEquals(inputObject, objectList.get(0));
    }
}
