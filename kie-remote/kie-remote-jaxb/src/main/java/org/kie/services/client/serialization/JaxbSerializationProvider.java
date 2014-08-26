package org.kie.services.client.serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import org.kie.remote.common.jaxb.JaxbException;
import org.kie.remote.common.jaxb.JaxbRequestStatus;
import org.kie.services.client.serialization.jaxb.impl.JaxbLongListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbOtherResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbPrimitiveResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbStringListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbNodeInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbVariableInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentDescriptor;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinition;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceFormResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceListResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceWithVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbWorkItem;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskContentResponse;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskFormResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

public class JaxbSerializationProvider implements SerializationProvider {

    // Classes -------------------------------------------------------------------------------------------------------------------
    
    public final static int JMS_SERIALIZATION_TYPE = 0;

    public static Set<Class<?>> KIE_JAXB_CLASS_SET;
    static { 
        Class<?> [] kieJaxbClasses = { 
                // command response
                JaxbTaskContentResponse.class,
                JaxbTaskFormResponse.class,
                JaxbProcessInstanceListResponse.class,
                JaxbProcessInstanceResponse.class,
                JaxbProcessInstanceWithVariablesResponse.class,
                JaxbProcessInstanceFormResponse.class,

                // REST other
                JaxbGenericResponse.class,
                JaxbLongListResponse.class,
                JaxbStringListResponse.class,
                JaxbOtherResponse.class,
                JaxbPrimitiveResponse.class,
                JaxbVariablesResponse.class,
                JaxbExceptionResponse.class,
                JaxbGenericResponse.class,
                JaxbRequestStatus.class,

                // deployment
                JaxbDeploymentJobResult.class,
                JaxbDeploymentUnit.class,
                JaxbDeploymentUnitList.class,
                JaxbDeploymentDescriptor.class,
                
                // process
                JaxbProcessDefinition.class,
                JaxbProcessDefinitionList.class,

                // workitem
                JaxbWorkItem.class,

                // history
                JaxbHistoryLogList.class,
                JaxbNodeInstanceLog.class,
                JaxbProcessInstanceLog.class,
                JaxbVariableInstanceLog.class,
                
                // exception
                JaxbException.class
        };
       
        String [] serviceSideClasses = { 
                "org.kie.remote.services.jaxb.JaxbCommandsRequest",
                "org.kie.remote.services.jaxb.JaxbCommandsResponse",
                "org.kie.remote.services.jaxb.JaxbContentResponse",
                "org.kie.remote.services.jaxb.JaxbTaskResponse",
                "org.kie.remote.services.jaxb.JaxbTaskSummaryListResponse"
        };
        String [] clientSideClasses = { 
                "org.kie.remote.client.jaxb.JaxbCommandsRequest",
                "org.kie.remote.client.jaxb.JaxbCommandsResponse",
                "org.kie.remote.client.jaxb.JaxbContentResponse",
                "org.kie.remote.client.jaxb.JaxbTaskResponse",
                "org.kie.remote.client.jaxb.JaxbTaskSummaryListResponse"
        };
        
        List<Class<?>> kieJaxbClassList = new ArrayList<Class<?>>(kieJaxbClasses.length + serviceSideClasses.length);
        kieJaxbClassList.addAll(Arrays.asList(kieJaxbClasses));
        
        try { 
            addClassesToList(clientSideClasses, kieJaxbClassList);
        } catch( ClassNotFoundException cnfe ) { 
            try { 
                addClassesToList(serviceSideClasses, kieJaxbClassList);
            } catch( ClassNotFoundException clientCnfe ) { 
                // do nothing
            }
        }
        KIE_JAXB_CLASS_SET = new CopyOnWriteArraySet<Class<?>>(kieJaxbClassList);
    };

    private static void addClassesToList(String [] classes, List<Class<?>> list) throws ClassNotFoundException { 
        for( int i = 0; i < classes.length; ++i ) { 
            Class moduleDependentClass = Class.forName(classes[i]);
            list.add(moduleDependentClass);
        }
    }
    
    public static Set<Class<?>> PRIMITIVE_ARRAY_CLASS_SET;
    static { 
        Class<?> [] primitiveClasses = { 
                new Boolean[]{}.getClass(),
                new Byte[]{}.getClass(),
                new Character[]{}.getClass(),
                new Double[]{}.getClass(),
                new Float[]{}.getClass(),
                new Integer[]{}.getClass(),
                new Long[]{}.getClass(),
                new Math[]{}.getClass(),
                new Number[]{}.getClass(),
                new Short[]{}.getClass(),
                new String[]{}.getClass()
        };
        PRIMITIVE_ARRAY_CLASS_SET = new CopyOnWriteArraySet<Class<?>>(Arrays.asList(primitiveClasses));
    };

    private static Class<?> [] ALL_BASE_JAXB_CLASSES = null;
    static { 
        int kieJaxbClassSetLength = KIE_JAXB_CLASS_SET.size();
        Class<?> [] types = new Class<?> [kieJaxbClassSetLength + PRIMITIVE_ARRAY_CLASS_SET.size()];
        System.arraycopy(KIE_JAXB_CLASS_SET.toArray(new Class<?>[kieJaxbClassSetLength]), 0, types, 0, kieJaxbClassSetLength);
        int primArrClassSetLength = PRIMITIVE_ARRAY_CLASS_SET.size();
        System.arraycopy(PRIMITIVE_ARRAY_CLASS_SET.toArray(new Class<?>[primArrClassSetLength]), 0, types, kieJaxbClassSetLength, primArrClassSetLength);
        ALL_BASE_JAXB_CLASSES = types;
    }
            
    public static Class<?> [] getAllBaseJaxbClasses() { 
        Class<?> [] copy = new Class<?>[ALL_BASE_JAXB_CLASSES.length];
        System.arraycopy(ALL_BASE_JAXB_CLASSES, 0, copy, 0, ALL_BASE_JAXB_CLASSES.length);
        return copy;
    }

    // Local/instance methods ----------------------------------------------------------------------------------------------------
    
    private boolean prettyPrint = false;
    private JAXBContext jaxbContext = null;
    private Set<Class<?>> extraJaxbClasses = new HashSet<Class<?>>();
  
    public JaxbSerializationProvider() {
        initializeJaxbContext(getAllBaseJaxbClasses());
    }

    public JaxbSerializationProvider(Collection<Class<?>> extraJaxbClassList) {
        Set<Class<?>> jaxbClasses = new HashSet<Class<?>>(Arrays.asList(getAllBaseJaxbClasses()));
        jaxbClasses.addAll(extraJaxbClassList);
        initializeJaxbContext(jaxbClasses.toArray(new Class<?>[jaxbClasses.size()]));
    }
   
    private void initializeJaxbContext(Class<?> [] jaxbClasses) {
        try {
            jaxbContext = JAXBContext.newInstance(jaxbClasses);
        } catch (JAXBException jaxbe) {
            throw new SerializationException("Unsupported JAXB Class encountered during initialization: " + jaxbe.getMessage(), jaxbe);
        }
    }
    public JaxbSerializationProvider(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public void dispose() { 
       if( this.extraJaxbClasses != null ) { 
           this.extraJaxbClasses.clear();
           this.extraJaxbClasses = null;
       }
       if( this.jaxbContext != null ) { 
           this.jaxbContext = null;
       }
    }
   
    public void setPrettyPrint(boolean prettyPrint) { 
        this.prettyPrint = prettyPrint;
    }

    public boolean getPrettyPrint() { 
        return this.prettyPrint;
    }

    public synchronized String serialize(Object object) {
        return serialize(jaxbContext, prettyPrint, object);
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

    public synchronized Object deserialize(String xmlStr) {
       return deserialize(jaxbContext, xmlStr);
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

    public void addJaxbClasses(Class... jaxbClass) {
        for (int i = 0; i < jaxbClass.length; ++i) {
            extraJaxbClasses.add(jaxbClass[i]);
        }
        Set<Class<?>> jaxbClassSet = new HashSet<Class<?>>(extraJaxbClasses); 
        jaxbClassSet.addAll(Arrays.asList(getAllBaseJaxbClasses()));
        initializeJaxbContext(jaxbClassSet.toArray(new Class<?>[jaxbClassSet.size()]));
    }

    public Collection<Class<?>> getExtraJaxbClasses() { 
        return new HashSet<Class<?>>(extraJaxbClasses);
    }
   
    // methods for class set properties (JMS messages) ----------------------------------------------------------------------------
    
    public static Set<Class<?>> commaSeperatedStringToClassSet(ClassLoader classloader, String extraClassNames) throws SerializationException { 
        Set<Class<?>> classList = new HashSet<Class<?>>();
        
        extraClassNames = extraClassNames.trim();
        if( extraClassNames.isEmpty() ) { 
            return classList;
        }
        String [] extraClassNameList = split(extraClassNames);
        if( extraClassNameList.length == 0 ) { 
            return classList;
        }

        // non-empty string/list
        for( String extraClassName : extraClassNameList ) { 
            if( extraClassName.endsWith("[]") ) {
                continue;
            }
            try { 
                classList.add(classloader.loadClass(extraClassName));
            } catch( ClassNotFoundException cnfe ) { 
                throw new SerializationException("Unable to load JAXB class '" + extraClassName, cnfe);
            }
        }
        return classList;
    } 

    public static String classSetToCommaSeperatedString(Collection<Class<?>> extraClassList) throws SerializationException { 
        StringBuilder out = new StringBuilder("");
        Set<Class<?>> extraClassSet = new HashSet<Class<?>>();
        extraClassSet.addAll(extraClassList);
        for( Class<?> extraClass : extraClassSet ) { 
            if (out.length() > 0) {
                out.append(",");
            }
            String extraClassName = extraClass.getCanonicalName();
            if( extraClassName == null ) { 
                throw new SerializationException("Only classes with canonical names can be used for serialization");
            }
            out.append(extraClassName);
        }
        return out.toString();
    }
    
    static String[] split(String in) {
        String[] splitIn = in.split(",");
        List<String> outList = new ArrayList<String>();
        for (int i = 0; i < splitIn.length; ++i) {
            splitIn[i] = splitIn[i].trim();
            if (!splitIn[i].isEmpty()) {
                outList.add(splitIn[i]);
            }
        }
        return outList.toArray(new String[outList.size()]);
    }

    public static Object unsupported(Class<?> realClass) { 
        String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on the JAXB " + realClass.getSimpleName() + " implementation.");
    }

}