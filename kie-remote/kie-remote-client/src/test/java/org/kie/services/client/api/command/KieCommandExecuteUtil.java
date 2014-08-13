package org.kie.services.client.api.command;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.audit.AuditService;
import org.kie.api.task.TaskService;
import org.kie.remote.client.jaxb.AcceptedClientCommands;
import org.kie.remote.jaxb.gen.AuditCommand;
import org.kie.remote.jaxb.gen.JaxbStringObjectPair;
import org.kie.remote.jaxb.gen.JaxbStringObjectPairArray;
import org.kie.remote.jaxb.gen.TaskCommand;
import org.kie.services.client.api.command.exception.RemoteApiException;

/**
 * What is this?!?
 * </p>
 * I wasn't thinking straight and started implementing the {@link KieSession#execute(Command)} method [1]. 
 * </p>
 * The code contained here implements about 80% of that functionality. In short, most of the code below maps {@link Command}
 * implementations, most (if not all) of which can be found in the drools-core, jbpm-human-task-core and jbpm-audit jars.
 * </p>
 * But that's exactly the point: the whole reason the org.kie.
 *
 */
public abstract class KieCommandExecuteUtil {

    abstract Object execute(Object cmdObj);
    
    protected <T> T executeCommand( Command<T> command, Class implementationClass ) {
        Class cmdClass = command.getClass();
        boolean supported = true;
        if( AcceptedClientCommands.isAcceptedCommandClass(cmdClass) ) {
            Class flatCmdClass = null;
            try {
                flatCmdClass = Class.forName("org.kie.remote.jaxb.gen." + cmdClass.getSimpleName());
            } catch( ClassNotFoundException e ) {
                // this should never happen!
                throw new RemoteApiException("Unable to instantiate " + cmdClass.getSimpleName()
                        + " instance: please contact the developers!)");
            }
            if( flatCmdClass != null ) {
                Object cmd = null;
                try {
                    cmd = flatCmdClass.getConstructor().newInstance();
                } catch( Exception e ) {
                    // this should never happen!
                    throw new RemoteApiException("Unable to construct " + cmdClass.getSimpleName()
                            + " instance: please contact the developers!)");
                }
                // check that the correct command type is being processed by the correct implementation type
                // (discourage abuse of API.. )
                if( cmd instanceof TaskCommand && !TaskService.class.equals(implementationClass) ) {
                    throw new UnsupportedOperationException("Task commands may only be used with the Remote Client "
                            + TaskService.class.getSimpleName() + " implementation.");
                } else if( cmd instanceof AuditCommand && !AuditService.class.equals(implementationClass) ) {
                    throw new UnsupportedOperationException("Audit commands may only be used with the Remote Client "
                            + AuditService.class.getSimpleName() + " implementation.");
                }
                mapKieCommandToGeneratedCommand(command, cmd);
                return (T) execute(cmd);
            }
        }
        if( !supported ) {
            throw new UnsupportedOperationException("The " + cmdClass.getName() + " is not supported on the Remote Client  "
                    + implementationClass.getSimpleName() + " implementation.");
        }
        return null;
    }

    static void mapKieCommandToGeneratedCommand( Command kieCmd, Object genCmd ) {
        Field kieFields[] = kieCmd.getClass().getDeclaredFields();
        try {
            mapFields(kieFields, kieCmd, genCmd);
            if( kieCmd instanceof org.jbpm.services.task.commands.TaskCommand ) { 
                Field [] taskfields = org.jbpm.services.task.commands.TaskCommand.class.getDeclaredFields();
                mapFields(taskfields, kieCmd, genCmd) ;
            }
        } catch( Exception e ) {
            throw new RuntimeException("Unable to map " + kieCmd.getClass().getName() + " to generated command equivalent ("
                    + genCmd.getClass().getName() + "), please contact the developers.", e);
        }
    }

    private static void mapFields(Field [] kieFields, Object kieCmd, Object genCmd) throws Exception { 
        for( Field kieField : kieFields ) {
            if( kieField.getAnnotation(XmlTransient.class) != null ) {
                continue;
            }
            if( Modifier.isStatic(kieField.getModifiers()) ) {
                continue;
            }
            copyFieldValueToOtherField(kieField, kieCmd, genCmd);
        } 
    }
   
    private final static Pattern dashLowerCase = Pattern.compile("-([a-z])");
    
    private static void copyFieldValueToOtherField( Field kieField, Object kieCmd, Object genCmd ) throws Exception {
        String kieFieldName = kieField.getName();
        String genFieldName = kieFieldName;
        
        Field genField = getFieldInOtherClass(genFieldName, genCmd);
        if( genField == null ) { 
            genFieldName = getFieldNameFromXmlAttrAnno(kieField); 
            if( genFieldName != null ) { 
                genField = getFieldInOtherClass(genFieldName, genCmd);
            }
        }
        if( genField == null ) { 
            genFieldName = getFieldNameFromXmlElemAnno(kieField); 
            if( genFieldName != null ) { 
                genField = getFieldInOtherClass(genFieldName, genCmd);
            }
        }
        if( genField == null ) { 
            // DBG
            throw new RuntimeException( kieFieldName + " (" + kieCmd.getClass().getName() + ")" );
        }
        kieField.setAccessible(true);
        Object val = kieField.get(kieCmd);
        if( val == null ) { 
            return;
        }
        if( val instanceof Map ) { 
          val = convertMapToPairArray((Map) val);
        } else if( val instanceof List && ! ((List) val).isEmpty() ) { 
            List valList = (List) val;
            Object listItem = valList.get(0);
            Class listItemClass = listItem.getClass();
            if( listItemClass.isEnum() ) { 
                Class genEnumClass = Class.forName( "org.kie.remote.jaxb.gen." + listItemClass.getSimpleName() );
                List newValList = new ArrayList(valList.size());
                for( Object valListItem : valList ) { 
                    for( Object enumVal : genEnumClass.getEnumConstants() ) { 
                        if( enumVal.toString().equalsIgnoreCase(valListItem.toString()) ) { 
                            newValList.add(enumVal);
                            break;
                        }
                    }
                }
                val = newValList;
            } else if( listItemClass.getName().startsWith("org") ) { 
                // DBG
               throw new RuntimeException( "List<" + listItemClass.getSimpleName() + "> (" + kieCmd.getClass().getSimpleName() + "." + kieFieldName );
            }
        }
        genField.setAccessible(true);
        genField.set(genCmd, val);
    }

    private static String getFieldNameFromXmlAttrAnno(Field field) { 
        XmlAttribute xmlAttrAnno = field.getAnnotation(XmlAttribute.class);
        String fieldName = null;
        if( xmlAttrAnno != null ) {
            fieldName = xmlAttrAnno.name();
            fieldName = convertXmlAnnoNameToFieldName(field, fieldName);
        } 
        return fieldName;
    }
    
    private static String getFieldNameFromXmlElemAnno(Field field) { 
        XmlElement xmlElemAnno = field.getAnnotation(XmlElement.class);
        String fieldName = null;
        if( xmlElemAnno != null ) {
            fieldName = xmlElemAnno.name();
            fieldName = convertXmlAnnoNameToFieldName(field, fieldName);
        } 
        return fieldName;
    }
    
    private static String convertXmlAnnoNameToFieldName(Field field, String fieldName) { 
        if( fieldName == null || "##default".equals(fieldName) ) {
            return null;
        } else if( fieldName.contains("-") ) { 
            Matcher matcher = dashLowerCase.matcher(fieldName);
            StringBuffer fixedGenFieldName = new StringBuffer();
            while( matcher.find() ) { 
                matcher.appendReplacement(fixedGenFieldName, matcher.group(1).toUpperCase());
            }
            matcher.appendTail(fixedGenFieldName);
            if( Collection.class.isAssignableFrom(field.getType()) ) { 
                if( field.getName().matches(".*[aeiou]s$" ) ) { 
                    fixedGenFieldName.append("e");
                } 
                fixedGenFieldName.append("s");
            }
            fieldName = fixedGenFieldName.toString();
        }  
        return fieldName;
    }
    
    private static Field getFieldInOtherClass( String fieldName, Object otherObj ) {
        Field otherField = null;

        java.util.Queue<Class> fieldClasses = new LinkedList<Class>();
        fieldClasses.add(otherObj.getClass());
        while( !fieldClasses.isEmpty() && otherField == null ) {
            Class fieldClass = fieldClasses.poll();
            try {
                otherField = fieldClass.getDeclaredField(fieldName);
            } catch( Exception e ) {
                // do nothing
            }
            if( otherField == null ) {
                fieldClasses.addAll(Arrays.asList(fieldClass.getInterfaces()));
                if( fieldClass.getSuperclass() != null ) { 
                    fieldClasses.add(fieldClass.getSuperclass());
                }
            }
        }
        return otherField;
    }

    protected static JaxbStringObjectPairArray convertMapToPairArray( Map<String, Object> parameters ) {
        JaxbStringObjectPairArray arrayMap = new JaxbStringObjectPairArray();
        if (parameters == null || parameters.isEmpty()) {
            return arrayMap;
        }
        List<JaxbStringObjectPair> items = arrayMap.getItems();
        for( Entry<String, Object> entry : parameters.entrySet() ) {
            JaxbStringObjectPair pair = new JaxbStringObjectPair();
            pair.setKey(entry.getKey());
            pair.setValue(entry.getValue());
            items.add(pair);
        }
        return arrayMap;
    }
}
