package org.kie.services.client.api.command;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jbpm.services.task.commands.CompositeCommand;
import org.junit.Test;
import org.kie.remote.client.jaxb.AcceptedClientCommands;
import org.kie.remote.jaxb.gen.ActivateTaskCommand;
import org.kie.remote.jaxb.gen.DeleteCommand;
import org.kie.remote.jaxb.gen.TaskCommand;
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

    private static Reflections reflections = new Reflections(ClasspathHelper.forPackage("org.drools.core.command"),
            ClasspathHelper.forPackage("org.jbpm.services.task.commands"),
            ClasspathHelper.forPackage("org.jbpm.process.audit.command"), new TypeAnnotationsScanner(),
            new FieldAnnotationsScanner(), new SubTypesScanner());

    private DatatypeFactory datatypeFactory;
    
    public RemoteCommandObjectTest() throws Exception {
        datatypeFactory = DatatypeFactory.newInstance();
    }
    
    @Test
    public void reflectionMappingTest() throws Exception {
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(XmlAccessorType.class);
        assertFalse(classes.isEmpty());

        List<Class<?>> classList = new LinkedList<Class<?>>(classes);
        Collections.sort(classList, new Comparator<Class<?>>() {
            public int compare( Class<?> o1, Class<?> o2 ) {
                return o1.getSimpleName().compareTo(o2.getSimpleName());
            }
        });
        classes = new LinkedHashSet<Class<?>>(classList);
        classes.remove(CompositeCommand.class);
        
        Map<Class, Class> kieCmdGenCmdClassMap = new LinkedHashMap<Class, Class>();
        for( Class<?> cmdClass : classes ) {
            if( !AcceptedClientCommands.isAcceptedCommandClass(cmdClass) ) {
                continue;
            }
            Class genCmdClass = Class.forName("org.kie.remote.jaxb.gen." + cmdClass.getSimpleName());
            kieCmdGenCmdClassMap.put(cmdClass, genCmdClass);
        }
        assertFalse(kieCmdGenCmdClassMap.isEmpty());

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

    private void fillField( Field field, Object obj ) throws Exception {
        field.setAccessible(true);
        String fieldTypeName = field.getType().getName();
        Class fieldType = field.getType();
        if( fieldTypeName.startsWith("java") || !fieldTypeName.contains(".") ) {
            if( fieldType.equals(String.class) ) {
                field.set(obj, UUID.randomUUID().toString());
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
                        } else {
                            fail(listItemClass.getName());
                        }
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
            } else if( fieldType.equals(Object.class) ) { 
                field.set(obj, "Object");
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

}
